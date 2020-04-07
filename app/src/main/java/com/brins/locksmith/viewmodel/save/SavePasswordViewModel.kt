package com.brins.locksmith.viewmodel.save

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.PassWordItem
import com.brins.locksmith.utils.aes256Encrypt
import com.brins.locksmith.viewmodel.passport.PassportRepository
import com.google.protobuf.ByteString
import org.bouncycastle.util.encoders.Hex
import tech.bluespace.id_guard.AccountItemOuterClass
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SavePasswordViewModel(private val repository: PassportRepository) : ViewModel() {


    private var filePath: File? = null
    var mPassWordData = MutableLiveData<ArrayList<PassWordItem>>()

    companion object {
        private val path = BaseApplication.context.applicationInfo.dataDir + "/account_file/"
        private val TAG = this::class.java.simpleName
    }

    /***保存密码*/
    fun savePassWord(
        mName: String,
        mAccountName: String,
        mPassword: String,
        mNote: String,
        activity: AppCompatActivity
    ) {
        val password = createItem(mName, mAccountName, mPassword, mNote)
        saveData(getAccountDirectory(), password, activity)
    }

    /***创建密码对象*/
    private fun createItem(
        mName: String,
        mAccountName: String,
        mPassword: String,
        mNote: String
    ): PassWordItem {
        return PassWordItem(mName, mAccountName, mPassword, mNote)
    }


    @Throws(IOException::class)
    private fun saveData(
        directory: File,
        password: PassWordItem,
        activity: AppCompatActivity
    ) {
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
        notifyData(password, activity)
    }

    private fun notifyData(
        password: PassWordItem,
        activity: AppCompatActivity
    ) {
        if (mPassWordData.value == null) {
            val list = ArrayList<PassWordItem>()
            list.add(password)
            mPassWordData.value = list
        } else {
            mPassWordData.value!!.add(password)
        }
        activity.finish()
    }


    private fun toBuilder(data: AesEncryptedData): AccountItemOuterClass.AesEncryptedData.Builder {
        return AccountItemOuterClass.AesEncryptedData.newBuilder()
            .setData(ByteString.copyFrom(data.data))
            .setIv(ByteString.copyFrom(data.iv))
//            .setTag(ByteString.copyFrom(data.tag))
    }

    /***获取文件保存路径*/
    private fun getAccountDirectory(): File {
        val directory = File(path)
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to prepare accounts directory")
            }
        }
        return directory
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
}