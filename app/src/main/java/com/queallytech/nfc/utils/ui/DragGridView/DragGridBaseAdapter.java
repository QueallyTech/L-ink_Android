package com.queallytech.nfc.utils.ui.DragGridView;

public interface DragGridBaseAdapter {
    /**
     * 重新排列数据
     *
     * @param oldPosition
     * @param newPosition
     */
    void reorderItems(int oldPosition, int newPosition);


    /**
     * 设置某个item隐藏
     *
     * @param hidePosition
     */
    void setHideItem(int hidePosition);

    /**
     * 删除某个item
     *
     * @param removePosition
     */
    void removeItem(int removePosition);

}