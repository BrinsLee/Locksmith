package com.brins.locksmith.data.card

import com.brins.locksmith.data.AppConfig.LOCATION
import com.brins.locksmith.data.AppConfig.PHONE
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.utils.newAes256Key

/**
 * @author lipeilin
 * @date 2020/4/16
 */
class CardItem(
    name: String = "",
    accountName: String = "",
    password: String = "",
    note: String = "",
    var phone : String = "",
    var location : String = ""

) : BaseMainData(name, accountName, password) {
    init {
        if (phone.isNotEmpty()) {
            generalItems[PHONE] = phone
        }
        if (location.isNotEmpty()){
            generalItems[LOCATION] = location
        }
    }

    override fun getItemType(): Int {
        return BaseMainItemType.ITEM_NORMAL_CARD
    }

    fun setPosition(size: Int): CardItem {
        pos = size
        return this
    }

}