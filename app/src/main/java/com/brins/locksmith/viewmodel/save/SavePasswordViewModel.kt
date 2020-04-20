package com.brins.locksmith.viewmodel.save

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.AppConfig
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.utils.aes256Decrypt
import com.brins.locksmith.utils.aes256Encrypt
import com.brins.locksmith.viewmodel.base.BaseViewModel
import com.brins.locksmith.viewmodel.passport.PassportRepository
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import org.bouncycastle.util.encoders.Hex
import tech.bluespace.id_guard.AccountItemOuterClass
import tech.bluespace.id_guard.AccountItemOuterClass.AccountGeneralData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap
import kotlin.collections.set

class SavePasswordViewModel(repository: PassportRepository) : BaseViewModel(repository) {


    var mPassWordData = MutableLiveData<ArrayList<PassWordItem>>()

    companion object {
        private val path = BaseApplication.context.applicationInfo.dataDir + "/account_file/"
    }

    /***保存密码*/
    fun savePassWord(
        mName: String,
        mAccountName: String,
        mPassword: String,
        mNote: String,
        finish: () -> Unit

    ) {
        val password = createItem(mName, mAccountName, mPassword, mNote)
        saveData(getAccountDirectory(), password, finish)
    }

    /***创建密码对象*/
    private fun createItem(
        mName: String,
        mAccountName: String,
        mPassword: String,
        mNote: String
    ): PassWordItem {
        return PassWordItem(
            mName,
            mAccountName,
            mPassword,
            mNote
        )
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


    fun loadPasswordItem(): ArrayList<PassWordItem> {

        if (mPassWordData.value == null)
            mPassWordData.value = ArrayList()
        else
            mPassWordData.value!!.clear()
        val mFiles = ArrayList<File>()
        val folder = File(path)
        if (!folder.exists()) {
            return mPassWordData.value!!
        }
        for (file in folder.listFiles()) {
            if (!file.isDirectory) {
                mFiles.add(file)
            }

        }
        if (mFiles.isEmpty()) {
            return mPassWordData.value!!
        }
        for (file in mFiles) {
            try {
                val item = getPasswordItem(file)
                mPassWordData.value!!.add(item.setPosition(mPassWordData.value!!.size))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mPassWordData.value!!
    }

/*    fun getPasswordItem(accountId: ByteArray?): PassWordItem? {
        return try {
            getPasswordItem(getAccountFile(accountId))
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Failed to load account " + Hex.toHexString(accountId))
            null
        }
    }*/

    @NonNull
    private fun getAccountFile(accountId: ByteArray): File {
        val fileName = Hex.toHexString(accountId) + ".data"
        return File(path, fileName)
    }

    @Throws(
        InvalidKeyException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        IOException::class
    )
    private fun getPasswordItem(file: File): PassWordItem {
        val item = PassWordItem()
        return loadFromFile(item, file)
    }

    @Throws(
        IOException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class
    )
    private fun loadFromFile(base: BaseMainData, file: File): PassWordItem {
        val fis = FileInputStream(file)
        val item: AccountItemOuterClass.AccountItem =
            AccountItemOuterClass.AccountItem.parseFrom(fis)
        if (item.version != AccountItemOuterClass.AccountItemVersion.accountItemV20200314) {
            throw RuntimeException("unsupported account item version " + item.version)
        }
        base.meta = decryptMeta(item)
        base.generalItems = decryptGeneralItems(item, base.meta!!)
        base.setAppName(base.generalItems[AppConfig.APPNAME])
        base.secretData = AesEncryptedData(
            item.secret.data.toByteArray()
            , item.secret.iv.toByteArray()
            , item.secret.tag.toByteArray()
        )
        return base as PassWordItem
    }

    @Throws(
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        InvalidProtocolBufferException::class
    )
    private fun decryptMeta(data: AccountItemOuterClass.AccountItem): AccountItemOuterClass.AccountItemMeta? {
        val decrypted: ByteArray =
            repository.decryptData(data.meta.data.toByteArray(), data.meta.iv.toByteArray())
        return AccountItemOuterClass.AccountItemMeta.parseFrom(decrypted)
    }

    @Throws(
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        InvalidProtocolBufferException::class
    )
    private fun decryptGeneralItems(
        data: AccountItemOuterClass.AccountItem,
        meta: AccountItemOuterClass.AccountItemMeta
    ): MutableMap<String, String> {
        val decrypted: ByteArray = aes256Decrypt(
            meta.getAccountKey().toByteArray(),
            data.general.data.toByteArray(),
            data.general.iv.toByteArray()
        )
        val general = AccountGeneralData.parseFrom(decrypted)
        val generals: MutableMap<String, String> = HashMap()
        for (item in general.itemsList) {
            generals[item.key] = item.value
        }
        return generals
    }

    fun hasPassword(): Boolean {
        return !mPassWordData.value.isNullOrEmpty()
    }

    fun dataSize(): Int {
        return mPassWordData.value?.size ?: 0
    }
}