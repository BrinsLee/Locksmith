package com.brins.locksmith.data

import java.io.File

class PassWordItem(
    name: String = "",
    accountName : String = "",
    password: String = "",
    note: String = ""
) : BaseMainData(name,accountName, password, /*createDate, modifyDate,*/ note){


}