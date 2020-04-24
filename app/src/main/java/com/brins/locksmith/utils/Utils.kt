package com.brins.locksmith.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Message
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.DimenRes
import androidx.annotation.NonNull
import androidx.collection.SimpleArrayMap
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.R
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.BaseMainData
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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


/***创建密码*/
fun newAesCipher(): Cipher? {
    try {
        return Cipher.getInstance("AES/GCM/NoPadding")
    } catch (e: GeneralSecurityException) {
        Log.e("newAesCipher", "Failed to create AES/GCM/NoPadding cipher", e)
    }

    return null
}

@Throws(
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
fun aes256Decrypt(key: ByteArray, encryped: ByteArray, iv: ByteArray): ByteArray {
    val aesKey = SecretKeySpec(key, 0, key.size, "AES")
    return aes256Decrypt(aesKey, encryped, iv)
}

@Throws(
    InvalidAlgorithmParameterException::class,
    InvalidKeyException::class,
    BadPaddingException::class,
    IllegalBlockSizeException::class
)
private fun aes256Decrypt(key: SecretKey, encryped: ByteArray, iv: ByteArray): ByteArray {
    val cipher = newAesCipher()!!
    cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
    return cipher.doFinal(encryped)
}

@Throws(BadPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class)
fun aes256Encrypt(key: ByteArray, data: ByteArray): AesEncryptedData {
    val aesKey = SecretKeySpec(key, 0, key.size, "AES")
    return aes256Encrypt(aesKey, data)
}

@Throws(InvalidKeyException::class, BadPaddingException::class, IllegalBlockSizeException::class)
private fun aes256Encrypt(key: SecretKey, data: ByteArray): AesEncryptedData {
    val cipher = newAesCipher()!!
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val iv = cipher.iv
    return AesEncryptedData(cipher.doFinal(data), iv)
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

fun getScreenHeight(): Int {
    val windowManager =
        BaseApplication.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        windowManager.defaultDisplay.getRealMetrics(dm)
    } else {
        windowManager.defaultDisplay.getMetrics(dm)
    }
    return dm.heightPixels
}

fun getScreenWeight(): Int {
    val windowManager =
        BaseApplication.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        windowManager.defaultDisplay.getRealMetrics(dm)
    } else {
        windowManager.defaultDisplay.getMetrics(dm)
    }
    return dm.widthPixels
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

fun dip2px(context: Context, dpValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

fun dip2px(dipValue: Float): Int {
    return (dipValue * 1.0 + 0.5f).toInt()
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


fun doBlur(context: Context, sentBitmap: Bitmap, radius: Int): Bitmap? {
    val bitmap = sentBitmap.copy(sentBitmap.config, true)
    val  rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs,sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,Allocation.USAGE_SCRIPT)
    val output = Allocation.createTyped(rs,input.getType())
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(radius.toFloat())/* e.g. 3.f */
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    return bitmap

}

fun getVersionCode(): Int {

    //获取包管理器
    val pm = BaseApplication.context.packageManager
    //获取包信息
    try {
        val packageInfo = pm.getPackageInfo(BaseApplication.context.packageName, 0)
        //返回版本号
        return packageInfo.versionCode
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

fun backgroundcolor(url: String): Int {
    val appNameUserName = url + url
    val hash = Math.abs(appNameUserName.hashCode())
    val colors = arrayOf(
        "#993399",
        "#3399CC",
        "#0099FF",
        "#FF3333",
        "#00CC33",
        "#FF9966",
        "#FF0099",
        "#3300CC",
        "#00CCFF",
        "#000000"
    )
    return Color.parseColor(colors[hash % colors.size])
}

fun isKnownAppName(url: String): Boolean {
    val unifiedName = KnownAppNames.knownNames[url]
    return if (unifiedName != null) {
        true
    } else {
        val unifiedUrl = DomainUtil.getDomainName(url)
        unifiedUrl != url
    }
}

//常用app名称
class KnownAppNames {
    companion object {
        internal val knownNames = makeKnownNames()
        internal val knownNameSortKeys = makeKnownNameSortKeys()
        internal val qq = "im.qq.com"
        internal val weixin = "weixin.qq.com"
        internal val zhifubao = "alipay.com"
        internal val three360 = "360.cn"
        internal val amazon = "amazon.com"
        internal val baidu = "baidu.com"
        internal val bilibili = "bilibili.com"
        internal val csdn = "csdn.net"
        internal val ctrip = "ctrip.com"
        internal val douban = "douban.com"
        internal val douyin = "douyin.com"
        internal val dropbox = "dropbox.com"
        internal val ebay = "ebay.com"
        internal val facebook = "facebook.com"
        internal val flipboard = "flipboard.com"
        internal val github = "github.com"
        internal val gitlab = "gitlab.com"
        internal val google = "google.com"
        internal val hupu = "hupu.com"
        internal val huya = "huya.com"
        internal val imdb = "imdb.com"
        internal val imoim = "imo.im"
        internal val instagram = "instagram.com"
        internal val iqiyi = "iqiyi.com"
        internal val jianshu = "jianshu.com"
        internal val jingdong = "jd.com"
        internal val line = "line.me"
        internal val linkedin = "linkedin.com"
        internal val kuaishou = "kuaishou.com"
        internal val mailru = "mail.ru"
        internal val mangoTv = "mgtv.com"
        internal val meituan = "meituan.com"
        internal val microsoft = "microsoft.com"
        internal val naver = "naver.com"
        internal val netease = "163.com"
        internal val netflix = "netflix.com"
        internal val okru = "ok.ru"
        internal val pinduoduo = "pinduoduo.com"
        internal val pinterest = "pinterest.com"
        internal val pornhub = "pornhub.com"
        internal val quora = "quora.com"
        internal val reddit = "reddit.com"
        internal val samsung = "samsung.com"
        internal val snapchat = "snap.com"
        internal val sina = "sina.com.cn"
        internal val sogou = "sogou.com"
        internal val sohu = "sohu.com"
        internal val stackoverflow = "stackoverflow.com"
        internal val taobao = "taobao.com"
        internal val twitch = "twitch.tv"
        internal val twitter = "twitter.com"
        internal val viber = "viber.com"
        internal val vipShop = "vip.com"
        internal val weibo = "weibo.com"
        internal val whatsapp = "whatsapp.com"
        internal val xiaohongshu = "xiaohongshu.com"
        internal val yandex = "yandex.ru"
        internal val yahoo = "yahoo.com"
        internal val youku = "youku.com"
        internal val youtube = "youtube.com"
        internal val zhihu = "zhihu.com"
        internal val zhongguoYinhang = "boc.cn"
        internal val gongshangYinhang = "icbc.com.cn"
        internal val jiansheYinhang = "ccb.com"
        internal val nongyeYinhang = "abchina.com"
        internal val jiaotongYinhang = "bankcomm.com"
        internal val youzhengYinhang = "psbc.com"
        internal val guangdaYinhang = "cebbank.com"
        internal val minshengYinhang = "cmbc.com.cn"
        internal val huaxiaYinhang = "hxb.com.cn"
        internal val zhongxinYinhang = "citicbank.com"
        internal val zhaoshangYinhang = "cmbchina.com"
        internal val pufaYinhang = "spdb.com.cn"
        internal val zheshangYinhang = "czbank.com"
        internal val guangfaYinhang = "cgbchina.com.cn"
        internal val bohaiYinhang = "cbhb.com.cn"
        internal val hengfengYinhang = "hfbank.com.cn"
        internal val pinganYinhang = "bank.pingan.com"

        private fun makeKnownNameSortKeys(): Map<String, String> {
            val m = HashMap<String, String>()
            m.put("im.qq.com", "qq")
            m.put("weixin.qq.com", "weixin")
            return m
        }

        private fun makeKnownNames(): Map<String, String> {
            val m = HashMap<String, String>()
            m.put("im.qq.com", "im.qq.com")
            m.put("QQ", "im.qq.com")
            m.put("qq.com", "im.qq.com")
            m.put("腾讯", "im.qq.com")
            m.put("tengxun", "im.qq.com")
            m.put("weixin.qq.com", "weixin.qq.com")
            m.put("weixin.com", "weixin.qq.com")
            m.put("wechat.com", "weixin.qq.com")
            m.put("weixin", "weixin.qq.com")
            m.put("wechat", "weixin.qq.com")
            m.put("微信", "weixin.qq.com")
            m.put("web.wechat.com", "weixin.qq.com")
            m.put("wx.qq.com", "weixin.qq.com")
            m.put("alipay.com", "alipay.com")
            m.put("zhifubao.com", "alipay.com")
            m.put("zhifubao", "alipay.com")
            m.put("支付宝", "alipay.com")
            m.put("com.eg.android.AlipayGphone", "alipay.com")
            m.put("alipay", "alipay.com")
            m.put("360.cn", "360.cn")
            m.put("360卫士", "360.cn")
            m.put("360安全卫士", "360.cn")
            m.put("amazon.com", "amazon.com")
            m.put("amazon", "amazon.com")
            m.put("亚马逊", "amazon.com")
            m.put("baidu.com", "baidu.com")
            m.put("baidu", "baidu.com")
            m.put("百度", "baidu.com")
            m.put("bilibili.com", "bilibili.com")
            m.put("bilibili", "bilibili.com")
            m.put("哔哩", "bilibili.com")
            m.put("哔哩哔哩", "bilibili.com")
            m.put("B站", "bilibili.com")
            m.put("tv.danmaku.bili", "bilibili.com")
            m.put("ctrip.com", "ctrip.com")
            m.put("携程", "ctrip.com")
            m.put("携程网", "ctrip.com")
            m.put("ctrip.android.view", "ctrip.com")
            m.put("csdn.net", "csdn.net")
            m.put("net.csdn.csdnplus", "csdn.net")
            m.put("douban.com", "douban.com")
            m.put("豆瓣", "douban.com")
            m.put("com.douban.frodo", "douban.com")
            m.put("douyin.com", "douyin.com")
            m.put("抖音", "douyin.com")
            m.put("抖音视频", "douyin.com")
            m.put("com.ss.android.ugc.aweme", "douyin.com")
            m.put("dropbox.com", "dropbox.com")
            m.put("dropbox", "dropbox.com")
            m.put("ebay.com", "ebay.com")
            m.put("ebay", "ebay.com")
            m.put("facebook.com", "facebook.com")
            m.put("facebook", "facebook.com")
            m.put("脸书", "facebook.com")
            m.put("脸谱网", "facebook.com")
            m.put("com.facebook.katana", "facebook.com")
            m.put("messenger.com", "facebook.com")
            m.put("facebook messenger", "facebook.com")
            m.put("com.facebook.orca", "facebook.com")
            m.put("flipboard.com", "flipboard.com")
            m.put("flipboard", "flipboard.com")
            m.put("红板报", "flipboard.com")
            m.put("github.com", "github.com")
            m.put("github", "github.com")
            m.put("gitlab.com", "gitlab.com")
            m.put("gitlab", "gitlab.com")
            m.put("google.com", "google.com")
            m.put("google", "google.com")
            m.put("谷歌", "google.com")
            m.put("youtube.com", "google.com")
            m.put("youtube", "google.com")
            m.put("油管", "google.com")
            m.put("blogspot.com", "google.com")
            m.put("blogger", "google.com")
            m.put("huya.com", "huya.com")
            m.put("虎牙", "huya.com")
            m.put("虎牙直播", "huya.com")
            m.put("com.duowan.kiwi", "huya.com")
            m.put("huya", "huya.com")
            m.put("hupu.com", "hupu.com")
            m.put("虎扑", "hupu.com")
            m.put("虎扑体育", "hupu.com")
            m.put("com.hupu.games", "hupu.com")
            m.put("hupu", "hupu.com")
            m.put("hupi sport", "hupu.com")
            m.put("imdb.com", "imdb.com")
            m.put("imdb", "imdb.com")
            m.put("imo.im", "imo.im")
            m.put("instagram.com", "instagram.com")
            m.put("instagram", "instagram.com")
            m.put("insta", "instagram.com")
            m.put("com.instagram.android", "instagram.com")
            m.put("Ins", "instagram.com")
            m.put("iqiyi.com", "iqiyi.com")
            m.put("aiqiyi", "iqiyi.com")
            m.put("iqiyi", "iqiyi.com")
            m.put("爱奇艺", "iqiyi.com")
            m.put("com.qiyi.video", "iqiyi.com")
            m.put("jd.com", "jd.com")
            m.put("jingdong", "jd.com")
            m.put("京东", "jd.com")
            m.put("com.jingdong.app.mall", "jd.com")
            m.put("jianshu.com", "jianshu.com")
            m.put("jianshu", "jianshu.com")
            m.put("简书", "jianshu.com")
            m.put("com.jianshu.haruki", "jianshu.com")
            m.put("kuaishou.com", "kuaishou.com")
            m.put("快手", "kuaishou.com")
            m.put("快手视频", "kuaishou.com")
            m.put("com.smile.gifmaker", "kuaishou.com")
            m.put("line.me", "line.me")
            m.put("linkedin.com", "linkedin.com")
            m.put("linkedin", "linkedin.com")
            m.put("领英", "linkedin.com")
            m.put("mail.ru", "mail.ru")
            m.put("meituan.com", "meituan.com")
            m.put("美团", "meituan.com")
            m.put("美团外卖", "meituan.com")
            m.put("com.sankuai.meituan", "meituan.com")
            m.put("com.sankuai.meituan.takeoutnew", "meituan.com")
            m.put("microsoft.com", "microsoft.com")
            m.put("microsoft", "microsoft.com")
            m.put("微软", "microsoft.com")
            m.put("live.com", "microsoft.com")
            m.put("outlook.com", "microsoft.com")
            m.put("mgtv.com", "mgtv.com")
            m.put("mangguo", "mgtv.com")
            m.put("芒果Tv", "mgtv.com")
            m.put("com.hunantv.imgo.activity", "mgtv.com")
            m.put("naver.com", "naver.com")
            m.put("naver", "naver.com")
            m.put("163.com", "163.com")
            m.put("netease", "163.com")
            m.put("wangyi", "163.com")
            m.put("网易", "163.com")
            m.put("music.163.com", "163.com")
            m.put("网易云音乐", "163.com")
            m.put("网易音乐", "163.com")
            m.put("com.netease.cloudmusic", "163.com")
            m.put("wangyiyunyinyue", "163.com")
            m.put("email.163.com", "163.com")
            m.put("网易邮箱", "163.com")
            m.put("wangyiyouxiang", "163.com")
            m.put("com.netease.mobimail", "163.com")
            m.put("netflix.com", "netflix.com")
            m.put("netflix", "netflix.com")
            m.put("ok.ru", "ok.ru")
            m.put("pinduoduo.com", "pinduoduo.com")
            m.put("拼多多", "pinduoduo.com")
            m.put("com.xunmeng.pinduoduo", "pinduoduo.com")
            m.put("pinterest.com", "pinterest.com")
            m.put("pinterest", "pinterest.com")
            m.put("pornhub.com", "pornhub.com")
            m.put("pornhub", "pornhub.com")
            m.put("quora.com", "quora.com")
            m.put("quora", "quora.com")
            m.put("reddit.com", "reddit.com")
            m.put("reddit", "reddit.com")
            m.put("samsung.com", "samsung.com")
            m.put("samsung", "samsung.com")
            m.put("snap.com", "snap.com")
            m.put("snapchat", "snap.com")
            m.put("sina.com.cn", "sina.com.cn")
            m.put("sina", "sina.com.cn")
            m.put("sina.com", "sina.com.cn")
            m.put("sina.cn", "sina.com.cn")
            m.put("新浪", "sina.com.cn")
            m.put("sogou.com", "sogou.com")
            m.put("sogou", "sogou.com")
            m.put("搜狗", "sogou.com")
            m.put("sohu.com", "sohu.com")
            m.put("sohu", "sohu.com")
            m.put("搜狐", "sohu.com")
            m.put("stackoverflow.com", "stackoverflow.com")
            m.put("stackoverflow", "stackoverflow.com")
            m.put("taobao.com", "taobao.com")
            m.put("taobao", "taobao.com")
            m.put("淘宝", "taobao.com")
            m.put("淘宝网", "taobao.com")
            m.put("twitch.tv", "twitch.tv")
            m.put("twitch", "twitch.tv")
            m.put("twitter.com", "twitter.com")
            m.put("twitter", "twitter.com")
            m.put("推特", "twitter.com")
            m.put("com.twitter.android", "twitter.com")
            m.put("viber.com", "viber.com")
            m.put("viber", "viber.com")
            m.put("vip.com", "vip.com")
            m.put("m.vip.com", "vip.com")
            m.put("唯品会", "vip.com")
            m.put("vipshop", "vip.com")
            m.put("weipinhui", "vip.com")
            m.put("com.achievo.vipshop", "vip.com")
            m.put("weibo.com", "weibo.com")
            m.put("weibo", "weibo.com")
            m.put("微博", "weibo.com")
            m.put("新浪微博", "weibo.com")
            m.put("weibo.cn", "weibo.com")
            m.put("passport.weibo.cn", "weibo.com")
            m.put("passport.weibo.com", "weibo.com")
            m.put("com.sina.weibo", "weibo.com")
            m.put("whatsapp.com", "whatsapp.com")
            m.put("whatsapp", "whatsapp.com")
            m.put("小红书", "xiaohongshu.com")
            m.put("xiaohongshu.com", "xiaohongshu.com")
            m.put("com.xingin.xhs", "xiaohongshu.com")
            m.put("xiaohongshu", "xiaohongshu.com")
            m.put("yandex.ru", "yandex.ru")
            m.put("yandex", "yandex.ru")
            m.put("yahoo.com", "yahoo.com")
            m.put("yahoo", "yahoo.com")
            m.put("雅虎", "yahoo.com")
            m.put("youku.com", "youku.com")
            m.put("m.youku.com", "youku.com")
            m.put("优酷", "youku.com")
            m.put("优酷网", "youku.com")
            m.put("com.youku.phone", "youku.com")
            m.put("youku", "youku.com")
            m.put("zhihu.com", "zhihu.com")
            m.put("zhihu", "zhihu.com")
            m.put("知乎", "zhihu.com")
            m.put("com.zhihu.android", "zhihu.com")
            m.put("boc.cn", "boc.cn")
            m.put("zhongguoyinhang", "boc.cn")
            m.put("zhonghang", "boc.cn")
            m.put("中国银行", "boc.cn")
            m.put("中行", "boc.cn")
            m.put("icbc.com.cn", "icbc.com.cn")
            m.put("icbc", "icbc.com.cn")
            m.put("gongshangyinhang", "icbc.com.cn")
            m.put("gonghang", "icbc.com.cn")
            m.put("中国工商银行", "icbc.com.cn")
            m.put("工商银行", "icbc.com.cn")
            m.put("工行", "icbc.com.cn")
            m.put("ccb.com", "ccb.com")
            m.put("jiansheyinhang", "ccb.com")
            m.put("jianhang", "ccb.com")
            m.put("中国建设银行", "ccb.com")
            m.put("建设银行", "ccb.com")
            m.put("建行", "ccb.com")
            m.put("abchina.com", "abchina.com")
            m.put("abchina", "abchina.com")
            m.put("nongyeyinhang", "abchina.com")
            m.put("nonghang", "abchina.com")
            m.put("中国农业银行", "abchina.com")
            m.put("农业银行", "abchina.com")
            m.put("农行", "abchina.com")
            m.put("bankcomm.com", "bankcomm.com")
            m.put("bankcomm", "bankcomm.com")
            m.put("jiaotongyinhang", "bankcomm.com")
            m.put("jiaohang", "bankcomm.com")
            m.put("中国交通银行", "bankcomm.com")
            m.put("交通银行", "bankcomm.com")
            m.put("交行", "bankcomm.com")
            m.put("psbc.com", "psbc.com")
            m.put("youzhengyinhang", "psbc.com")
            m.put("youzhengchuxu", "psbc.com")
            m.put("邮政银行", "psbc.com")
            m.put("邮政储蓄银行", "psbc.com")
            m.put("中国邮政储蓄银行", "psbc.com")
            m.put("cebbank.com", "cebbank.com")
            m.put("cebbank", "cebbank.com")
            m.put("guangdayinhang", "cebbank.com")
            m.put("guangda", "cebbank.com")
            m.put("中国光大银行", "cebbank.com")
            m.put("光大银行", "cebbank.com")
            m.put("光大", "cebbank.com")
            m.put("cmbc.com.cn", "cmbc.com.cn")
            m.put("mingshengyinhang", "cmbc.com.cn")
            m.put("minsheng", "cmbc.com.cn")
            m.put("民生银行", "cmbc.com.cn")
            m.put("民生", "cmbc.com.cn")
            m.put("hxb.com.cn", "hxb.com.cn")
            m.put("huaxiayinhang", "hxb.com.cn")
            m.put("华夏银行", "hxb.com.cn")
            m.put("citicbank.com", "citicbank.com")
            m.put("citicbank", "citicbank.com")
            m.put("zhongxinyinhang", "citicbank.com")
            m.put("中信银行", "citicbank.com")
            m.put("cmbchina.com", "cmbchina.com")
            m.put("招行", "cmbchina.com")
            m.put("cmb", "cmbchina.com")
            m.put("招商银行", "cmbchina.com")
            m.put("cmb.pb", "cmbchina.com")
            m.put("zhaoshangyinhang", "cmbchina.com")
            m.put("spdb.com.cn", "spdb.com.cn")
            m.put("pufayinhang", "spdb.com.cn")
            m.put("pufa", "spdb.com.cn")
            m.put("上海浦东发展银行", "spdb.com.cn")
            m.put("浦发银行", "spdb.com.cn")
            m.put("浦发", "spdb.com.cn")
            m.put("czbank.com", "czbank.com")
            m.put("czbank", "czbank.com")
            m.put("zheshangyinhang", "czbank.com")
            m.put("浙商银行", "czbank.com")
            m.put("cgbchina.com.cn", "cgbchina.com.cn")
            m.put("guangfayinhang", "cgbchina.com.cn")
            m.put("cgbchina", "cgbchina.com.cn")
            m.put("广发银行", "cgbchina.com.cn")
            m.put("广东发展银行", "cgbchina.com.cn")
            m.put("cbhb.com.cn", "cbhb.com.cn")
            m.put("bohaiyinhang", "cbhb.com.cn")
            m.put("渤海银行", "cbhb.com.cn")
            m.put("hfbank.com.cn", "hfbank.com.cn")
            m.put("hefengyinhang", "hfbank.com.cn")
            m.put("恒丰银行", "hfbank.com.cn")
            m.put("bank.pingan.com", "bank.pingan.com")
            m.put("pinganyinhang", "bank.pingan.com")
            m.put("平安银行", "bank.pingan.com")
            m.put("中国平安银行", "bank.pingan.com")
            return m
        }
    }
}


//域名处理
class DomainUtil {
    companion object {
        val topLevelDomains = makeTopLevelDomains()
        val countryTopLevelDomains = makeCountryTopLevelDomains()

        fun getDomainName(uri: String): String {
            if (uri.indexOf(".") == -1) {
                Log.d("uri", uri)
                return uri
            } else {
                var normalizedUri = uri
                var schemePosition = uri.indexOf("://")
                if (schemePosition != -1) {
                    schemePosition += 3
                    if (uri.length <= schemePosition) {
                        return uri
                    }

                    if (uri[schemePosition] == '/') {
                        ++schemePosition
                    }

                    normalizedUri = uri.substring(schemePosition)
                }

                val firstSlashPosition = normalizedUri.indexOf("/")
                if (firstSlashPosition != -1) {
                    normalizedUri = normalizedUri.substring(0, firstSlashPosition)
                }

                val lastDotPosition = normalizedUri.lastIndexOf(".")
                if (lastDotPosition == -1) {
                    return uri
                } else {
                    val topLevelDomain = normalizedUri.substring(lastDotPosition)
                    if (!countryTopLevelDomains.contains(topLevelDomain)) {
                        return if (topLevelDomains.contains(topLevelDomain)) extractDomain(
                            normalizedUri,
                            1
                        ) else uri
                    } else {
                        val previousDotPosition =
                            normalizedUri.lastIndexOf(".", lastDotPosition - 1)
                        if (previousDotPosition == -1) {
                            return normalizedUri
                        } else {
                            val secondLevelDomain =
                                normalizedUri.substring(previousDotPosition, lastDotPosition)
                            return if (topLevelDomains.contains(secondLevelDomain)) extractDomain(
                                normalizedUri,
                                2
                            ) else extractDomain(normalizedUri, 1)
                        }
                    }
                }
            }
        }

        private fun extractDomain(domain: String, level: Int): String {
            var index = domain.lastIndexOf(".")

            for (x in 0 until level) {
                val i = domain.lastIndexOf(".", index - 1)
                if (i == -1) {
                    return domain
                }

                index = i
            }

            return domain.substring(index + 1)
        }

        private fun makeTopLevelDomains(): Set<String> {
            val s = HashSet<String>()
            s.add(".com")
            s.add(".org")
            s.add(".net")
            s.add(".int")
            s.add(".edu")
            s.add(".gov")
            s.add(".mil")
            s.add(".arpa")
            return s
        }

        private fun makeCountryTopLevelDomains(): Set<String> {
            val s = HashSet<String>()
            s.add(".ac")
            s.add(".ad")
            s.add(".ae")
            s.add(".af")
            s.add(".ag")
            s.add(".ai")
            s.add(".al")
            s.add(".am")
            s.add(".an")
            s.add(".ao")
            s.add(".aq")
            s.add(".ar")
            s.add(".as")
            s.add(".at")
            s.add(".au")
            s.add(".aw")
            s.add(".ax")
            s.add(".az")
            s.add(".ba")
            s.add(".bb")
            s.add(".bd")
            s.add(".be")
            s.add(".bf")
            s.add(".bg")
            s.add(".bh")
            s.add(".bi")
            s.add(".bj")
            s.add(".bl")
            s.add(".bm")
            s.add(".bn")
            s.add(".bo")
            s.add(".bq")
            s.add(".br")
            s.add(".bs")
            s.add(".bt")
            s.add(".bv")
            s.add(".bw")
            s.add(".by")
            s.add(".bz")
            s.add(".ca")
            s.add(".cc")
            s.add(".cd")
            s.add(".cf")
            s.add(".cg")
            s.add(".ch")
            s.add(".ci")
            s.add(".ck")
            s.add(".cl")
            s.add(".cm")
            s.add(".cn")
            s.add(".co")
            s.add(".cr")
            s.add(".cu")
            s.add(".cv")
            s.add(".cw")
            s.add(".cx")
            s.add(".cy")
            s.add(".cz")
            s.add(".de")
            s.add(".dj")
            s.add(".dk")
            s.add(".dm")
            s.add(".do")
            s.add(".dz")
            s.add(".ec")
            s.add(".ee")
            s.add(".eg")
            s.add(".eh")
            s.add(".er")
            s.add(".es")
            s.add(".et")
            s.add(".eu")
            s.add(".fi")
            s.add(".fj")
            s.add(".fk")
            s.add(".fm")
            s.add(".fo")
            s.add(".fr")
            s.add(".ga")
            s.add(".gb")
            s.add(".gd")
            s.add(".ge")
            s.add(".gf")
            s.add(".gg")
            s.add(".gh")
            s.add(".gi")
            s.add(".gl")
            s.add(".gm")
            s.add(".gn")
            s.add(".gp")
            s.add(".gq")
            s.add(".gr")
            s.add(".gs")
            s.add(".gt")
            s.add(".gu")
            s.add(".gw")
            s.add(".gy")
            s.add(".hk")
            s.add(".hm")
            s.add(".hn")
            s.add(".hr")
            s.add(".ht")
            s.add(".hu")
            s.add(".id")
            s.add(".ie")
            s.add(".il")
            s.add(".im")
            s.add(".in")
            s.add(".io")
            s.add(".iq")
            s.add(".ir")
            s.add(".is")
            s.add(".it")
            s.add(".je")
            s.add(".jm")
            s.add(".jo")
            s.add(".jp")
            s.add(".ke")
            s.add(".kg")
            s.add(".kh")
            s.add(".ki")
            s.add(".km")
            s.add(".kn")
            s.add(".kp")
            s.add(".kr")
            s.add(".kw")
            s.add(".ky")
            s.add(".kz")
            s.add(".la")
            s.add(".lb")
            s.add(".lc")
            s.add(".li")
            s.add(".lk")
            s.add(".lr")
            s.add(".ls")
            s.add(".lt")
            s.add(".lu")
            s.add(".lv")
            s.add(".ly")
            s.add(".ma")
            s.add(".mc")
            s.add(".md")
            s.add(".me")
            s.add(".mf")
            s.add(".mg")
            s.add(".mh")
            s.add(".mk")
            s.add(".ml")
            s.add(".mm")
            s.add(".mn")
            s.add(".mo")
            s.add(".mp")
            s.add(".mq")
            s.add(".mr")
            s.add(".ms")
            s.add(".mt")
            s.add(".mu")
            s.add(".mv")
            s.add(".mw")
            s.add(".mx")
            s.add(".my")
            s.add(".mz")
            s.add(".na")
            s.add(".nc")
            s.add(".ne")
            s.add(".nf")
            s.add(".ng")
            s.add(".ni")
            s.add(".nl")
            s.add(".no")
            s.add(".np")
            s.add(".nr")
            s.add(".nu")
            s.add(".nz")
            s.add(".om")
            s.add(".pa")
            s.add(".pe")
            s.add(".pf")
            s.add(".pg")
            s.add(".ph")
            s.add(".pk")
            s.add(".pl")
            s.add(".pm")
            s.add(".pn")
            s.add(".pr")
            s.add(".ps")
            s.add(".pt")
            s.add(".pw")
            s.add(".py")
            s.add(".qa")
            s.add(".re")
            s.add(".ro")
            s.add(".rs")
            s.add(".ru")
            s.add(".rw")
            s.add(".sa")
            s.add(".sb")
            s.add(".sc")
            s.add(".sd")
            s.add(".se")
            s.add(".sg")
            s.add(".sh")
            s.add(".si")
            s.add(".sj")
            s.add(".sk")
            s.add(".sl")
            s.add(".sm")
            s.add(".sn")
            s.add(".so")
            s.add(".sr")
            s.add(".ss")
            s.add(".st")
            s.add(".su")
            s.add(".sv")
            s.add(".sx")
            s.add(".sy")
            s.add(".sz")
            s.add(".tc")
            s.add(".td")
            s.add(".tf")
            s.add(".tg")
            s.add(".th")
            s.add(".tj")
            s.add(".tk")
            s.add(".tl")
            s.add(".tm")
            s.add(".tn")
            s.add(".to")
            s.add(".tp")
            s.add(".tr")
            s.add(".tt")
            s.add(".tv")
            s.add(".tw")
            s.add(".tz")
            s.add(".ua")
            s.add(".ug")
            s.add(".uk")
            s.add(".um")
            s.add(".us")
            s.add(".uy")
            s.add(".uz")
            s.add(".va")
            s.add(".vc")
            s.add(".ve")
            s.add(".vg")
            s.add(".vi")
            s.add(".vn")
            s.add(".vu")
            s.add(".wf")
            s.add(".ws")
            s.add(".ye")
            s.add(".yt")
            s.add(".za")
            s.add(".zm")
            s.add(".zw")
            return s
        }
    }
}


class AccountIconUtil {
    companion object {
        private var path: String? = null

        fun getResourceIcon(name: String): Drawable? {
            if (name.isEmpty()) {
                return null
            } else {
                val resources = BaseApplication.context.resources
                var resourceName = name.replace('.', '_')
                if (Character.isDigit(resourceName[0])) {
                    resourceName = "_$resourceName"
                }

                val resourceId = resources.getIdentifier(
                    resourceName,
                    "drawable",
                    BaseApplication.context.packageName
                )
                return if (resourceId == 0) null else resources.getDrawable(
                    resourceId,
                    null as Resources.Theme?
                )
            }
        }

        fun backgroundcolor(item: BaseMainData): Int {
            val icon = getResourceIcon(item.getUnifiedName())
            if (icon != null) {
                return 0
            } else {
                val apkIcon = getApkIcons(item.getAppName())
                if (apkIcon != null) {
                    return 0
                } else {
                    val appNameUserName = item.getUnifiedName() + item.getUnifiedName()
                    val hash = Math.abs(appNameUserName.hashCode())
                    val colors = arrayOf(
                        "#669999",
                        "#0d5ad8",
                        "#660066",
                        "#ff5c3f",
                        "#079637",
                        "#8c8410",
                        "#3cbc8d",
                        "#ff9400",
                        "#380d0d",
                        "#ff006a"
                    )
                    return Color.parseColor(colors[hash % colors.size])
                }
            }
        }

        fun generateBitmap(iconSize: Int, item: BaseMainData): Bitmap {
            val appNameUserName = item.getUnifiedName() + item.getUnifiedName()
            val hash = Math.abs(appNameUserName.hashCode())
            val colors = arrayOf(
                "#669999",
                "#0d5ad8",
                "#660066",
                "#ff5c3f",
                "#079637",
                "#8c8410",
                "#3cbc8d",
                "#ff9400",
                "#380d0d",
                "#ff006a"
            )
            val backgroundColor = Color.parseColor(colors[hash % colors.size])
            val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val rect = RectF(0.0f, 0.0f, canvas.width.toFloat(), canvas.height.toFloat())
            val paint = Paint(257)
            val background = Paint(257)
            background.color = backgroundColor
            canvas.drawRect(rect, background)
            paint.color = -1
            paint.strokeWidth = 3.0f
            paint.textSize = iconSize.toFloat() * 0.6f
            paint.textAlign = Paint.Align.CENTER
            val fontMetrics = paint.fontMetricsInt
            val baseline =
                (rect.bottom + rect.top - fontMetrics.bottom.toFloat() - fontMetrics.top.toFloat()) / 2.0f
            if (item.getUnifiedName().isNotEmpty()) {
                canvas.drawText(
                    item.getUnifiedName().substring(0, 1).toUpperCase(),
                    rect.centerX(),
                    baseline,
                    paint
                )
            }

            return bitmap
        }

        fun generateBitmap(item: BaseMainData, context: Context): Drawable {
            val icon = getResourceIcon(item.getUnifiedName())
            if (icon != null) {
                return icon
            } else {
                val apkIcon = getApkIcons(item.getAppName())
                if (apkIcon != null) {
                    return apkIcon
                } else {
                    val bitmap = generateBitmap(180, item)
                    return BitmapDrawable(context.resources, bitmap)
                }
            }
        }

        fun saveApkIcons(packageName: String): Boolean {
            val packageManager = BaseApplication.context.getPackageManager()

            try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val drawable = packageManager.getApplicationIcon(applicationInfo)
                saveIcon(apkIconsDirectory, packageName, drawable)
                return true
            } catch (var4: PackageManager.NameNotFoundException) {
                return false
            }

        }

        private fun getApkIcons(packageName: String): Drawable? {
            val resourceName = packageName.replace('.', '_') + ".png"
            val filePath = File(path!! + resourceName)
            return if (filePath.exists()) {
                Drawable.createFromPath(filePath.path)
            } else {
                null
            }
        }

        private fun saveIcon(directory: File, packageName: String, drawable: Drawable) {
            val resourceName = packageName.replace('.', '_') + ".png"
            val filePath = File(directory, resourceName)
            if (!filePath.exists()) {
                val bytes = ByteArrayOutputStream()
                val bitmap = getBitmapFromDrawable(drawable)
                bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytes)

                try {
                    val fos = FileOutputStream(filePath)
                    fos.write(bytes.toByteArray())
                    fos.close()
                } catch (var8: IOException) {
                    Log.e("ContentValues", "Fail to save apk Icons of $packageName")
                    filePath.delete()
                }

            }

        }

        private val apkIconsDirectory: File
            get() {
                val directory = File(path!!)
                if (!directory.exists() && !directory.mkdirs()) {
                    Log.e("ContentValues", "Failed to prepare apk icons directory")
                }

                return directory
            }

        @NonNull
        private fun getBitmapFromDrawable(@NonNull drawable: Drawable): Bitmap {
            val bmp = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bmp
        }

        init {
            path = BaseApplication.context.getApplicationInfo().dataDir + "/apk_icons/"
        }
    }
}