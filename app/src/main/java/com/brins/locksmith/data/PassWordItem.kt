package com.brins.locksmith.data

class PassWordItem(
    name: String = "",
    accountName : String,
    password: String,
    note: String = ""
) : BaseMainData(name,accountName, password, /*createDate, modifyDate,*/ note){

}