package com.queallytech.nfc.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;

import com.queallytech.nfc.R;
import com.queallytech.nfc.activity.NoteActivity;
import com.queallytech.nfc.activity.QrCodeActivity;
import com.queallytech.nfc.activity.SendEpdActivity;
import com.queallytech.nfc.utils.ui.DragGridView.DragAdapter;
import com.queallytech.nfc.utils.ui.DragGridView.DragGridView;
import com.queallytech.nfc.utils.ui.DragGridView.GridItem;
import com.queallytech.nfc.utils.ui.FullScreenActivityBase;
import com.queallytech.nfc.utils.ui.ScreenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SelectAlgorithmBase extends FullScreenActivityBase {
    private List<HashMap<String, Object>> mAlgorithmList = new ArrayList<HashMap<String, Object>>();
    protected HashMap<Integer, GridItem> mAlgorithmItems;

    public static int mCurrentSelectedAlgorithm = -1;

    public static int getCurrentAlgorithmIndex() {
        return mCurrentSelectedAlgorithm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mAlgorithmItems = new HashMap<>();
    }

    public void commitItems(HashMap<Integer, GridItem> items) {
        DragGridView mDragGridView = (DragGridView) findViewById(R.id.drag_grid_view);
        for (Map.Entry<Integer, GridItem> itemMap : items.entrySet()) {
            GridItem gItem = itemMap.getValue();
            HashMap<String, Object> itemHashMap = new HashMap<>();
            itemHashMap.put("item_image", gItem.getIconBitmapId());
            itemHashMap.put("item_name", gItem.getName());
            itemHashMap.put("item_is_cloud", gItem.isCloud());
            itemHashMap.put("item_is_last", false);
            this.mAlgorithmList.add(itemHashMap);
        }
        DragAdapter mDragAdapter = new DragAdapter(this, this.mAlgorithmList);
        mDragGridView.setAdapter((ListAdapter) mDragAdapter);
        mDragGridView.setNumColumns(2);
        mDragGridView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (position < adapterView.getChildCount()) {
                View image = view.findViewById(R.id.item_image);
                image.setElevation(ScreenUtil.Dp2Pixel(30.0f));
                image.setScaleX(1.0f);
                image.setScaleY(1.0f);
                mCurrentSelectedAlgorithm = position;
                GridItem.onItemSelectedListener l = Objects.requireNonNull(mAlgorithmItems.get(mCurrentSelectedAlgorithm)).mOnItemSelectedListener;
                if (l != null) {
                    l.onSelected();
                }
                if (id == 2) {
                    Intent intent = new Intent(SelectAlgorithmBase.this, QrCodeActivity.class);
                    startActivity(intent);
                } else if (id == 3) {
                    Intent intent2 = new Intent(SelectAlgorithmBase.this, NoteActivity.class);
                    startActivity(intent2);
                } else {
                    Intent intent3 = new Intent(SelectAlgorithmBase.this, SendEpdActivity.class);
                    startActivity(intent3);
                }
            }
        });
    }
}