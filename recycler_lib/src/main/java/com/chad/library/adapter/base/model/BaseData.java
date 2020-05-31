package com.chad.library.adapter.base.model;

public abstract class BaseData {
    private int mIndex;

    /**
     * 这里如果需要自定义id，就覆盖返回真实的id
     */
    public int getIndex() {
        return mIndex;
    }

    public BaseData setIndex(int index) {
        mIndex = index;
        return this;
    }

    /**
     * 只有isValidData()返回true，这个返回值才会被考虑，否则会被忽略
     * 如果返回true，adapter会自动根据数据的position自动为其赋值id。
     * 如果返回false，adapter就通过getIndex获取数据自定义的index。
     */
    public abstract boolean isAutoIndex();

    /**
     * 是否是有效的数据，无效数据会忽略其index，也不会为其自动赋值index
     * 比如在一些混合数据类型的列表中，有一些header，title的item，这些实际上是不算有效数据的
     */
    public abstract boolean isValidData();

    /**
     * 如果是true，那么这个数据还是会存在于list中，只不过不显示出来，比如15个数据，有一个是empty，那么这个数据不会显示，但是实际的数量还是15
     */
    public abstract boolean isEmpty();

    public abstract int getItemType();
}
