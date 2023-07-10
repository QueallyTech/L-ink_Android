package com.queallytech.nfc.utils.ui.DragGridView;

import android.content.Context;

public class GridItem {
    private String mName;
    private int mIconBitmapId;
    private boolean mIsCloud;
    private int mSettingLayoutId;
    public onItemSelectedListener mOnItemSelectedListener;

    public String getName() {
        return this.mName;
    }

    public void setName(final String mName) {
        this.mName = mName;
    }

    public int getSettingLayoutId() {
        return this.mSettingLayoutId;
    }

    public void setIconBitmapId(final int mIconBitmapId) {
        this.mIconBitmapId = mIconBitmapId;
    }

    public int getIconBitmapId() {
        return this.mIconBitmapId;
    }

    public boolean isCloud() {
        return this.mIsCloud;
    }

    public void setmIsCloud(final boolean mIsCloud) {
        this.mIsCloud = mIsCloud;
    }

    public GridItem(final Context context, final Builder builder) {
        this.mName = "";
        this.mIsCloud = false;
        this.mName = builder.mName;
        this.mSettingLayoutId = builder.mSettingLayoutId;
        this.mOnItemSelectedListener = builder.mOnItemSelectedListener;
        this.mIconBitmapId = builder.mIconBitmapId;
        this.mIsCloud = builder.mIsCloud;
    }

    public static class Builder {
        private Context mContext;
        private String mName;
        private int mIconBitmapId;
        private int mSettingLayoutId;
        private onItemSelectedListener mOnItemSelectedListener;
        private boolean mIsCloud;

        public Builder(final Context context) {
            this.mIsCloud = false;
            this.mContext = context;
        }

        public Builder setIconBitmapId(final int id) {
            this.mIconBitmapId = id;
            return this;
        }

        public Builder setName(final String s) {
            this.mName = s;
            return this;
        }

        public Builder isCloud(final boolean b) {
            this.mIsCloud = b;
            return this;
        }

        public Builder setSettingLayoutId(final int id) {
            this.mSettingLayoutId = id;
            return this;
        }

        public Builder setOnItemSelectedListener(final onItemSelectedListener ls) {
            this.mOnItemSelectedListener = ls;
            return this;
        }

        public GridItem build() {
            return new GridItem(this.mContext, this);
        }
    }

    public interface onItemSelectedListener {
        void onSelected();
    }
}
