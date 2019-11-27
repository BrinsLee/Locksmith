package com.brins.locksmith.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.brins.locksmith.R
import com.brins.locksmith.ui.dialog.LoadingDialogFragment
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.WeakHandler
import com.brins.locksmith.viewmodel.PassportViewModel
import kotlinx.android.synthetic.main.activity_guide.*
import java.lang.Exception

class GuideActivity : BaseActivity(), View.OnClickListener, WeakHandler.IHandler {

    private val mHandler = WeakHandler(this)

    override fun onClick(v: View?) {
        if (mPassportViewModel.loadPassport()) {
            MainActivity.startThis(this@GuideActivity)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_guide
    }

    private var mLoadingDialogFragment: LoadingDialogFragment? = null

    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProviders.of(this@GuideActivity).get(PassportViewModel::class.java)
    }

    companion object {
        fun startThis(activity: AppCompatActivity) {
            val intent = Intent(activity, GuideActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        btnStart.setOnClickListener(this)
        mLoadingDialogFragment = LoadingDialogFragment.showSelf(supportFragmentManager)
        val message = mHandler.obtainMessage()
        mHandler.sendMessageDelayed(message, 1500)

    }


    /***创建密钥*/
    private fun createPassPort() {
        try {
            if (!mPassportViewModel.createPassport()) {
                mLoadingDialogFragment?.dismissAllowingStateLoss()
                MissPasswordDialogFragment.showSelf(
                    supportFragmentManager,
                    getString(R.string.hardward_error)
                )
            } else {
                createPassportDone()
            }
        } catch (e: Exception) {
            MissPasswordDialogFragment.showSelf(
                supportFragmentManager,
                getString(R.string.hardward_error)
            )
        }
    }

    private fun createPassportDone() {
        mLoadingDialogFragment?.dismiss()
        done?.visibility = View.VISIBLE
        btnStart.isClickable = true
        btnStart.isEnabled = true
    }

    override fun getOffsetView(): View? {
        return toolbar
    }
    override fun handleMsg(msg: Message) {
        if (!mPassportViewModel.loadPassport()) {
            createPassPort()
        }
    }
}
