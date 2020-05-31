package com.chad.library.adapter.base;

import com.chad.library.adapter.base.model.BaseData;

import java.util.List;

/**
 * Created by mushroom0417 on 08/12/2017.
 * 外部请求结束的时候去通知内部的callback
 */

public interface OnLoadDataCompleteCallback<T extends BaseData> {
    void onLoadDataSuccess(List<T> newData);
    void onLoadDataFail();
    void onLoadDataFail(int extraStatus);
}
