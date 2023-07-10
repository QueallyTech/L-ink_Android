package com.queallytech.nfc.utils.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DropDownMessage extends LinearLayout {
    private TextView textView;
    private String warningMessage;
    private int backgroundColor;
    private int foregroundColor;
    private Animation fadeIn;
    private Animation fadeOut;
    private ViewGroup parent;
    private int height;
    private Interpolator interpolatorIn;
    private Interpolator interpolatorOut;
    private int animationLength;
    boolean isVisible;

    public DropDownMessage(final Builder builder) {
        super(builder.context);
        this.isVisible = false;
        this.warningMessage = builder.warningMessage;
        this.backgroundColor = builder.backgroundColor;
        this.foregroundColor = builder.foregroundColor;
        this.height = builder.height;
        this.interpolatorIn = builder.interpolatorIn;
        this.interpolatorOut = builder.interpolatorOut;
        this.animationLength = builder.animationLength;
        this.parent = builder.parent;
        this.addWarningView();
        this.setUpLayoutParams();
        this.initializeAnimation();
    }

    public DropDownMessage setMessage(final String message) {
        this.textView.setText((CharSequence) message);
        return this;
    }

    private void addWarningView() {
        (this.textView = new TextView(this.getContext())).setText((CharSequence) this.warningMessage);
        this.textView.setBackgroundColor(this.backgroundColor);
        this.textView.setGravity(17);
        this.textView.setPadding(0, 12, 0, 12);
        this.textView.setVisibility(4);
        this.textView.setTextColor(this.foregroundColor);
        final LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(-1, this.height);
        this.textView.setLayoutParams((ViewGroup.LayoutParams) l);
        this.addView((View) this.textView);
    }

    private void setUpLayoutParams() {
        final LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(-1, -1);
        this.setLayoutParams((ViewGroup.LayoutParams) l);
        this.parent.addView((View) this);
    }

    private void initializeAnimation() {
        this.animationLength = 500;
        (this.fadeIn = (Animation) new TranslateAnimation(0.0f, 0.0f, (float) (-this.height), 0.0f)).setDuration((long) this.animationLength);
        (this.fadeOut = (Animation) new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-this.height))).setDuration((long) this.animationLength);
        if (this.interpolatorIn != null) {
            this.fadeIn.setInterpolator(this.interpolatorIn);
        }
        if (this.interpolatorOut != null) {
            this.fadeOut.setInterpolator(this.interpolatorOut);
        }
    }

    public void show() {
        if (!this.isVisible) {
            this.textView.setVisibility(0);
            this.textView.startAnimation(this.fadeIn);
            this.isVisible = true;
        }
    }

    public void show(final int time_ms) {
        if (!this.isVisible) {
            this.textView.setVisibility(0);
            this.textView.startAnimation(this.fadeIn);
            this.isVisible = true;
        }
        this.postDelayed((Runnable) new Runnable() {
            @Override
            public void run() {
                DropDownMessage.this.hide();
            }
        }, (long) time_ms);
    }

    @Override
    public void setOnClickListener(final View.OnClickListener listener) {
        this.textView.setOnClickListener(listener);
    }

    public void hide() {
        if (!this.isVisible) {
            return;
        }
        this.fadeOut.setAnimationListener((Animation.AnimationListener) new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(final Animation animation) {
            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                DropDownMessage.this.textView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
            }
        });
        this.isVisible = false;
        this.textView.startAnimation(this.fadeOut);
    }

    public static class Builder {
        private Interpolator interpolatorIn;
        private Interpolator interpolatorOut;
        private int animationLength;
        private int height;
        private String warningMessage;
        private Context context;
        private ViewGroup parent;
        private int backgroundColor;
        private int foregroundColor;

        public Builder(final Context context, final ViewGroup parent) {
            this.context = context;
            this.parent = parent;
            this.warningMessage = "My Message";
            this.height = 60;
            this.animationLength = 500;
            this.interpolatorIn = new LinearInterpolator();
            this.interpolatorOut = new LinearInterpolator();
            this.backgroundColor = 0xFFFFFFFF;
            this.foregroundColor = 0xFF000000;
        }

        public Builder interpolatorIn(final Interpolator interpolator) {
            this.interpolatorIn = interpolator;
            return this;
        }

        public Builder interpolatorOut(final Interpolator interpolator) {
            this.interpolatorOut = interpolator;
            return this;
        }

        public Builder animationLength(final int length) {
            this.animationLength = length;
            return this;
        }

        public Builder textHeight(final int height) {
            this.height = height;
            return this;
        }

        public Builder message(final String message) {
            this.warningMessage = message;
            return this;
        }

        public Builder foregroundColor(final int color) {
            this.foregroundColor = color;
            return this;
        }

        public Builder backgroundColor(final int color) {
            this.backgroundColor = color;
            return this;
        }

        public DropDownMessage build() {
            return new DropDownMessage(this);
        }
    }
}