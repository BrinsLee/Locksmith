package com.chad.library.adapter.base.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 简单地将一个普通类型的data转化成BaseData类型
 * @param <T>
 */
public class BaseDataWrapper<T> extends BaseData {
    public T mRealData;

    public List<BaseDataWrapper<T>> convert(List<T> realDataList) {
        List<BaseDataWrapper<T>> convertList = new ArrayList<>();
        for (T data : realDataList) {
            BaseDataWrapper<T> baseDataWrapper = new BaseDataWrapper<T>();
            baseDataWrapper.mRealData = data;
            convertList.add(baseDataWrapper);
        }
        return convertList;
    }

    public List<T> reverse(List<BaseDataWrapper<T>> baseDataWrapperList) {
        List<T> reverseList = new ArrayList<>();
        for (BaseDataWrapper<T> baseData : baseDataWrapperList) {
            reverseList.add(baseData.mRealData);
        }
        return reverseList;
    }

    @Override
    public boolean isAutoIndex() {
        return true;
    }

    @Override
    public boolean isValidData() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
