package com.brins.locksmith.viewmodel.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brins.locksmith.AccountItemOuterClass
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.utils.aes256Encrypt
import com.brins.locksmith.viewmodel.passport.PassportRepository
import com.google.protobuf.ByteString
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Hex
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author lipeilin
 * @date 2020/4/15
 */
open class BaseViewModel(protected val repository: PassportRepository) : ViewModel() {


    companion object {
        val TAG = this::class.java.simpleName
    }

    /*    val provider: AndroidLifecycleScopeProvider by lazy {
            AndroidLifecycleScopeProvider.from(getLifeActivity(), Lifecycle.Event.ON_DESTROY)
        }*/
    private var filePath: File? = null

    @Throws(IOException::class)
    protected fun saveData(
        directory: File,
        password: BaseMainData,
        f: () -> Unit
    ): Boolean {
        filePath = File(directory, Hex.toHexString(getAccountId(password.meta)) + ".data")
        val encryptedMeta = encryptMeta(password.meta)
        val encryptedGeneral = encryptGeneral(password.meta!!, password.generalItems)
        assert(encryptedMeta != null)
        assert(encryptedGeneral != null)
        val builder = AccountItemOuterClass.AccountItem.newBuilder()
            .setVersion(AccountItemOuterClass.AccountItemVersion.accountItemV20200314)
            .setMeta(toBuilder(encryptedMeta!!))
            .setGeneral(toBuilder(encryptedGeneral!!))
            .setSecret(toBuilder(password.secretData!!))
        val fos = FileOutputStream(filePath)
        fos.write(builder.build().toByteArray())
        fos.close()
        f()
        return true
    }

    protected fun deleteData(
        directory: File,
        data: BaseMainData,
        f: () -> Unit
    ) {
        filePath = File(directory, Hex.toHexString(getAccountId(data.meta)) + ".data")
        filePath?.let {
            if (it.exists()){
                it.delete()
            }
            f()
        }
    }


/*    protected fun updateData(item: BaseMainData, directory: File) {
        val encryptedMeta = encryptMeta(item.meta)
        val encryptedGeneral = encryptGeneral(item.meta!!, item.generalItems)
        filePath = File(directory, Hex.toHexString(getAccountId(item.meta)) + ".data")

    }*/

    private fun toBuilder(data: AesEncryptedData): AccountItemOuterClass.AesEncryptedData.Builder {
        return AccountItemOuterClass.AesEncryptedData.newBuilder()
            .setData(ByteString.copyFrom(data.data))
            .setIv(ByteString.copyFrom(data.iv))
//            .setTag(ByteString.copyFrom(data.tag))
    }

    /***加密元数据*/
    private fun encryptMeta(meta: AccountItemOuterClass.AccountItemMeta?): AesEncryptedData? {
        return try {
            repository.encryptData(meta!!.toByteArray())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt meta", e)
            null
        }

    }

    /***加密数据*/
    private fun encryptGeneral(
        meta: AccountItemOuterClass.AccountItemMeta,
        generalItems: MutableMap<String, String>
    ): AesEncryptedData? {
        val builder = AccountItemOuterClass.AccountGeneralData.newBuilder()
        for (entry in generalItems.entries) {
            builder.addItems(
                AccountItemOuterClass.GeneralItem.newBuilder()
                    .setKey(entry.key)
                    .setValue(entry.value)
            )
        }
        val plainText = builder.build().toByteArray()
        return try {
            aes256Encrypt(meta.accountKey.toByteArray(), plainText)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt general items", e)
            null
        }

    }


    private fun getAccountId(meta: AccountItemOuterClass.AccountItemMeta?): ByteArray {
        return meta!!.accountID.toByteArray()
    }

    protected fun launch(block: suspend () -> Unit, error: suspend (Throwable) -> Unit) =
        viewModelScope.launch {
            try {
                block()
            } catch (e: Throwable) {
                error(e)
            }
        }
}