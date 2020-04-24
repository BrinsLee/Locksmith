package com.brins.locksmith.ui.dialog

import android.app.KeyguardManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.hardware.fingerprint.FingerprintManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.R
import com.brins.locksmith.ui.activity.AuthRequestActivity
import com.brins.locksmith.utils.*
import kotlinx.android.synthetic.main.dialog_finger_authentication.*


class FingerAuthDialogFragment : BaseDialogFragment(),
    FingerprintUiHelper.Callback {


    private var listener: View.OnClickListener? = null

    private val mKeyguardManager: KeyguardManager by lazy {
        activity?.getSystemService(
            AppCompatActivity.KEYGUARD_SERVICE
        ) as KeyguardManager
    }
    private val mFingerprintManager by lazy {
        activity!!.getSystemService(FingerprintManager::class.java)
    }
    private lateinit var mFingerprintUiHelper: FingerprintUiHelper


    enum class Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }

    var mStage = Stage.FINGERPRINT
    private val mCryptoObject: FingerprintManager.CryptoObject? = null


/*    private val mBiometricPrompt by lazy {
        activity?.getSystemService(BiometricPrompt::class.java)

    }*/

    override fun getLayoutResId(): Int {
        return R.layout.dialog_finger_authentication
    }

    companion object {
        private val ERROR_TIMEOUT_MILLIS: Long = 1300
        private val SUCCESS_DELAY_MILLIS: Long = 100
        private val ERROR_MUCH_TIME: Long = 300000

        val AUTH_REQUEST_CODE = 0x32

        fun showSelf(
            manager: FragmentManager,
            stage: Stage,
            listener: View.OnClickListener
        ): FingerAuthDialogFragment {
            val dialog = FingerAuthDialogFragment()
            dialog.mStage = stage
            dialog.listener = listener
            return dialog.show(manager) as FingerAuthDialogFragment
        }
    }

    override fun onCreateViewAfterBinding(view: View) {
        super.onCreateViewAfterBinding(view)
        cancel.setOnClickListener(listener)
        usePassword.setOnClickListener(listener)
        mFingerprintUiHelper = FingerprintUiHelper(mFingerprintManager, this)
        createBlurBackground()
        if (!isFingerprintAuthAvailable()) {
            //指纹不可用
            tryPassword()
        }
    }

    private fun createBlurBackground() {
        blur_view.setBlurredView(activity!!.window.decorView)
        blur_view.invalidate()

        /*val rad = 8
        val bitmap : Bitmap
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = context!!.getDrawable(R.drawable.unnamed)
            bitmap = Bitmap.createBitmap(getScreenWeight(),
                getScreenWeight(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            vectorDrawable.draw(canvas)
        } else {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.unnamed)
        }

        var bitmapBlur = Bitmap.createBitmap(
            getScreenWeight() / rad,
            getScreenHeight() / rad,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmapBlur)
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        bitmapBlur = doBlur(bitmapBlur!!, 10, true)
        contain_root.background = BitmapDrawable(resources, bitmapBlur)*/

    }


    override fun onResume() {
        super.onResume()
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        mFingerprintUiHelper.stopListening()
    }

    private fun tryPassword() {
        mStage = Stage.PASSWORD
        updateStage()
    }


    private fun updateStage() {
        when (mStage) {
            Stage.FINGERPRINT -> {
                cancel.setText(R.string.cancel)
                usePassword.visibility = View.GONE
                divider2.visibility = View.GONE
            }
            Stage.NEW_FINGERPRINT_ENROLLED,
                // Intentional fall through
            Stage.PASSWORD -> {
                cancel.setText(R.string.cancel)
                usePassword.visibility = View.VISIBLE
                divider2.visibility = View.VISIBLE
            }
        }
    }


    override fun isCanceledOnTouchOutside(): Boolean {
        return true
    }

    override fun isInterceptKeyCodeBack(): Boolean {
        return true
    }

    override fun getDialogAnimResId(): Int {
        return R.style.CustomCenterDialogAnim
    }

    override fun getDialogWidth(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun getDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }


    private fun isFingerprintAuthAvailable(): Boolean {
        return mFingerprintManager.isHardwareDetected && mFingerprintManager.hasEnrolledFingerprints()
    }

    override fun onAuthenticated() {
        cancel.isClickable = false
        usePassword.isEnabled = false
        if (activity != null) {
            if (activity is AuthRequestActivity) {
                (activity!! as AuthRequestActivity).authencitatedCallback()
            }
        }
        dismissAllowingStateLoss()
    }

    override fun onError(errorCode: Int, message: String) {
        tryPassword()
        showError(message, errorCode)
    }

    override fun onHelp(message: String) {
        showError(message, 0)
    }
/*
    @RequiresApi(Build.VERSION_CODES.P)
    class FingerprintUiHelperApiP : BiometricPrompt.AuthenticationCallback() {

    }*/

    private fun showError(error: String, errorMsgId: Int) {

        usePassword.visibility = View.VISIBLE
        divider2.visibility = View.VISIBLE
        ivFingerprint.setImageResource(R.drawable.ic_fingerprint_error)
        title.text = error
        title.setTextColor(
            ContextCompat.getColor(
                context!!,
                R.color.warning_color
            )
        )
        title.removeCallbacks(mResetErrorTextRunnable)
        //失败过多，30s后重试
        if (errorMsgId == 7 || errorMsgId == 9) {
            title.postDelayed(mResetErrorTextRunnable, ERROR_MUCH_TIME)
        } else
            title.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS)
    }

    private var mResetErrorTextRunnable = Runnable {
        if (title != null) {
            title.setTextColor(Color.BLACK)
            title.text = BaseApplication.context.getString(R.string.finger_print)
            ivFingerprint.setImageResource(R.drawable.ic_fingerprint_black_24dp)
        }

    }


}