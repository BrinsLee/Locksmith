package com.brins.locksmith.viewmodel.save

import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.AppConfig
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.BaseMainData
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.data.password.SinglePasswordLiveData
import com.brins.locksmith.ui.activity.BaseActivity
import com.brins.locksmith.utils.DomainUtil
import com.brins.locksmith.utils.KnownAppNames
import com.brins.locksmith.utils.aes256Decrypt
import com.brins.locksmith.utils.getSortKey
import com.brins.locksmith.viewmodel.base.BaseViewModel
import com.brins.locksmith.viewmodel.passport.PassportRepository
import com.github.promeg.pinyinhelper.Pinyin.toPinyin
import com.google.protobuf.InvalidProtocolBufferException
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.util.encoders.Hex
import tech.bluespace.id_guard.AccountItemOuterClass
import tech.bluespace.id_guard.AccountItemOuterClass.AccountGeneralData
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import kotlin.collections.ArrayList
import kotlin.collections.set

class SavePasswordViewModel(repository: PassportRepository) : BaseViewModel(repository) {


    var mPassWordData = SinglePasswordLiveData.get()

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
        if (saveData(getAccountDirectory(), password, finish)) {
            val list = ArrayList<PassWordItem>()
            list.addAll(mPassWordData.value!!)
            list.add(password.setPosition(list.size))
//            list.sortBy { getSortKey(it.generalItems[APPNAME]!!.first()) }
            mPassWordData.value = list
        }
    }

    fun updatePassWord(item: PassWordItem, finish: () -> Unit) {
        saveData(getAccountDirectory(), item, finish)
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
        mPassWordData.value!!.sortBy { it.getSort() }
        for (i in 0 until  mPassWordData.value!!.size){
            mPassWordData.value!![i].setPosition(i)
        }
        return mPassWordData.value!!
    }

    fun loadPasswordItemAsync(activity: BaseActivity) {
        if (mPassWordData.value == null)
            mPassWordData.value = ArrayList()
        val provider = AndroidLifecycleScopeProvider.from(activity, Lifecycle.Event.ON_DESTROY)
        Observable.create(ObservableOnSubscribe<ArrayList<PassWordItem>> {
            val mFiles = ArrayList<File>()
            val data = ArrayList<PassWordItem>()
            val folder = File(path)
            if (!folder.exists()) {
                it.onError(Throwable("folder not exists"))
            }
            for (file in folder.listFiles()) {
                if (!file.isDirectory) {
                    mFiles.add(file)
                }

            }
            if (mFiles.isEmpty()) {
                it.onError(Throwable("files not exists"))
            }
            for (file in mFiles) {
                try {
                    val item = getPasswordItem(file)
                    data.add(item.setPosition(mPassWordData.value!!.size))
                } catch (e: Exception) {
                    it.onError(e)
                }
            }
            it.onNext(data)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(provider)
            .subscribe(object : Observer<ArrayList<PassWordItem>> {
                override fun onNext(t: ArrayList<PassWordItem>) {
                    mPassWordData.value = t
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

            })

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

    fun getMatchItems(url: String): ArrayList<PassWordItem> {
        val matchData = ArrayList<PassWordItem>()
        if (hasPassword()) {
            val query = KnownAppNames.knownNames[DomainUtil.getDomainName(url)]
            for (item in mPassWordData.value!!) {
//                val str = toPinyin(item.generalItems[AppConfig.APPNAME]!!, "").toLowerCase()
                if (item.generalItems[AppConfig.APPNAME]!!.matches("[\\u4e00-\\u9fa5]+".toRegex()) &&
                    query == KnownAppNames.knownNames[item.generalItems[APPNAME]]
                ) {

                    matchData.add(item)
                }
            }
        }
        return matchData
    }
}