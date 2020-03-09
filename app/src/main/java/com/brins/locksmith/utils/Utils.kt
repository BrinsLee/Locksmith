package com.brins.locksmith.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Message
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
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


//状态栏相关
fun setTranslucent(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        // 设置状态栏透明
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 设置根布局的参数
        /*            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);*/
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }
}


fun setTextDark(window: Window, isDark: Boolean) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val decorView = window.decorView
        val systemUiVisibility = decorView.systemUiVisibility;
        if (isDark) {
            decorView.systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun setColorTranslucent(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        // 设置状态栏透明
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // 设置根布局的参数
        /*            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);*/
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun getStatusBarHeight(context: Context): Int {
    // 获得状态栏高度
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
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
class WeakHandler constructor(handler: IHandler) : Handler() {
    interface IHandler {
        fun handleMsg(msg: Message)
    }

    private val mRef: WeakReference<IHandler> = WeakReference(handler)

    override fun handleMessage(msg: Message?) {
        val handler = mRef.get()
        if (handler != null && msg != null) {
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


//高斯模糊
fun doBlur(
    sentBitmap: Bitmap, radius: Int,
    canReuseInBitmap: Boolean
): Bitmap? {
    val bitmap: Bitmap
    bitmap = if (canReuseInBitmap) {
        sentBitmap
    } else {
        sentBitmap.copy(sentBitmap.config, true)
    }
    if (radius < 1) {
        return null
    }
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(w.coerceAtLeast(h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack =
        Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - Math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = Math.min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = 0.coerceAtLeast(yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - Math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {
            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = (-0x1000000 and pix[yi] or (dv[rsum] shl 16)
                    or (dv[gsum] shl 8) or dv[bsum])
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = (y + r1).coerceAtMost(hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}