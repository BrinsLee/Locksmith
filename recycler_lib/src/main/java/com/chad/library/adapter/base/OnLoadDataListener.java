package com.chad.library.adapter.base;

import com.chad.library.adapter.base.model.BaseData;

/**
 * Created by mushroom0417 on 08/12/2017.
 * 外部去监听什么时候去请求数据
 */
public interface OnLoadDataListener<T extends BaseData> {
    /**
     * 在内部监听到需要请求数据的时候就会回调这个方法
     * @param headIndex 数据开头的Index
     * @param count 需要请求多少个数据
     * @param onLoadDataCompleteCallback 在请求数据结束的时候，外部通知内部的callback
     */
    void onRequestData(int headIndex, int count, OnLoadDataCompleteCallback<T> onLoadDataCompleteCallback);
}
