package com.brins.locksmith.ui.activity

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Process
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.brins.locksmith.R
import com.brins.locksmith.ui.dialog.FingerAuthDialogFragment
import com.brins.locksmith.ui.dialog.LoadingDialogFragment
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.setTextDark
import com.brins.locksmith.utils.setTranslucent
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    protected open val TAG = this::class.java.simpleName
    private val mMainThread = Looper.getMainLooper().thread
    private var mScheduledExecutorService: ScheduledExecutorService? = null
    protected var needAuthRequest = false
    protected var mFingerDialog: FingerAuthDialogFragment? = null
    protected val mKeyguardManager: KeyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    protected var mLoadingDialogFragment: LoadingDialogFragment? = null


    private val mTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
            if (!isInForeground()) {
//                ActivityCollector.finishall()
                needAuthRequest = true
            }
            stopTimer()
        }
    }

    protected val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProvider(this, InjectorUtil.getPassportModelFactory()).get(
            PassportViewModel::class.java
        )
    }

    protected val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }
    protected val mSaveCardViewModel: SaveCardViewModel by lazy {
        ViewModelProvider(this, InjectorUtil.getCardFactory()).get(
            SaveCardViewModel::class.java
        )
    }

    protected fun showLoading(content: String? = null) {
        if (mLoadingDialogFragment == null) {
            mLoadingDialogFragment = LoadingDialogFragment.showSelf(supportFragmentManager, content)
        }
    }

    protected fun hideLoading() {
        mLoadingDialogFragment?.dismiss()
        mLoadingDialogFragment = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)
        onCreateBeforeBinding(savedInstanceState)
        val resId = getLayoutResId()
        if (resId != 0) {
            setContentView(resId)
        }
        onCreateAfterBinding(savedInstanceState)
        setStatusBarTranslucent()

    }

    protected open fun startTimer() {
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor()
            cancelTimeTask(mTimerTask)
            mScheduledExecutorService!!.scheduleAtFixedRate(
                mTimerTask,
                5,
                5,
                TimeUnit.SECONDS
            )
        }
    }

    protected open fun cancelTimeTask(task: TimerTask?) {
        if (null != task) {
            try {
                task.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected open fun stopTimer() {
        cancelTimeTask(mTimerTask)
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService!!.shutdown()
            mScheduledExecutorService = null
        }
    }

    protected fun launchFingerAuth() {
        mFingerDialog = FingerAuthDialogFragment.showSelf(
            supportFragmentManager,
            FingerAuthDialogFragment.Stage.FINGERPRINT
            , this
        )
    }

    private fun isInForeground(): Boolean {
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = manager.runningAppProcesses
        val processInfo = info[0]
        return applicationInfo.packageName == processInfo.processName && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    override fun onStart() {
        super.onStart()
        if (needAuthRequest) {
            if (mKeyguardManager.isKeyguardSecure) {
                launchFingerAuth()
            } else {
                MissPasswordDialogFragment.showSelf(supportFragmentManager)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isInForeground())
            startTimer()
    }

    protected open fun killApp() {
        ActivityCollector.finishall()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel -> {
                mFingerDialog?.dismissAllowingStateLoss()
                ActivityCollector.finishall()
            }

            R.id.usePassword -> {
                onClickUsePassword()
            }
        }
    }

    protected open fun authencitatedCallback() {
        mFingerDialog?.dismiss()
    }


    protected open fun onClickUsePassword() {
        val intent: Intent? = mKeyguardManager.createConfirmDeviceCredentialIntent(
            "Authentication required",
            "PASSWORD"
        )
        if (intent != null) {
            startActivityForResult(intent, FingerAuthDialogFragment.AUTH_REQUEST_CODE)
        }
    }

    protected open fun onCreateBeforeBinding(@Nullable savedInstanceState: Bundle?) {}


    protected abstract fun getLayoutResId(): Int

    protected open fun onCreateAfterBinding(@Nullable savedInstanceState: Bundle?) {
    }


    private fun setStatusBarTranslucent() {
        setTranslucent(this)
        setTextDark(this.window, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FingerAuthDialogFragment.AUTH_REQUEST_CODE && resultCode == RESULT_OK) {
            authencitatedCallback()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
