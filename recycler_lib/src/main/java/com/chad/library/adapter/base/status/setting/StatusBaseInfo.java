package com.chad.library.adapter.base.status.setting;

import android.view.View;

public class StatusBaseInfo {
    private int mStatusRes; // 状态图
    private String mStatusText; // 状态图的wording
    private String mBtnText; // 状态按钮wording
    private int mBtnBg; // 按钮背景
    private int mBtnTextSize; // 按钮字体大小
    private int mBtnTextColor;
    private int mBtnVisibility;
    private View.OnClickListener mOnBtnClickListener;

    public String getStatusText() {
        return mStatusText;
    }

    public StatusBaseInfo setStatusText(String statusText) {
        mStatusText = statusText;
        return this;
    }

    public int getStatusRes() {
        return mStatusRes;
    }

    public StatusBaseInfo setStatusRes(int statusRes) {
        mStatusRes = statusRes;
        return this;
    }

    public String getBtnText() {
        return mBtnText;
    }

    public StatusBaseInfo setBtnText(String btnText) {
        mBtnText = btnText;
        return this;
    }

    public int getBtnBg() {
        return mBtnBg;
    }

    public StatusBaseInfo setBtnBg(int btnBg) {
        mBtnBg = btnBg;
        return this;
    }

    public int getBtnTextSize() {
        return mBtnTextSize;
    }

    public StatusBaseInfo setBtnTextSize(int btnTextSize) {
        mBtnTextSize = btnTextSize;
        return this;
    }

    public int getBtnTextColor() {
        return mBtnTextColor;
    }

    public StatusBaseInfo setBtnTextColor(int btnTextColor) {
        mBtnTextColor = btnTextColor;
        return this;
    }

    public int getBtnVisibility() {
        return mBtnVisibility;
    }

    public StatusBaseInfo setBtnVisibility(int btnVisibility) {
        mBtnVisibility = btnVisibility;
        return this;
    }

    public View.OnClickListener getOnBtnClickListener() {
        return mOnBtnClickListener;
    }

    public StatusBaseInfo setOnBtnClickListener(View.OnClickListener onBtnClickListener) {
        mOnBtnClickListener = onBtnClickListener;
        return this;
    }
}
