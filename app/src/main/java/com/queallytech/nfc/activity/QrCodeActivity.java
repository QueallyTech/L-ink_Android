package com.queallytech.nfc.activity;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.queallytech.nfc.R;
import com.queallytech.nfc.utils.FileUtils;
import com.queallytech.nfc.utils.QRCodeUtil;
import com.queallytech.nfc.utils.st25dv.ST25DVTransferTaskEpd;
import com.queallytech.nfc.utils.st25dv.TagDiscovery;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.TagCache;
import com.st.st25sdk.TagHelper;
import com.st.st25sdk.ndef.NDEFRecord;
import com.st.st25sdk.type5.st25dv.ST25DVTag;
import com.yalantis.ucrop.UCrop;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class QrCodeActivity extends AppCompatActivity implements View.OnClickListener, TagDiscovery.onTagDiscoveryCompletedListener {
    private static final String TAG = "QrCodeActivity";
    private EditText et_content;
    private ImageView iv_qrcode;
    private ImageView picture_logo, picture_black; // logo，代替黑色色块的图片

    private EditText et_width, et_height;
    private String error_correction_level, margin; // 容错率，空白边距
    private int color_black, color_white; // 黑色色块，白色色块
    private Uri imageUri;
    private Bitmap logoBitmap; // logo图片
    private Bitmap blackBitmap; // 代替黑色色块的图片
    private int remark; // 标记返回的是logo还是代替黑色色块图片

    private Bitmap qrcode_bitmap; // 生成的二维码

    public static Mat epd_image;
    public static Handler mHandler;
    private static SendEpdActivity.NfcIntentHook mNfcIntentHook;
    private static NFCTag mTag;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    public ST25DVTag mST25DVTag;

    public interface NfcIntentHook {
        void newNfcIntent(Intent intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        et_content = findViewById(R.id.et_content);
        Button btn_generate = findViewById(R.id.btn_generate);
        iv_qrcode = findViewById(R.id.iv_qrcode);
        picture_logo = findViewById(R.id.picture_logo);
        picture_black = findViewById(R.id.picture_black);
        et_width = findViewById(R.id.et_width);
        et_height = findViewById(R.id.et_height);
        Spinner sp_error_correction_level = findViewById(R.id.sp_error_correction_level);
        Spinner sp_margin = findViewById(R.id.sp_margin);
        Spinner sp_color_black = findViewById(R.id.sp_color_black);
        Spinner sp_color_white = findViewById(R.id.sp_color_white);
        btn_generate.setOnClickListener(this);
        picture_logo.setOnClickListener(this);
        picture_black.setOnClickListener(this);
        iv_qrcode.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                imgChooseDialog();
                return true;
            }
        });
        sp_error_correction_level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                error_correction_level = getResources().getStringArray(R.array.spinarr_error_correction)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_margin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                margin = getResources().getStringArray(R.array.spinarr_margin)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_color_black.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_black = getResources().getStringArray(R.array.spinarr_color_black)[position];
                if (str_color_black.equals("黑色")) {
                    color_black = Color.BLACK;
                } else if (str_color_black.equals("白色")) {
                    color_black = Color.WHITE;
                } else if (str_color_black.equals("蓝色")) {
                    color_black = Color.BLUE;
                } else if (str_color_black.equals("绿色")) {
                    color_black = Color.GREEN;
                } else if (str_color_black.equals("黄色")) {
                    color_black = Color.YELLOW;
                } else if (str_color_black.equals("红色")) {
                    color_black = Color.RED;
                } else if (str_color_black.equals("紫色")) {
                    color_black = Color.parseColor("#9370DB");
                } else if (str_color_black.equals("粉红色")) {
                    color_black = Color.parseColor("#ffc0cb");
                } else if (str_color_black.equals("薄荷色")) {
                    color_black = Color.parseColor("#BDFCC9");
                } else {
                    color_black = Color.BLACK;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_color_white.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str_color_white = getResources().getStringArray(R.array.spinarr_color_white)[position];
                if (str_color_white.equals("黑色")) {
                    color_white = Color.BLACK;
                } else if (str_color_white.equals("白色")) {
                    color_white = Color.WHITE;
                } else if (str_color_white.equals("蓝色")) {
                    color_white = Color.BLUE;
                } else if (str_color_white.equals("绿色")) {
                    color_white = Color.GREEN;
                } else if (str_color_white.equals("黄色")) {
                    color_white = Color.YELLOW;
                } else if (str_color_white.equals("红色")) {
                    color_white = Color.RED;
                } else if (str_color_white.equals("紫色")) {
                    color_white = Color.parseColor("#9370DB");
                } else if (str_color_white.equals("粉红色")) {
                    color_white = Color.parseColor("#ffc0cb");
                } else if (str_color_white.equals("薄荷色")) {
                    color_white = Color.parseColor("#BDFCC9");
                } else {
                    color_white = Color.WHITE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        epd_image = new Mat(200, 200, CvType.CV_8UC1);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == 2) {
                    QrCodeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QrCodeActivity.this, "传输完成~", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.btn_generate) {
            generateQrcodeAndDisplay();
        } else if (viewId == R.id.picture_black) {
            this.remark = 1;
            showChooseDialog();
        } else if (viewId == R.id.picture_logo) {
            this.remark = 0;
            showChooseDialog();
        }
    }

    private void shareImg(Bitmap bitmap) {
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");//设置分享内容的类型
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent = Intent.createChooser(intent, "分享");
        startActivity(intent);
    }

    private void generateQrcodeAndDisplay() {
        //二维码内容
        String content = et_content.getText().toString();
        String str_width = et_width.getText().toString();
        String str_height = et_height.getText().toString();
        int width;
        //宽度，高度
        int height;
        if (str_width.length() <= 0 || str_height.length() <= 0) {
            width = 650;
            height = 650;
        } else {
            width = Integer.parseInt(str_width);
            height = Integer.parseInt(str_height);
        }

        if (content.length() <= 0) {
            Toast.makeText(this, "你没有输入二维码内容哟！", Toast.LENGTH_SHORT).show();
            return;
        }
        qrcode_bitmap = QRCodeUtil.createQRCodeBitmap(content, width, height, "UTF-8", error_correction_level, margin, color_black, color_white, logoBitmap, 0.2F, blackBitmap);
        iv_qrcode.setImageBitmap(qrcode_bitmap);

        Mat src = new Mat();
        Bitmap bmp32 = qrcode_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(src, src, new Size(200.0d, 200.0d));
        SendEpdActivity.DitheringInNative(src.getNativeObjAddr());
        Imgproc.threshold(src, epd_image, 120.0d, 255.0d, 0);
        Mat mat = epd_image;
        Core.flip(mat, mat, 0);
    }

    private void showChooseDialog() {
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(this);
        choiceBuilder.setCancelable(false);
        choiceBuilder
            .setTitle("选择图片")
            .setSingleChoiceItems(new String[]{"拍照上传", "从相册选择"}, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0://拍照
                                imageUri = FileUtils.takePhoto(QrCodeActivity.this);
                                break;
                            case 1:// 从相册选择
                                FileUtils.choosePhotoFromAlbum(QrCodeActivity.this);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        choiceBuilder.create();
        choiceBuilder.show();
    }

    private void imgChooseDialog() {
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(QrCodeActivity.this);
        choiceBuilder.setCancelable(false);
        choiceBuilder
            .setTitle("选择")
            .setSingleChoiceItems(new String[]{"分享"}, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                shareImg(qrcode_bitmap);
                                break;
                        }
                        dialog.dismiss();
                    }
                })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        choiceBuilder.create();
        choiceBuilder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileUtils.openAlbum(this);
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相册哟", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FileUtils.TAKE_PHOTO:
                    if (imageUri != null) {
                        FileUtils.startCrop(this, imageUri);
                    }
                    break;
                case FileUtils.CHOOSE_PHOTO:
                    if (data != null) {
                        Uri selectedUri = data.getData();
                        if (selectedUri != null) {
                            FileUtils.startCrop(this, selectedUri);
                        }
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    if (data != null) {
                        Uri resultUri = UCrop.getOutput(data);
                        if (resultUri != null) {
                            String imagePath = FileUtils.getPathFromUri(this, resultUri);
                            displayImage(imagePath);
                        }
                    }
                    break;
            }
        } else if (requestCode == UCrop.RESULT_ERROR) {
            Log.e(TAG, "handleCropError: ", UCrop.getError(data));
        }
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            int angle = FileUtils.readImageExif(imagePath);
            if (remark == 0) {
                logoBitmap = BitmapFactory.decodeFile(imagePath); //logo
                if (FileUtils.readImageExif(imagePath) != 0) {
                    logoBitmap = FileUtils.rotateImage(logoBitmap, angle);
                }
                picture_logo.setImageBitmap(logoBitmap);
            } else if (remark == 1) {
                blackBitmap = BitmapFactory.decodeFile(imagePath); //black
                if (FileUtils.readImageExif(imagePath) != 0) {
                    blackBitmap = FileUtils.rotateImage(blackBitmap, angle);
                }
                picture_black.setImageBitmap(blackBitmap);
            }
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
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
        Log.v(TAG, "Starting TagDiscovery");
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
                        Toast.makeText(QrCodeActivity.this, "正在传输图片，请不要拿开卡片", Toast.LENGTH_SHORT).show();
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
                ST25DVTag st25DVTag = (ST25DVTag) QrCodeActivity.mTag;
                try {
                    st25DVTag.isMailboxEnabled(true);
                } catch (STException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}