package com.brins.locksmith.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.annotation.Nullable
import com.brins.locksmith.utils.registerEventBus
import com.brins.locksmith.utils.unregisterEventBus
import com.jaeger.library.StatusBarUtil

abstract class BaseActivity : AppCompatActivity() {

    protected open val TAG = this::class.java.simpleName
    private val mMainThread = Looper.getMainLooper().thread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateBeforeBinding(savedInstanceState)
        val resId = getLayoutResId()
        if (resId != 0){
            setContentView(resId)
        }
        onCreateAfterBinding(savedInstanceState)
        setStatusBarTranslucent()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    protected open fun onCreateBeforeBinding(@Nullable savedInstanceState: Bundle?) {}


    protected abstract fun getLayoutResId(): Int

    protected open fun onCreateAfterBinding(@Nullable savedInstanceState: Bundle?) {}


    protected open fun isStatusBarTranslucent(): Boolean {
        return true
    }

    protected open fun getOffsetView(): View? {
        return null
    }

    private fun setStatusBarTranslucent() {
        if (!isStatusBarTranslucent()) {
            return
        }
        StatusBarUtil.setTranslucentForImageView(this, 1, getOffsetView())
    }


}
