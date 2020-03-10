package com.brins.locksmith.viewmodel.passport

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Log
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.utils.*
import org.bouncycastle.util.encoders.Hex
import java.io.IOException
import java.lang.Exception
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class PassportRepository private constructor() {

    /***主密钥*/
    private var masterSecretKey: SecretKey? = null
    /***设备密钥*/
    private var deviceSecretKey: SecretKey? = null


    companion object {
        private val PassportPreferenceName = "passport"
        private var userID: ByteArray? = null
        private var deviceID: ByteArray? = null
        private val KeyStoreProvider = "AndroidKeyStore"
        private val DeviceKeyAlias = "com.brins.locksmith.locksmith"

        private lateinit var instance: PassportRepository


        fun getInstance(): PassportRepository {
            if (!::instance.isInitialized) {
                synchronized(PassportRepository::class.java) {
                    if (!::instance.isInitialized) {
                        instance = PassportRepository()
                    }
                }
            }
            return instance
        }
    }


    /***判断密钥是都有效*/
    fun isPassportValid(): Boolean {
        if (deviceSecretKey == null) {
            return false
        }

        if (userID == null) {
            return false
        }

        if (masterSecretKey == null) {
            return false
        }

        if (deviceID == null) {
            return false
        }
        return true

    }

    /***从KeyStore加载设备密钥*/
    fun loadDeviceSecretKey(): Boolean {
        try {
            val keyStore = KeyStore.getInstance(KeyStoreProvider)
            keyStore.load(null)
            val secretKeyEntry =
                keyStore.getEntry(DeviceKeyAlias, null) as? KeyStore.SecretKeyEntry ?: return false
            deviceSecretKey = secretKeyEntry.secretKey
            return true
        } catch (e: CertificateException) {
            Log.e("Passport", "Failed to load device secret key", e)
            return false
        } catch (e: NoSuchAlgorithmException) {
            Log.e("Passport", "Failed to load device secret key", e)
            return false
        } catch (e: IOException) {
            Log.e("Passport", "Failed to load device secret key", e)
            return false
        } catch (e: UnrecoverableEntryException) {
            Log.e("Passport", "Failed to load device secret key", e)
            return false
        } catch (e: KeyStoreException) {
            Log.e("Passport", "Failed to load device secret key", e)
            return false
        }
    }

    /***初始化密钥信息*/
    fun initPassport(): Boolean {
        return try {
            userID = decryptFromPreference(
                SpUtils.obtain(PassportPreferenceName).getString(USERID_IV_KEY, "")
                , SpUtils.obtain(PassportPreferenceName).getString(USERID_DATA_KEY, "")
            )
            val masterKey = decryptFromPreference(
                SpUtils.obtain(PassportPreferenceName).getString(
                    MASTER_IV_KEY, ""
                ),
                SpUtils.obtain(PassportPreferenceName).getString(MASTER_DATA_KEY, "")
            )
            masterSecretKey = SecretKeySpec(masterKey, "AES")
            deviceID = decryptFromPreference(
                SpUtils.obtain(PassportPreferenceName).getString(
                    DEVICEID_IV_KEY, ""
                ),
                SpUtils.obtain(PassportPreferenceName).getString(DEVICEID_DATA_KEY, "")
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun decryptFromPreference(ivString: String, dataString: String): ByteArray? {
        val iv = Hex.decode(ivString)
        val data = Hex.decode(dataString)
        return decryptInKeystore(data, iv)
    }


    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidAlgorithmParameterException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    /***使用设备密钥进行加密*/
    private fun encryptInKeystore(data: ByteArray): AesEncryptedData {
        val cipher = newAesInKeystore()
        cipher.init(Cipher.ENCRYPT_MODE, deviceSecretKey)
        return AesEncryptedData(cipher.doFinal(data), cipher.iv)
    }

    /***解密*/
    private fun decryptInKeystore(data: ByteArray?, iv: ByteArray?): ByteArray? {
        require(!(data == null || iv == null)) { "data or iv must not be null" }
        val cipher = newAesInKeystore()
        cipher.init(Cipher.DECRYPT_MODE, deviceSecretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(data)

    }

    /***创建密钥信息*/
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidAlgorithmParameterException::class
    )
    fun createPassport(): Boolean {
        if (!createHardwareDeviceKey()) {
            return false
        }
        /***生成UserUUId*/
        userID = newUUID()
        masterSecretKey = newAes256Key()
        val passportKeyPair = generateKeyPair()

        /***生成deviceUUid*/
        deviceID = newUUID()
        val deviceKeyPair = generateKeyPair()
        /***加密UserUUid*/
        val encryptedUserID = encryptInKeystore(userID!!)
        /***加密主密钥*/
        val encryptedMasterKey = encryptInKeystore(masterSecretKey!!.encoded)

        val encryptedPassportPublicKey = encryptInKeystore(passportKeyPair.public.encoded)
        val encryptedPassportPrivateKey = encryptInKeystore(passportKeyPair.private.encoded)
        val encryptedDeviceID = encryptInKeystore(deviceID!!)
        val encryptedDevicePublicKey = encryptInKeystore(deviceKeyPair.public.encoded)
        val encryptedDevicePrivateKey = encryptInKeystore(deviceKeyPair.private.encoded)
        /***Uid*/
        SpUtils.obtain(PassportPreferenceName)
            .save(USERID_IV_KEY, Hex.toHexString(encryptedUserID.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(USERID_DATA_KEY, Hex.toHexString(encryptedUserID.data))
        /***密钥*/
        SpUtils.obtain(PassportPreferenceName)
            .save(MASTER_IV_KEY, Hex.toHexString(encryptedMasterKey.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(MASTER_DATA_KEY, Hex.toHexString(encryptedMasterKey.data))
        /***User密钥对公钥*/
        SpUtils.obtain(PassportPreferenceName)
            .save(PASSPORT_PUBLIC_IV_KEY, Hex.toHexString(encryptedPassportPublicKey.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(PASSPORT_PUBLIC_DATA_KEY, Hex.toHexString(encryptedPassportPublicKey.data))
        /***User密钥对私钥*/
        SpUtils.obtain(PassportPreferenceName)
            .save(PASSPORT_PRIVATE_IV_KEY, Hex.toHexString(encryptedPassportPrivateKey.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(PASSPORT_PRIVATE_DATA_KEY, Hex.toHexString(encryptedPassportPrivateKey.data))
        /***DeviceId*/
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICEID_IV_KEY, Hex.toHexString(encryptedDeviceID.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICEID_DATA_KEY, Hex.toHexString(encryptedDeviceID.data))
        /***Device公钥*/
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICE_PUBLIC_IV_KEY, Hex.toHexString(encryptedDevicePublicKey.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICE_PUBLIC_DATA_KEY, Hex.toHexString(encryptedDevicePublicKey.data))
        /***Device私钥*/
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICE_PRIVATE_IV_KEY, Hex.toHexString(encryptedDevicePrivateKey.iv))
        SpUtils.obtain(PassportPreferenceName)
            .save(DEVICE_PRIVATE_DATA_KEY, Hex.toHexString(encryptedDevicePrivateKey.data))
        return true
    }

    /***判断密钥是否存在硬件模块中*/
    private fun createHardwareDeviceKey(): Boolean {
        return try {
            /***创建密钥生成器*/
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KeyStoreProvider)
            /***配置密钥生成器参数*/
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(DeviceKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
            keyGenerator.init(keyGenParameterSpec)
            /***生成设备密钥*/
            deviceSecretKey = keyGenerator.generateKey()

            val factory = SecretKeyFactory.getInstance(deviceSecretKey?.algorithm, KeyStoreProvider)
            val keyInfo = factory.getKeySpec(deviceSecretKey, KeyInfo::class.java) as KeyInfo
            /***判断设备是否支持硬件安全模块*/
            keyInfo.isInsideSecureHardware
        } catch (e: Exception) {
            false
        }
    }
}