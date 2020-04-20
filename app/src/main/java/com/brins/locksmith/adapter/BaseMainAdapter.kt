package com.brins.locksmith.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brins.locksmith.R
import com.brins.locksmith.holder.BasePasswordHolder
import com.brins.locksmith.holder.BaseTitleHolder
import com.brins.locksmith.ui.base.BaseMainItemType
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
        when (viewType) {
            BaseMainItemType.ITEM_NORMAL_PASS, BaseMainItemType.ITEM_NORMAL_CARD -> return BasePasswordHolder(
                getItemView(
                    R.layout.item_password_recycler,
                    parent
                )
            )

            else -> {
                return BaseTitleHolder(
                    getItemView(
                        R.layout.item_general_title,
                        parent
                    )
                )
            }
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            val gridManager = manager
            gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (!mData.isEmpty && BaseMainItemType.ITEM_NORMAL_PASS != mData[position].itemType && BaseMainItemType.ITEM_NORMAL_CARD != mData[position].itemType) {
                        gridManager.spanCount
                    } else {
                        1
                    }
                }

            }
        }
    }
}