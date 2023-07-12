package com.queallytech.nfc.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.queallytech.nfc.base.SendEpdBase;
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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class SendEpdActivity extends SendEpdBase implements TagDiscovery.onTagDiscoveryCompletedListener {

    private static final String TAG = "SendEpdActivity";
    public static Mat epd_image;
    public static Handler mHandler;
    private static NfcIntentHook mNfcIntentHook;
    private static NFCTag mTag;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    public ST25DVTag mST25DVTag;

    public interface NfcIntentHook {
        void newNfcIntent(Intent intent);
    }

    public static native void DitheringInNative(long matAddr);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        epd_image = new Mat(200, 200, CvType.CV_8UC1);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    notifyProcessingProgress(message.getData().getFloat(NotificationCompat.CATEGORY_PROGRESS));
                    return false;
                } else if (i == 2) {
                    notifyProcessingProgress(1.0f);
                    SendEpdActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SendEpdActivity.this, "传输完成~", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onImageSelected(String _imagePath) {
        Mat image = Imgcodecs.imread(_imagePath);
        int currentAlgorithmIndex = MainActivity.getCurrentAlgorithmIndex();
        if (currentAlgorithmIndex == 0) {
            Imgproc.cvtColor(image, image, 6);
            Imgproc.resize(image, image, new Size(200.0d, 200.0d));
            Imgproc.threshold(image, epd_image, 120.0d, 255.0d, 0);
            Bitmap bitmap = Bitmap.createBitmap(epd_image.cols(), epd_image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(epd_image, bitmap);
            updateCanvas(bitmap, _imagePath);
            notifyProcessingProgress(1.0f);
            Mat mat = epd_image;
            Core.flip(mat, mat, 0);
        } else if (currentAlgorithmIndex == 1) {
            Imgproc.cvtColor(image, image, 6);
            Imgproc.resize(image, image, new Size(200.0d, 200.0d));
            DitheringInNative(image.getNativeObjAddr());
            Imgproc.threshold(image, epd_image, 120.0d, 255.0d, 0);
            Bitmap bitmap1 = Bitmap.createBitmap(epd_image.cols(), epd_image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(epd_image, bitmap1);
            updateCanvas(bitmap1, _imagePath);
            notifyProcessingProgress(1.0f);
            Mat mat2 = epd_image;
            Core.flip(mat2, mat2, 0);
        }
    }

    @Override
    protected void onSliderTriggered(String str) {
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
        NfcIntentHook nfcIntentHook = mNfcIntentHook;
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

    public static void setNfcIntentHook(NfcIntentHook nfcIntentHook) {
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
            Log.i(TAG, e.toString());
            Toast.makeText(getApplication(), "error_while_reading_the_tag", Toast.LENGTH_LONG).show();
            return;
        }
        mTag = nfcTag;
        this.mST25DVTag = (ST25DVTag) mTag;
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
                        Toast.makeText(SendEpdActivity.this, "正在传输图片，请不要拿开卡片", Toast.LENGTH_SHORT).show();
                    }
                });
                notifyProcessingProgress(0.0f);
                ST25DVTag sT25DVTag = this.mST25DVTag;
                if (sT25DVTag != null) {
                    ST25DVTransferTaskEpd mTransferTask = new ST25DVTransferTaskEpd(sT25DVTag, epd_image, mHandler);
                    new Thread(mTransferTask).start();
                    return;
                }
                return;
            default:
        }
    }

    private void checkMailboxActivation() {
        new Thread(() -> {
            ST25DVTag st25DVTag = (ST25DVTag) SendEpdActivity.mTag;
            try {
                st25DVTag.isMailboxEnabled(true);
            } catch (STException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
