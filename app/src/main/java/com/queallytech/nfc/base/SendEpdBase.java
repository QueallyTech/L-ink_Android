package com.queallytech.nfc.base;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.vectordrawable.graphics.drawable.PathInterpolatorCompat;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.queallytech.nfc.R;
import com.queallytech.nfc.utils.FileUtils;
import com.queallytech.nfc.utils.ui.DropDownMessage;
import com.queallytech.nfc.utils.ui.FullScreenActivityBase;
import com.queallytech.nfc.utils.ui.SlideLayout;
import com.wang.avi.AVLoadingIndicatorView;
import com.yalantis.ucrop.UCrop;

import java.io.FileNotFoundException;
import java.io.InputStream;

public abstract class SendEpdBase extends FullScreenActivityBase implements SlideLayout.onProgressChangedListener {
    protected ImageView mCompareButton;
    protected DropDownMessage mDropDownMessage;
    protected SubsamplingScaleImageView mImageCanvas;
    protected ImageView mImageProcessingArrow;
    protected ImageView mImageProcessingMask;
    protected AVLoadingIndicatorView mLoadingAnimationView;
    protected Bitmap mOrigBitmap;
    protected Bitmap mResultBitmap;
    protected String mSelectedImagePath;
    protected ImageView mSlideButton;
    protected SlideLayout mSlideLayout;
    protected TextView mStatusText;
    private long time_last;
    protected Uri tmpUri;
    protected boolean mIsProcessing = false;
    private boolean confirm_exit = false;

    protected abstract void onImageSelected(String str);

    protected abstract void onSliderTriggered(String str);

    static {
        System.loadLibrary("dithering");
    }

    @Override
    @SuppressLint({"ClickableViewAccessibility"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendepd);

        showChooseDialog();

        mImageCanvas = findViewById(R.id.image_view_canvas);
        mImageCanvas.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mImageCanvas.setMinimumDpi(20);
        mImageCanvas.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        mImageCanvas.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);

        mSlideLayout = findViewById(R.id.slider);
        mSlideLayout.setOnProgressChangedListener(this);

        mImageProcessingMask = findViewById(R.id.image_process_mask);
        mImageProcessingMask.setAlpha(0.f);

        mImageProcessingArrow = findViewById(R.id.image_process_arrow);
        mImageProcessingArrow.setAlpha(0.8f);

        mSlideButton = findViewById(R.id.slide_button);
        mCompareButton = findViewById(R.id.image_compare_button);

        mCompareButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mOrigBitmap != null)
                        mImageCanvas.post(() -> mImageCanvas
                            .setImage(ImageSource.bitmap(mOrigBitmap.copy(Bitmap.Config.ARGB_8888, true))));
                    break;
                case MotionEvent.ACTION_UP:
                    if (mResultBitmap != null)
                        mImageCanvas.post(() -> mImageCanvas
                            .setImage(ImageSource.bitmap(mResultBitmap.copy(Bitmap.Config.ARGB_8888, true))));
                    break;
            }
            return true;
        });

        mLoadingAnimationView = findViewById(R.id.loading_indicator_view);
        mLoadingAnimationView.hide();

        mDropDownMessage = new DropDownMessage.Builder(getApplicationContext(),
            findViewById(R.id.root))
            .message("mDropDownMessage")
            .backgroundColor(0xff1976D2)
            .foregroundColor(0xffffffff)
            .interpolatorIn(new BounceInterpolator())
            .interpolatorOut(new AnticipateOvershootInterpolator())
            .textHeight(80)
            .build();

        playArrowAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FileUtils.TAKE_PHOTO:
                    if (tmpUri != null) {
                        FileUtils.startCrop(this, tmpUri);
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
                            mImageCanvas.setImage(ImageSource.uri(resultUri));

                            mSelectedImagePath = FileUtils.getPathFromUri(this, resultUri);

                            mOrigBitmap = decodeUriAsBitmap(resultUri);
                            int angle = FileUtils.readImageExif(mSelectedImagePath);
                            if (FileUtils.readImageExif(mSelectedImagePath) != 0) {
                                mOrigBitmap = FileUtils.rotateImage(mOrigBitmap, angle);
                            }

                            onImageSelected(mSelectedImagePath);
                        }
                    }
                    break;
            }
        }
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
                                tmpUri = FileUtils.takePhoto(SendEpdBase.this);
                                break;
                            case 1:// 从相册选择
                                FileUtils.choosePhotoFromAlbum(SendEpdBase.this);
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

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        if (uri == null) {
            return null;
        }
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (!confirm_exit) {
                    time_last = System.currentTimeMillis();
                    confirm_exit = true;
                    Toast.makeText(getApplicationContext(), "再次按返回退出", Toast.LENGTH_SHORT).show();
                } else {
                    if (System.currentTimeMillis() - time_last < 2000) {
                        finish();
                    }
                    confirm_exit = false;
                }
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(float progress) {
        if (progress >= mSlideLayout.mThreshold) {
            mIsProcessing = true;
            playButtonAnimation(true);
            mSlideLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSelectedImagePath != null) {
                        onSliderTriggered(mSelectedImagePath);
                        return;
                    }
                    mDropDownMessage.setMessage("未选择图片!").show(PathInterpolatorCompat.MAX_NUM_POINTS);
                    notifyProcessingProgress(1.0f, "");
                }
            }, 500L);
        } else if (progress == 0 && mIsProcessing) {
            mIsProcessing = false;
            playButtonAnimation(false);
        }
        if (progress > 0.2f) {
            mImageProcessingArrow.animate().alpha(0).setDuration(500).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mImageProcessingArrow.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }
    }

    protected void notifyProcessingProgress(float progress) {
        mSlideLayout.post(() -> mSlideLayout.setProgress(1 - progress));

        if (progress == 1)
            playShatterAnimation();
    }

    protected void notifyProcessingProgress(float progress, String text) {
        mSlideLayout.post(() -> mSlideLayout.setProgress(1 - progress));

        if (progress == 1)
            playShatterAnimation();
    }

    protected void setSliderText(String text) {
        mStatusText.post(() -> mStatusText.setText(text));
    }

    protected void showToast(String msg) {
        mSlideLayout.post(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    protected void updateCanvas(final Bitmap bitmap, final String _imagePath) {
        mImageCanvas.post(() -> {
            mResultBitmap = bitmap;
            mImageCanvas.setImage(ImageSource.bitmap(mResultBitmap.copy(Bitmap.Config.ARGB_8888, true)));
        });
    }


    @Override
    public void onButtonClick() {
        showChooseDialog();
    }

    public void playShatterAnimation() {
        mImageProcessingMask.postDelayed(() -> {
            mImageProcessingMask.setAlpha(1.f);
            mImageProcessingMask.animate().cancel();
            mImageProcessingMask.animate().alpha(0)
                .setDuration(500)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        }, 100);
    }

    public void playArrowAnimation() {
        AnimationSet ams = new AnimationSet(true);
        AlphaAnimation ama = new AlphaAnimation(1, 0);
        ama.setDuration(2000);
        ama.setInterpolator(new FastOutSlowInInterpolator());
        ama.setRepeatMode(Animation.RESTART);
        ama.setRepeatCount(Animation.INFINITE);
        TranslateAnimation amt = new TranslateAnimation(0, 300, 0, 0);
        amt.setDuration(2000);
        amt.setInterpolator(new FastOutSlowInInterpolator());
        amt.setRepeatMode(Animation.RESTART);
        amt.setRepeatCount(Animation.INFINITE);

        ams.addAnimation(ama);
        ams.addAnimation(amt);

        mImageProcessingArrow.startAnimation(ams);
    }

    public void playButtonAnimation(boolean isProcessing) {
        if (isProcessing) {
            this.mSlideButton.animate().alpha(0.0f).setDuration(500L).start();
            this.mLoadingAnimationView.show();
            return;
        }
        this.mLoadingAnimationView.hide();
        this.mSlideButton.animate().alpha(1.0f).setDuration(500L).start();
    }
}

