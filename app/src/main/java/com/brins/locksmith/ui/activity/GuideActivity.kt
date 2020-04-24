package com.brins.locksmith.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.brins.locksmith.R
import com.brins.locksmith.ui.dialog.LoadingDialogFragment
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.WeakHandler
import com.brins.locksmith.utils.getStatusBarHeight
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import kotlinx.android.synthetic.main.activity_guide.*
import java.lang.Exception

class GuideActivity : BaseActivity(), View.OnClickListener, WeakHandler.IHandler {

    private val mHandler = WeakHandler(this)

    fun onClickView(v: View?) {
        if (mPassportViewModel.loadPassport()) {
            MainActivity.startThis(this@GuideActivity)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_guide
    }

    private var mLoadingDialogFragment: LoadingDialogFragment? = null

    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProvider(this@GuideActivity, InjectorUtil.getPassportModelFactory())
            .get(PassportViewModel::class.java)
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
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0)
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

    override fun handleMsg(msg: Message) {
        if (!mPassportViewModel.loadPassport()) {
            //若无密钥信息，创建密钥
            createPassPort()
        }
    }
}
