package com.brins.locksmith.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.data.PassWordItem
import com.chad.library.adapter.base.BaseViewHolder

class BasePasswordHolder(view: View) : BaseViewHolder<PassWordItem>(view) {

    private val tvName: TextView = view.findViewById(R.id.tv_name)
    private val tvAccount: TextView = view.findViewById(R.id.tv_account)
    private val ivIcon: ImageView = view.findViewById(R.id.iv_password)

    override fun setData(data: PassWordItem) {
        super.setData(data)
        tvName.text = data.generalItems[APPNAME]
        tvAccount.text = data.generalItems[USERNAME]
    }
}