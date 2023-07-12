package com.queallytech.nfc.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.queallytech.nfc.R;
import com.queallytech.nfc.utils.st25dv.ST25DVTransferTaskEpd;
import com.queallytech.nfc.utils.st25dv.TagDiscovery;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.TagCache;
import com.st.st25sdk.TagHelper;
import com.st.st25sdk.ndef.NDEFRecord;
import com.st.st25sdk.type5.st25dv.ST25DVTag;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class NoteActivity extends AppCompatActivity implements TagDiscovery.onTagDiscoveryCompletedListener {
    private static final String TAG = "NoteActivity";
    public static Mat epd_image;
    public static Handler mHandler;
    private static SendEpdActivity.NfcIntentHook mNfcIntentHook;
    private static NFCTag mTag;
    protected SubsamplingScaleImageView mImageCanvas;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    public ST25DVTag mST25DVTag;

    public interface NfcIntentHook {
        void newNfcIntent(Intent intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        this.mImageCanvas = (SubsamplingScaleImageView) findViewById(R.id.image_view_canvas);
        this.mImageCanvas.setOrientation(-1);
        this.mImageCanvas.setMinimumDpi(20);
        this.mImageCanvas.setDoubleTapZoomStyle(2);
        this.mImageCanvas.setPanLimit(1);
        final Bitmap bitmap = getNewBitMap("请输入笔记", true);
        if (bitmap != null) {
            this.mImageCanvas.post((Runnable) () -> this.mImageCanvas.setImage(ImageSource.bitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true))));
        }
        EditText edt = (EditText) findViewById(R.id.editText);
        edt.addTextChangedListener(new AnonymousClass1());
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        epd_image = new Mat(200, 200, CvType.CV_8UC1);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 2) {
                    NoteActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NoteActivity.this, "传输完成~", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
                return false;
            }
        });
    }

    public class AnonymousClass1 implements TextWatcher {
        AnonymousClass1() {
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            final Bitmap bitmap = getNewBitMap(editable.toString(), true);
            if (bitmap != null) {
                mImageCanvas.post((Runnable) () -> mImageCanvas.setImage(ImageSource.bitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true))));
            }
            Bitmap bitmap2 = getNewBitMap(editable.toString(), false);
            Mat image = new Mat();
            Utils.bitmapToMat(bitmap2, image);
            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
            Imgproc.resize(image, image, new Size(200.0d, 200.0d));
            SendEpdActivity.DitheringInNative(image.getNativeObjAddr());
            Imgproc.threshold(image, NoteActivity.epd_image, 120.0d, 255.0d, 0);
            Core.flip(NoteActivity.epd_image, NoteActivity.epd_image, 0);
        }
    }

    @RequiresApi(api = 28)
    public Bitmap getNewBitMap(String text, boolean bg) {
        Bitmap newBitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        if (bg) {
            canvas.drawARGB(255, 235, 195, 83);
        } else {
            canvas.drawARGB(255, 255, 255, 255);
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(100.0f);
        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth() - 8, Layout.Alignment.ALIGN_LEFT, 1.0f, 0.0f, false);
        canvas.translate(40.0f, 40.0f);
        sl.draw(canvas);
        return newBitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        NfcAdapter nfcAdapter = mNfcAdapter;
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
        processIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = mNfcAdapter;
        if (nfcAdapter != null) {
            try {
                nfcAdapter.disableForegroundDispatch(this);
            } catch (IllegalStateException e) {
                Log.w(TAG, "Illegal State Exception disabling NFC. Assuming application is terminating.");
            } catch (UnsupportedOperationException e2) {
                Log.w(TAG, "FEATURE_NFC is unavailable.");
            }
        }
    }

    public static NFCTag getTag() {
        return mTag;
    }

    private void enableDebugCode() {
        try {
            TagCache.class.getField("DBG_CACHE_MANAGER").set(null, true);
            NDEFRecord.class.getField("DBG_NDEF_RECORD").set(null, true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        SendEpdActivity.NfcIntentHook nfcIntentHook = mNfcIntentHook;
        if (nfcIntentHook != null) {
            nfcIntentHook.newNfcIntent(intent);
            return;
        }
        Tag androidTag = (Tag) intent.getParcelableExtra("android.nfc.extra.TAG");
        if (androidTag != null) {
            new TagDiscovery(this).execute(androidTag);
            setIntent(null);
        }
    }

    public static void setNfcIntentHook(SendEpdActivity.NfcIntentHook nfcIntentHook) {
        mNfcIntentHook = nfcIntentHook;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent " + intent);
        setIntent(intent);
    }

    @Override
    public void onTagDiscoveryCompleted(NFCTag nfcTag, TagHelper.ProductID productId, STException e) {
        if (e != null) {
            Toast.makeText(getApplication(), "error_while_reading_the_tag", Toast.LENGTH_LONG).show();
            return;
        }
        mTag = nfcTag;
        mST25DVTag = (ST25DVTag) mTag;
        switch (productId) {
            case PRODUCT_ST_ST25DV64K_I:
            case PRODUCT_ST_ST25DV64K_J:
            case PRODUCT_ST_ST25DV16K_I:
            case PRODUCT_ST_ST25DV16K_J:
            case PRODUCT_ST_ST25DV04K_I:
            case PRODUCT_ST_ST25DV04K_J:
                checkMailboxActivation();
                Log.d(TAG, "onTagDiscoveryCompleted: got card!!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NoteActivity.this, "正在传输图片，请不要拿开卡片", Toast.LENGTH_SHORT).show();
                    }
                });
                ST25DVTag sT25DVTag = mST25DVTag;
                if (sT25DVTag != null) {
                    ST25DVTransferTaskEpd mTransferTask = new ST25DVTransferTaskEpd(sT25DVTag, epd_image, mHandler);
                    new Thread(mTransferTask).start();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void checkMailboxActivation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ST25DVTag st25DVTag = (ST25DVTag) NoteActivity.mTag;
                try {
                    st25DVTag.isMailboxEnabled(true);
                } catch (STException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}