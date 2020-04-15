package com.brins.locksmith.ui.activity

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.os.Process
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.setTextDark
import com.brins.locksmith.utils.setTranslucent
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

abstract class BaseActivity : AppCompatActivity() {

    protected open val TAG = this::class.java.simpleName
    private val mMainThread = Looper.getMainLooper().thread
    private var mScheduledExecutorService: ScheduledExecutorService? = null


    private val mTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
            if (!isInForeground()) {
                ActivityCollector.finishall()
            }
            stopTimer()
        }
    }

    protected val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this@BaseActivity, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }
    protected val mSaveCardViewModel: SaveCardViewModel by lazy {
        ViewModelProvider(this@BaseActivity, InjectorUtil.getCardFactory()).get(
            SaveCardViewModel::class.java
        )
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

    private fun isInForeground(): Boolean {
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = manager.runningAppProcesses
        val processInfo = info[0]
        return applicationInfo.packageName == processInfo.processName && processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
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

    protected open fun onCreateBeforeBinding(@Nullable savedInstanceState: Bundle?) {}


    protected abstract fun getLayoutResId(): Int

    protected open fun onCreateAfterBinding(@Nullable savedInstanceState: Bundle?) {
    }


    private fun setStatusBarTranslucent() {
        setTranslucent(this)
        setTextDark(this.window, true)
    }


}
