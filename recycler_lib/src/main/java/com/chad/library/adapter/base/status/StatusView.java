package com.chad.library.adapter.base.status;

import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by flamingo on 2017/12/6.
 */

public abstract class StatusView {

    protected int mStatus;
    protected View mRoot;
    protected int mLayoutResourceId;
    private boolean mIsShow = false;

    public static final int STATUS_LOADING = 1;
    public static final int STATUS_NO_DATA = 2;
    public static final int STATUS_NO_NET = 3;
    public static final int STATUS_LOAD_FAILED = 4;
    public static final int STATUS_NO_LOGIN = 5;
    public static final int STATUS_EMPTY = 6; // 空白页面

    @IntDef({STATUS_LOADING, STATUS_NO_DATA, STATUS_NO_NET, STATUS_LOAD_FAILED, STATUS_NO_LOGIN, STATUS_EMPTY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {

    }

    protected SparseArray<View> mViewMap;
    private OnStateViewShowObserver mOnStateViewClickObserver;

    @CallSuper
    public void showStatus(@Status int status) {
        mRoot.setVisibility(View.VISIBLE);
        mStatus = status;
        resetStatus();
        if (status != STATUS_EMPTY) {
            // 不是空白页面才根据状态进行显示
            mViewMap.get(status).setVisibility(View.VISIBLE);
        }
        if (null != mOnStateViewClickObserver) {
            mOnStateViewClickObserver.onStateShowView(status);
        }
        mIsShow = true;
    }

    protected abstract void onBindState();

    protected void bindState(@Status int status, @IdRes int viewId) {
        bindState(status, mRoot.findViewById(viewId));
    }

    protected void bindState(@Status int status, View view) {
        mViewMap.put(status, view);
    }

    /**
     * 把所有的View给Gone掉
     */
    @CallSuper
    public void resetStatus() {
        for (int i = 0; i < mViewMap.size(); i++) {
            View view = mViewMap.valueAt(i);
            view.setVisibility(View.GONE);
        }
        mIsShow = false;
    }

    public StatusView init(Context context) {
        mRoot = LayoutInflater.from(context).inflate(getLayoutId(), null, false);
        mViewMap = new SparseArray<>();
        onBindState();
        resetStatus();
        return this;
    }

    public StatusView init(Context context, ViewGroup parent) {
        mRoot = LayoutInflater.from(context).inflate(getLayoutId(), parent, false);
        mViewMap = new SparseArray<>();
        onBindState();
        resetStatus();
        return this;
    }

    public @Status
    int getStatus() {
        return mStatus;
    }

    public View getRoot() {
        return mRoot;
    }

    /**
     * 返回布局Id
     *
     * @return
     */
    public @LayoutRes
    int getLayoutId() {
        return mLayoutResourceId;
    }

    public void setLayoutId(@LayoutRes int layoutId) {
        mLayoutResourceId = layoutId;
    }

    public boolean isShow() {
        return mIsShow;
    }

    public void setOnStateViewShowObserver(OnStateViewShowObserver onStateViewShowObserver) {
        mOnStateViewClickObserver = onStateViewShowObserver;
    }

    public interface OnStateViewShowObserver {
        void onStateShowView(int state);
    }
}
