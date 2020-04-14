package com.brins.locksmith.data

import com.brins.locksmith.ui.base.BaseMainItemType
import java.io.File

class PassWordItem(
    name: String = "",
    accountName : String = "",
    password: String = "",
    note: String = ""
) : BaseMainData(name,accountName, password, /*createDate, modifyDate,*/ note){

    override fun getItemType(): Int {
        return BaseMainItemType.ITEM_NORMAL
    }
}