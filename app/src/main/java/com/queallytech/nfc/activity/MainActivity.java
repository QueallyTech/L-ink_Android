package com.queallytech.nfc.activity;

import android.os.Bundle;

import com.queallytech.nfc.R;
import com.queallytech.nfc.base.SelectAlgorithmBase;
import com.queallytech.nfc.utils.ui.DragGridView.GridItem;

public class MainActivity extends SelectAlgorithmBase {

    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CheckPermissions(new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_MEDIA_IMAGES", "android.permission.INTERNET", "android.permission.NFC", "android.permission.NFC_TRANSACTION_EVENT"});

        this.mAlgorithmItems.put(0, new GridItem.Builder(getApplicationContext()).setName("二值化").setIconBitmapId(R.mipmap.galerry_pick_icon_bin).isCloud(true).build());
        this.mAlgorithmItems.put(1, new GridItem.Builder(getApplicationContext()).setName("模拟灰度").setIconBitmapId(R.mipmap.galerry_pick_icon_gray).isCloud(true).build());
        this.mAlgorithmItems.put(2, new GridItem.Builder(getApplicationContext()).setName("二维码").setIconBitmapId(R.mipmap.galerry_pick_icon_qr).isCloud(true).build());
        this.mAlgorithmItems.put(3, new GridItem.Builder(getApplicationContext()).setName("记事本").setIconBitmapId(R.mipmap.galerry_pick_icon_note).isCloud(true).build());
        commitItems(this.mAlgorithmItems);
    }

}