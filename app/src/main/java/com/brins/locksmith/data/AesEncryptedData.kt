package com.brins.locksmith.data
/***
 * @param data : 加密密文
 * @param iv ： 参数因子
 * */
class AesEncryptedData constructor(var data: ByteArray, var iv: ByteArray) {
    var tag : ByteArray? = null
    constructor(data: ByteArray, iv: ByteArray, tag: ByteArray) : this(data, iv){
        this.tag = tag
    }
}