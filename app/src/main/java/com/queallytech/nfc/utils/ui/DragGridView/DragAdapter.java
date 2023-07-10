package com.queallytech.nfc.utils.ui.DragGridView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.queallytech.nfc.R;
import com.queallytech.nfc.utils.ui.ScreenUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter {
    private List<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private int mHidePosition = -1;

    public DragAdapter(Context context, List<HashMap<String, Object>> list) {
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.layout_grid_item, null);
        ImageView mImage = (ImageView) convertView.findViewById(R.id.item_image);
        TextView mName = (TextView) convertView.findViewById(R.id.item_name);

        mName.setText((CharSequence) list.get(position).get("item_name"));

        boolean isCloud = (boolean) list.get(position).get("item_is_cloud");
        boolean isLast = (boolean) list.get(position).get("item_is_last");
        if (isCloud) {
            mName.setBackgroundResource(R.drawable.shape_label_cloud_bg);
        } else {
            mName.setBackgroundResource(R.drawable.shape_label_bg);
        }
        if (!isLast) {
            mImage.setImageResource((int) list.get(position).get("item_image"));
            mImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 0) {
                        mImage.setElevation(ScreenUtil.Dp2Pixel(10.0f));
                        mImage.setScaleX(0.95f);
                        mImage.setScaleY(0.95f);
                    } else if (motionEvent.getAction() == 1) {
                        mImage.setElevation(ScreenUtil.Dp2Pixel(30.0f));
                        mImage.setScaleX(1.0f);
                        mImage.setScaleY(1.0f);
                    }
                    return false;
                }
            });
        } else {
            mImage.setElevation(ScreenUtil.Dp2Pixel(0.0f));
            mImage.setScaleX(1.05f);
            mImage.setScaleY(1.05f);
            mImage.setBackgroundResource((int) list.get(position).get("item_image"));
            mName.setElevation(ScreenUtil.Dp2Pixel(0.0f));
            mName.setBackgroundColor(0);
            mName.setTextColor(-14969613);
            mName.setAlpha(1.0f);
        }
        if (position == this.mHidePosition) {
            convertView.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public void reorderItems(int oldPosition, int newPosition) {
        HashMap<String, Object> temp = list.get(oldPosition);
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else if (oldPosition > newPosition) {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }

        list.set(newPosition, temp);
    }

    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    @Override
    public void removeItem(int removePosition) {
        list.remove(removePosition);
        notifyDataSetChanged();
    }
}