package com.chad.library.adapter.base.status;

/**
 * Created by flamingo on 2017/12/6.
 */

/**
 * 错误状的点击事件
 */
public interface OnStatusClickListener {
    /**
     * 点击状态
     * @param status :StatusView.STATUS_LOADING(加载中),
     *               StatusView.STATUS_NO_DATA(无数据),
     *               StatusView.STATUS_NO_NET(无网络),
     *               StatusView.STATUS_LOAD_FAILED(加载失败),
     */
    void onClick(int status);
}
