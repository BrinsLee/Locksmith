package com.brins.locksmith.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.brins.locksmith.BaseApplication.Companion.context
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.utils.AccountIconUtil
import com.brins.locksmith.utils.jumpToEditActivity
import com.chad.library.adapter.base.BaseViewHolder

class BasePasswordHolder(view: View) : BaseViewHolder<BaseMainData>(view) {

    private val itemRoot: ConstraintLayout = view.findViewById(R.id.item_root)
    private val tvName: TextView = view.findViewById(R.id.tv_name)
    private val tvAccount: TextView = view.findViewById(R.id.tv_account)
    private val ivIcon: ImageView = view.findViewById(R.id.iv_password)

    override fun setData(data: BaseMainData) {
        super.setData(data)
        val image = AccountIconUtil.generateBitmap(data, mContext)
        ivIcon.setImageDrawable(image)
        tvName.text = data.generalItems[APPNAME]
        tvAccount.text = data.generalItems[USERNAME]
        itemRoot.setOnClickListener {
            jumpToEditActivity(data.pos, data.itemType)
        }
    }
}