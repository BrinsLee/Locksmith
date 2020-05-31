package com.chad.library.adapter.base.status.setting;

public class StatusViewSetting {
    public interface ILoadingStatusView {
        StatusBaseInfo getLoadingStatusInfo();
    }

    public interface INoDataStatusView {
        StatusBaseInfo getNoDataStatusInfo();
    }

    public interface INoNetStatusView {
        StatusBaseInfo getNoNetStatusInfo();
    }

    public interface IRequestFailedStatusView {
        StatusBaseInfo getRequestFailedStatusInfo();
    }

    public interface INoLoginStatusView {
        StatusBaseInfo getNoLoginStatusInfo();
    }

    private static StatusViewSetting sInstance;
    private ILoadingStatusView mLoadingStatus;
    private INoDataStatusView mNoDataStatus;
    private INoNetStatusView mNoNetStatus;
    private IRequestFailedStatusView mRequestFailedStatus;
    private INoLoginStatusView mNoLoginStatus;
    private int mTextSize;
    private int mTextColor;
    private int mIconWidth;
    private int mIconHeight;

    public static StatusViewSetting getInstance() {
        if (sInstance == null) {
            synchronized (StatusViewSetting.class) {
                if (sInstance == null) {
                    sInstance = new StatusViewSetting();
                }
            }
        }
        return sInstance;
    }

    public void init(ILoadingStatusView loadingStatus, INoDataStatusView noDataStatus, INoNetStatusView noNetStatus,
                     IRequestFailedStatusView requestFailedStatus, INoLoginStatusView noLoginStatus) {
        init(loadingStatus, noDataStatus, noNetStatus, requestFailedStatus, noLoginStatus, -1, -1, -1, -1);
    }

    public void init(ILoadingStatusView loadingStatus, INoDataStatusView noDataStatus, INoNetStatusView noNetStatus,
                     IRequestFailedStatusView requestFailedStatus, INoLoginStatusView noLoginStatus,
                     int textSize, int textColor) {
        init(loadingStatus, noDataStatus, noNetStatus, requestFailedStatus, noLoginStatus, textSize, textColor, -1, -1);
    }

    public void init(ILoadingStatusView loadingStatus, INoDataStatusView noDataStatus,
                     INoNetStatusView noNetStatus, IRequestFailedStatusView requestFailedStatus,
                     INoLoginStatusView noLoginStatus,
                     int textSize, int textColor, int iconWidth, int iconHeight) {
        mLoadingStatus = loadingStatus;
        mNoDataStatus = noDataStatus;
        mNoNetStatus = noNetStatus;
        mRequestFailedStatus = requestFailedStatus;
        mNoLoginStatus = noLoginStatus;
        mTextSize = textSize;
        mTextColor = textColor;
        mIconWidth = iconWidth;
        mIconHeight = iconHeight;
    }

    public ILoadingStatusView getLoadingStatus() {
        return mLoadingStatus;
    }

    public StatusViewSetting setLoadingStatus(ILoadingStatusView loadingStatus) {
        mLoadingStatus = loadingStatus;
        return this;
    }

    public INoDataStatusView getNoDataStatus() {
        return mNoDataStatus;
    }

    public StatusViewSetting setNoDataStatus(INoDataStatusView noDataStatus) {
        mNoDataStatus = noDataStatus;
        return this;
    }

    public INoNetStatusView getNoNetStatus() {
        return mNoNetStatus;
    }

    public StatusViewSetting setNoNetStatus(INoNetStatusView noNetStatus) {
        mNoNetStatus = noNetStatus;
        return this;
    }

    public IRequestFailedStatusView getRequestFailedStatus() {
        return mRequestFailedStatus;
    }

    public StatusViewSetting setRequestFailedStatus(IRequestFailedStatusView requestFailedStatus) {
        mRequestFailedStatus = requestFailedStatus;
        return this;
    }

    public INoLoginStatusView getNoLoginStatus() {
        return mNoLoginStatus;
    }

    public StatusViewSetting setNoLoginStatus(INoLoginStatusView noLoginStatus) {
        mNoLoginStatus = noLoginStatus;
        return this;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public StatusViewSetting setTextSize(int textSize) {
        mTextSize = textSize;
        return this;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public StatusViewSetting setTextColor(int textColor) {
        mTextColor = textColor;
        return this;
    }

    public int getIconWidth() {
        return mIconWidth;
    }

    public StatusViewSetting setIconWidth(int iconWidth) {
        mIconWidth = iconWidth;
        return this;
    }

    public int getIconHeight() {
        return mIconHeight;
    }

    public StatusViewSetting setIconHeight(int iconHeight) {
        mIconHeight = iconHeight;
        return this;
    }
}
