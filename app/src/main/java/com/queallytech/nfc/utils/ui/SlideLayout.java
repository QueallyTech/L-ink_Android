package com.queallytech.nfc.utils.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.IdRes;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class SlideLayout extends FrameLayout implements View.OnTouchListener {
    public float mThreshold;
    private float mProgress;
    private float tmp;
    private onProgressChangedListener mOnProgressChangedListener;
    private ValueAnimator mAnimator;
    private boolean isSliderTouched;
    private boolean isSliderTouchable;
    @IdRes
    private int mChildId;
    private ViewGroup mChildSlider;
    private View mImageButton;
    private int mChildInitWidth;
    private int mInitWidth;
    private int mInitHeight;
    private ViewGroup.MarginLayoutParams mChildParams;
    private float lastX;
    private float lastY;

    public void setSliderTouchable(final boolean sliderTouchable) {
        this.isSliderTouchable = sliderTouchable;
    }

    public SlideLayout(Context context) {
        super(context);
        this.mThreshold = 1.0f;
        this.mProgress = 0.0f;
        this.isSliderTouched = false;
        this.isSliderTouchable = true;
        this.mChildInitWidth = -1;
        this.mInitWidth = -1;
        this.mInitHeight = -1;
        constructInit();
    }

    public SlideLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mThreshold = 1.0f;
        this.mProgress = 0.0f;
        this.isSliderTouched = false;
        this.isSliderTouchable = true;
        this.mChildInitWidth = -1;
        this.mInitWidth = -1;
        this.mInitHeight = -1;
        this.constructInit();
    }

    public SlideLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mThreshold = 1.0f;
        this.mProgress = 0.0f;
        this.isSliderTouched = false;
        this.isSliderTouchable = true;
        this.mChildInitWidth = -1;
        this.mInitWidth = -1;
        this.mInitHeight = -1;
        this.constructInit();
    }

    private void constructInit() {
    }

    public void setOnProgressChangedListener(onProgressChangedListener onProgressChangedListener2) {
        this.mOnProgressChangedListener = onProgressChangedListener2;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setOnTouchListener((View.OnTouchListener) this);
        if (this.mChildId == 0 && this.getChildCount() > 0) {
            this.mChildSlider = (ViewGroup) this.getChildAt(this.getChildCount() - 1);
        }
        this.mChildParams = (ViewGroup.MarginLayoutParams) this.mChildSlider.getLayoutParams();
        this.mAnimator = new ValueAnimator();
        this.mImageButton = this.mChildSlider.getChildAt(this.mChildSlider.getChildCount() - 1);
        this.mChildSlider.setOnTouchListener((View.OnTouchListener) new View.OnTouchListener() {
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case 0:
                        SlideLayout.this.isSliderTouched = true;
                    case 2:
                }
                return false;
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setOnTouchListener(null);
    }

    public void setChildId(@IdRes final int id) {
        this.mChildId = id;
        this.mChildSlider = null;
    }

    public void setThreshold(final float threshold) {
        this.mThreshold = threshold;
    }

    public View getChild() {
        if (null == this.mChildSlider) {
            this.mChildSlider = (ViewGroup) this.findViewById(this.mChildId);
        }
        return (View) this.mChildSlider;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0: {
                if (this.isSliderTouchable && this.isSliderTouched) {
                    this.mImageButton.setScaleX(0.8f);
                    this.mImageButton.setScaleY(0.8f);
                    this.lastX = motionEvent.getX();
                    this.lastY = motionEvent.getY();
                    break;
                }
                break;
            }
            case 2: {
                if (this.isSliderTouchable && this.isSliderTouched) {
                    final float leftBoundary = this.mChildInitWidth;
                    final float rightBoundary = this.mInitWidth;
                    if (motionEvent.getX() < leftBoundary) {
                        this.mChildParams.width = (int) leftBoundary;
                    } else if (motionEvent.getX() > rightBoundary) {
                        this.mChildParams.width = (int) rightBoundary;
                    } else {
                        this.mChildParams.width = (int) motionEvent.getX();
                    }
                    this.mChildSlider.setLayoutParams((ViewGroup.LayoutParams) this.mChildParams);
                    this.mProgress = (this.mChildParams.width - leftBoundary) / (rightBoundary - leftBoundary);
                    this.mOnProgressChangedListener.onProgressChanged(this.mProgress);
                    if (this.mProgress >= this.mThreshold) {
                        this.isSliderTouchable = false;
                    }
                    break;
                }
                break;
            }
            case 1:
            case 3: {
                if (this.isSliderTouchable && this.isSliderTouched && this.mProgress < this.mThreshold) {
                    this.setProgress(0.0f);
                    if (Math.sqrt((motionEvent.getX() - this.lastX) * (motionEvent.getX() - this.lastX) + (motionEvent.getY() - this.lastY) * (motionEvent.getY() - this.lastY)) < 10.0 && this.mProgress == 0.0f) {
                        this.mOnProgressChangedListener.onButtonClick();
                    }
                }
                this.isSliderTouched = false;
                this.isSliderTouchable = true;
                this.mImageButton.setScaleX(1.0f);
                this.mImageButton.setScaleY(1.0f);
                break;
            }
        }
        return true;
    }

    public void setProgress(final float p) {
        this.tmp = p;
        if (this.mChildParams != null) {
            this.mAnimator.cancel();
            final int currentPos = (int) (this.mChildInitWidth + this.mProgress * (this.mInitWidth - this.mChildInitWidth));
            final int targetPos = (int) (this.mChildInitWidth + this.tmp * (this.mInitWidth - this.mChildInitWidth));
            this.mAnimator.setFloatValues(0.0f, 1.0f);
            this.mAnimator.setInterpolator((TimeInterpolator) new FastOutSlowInInterpolator());
            this.mAnimator.addUpdateListener((ValueAnimator.AnimatorUpdateListener) new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                    final float fraction = valueAnimator.getAnimatedFraction();
                    SlideLayout.this.mChildParams.width = (int) (fraction * (targetPos - currentPos)) + currentPos;
                    SlideLayout.this.mChildSlider.setLayoutParams((ViewGroup.LayoutParams) SlideLayout.this.mChildParams);
                }
            });
            this.mAnimator.setDuration(500L);
            this.mAnimator.addListener((Animator.AnimatorListener) new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    super.onAnimationEnd(animation);
                    SlideLayout.this.mProgress = SlideLayout.this.tmp;
                    SlideLayout.this.mOnProgressChangedListener.onProgressChanged(SlideLayout.this.tmp);
                }
            });
            this.mAnimator.start();
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mChildInitWidth <= 0) {
            this.mChildInitWidth = this.mChildSlider.getWidth();
            this.mInitWidth = this.getWidth();
            this.mInitHeight = this.getHeight();
        }
    }

    public interface onProgressChangedListener {
        void onProgressChanged(final float p0);

        void onButtonClick();
    }
}