package com.brins.locksmith.adapter

import android.view.ViewGroup
import com.brins.locksmith.R
import com.brins.locksmith.holder.BasePasswordHolder
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.model.BaseData

class BaseMainAdapter :
    BaseQuickAdapter<BaseData, BaseViewHolder<out BaseData>>() {

    override fun onCreateViewHolderByType(
        parent: ViewGroup?,
        viewType: Int
    ): BaseViewHolder<out BaseData> {
/*        when (viewType) {
            TYPE_ITEM_PASSWOED ->
            else ->
        }*/
        return BasePasswordHolder(
            getItemView(
                R.layout.item_password_recycler,
                parent
            )
        )
    }


}