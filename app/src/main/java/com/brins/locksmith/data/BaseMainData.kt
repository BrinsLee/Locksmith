package com.brins.locksmith.data

import android.util.Log
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.NOTE
import com.brins.locksmith.data.AppConfig.PASSWORD
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.utils.*
import com.chad.library.adapter.base.model.BaseData
import com.google.protobuf.ByteString
import tech.bluespace.id_guard.AccountItemOuterClass
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.*

abstract class BaseMainData(
    protected var name: String = "",
    protected var accountName: String,
    protected var password: String,
    protected var mNote: String = ""
) : Serializable, BaseData() {


    override fun isAutoIndex(): Boolean {
        return false
    }

    override fun isValidData(): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun getItemType(): Int {
        return 0
    }

    fun getAppName(): String {
        return name
    }

    fun setAppName(name : String?){
        this.name = name?:""
    }


    var background = -1
    var meta: AccountItemOuterClass.AccountItemMeta? = null
    var generalItems: MutableMap<String, String> = HashMap()
    var secretData: AesEncryptedData? = null

    init {
        if (!isKnownAppName(name)) {
            background = backgroundcolor(name)
        }
        this.makeMetaData()
        generalItems[APPNAME] = name
        generalItems[USERNAME] = accountName
        if (mNote.isNotEmpty()) {
            generalItems[NOTE] = mNote
        }
        encryptPassword(password)
    }

    companion object {
        private val TAG = this::class.java.simpleName

    }

    /***创建元数据*/
    fun makeMetaData() {
        val accountKey = newAes256Key().encoded
        val builder: AccountItemOuterClass.AccountItemMeta.Builder =
            AccountItemOuterClass.AccountItemMeta.newBuilder()
                .setSource(AccountItemOuterClass.AccountItemMeta.AccountSource.create)
                .setType(AccountItemOuterClass.AccountItemMeta.AccountType.unknownType)
                .setAccountID(ByteString.copyFrom(newUUID()))
                .setAccountKey(ByteString.copyFrom(accountKey))
                .setIconTextColor(0xFFFFFFFF.toInt())
                .setCreationDate(Date().time / 1000)
                .setAppVersion(getVersionCode().toString())
        if (background != -1)
            builder.setIconBackgroundColor(background)
        meta = builder.build()
    }

    fun encryptPassword(pass: String) {
        val secretItems = decryptSecret()
        secretItems?.let {
            it[PASSWORD] = password.toByteArray(StandardCharsets.UTF_8)
            secretData = encryptSecret(it)
        }

    }

    private fun encryptSecret(secretItems: Map<String, ByteArray>): AesEncryptedData? {
        val builder = AccountItemOuterClass.AccountSecretData.newBuilder()
        for ((key, value) in secretItems) {
            builder.addItems(
                AccountItemOuterClass.SecretItem.newBuilder()
                    .setKey(key)
                    .setValue(ByteString.copyFrom(value))
            )
        }

        val plainText = builder.build().toByteArray()
        return try {
            aes256Encrypt(meta!!.accountKey.toByteArray(), plainText)
        } catch (e: Exception) {
            Log.e(TAG, "Encrypt secret data failed", e)
            null
        }

    }

    private fun decryptSecret(): MutableMap<String, ByteArray>? {
        val secrets = HashMap<String, ByteArray>()
        if (secretData == null) {
            return secrets
        }
        try {
            val decrypted = aes256Decrypt(
                meta!!.accountKey.toByteArray(),
                secretData!!.data,
                secretData!!.iv
            )
            val secret = AccountItemOuterClass.AccountSecretData.parseFrom(decrypted)
            for (item in secret.itemsList) {
                secrets[item.key] = item.value.toByteArray()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt secret data", e)
        }
        return secrets
    }

    fun getUnifiedName(): String {
        val unifiedName = KnownAppNames.knownNames[name]
        return unifiedName ?: DomainUtil.getDomainName(name)
    }

}