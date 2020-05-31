package com.chad.library.adapter.base;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.model.BaseData;
import com.chad.library.adapter.base.status.IPullToRefresh;

/**
 * SwipeRefreshLayout嵌套RecyclerView的时候，SwipeRefreshLayout的高度设为WRAP_CONTENT，会被子布局填充整个屏幕，高度不能随item自适应。
 * BaseNoRefreshQuickAdapter把SwipeRefreshLayout去掉，不嵌套RecyclerView。
 */
public abstract class BaseNoRefreshQuickAdapter<T extends BaseData, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    public BaseNoRefreshQuickAdapter() {
        setEnableRefresh(false);
    }

    @Override
    public IPullToRefresh getIPullToRefresh() {
        return new RecyclerViewWrapper() {
            @Override
            public void attachRecyclerView(RecyclerView recyclerView) {
            }
        };
    }
}

