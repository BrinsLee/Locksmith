package com.brins.locksmith.viewmodel

import android.app.Application
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.utils.*
import org.bouncycastle.util.encoders.Hex
import java.io.IOException
import java.lang.Exception
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec

class PassportViewModel(application: Application) : AndroidViewModel(application) {

    private var masterSecretKey: SecretKey? = null
    private var deviceSecretKey: SecretKey? = null
    private val KeyStoreProvider = "AndroidKeyStore"
    private val DeviceKeyAlias = "com.brins.locksmith.locksmith"
    private val mUserIdLiveData : MutableLiveData<ByteArray> = MutableLiveData()


    companion object {
        private val PassportPreferenceName = "passport"
        private var userID: ByteArray? = null
        private var deviceID: ByteArray? = null
    }

    fun loadPassport(): Boolean {
        if (isPassportValid()) {
            return true
        }
        if (!loadDeviceSecretKey()) {
            return false
        }
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
    /***加密*/
    private fun encryptInKeystore(data: ByteArray): AesEncryptedData {
        val cipher = newAesInKeystore()
        cipher.init(Cipher.ENCRYPT_MODE, deviceSecretKey)
        return AesEncryptedData(cipher.doFinal(data), cipher.iv)
    }

    /***解密*/
    private fun decryptInKeystore(data: ByteArray?, iv: ByteArray?): ByteArray? {
        if (data == null || iv == null) {
            throw IllegalArgumentException("data or iv must not be null")
        }
        val cipher = newAesInKeystore()
        cipher.init(Cipher.DECRYPT_MODE, deviceSecretKey)
        return cipher.doFinal(data)

    }

    /***判断密钥是都有效*/
    private fun isPassportValid(): Boolean {
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
            false
        }
        return true

    }

    private fun loadDeviceSecretKey(): Boolean {
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
        userID = newUUID()
        masterSecretKey = newAes256Key()
        val passportKeyPair = generateKeyPair()
        /***生成deviceUUid*/

        deviceID = newUUID()
        val deviceKeyPair = generateKeyPair()
        val encryptedUserID = encryptInKeystore(userID!!)
        val encryptedMasterKey = encryptInKeystore(masterSecretKey!!.getEncoded())

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
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KeyStoreProvider)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                DeviceKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()
            keyGenerator.init(keyGenParameterSpec)
            deviceSecretKey = keyGenerator.generateKey()

            val factory =
                SecretKeyFactory.getInstance(deviceSecretKey?.algorithm, KeyStoreProvider)
            val keyInfo = factory.getKeySpec(deviceSecretKey, KeyInfo::class.java) as KeyInfo
            keyInfo.isInsideSecureHardware
        } catch (e: Exception) {
            false
        }
    }

}