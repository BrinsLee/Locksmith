package com.chad.library.adapter.base.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mushroom0417 on 08/12/2017.
 */

public class BaseCollection<T extends BaseData> {

    private final static int INVALID_INDEX = -1;

    private List<T> mData;
    private int mRealSize = 0;
    private int mHeadIndex = 0;
    private int mTailIndex = 0;
    private int mDefaultHeadIndex = 0;

    public void setDefaultHeadIndex(int defaultHeadIndex) {
        mDefaultHeadIndex = defaultHeadIndex;
    }

    public int getDefaultHeadIndex() {
        return mDefaultHeadIndex;
    }

    public BaseCollection() {
        mData = new ArrayList<>();
    }

    protected void evaluateData() {
        mRealSize = 0;
        mHeadIndex = INVALID_INDEX;
        mTailIndex = INVALID_INDEX;
        int tempIndex = getDefaultHeadIndex();
        for (int i = 0; i < mData.size(); i++) {
            T data = mData.get(i);
            if (data.isValidData()) {
                mRealSize++;
                if (data.isAutoIndex()) {
                    data.setIndex(tempIndex);
                    if (mHeadIndex == INVALID_INDEX)
                        mHeadIndex = tempIndex;
                    mTailIndex = ++tempIndex;
                } else {
                    if (mHeadIndex == INVALID_INDEX) {
                        mHeadIndex = data.getIndex();

                    }
                    mTailIndex = data.getIndex();
                }
            }
        }
        if (mData.size() == 0 || mRealSize == 0) {
            mHeadIndex = 0;
            mTailIndex = 0;
        }
    }

    public boolean isEmpty() {
        return mData.size() == 0;
    }

    public int size() {
        return mData.size();
    }

    public void setNewData(List<T> newData) {
        mData.clear();
        mData.addAll(newData);
        evaluateData();
    }

    public void add(T data) {
        mData.add(data);
        evaluateData();
    }

    public void add(int index, T data) {
        mData.add(index, data);
        evaluateData();
    }

    public T get(int index) {
        return mData.get(index);
    }

    public T remove(int index) {
        T data = mData.remove(index);
        evaluateData();
        return data;
    }

    public T set(int index, T data) {
        T result = mData.set(index, data);
        evaluateData();
        return result;
    }

    public boolean addAll(int index, Collection<? extends T> collection) {
        boolean result = mData.addAll(index, collection);
        evaluateData();
        return result;
    }

    public boolean addAll(Collection<? extends T> collection) {
        boolean result = mData.addAll(collection);
        evaluateData();
        return result;
    }

    public void clear() {
        mData.clear();
        evaluateData();
    }

    public List<T> getList() {
        return mData;
    }

    public int indexOf(T data) {
        return mData.indexOf(data);
    }

    public int getHeadIndex() {
        return mHeadIndex;
    }

    public int getTailIndex() {
        return mTailIndex;
    }

    public int getRealSize() {
        return mRealSize;
    }
}
