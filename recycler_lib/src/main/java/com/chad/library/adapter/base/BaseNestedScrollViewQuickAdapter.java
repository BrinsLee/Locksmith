package com.chad.library.adapter.base;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;

import androidx.core.widget.NestedScrollView;

import com.chad.library.adapter.base.model.BaseData;
import com.chad.library.adapter.base.status.IPullToRefresh;

/**
 * NestedScrollView嵌套RecyclerView的时候使用BaseNestedScrollViewQuickAdapter
 */
public abstract class BaseNestedScrollViewQuickAdapter<T extends BaseData, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    public BaseNestedScrollViewQuickAdapter(NestedScrollView view) {
        this(view, null);
    }

    public BaseNestedScrollViewQuickAdapter(final NestedScrollView view, final NestedScrollView.OnScrollChangeListener onScrollChangeListener) {
        if (null != view) {
            setEnableLoadMore(true);
            setEnableManualLoadMore(true);
            view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (null != onScrollChangeListener) {
                        onScrollChangeListener.onScrollChange(nestedScrollView, scrollX, scrollY, oldScrollX, oldScrollY);
                    }
                    //判断是否滑到的底部、判断是否刷新中
                    if (mUserDefineEnableLoadMore && mManuallyLoadMoreEnable
                            && scrollY == (nestedScrollView.getChildAt(0).getMeasuredHeight()
                            - nestedScrollView.getMeasuredHeight()) && !isLoading()) {
                        manuallyLoadMore();
                    }
                }
            });
        }
    }

    @Override
    public IPullToRefresh getIPullToRefresh() {
        // SwipeRefreshLayout嵌套NestedScrollView,NestedScrollView嵌套RecyclerView
        return new RecyclerViewWrapper() {
            @Override
            protected ViewParent getSwipeRefreshLayoutViewParent(RecyclerView recyclerView) {
                return recyclerView.getParent().getParent().getParent();
            }

            @Override
            protected View getSwipeRefreshLayoutView(RecyclerView recyclerView) {
                return (View) recyclerView.getParent().getParent();
            }
        };
    }
}

