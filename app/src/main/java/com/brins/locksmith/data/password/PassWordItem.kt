package com.brins.locksmith.data.password

import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.ui.base.BaseMainItemType

class PassWordItem(
    name: String = "",
    accountName : String = "",
    password: String = "",
    note: String = ""
) : BaseMainData(name,accountName, password, /*createDate, modifyDate,*/ note){

    override fun getItemType(): Int {
        return BaseMainItemType.ITEM_NORMAL_PASS
    }


    fun setPosition(pos : Int) : PassWordItem{
        this.pos = pos
        return this
    }



}