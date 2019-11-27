package com.brins.locksmith.data

class AesEncryptedData constructor(var data: ByteArray, var iv: ByteArray) {
    constructor(data: ByteArray, iv: ByteArray, tag: ByteArray) : this(data, iv)
}