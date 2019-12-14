package com.brins.locksmith.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.collection.SimpleArrayMap
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.R
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

//EventBus
fun registerEventBus(subscriber: Any) {
    val bus = EventBus.getDefault()
    if (!bus.isRegistered(subscriber)) {
        bus.register(subscriber)
    }
}

fun unregisterEventBus(subscriber: Any) {
    val bus = EventBus.getDefault()
    if (bus.isRegistered(subscriber)) {
        bus.unregister(subscriber)
    }
}

fun post(event: EventMsg<*>) {
    EventBus.getDefault().post(event)
}

fun postSticky(event: EventMsg<*>) {
    EventBus.getDefault().postSticky(event)
}


class EventMsg<T> {
    var code: Int = 0
    var data: T? = null

    constructor(code: Int) {
        this.code = code
    }

    constructor(code: Int, data: T) {
        this.code = code
        this.data = data
    }
}

fun getTypeface(context: Context, fontType: Int): Typeface? {
    val typefacePath = getTypefacePath(fontType)
    var typeface: Typeface? = null
    if (TYPEFACE_CACHE.containsKey(typefacePath)) {
        val typefaceWr = TYPEFACE_CACHE.get(typefacePath)
        if (typefaceWr != null) {
            typeface = typefaceWr.get()
        }
    }
    if (typeface == null) {
        typeface = Typeface.createFromAsset(context.assets, typefacePath)
        TYPEFACE_CACHE.put(typefacePath, WeakReference(typeface))
    }
    return typeface
}

val TYPEFACE_CACHE = SimpleArrayMap<String, WeakReference<Typeface>>()
private fun getTypefacePath(fontType: Int): String? {
    var typefacePath: String? = null
    when (fontType) {
        1 -> typefacePath = "fonts/DIN-Bold.otf"
        2 -> typefacePath = "fonts/DIN-Medium.otf"
        3 -> typefacePath = "fonts/DIN-Regular.otf"
        4 -> typefacePath = "fonts/LilyScriptOne-Regular.ttf"
        else -> {
        }
    }
    return typefacePath
}

/***低版本设配*/
class FingerprintUiHelper constructor(
    var mFingerprintManager: FingerprintManager,
    var callback: Callback
) : FingerprintManager.AuthenticationCallback() {


    private var mCancellationSignal: CancellationSignal? = null
    private var mSelfCancelled: Boolean = false
    fun startListening() {
        mCancellationSignal = CancellationSignal()
        mSelfCancelled = false
        mFingerprintManager
            .authenticate(null, mCancellationSignal, 0, this, null)
    }

    fun stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true
            mCancellationSignal?.cancel()
            mCancellationSignal = null
        }
    }

    /***验证出错*/
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        if (!mSelfCancelled)
            callback.onError(errorCode, errString.toString())

    }

    /***帮助信息*/
    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpCode, helpString)
//        callback.onHelp(helpString.toString())
    }

    /***验证失败*/
    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        callback.onError(0, BaseApplication.context.getString(R.string.fingerprint_not_recognized))
    }

    /***验证成功*/
    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        callback.onAuthenticated()
    }

    interface Callback {
        fun onAuthenticated()
        fun onError(errorCode: Int, message: String)
        fun onHelp(message: String)
    }
}

//SP
val USERID_IV_KEY = "encryptedUserID.iv"
val USERID_DATA_KEY = "encryptedUserID.data"

val MASTER_IV_KEY = "encryptedMasterKey.iv"
val MASTER_DATA_KEY = "encryptedMasterKey.data"

val DEVICEID_IV_KEY = "encryptedDeviceID.iv"
val DEVICEID_DATA_KEY = "encryptedDeviceID.data"

val PASSPORT_PUBLIC_IV_KEY = "encryptedPassportPublicKey.iv"
val PASSPORT_PUBLIC_DATA_KEY = "encryptedPassportPublicKey.data"

val PASSPORT_PRIVATE_IV_KEY = "encryptedPassportPrivateKey.iv"
val PASSPORT_PRIVATE_DATA_KEY = "encryptedPassportPrivateKey.data"

val DEVICE_PUBLIC_IV_KEY = "encryptedDevicePublicKey.iv"
val DEVICE_PUBLIC_DATA_KEY = "encryptedDevicePublicKey.data"

val DEVICE_PRIVATE_IV_KEY = "encryptedDevicePrivateKey.iv"
val DEVICE_PRIVATE_DATA_KEY = "encryptedDevicePrivateKey.data"
//Cipher
@Throws(NoSuchPaddingException::class, NoSuchAlgorithmException::class)
fun newAesInKeystore(): Cipher {
    return Cipher.getInstance("AES/GCM/NoPadding")
}
/***生成uuid*/
fun newUUID(): ByteArray {
    val uuid = UUID.randomUUID()
    val bb = ByteBuffer.wrap(ByteArray(16))
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)
    return bb.array()
}
/***生成AES256密钥*/
@Throws(NoSuchAlgorithmException::class)
fun newAes256Key(): SecretKey {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256)
    return keyGen.generateKey()
}
/***生成EC密钥对*/
@Throws(
    RuntimeException::class,
    NoSuchAlgorithmException::class,
    InvalidAlgorithmParameterException::class,
    NoSuchProviderException::class
)
fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    val spec = ECGenParameterSpec("prime256v1")
    keyPairGenerator.initialize(spec)
    return keyPairGenerator.generateKeyPair()
}
//WeakHandler
class WeakHandler constructor(handler : IHandler): Handler() {
    interface IHandler{
        fun handleMsg(msg : Message)
    }
    private val mRef : WeakReference<IHandler> = WeakReference(handler)

    override fun handleMessage(msg: Message?) {
        val handler = mRef.get()
        if (handler != null && msg != null){
            handler.handleMsg(msg)
        }
    }
}


fun getDimension(context: Context, @DimenRes id: Int): Int {
    return context.resources.getDimensionPixelSize(id)
}

fun dpToPx(dp: Float, res: Resources): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        res.displayMetrics
    ).toInt()
}

fun createLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams {
    return FrameLayout.LayoutParams(width, height)
}

fun createMatchParams(): FrameLayout.LayoutParams {
    return createLayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
}

fun createWrapParams(): FrameLayout.LayoutParams {
    return createLayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

fun createWrapMatchParams(): FrameLayout.LayoutParams {
    return createLayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
}

fun createMatchWrapParams(): FrameLayout.LayoutParams {
    return createLayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

/*fun setInsets(context: Activity, view: View) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
    val tintManager = SystemBarTintManager(context)
    val config = tintManager.getConfig()
    view.setPadding(
        0,
        config.getPixelInsetTop(false),
        config.getPixelInsetRight(),
        config.getPixelInsetBottom()
    )
}

fun getInsetsTop(context: Activity, view: View): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return 0
    val tintManager = SystemBarTintManager(context)
    val config = tintManager.getConfig()
    return config.getPixelInsetTop(false)
}

fun getInsetsBottom(context: Activity, view: View): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return 0
    val tintManager = SystemBarTintManager(context)
    val config = tintManager.getConfig()
    return config.getPixelInsetBottom()
}*/
