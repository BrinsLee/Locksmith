package com.brins.locksmith.data.password

import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.utils.getVersionCode
import com.brins.locksmith.utils.newAes256Key
import com.brins.locksmith.utils.newUUID
import com.google.protobuf.ByteString
import tech.bluespace.id_guard.AccountItemOuterClass
import java.util.*

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