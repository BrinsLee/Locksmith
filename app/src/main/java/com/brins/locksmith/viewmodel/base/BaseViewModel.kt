package com.brins.locksmith.viewmodel.base

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.data.PassWordItem
import com.brins.locksmith.utils.aes256Decrypt
import com.brins.locksmith.utils.aes256Encrypt
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import org.bouncycastle.util.encoders.Hex
import tech.bluespace.id_guard.AccountItemOuterClass
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.HashMap
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

/**
 * @author lipeilin
 * @date 2020/4/15
 */
open class BaseViewModel : ViewModel() {
    
}