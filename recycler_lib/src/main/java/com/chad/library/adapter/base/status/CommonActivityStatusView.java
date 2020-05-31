package com.chad.library.adapter.base.status;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mushroom0417 on 19/12/2017.
 */

public class CommonActivityStatusView extends CommonStatusView {

    private ViewGroup mRoot;
    private View mContentView;

    public CommonActivityStatusView init(ViewGroup root, int contentViewId) {
        return init(root, root.findViewById(contentViewId));
    }

    public CommonActivityStatusView init(ViewGroup root, View contentView) {
        init(root.getContext());
        mRoot = root;
        mContentView = contentView;
        getRoot().setLayoutParams(mContentView.getLayoutParams());
        return this;
    }

    public void hideStatus() {
        resetStatus();
        if (mContentView.getParent() == null) {
            mRoot.addView(mContentView);
        }
        if (getRoot().getParent() != null) {
            mRoot.removeView(getRoot());
        }
    }

    @Override
    public void showStatus(int status) {
        super.showStatus(status);
        if (mContentView.getParent() != null) {
            mRoot.removeView(mContentView);
        }
        if (getRoot().getParent() == null) {
            mRoot.addView(getRoot());
        }
    }
}
