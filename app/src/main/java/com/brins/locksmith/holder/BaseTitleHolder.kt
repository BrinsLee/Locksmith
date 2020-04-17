package com.brins.locksmith.holder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.brins.locksmith.R
import com.brins.locksmith.data.GeneralTitleItem
import com.chad.library.adapter.base.BaseViewHolder

/**
 * @author lipeilin
 * @date 2020/4/14
 */
class BaseTitleHolder(view: View) : BaseViewHolder<GeneralTitleItem>(view) {

    private val rlRoot: RelativeLayout = view.findViewById(R.id.title_root)
    private val tvName: TextView = view.findViewById(R.id.tv_title)
    private val ivIcon: ImageView = view.findViewById(R.id.iv_up_down)

    override fun setData(data: GeneralTitleItem) {
        super.setData(data)
        tvName.text = data.title
        if (data.expend) ivIcon.setImageResource(R.drawable.ic_up) else ivIcon.setImageResource(R.drawable.ic_down)
        rlRoot.setOnClickListener {
            data.expend = !data.expend
            if (data.expend) ivIcon.setImageResource(R.drawable.ic_up) else ivIcon.setImageResource(
                R.drawable.ic_down
            )
            data.getListener()?.onExpend(it, data.expend, data.type)
        }
    }
}