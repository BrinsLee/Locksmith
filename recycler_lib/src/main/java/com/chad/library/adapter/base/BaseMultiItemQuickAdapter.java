package com.chad.library.adapter.base;

import com.chad.library.adapter.base.model.BaseData;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public abstract class BaseMultiItemQuickAdapter<T extends BaseData, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    /**
     * layouts indexed with their types
     */
    private static final int DEFAULT_VIEW_TYPE = -0xff;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     */
    public BaseMultiItemQuickAdapter() {
        super();
    }

    @Override
    protected int getDefItemViewType(int position) {
        T item = mData.get(position);
        if (item != null) {
            return item.getItemType();
        }
        return DEFAULT_VIEW_TYPE;
    }


//    @Override
//    public void remove(@IntRange(from = 0L) int position) {
//        super.remove(position);
//        if (mData == null
//                || position < 0
//                || position >= mData.size()) {
//            return;
//        }
//
//        T entity = mData.get(position);
//        if (entity instanceof IExpandable) {
//            removeAllChild((IExpandable) entity, position);
//        }
//        removeDataFromParent(entity);
//        super.remove(position);
//    }

    /**
     * 移除父控件时，若父控件处于展开状态，则先移除其所有的子控件
     *
     * @param parent         父控件实体
     * @param parentPosition 父控件位置
     */
//    @Override
//    protected void removeAllChild(IExpandable parent, int parentPosition) {
//        super.removeAllChild(parent, parentPosition);
//        if (parent.isExpanded()) {
//            List<BaseData> chidChilds = parent.getSubItems();
//            if (chidChilds == null || chidChilds.size() == 0) return;
//
//            int childSize = chidChilds.size();
//            for (int i = 0; i < childSize; i++) {
//                remove(parentPosition + 1);
//            }
//        }
//    }

    /**
     * 移除子控件时，移除父控件实体类中相关子控件数据，避免关闭后再次展开数据重现
     *
     * @param child 子控件实体
     */
//    @Override
//    protected void removeDataFromParent(T child) {
//        super.removeDataFromParent(child);
//        int position = getParentPosition(child);
//        if (position >= 0) {
//            IExpandable parent = (IExpandable) mData.get(position);
//            parent.getSubItems().remove(child);
//        }
//    }
}


