package com.brins.locksmith.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModelProvider
import com.brins.locksmith.utils.*
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.jaeger.library.StatusBarUtil

abstract class BaseActivity : AppCompatActivity() {

    protected open val TAG = this::class.java.simpleName
    private val mMainThread = Looper.getMainLooper().thread

    protected val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this@BaseActivity, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateBeforeBinding(savedInstanceState)
        val resId = getLayoutResId()
        if (resId != 0) {
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

    protected open fun onCreateAfterBinding(@Nullable savedInstanceState: Bundle?) {
    }


    private fun setStatusBarTranslucent() {
        setTranslucent(this)
        setTextDark(this.window, true)
    }


}
