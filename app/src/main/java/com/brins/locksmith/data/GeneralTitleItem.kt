package com.brins.locksmith.data

import android.view.View
import com.brins.locksmith.ui.base.BaseMainItemType
import com.chad.library.adapter.base.model.BaseData

/**
 * @author lipeilin
 * @date 2020/4/14
 */
class GeneralTitleItem(var title: String, var expend: Boolean = false, var type: Int) : BaseData() {


    private var listener: onExpendListener? = null

    fun setListener(listener: onExpendListener): GeneralTitleItem {
        this.listener = listener
        return this
    }

    fun getListener(): onExpendListener? {
        return listener
    }

    override fun isValidData(): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun isAutoIndex(): Boolean {
        return false
    }

    override fun getItemType(): Int {
        return type
    }

    interface onExpendListener {
        fun onExpend(view: View, expend: Boolean, type: Int)
    }

}