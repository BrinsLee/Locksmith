package com.brins.locksmith.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.data.password.PassWordItem
import com.chad.library.adapter.base.BaseViewHolder

class BasePasswordHolder(view: View) : BaseViewHolder<BaseMainData>(view) {

    private val tvName: TextView = view.findViewById(R.id.tv_name)
    private val tvAccount: TextView = view.findViewById(R.id.tv_account)
    private val ivIcon: ImageView = view.findViewById(R.id.iv_password)

    override fun setData(data: BaseMainData) {
        super.setData(data)
        tvName.text = data.generalItems[APPNAME]
        tvAccount.text = data.generalItems[USERNAME]
    }
}