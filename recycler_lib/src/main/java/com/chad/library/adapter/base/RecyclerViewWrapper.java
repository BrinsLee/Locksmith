package com.chad.library.adapter.base;

import androidx.annotation.ColorRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.chad.library.adapter.base.status.IPullToRefresh;

/**
 * Created by flamingo on 2017/12/6.
 * 抛出两个RecyclerView接口，可以动态选择SwipeRefreshLayout嵌套在RecyclerView的某个父布局中
 */

public class RecyclerViewWrapper implements IPullToRefresh {

    private class DelaySetOnRefreshListenerRunnable implements Runnable {

        private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener;

        public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
            mOnRefreshListener = onRefreshListener;
        }

        @Override
        public void run() {
            mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        }

    }

    private class DelaySetEnableRunnable implements Runnable {

        private boolean mEnable;

        public void setEnable(boolean enable) {
            mEnable = enable;
        }

        @Override
        public void run() {
            mSwipeRefreshLayout.setEnabled(mEnable);
        }

    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int[] mSwipeRefreshLayoutColorResIds;
    private DelaySetOnRefreshListenerRunnable mDelaySetOnRefreshListenerRunnable;
    private DelaySetEnableRunnable mDelaySetEnableRunnable;

    public RecyclerViewWrapper() {
    }

/*    @Override
    public void attachRecyclerView(RecyclerView recyclerView) {
        mSwipeRefreshLayout = new SwipeRefreshLayout(recyclerView.getContext());
        if (mSwipeRefreshLayoutColorResIds != null) {
            mSwipeRefreshLayout.setColorSchemeResources(mSwipeRefreshLayoutColorResIds);
        }
        if (mDelaySetOnRefreshListenerRunnable != null)
            mDelaySetOnRefreshListenerRunnable.run();
        if (mDelaySetEnableRunnable != null)
            mDelaySetEnableRunnable.run();
        ViewParent viewParent = getSwipeRefreshLayoutViewParent(recyclerView);
        View view = getSwipeRefreshLayoutView(recyclerView);
        if (viewParent != null && !(viewParent instanceof SwipeRefreshLayout)) {
            ViewGroup viewGroup = (ViewGroup) viewParent;
            int originalIndex = viewGroup.indexOfChild(view);
            viewGroup.removeView(view);
            mSwipeRefreshLayout.setLayoutParams(view.getLayoutParams());
            mSwipeRefreshLayout.addView(view);
            viewGroup.addView(mSwipeRefreshLayout, originalIndex);
        }
    }*/

    protected ViewParent getSwipeRefreshLayoutViewParent(RecyclerView recyclerView) {
        return recyclerView.getParent();
    }

    protected View getSwipeRefreshLayoutView(RecyclerView recyclerView) {
        return recyclerView;
    }

    @Override
    public void attachRecyclerView(RecyclerView recyclerView) {
        mSwipeRefreshLayout = new SwipeRefreshLayout(recyclerView.getContext());
        if (mSwipeRefreshLayoutColorResIds != null) {
            mSwipeRefreshLayout.setColorSchemeResources(mSwipeRefreshLayoutColorResIds);
        }
        if (mDelaySetOnRefreshListenerRunnable != null)
            mDelaySetOnRefreshListenerRunnable.run();
        if (mDelaySetEnableRunnable != null)
            mDelaySetEnableRunnable.run();
        ViewParent viewParent = getSwipeRefreshLayoutViewParent(recyclerView);
        View view = getSwipeRefreshLayoutView(recyclerView);
        if (viewParent != null && !(viewParent instanceof SwipeRefreshLayout)) {
            ViewGroup viewGroup = (ViewGroup) viewParent;
            int originalIndex = viewGroup.indexOfChild(view);
            viewGroup.removeView(view);
            mSwipeRefreshLayout.setLayoutParams(view.getLayoutParams());
            mSwipeRefreshLayout.addView(view);
            viewGroup.addView(mSwipeRefreshLayout, originalIndex);
        }
    }

    @Override
    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        mSwipeRefreshLayoutColorResIds = colorResIds;
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(mSwipeRefreshLayoutColorResIds);
        }
    }

    @Override
    public void setRefreshing(boolean isRefreshing) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public boolean isRefreshing() {
        return mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing();
    }

    @Override
    public void setEnable(boolean isEnable) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setEnabled(isEnable);
        else {
            mDelaySetEnableRunnable = new DelaySetEnableRunnable();
            mDelaySetEnableRunnable.setEnable(isEnable);
        }
    }


    @Override
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setOnRefreshListener(listener);
        else {
            mDelaySetOnRefreshListenerRunnable = new DelaySetOnRefreshListenerRunnable();
            mDelaySetOnRefreshListenerRunnable.setOnRefreshListener(listener);
        }
    }


}
