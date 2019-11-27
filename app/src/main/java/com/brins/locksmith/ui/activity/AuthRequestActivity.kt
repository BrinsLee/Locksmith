package com.brins.locksmith.ui.activity

import android.app.KeyguardManager
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.brins.locksmith.R
import com.brins.locksmith.ui.dialog.FingerAuthDialogFragment
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.viewmodel.PassportViewModel
import kotlinx.android.synthetic.main.activity_auth_request.*

class AuthRequestActivity : BaseActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        launchFingerAuth()
    }

    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProviders.of(this@AuthRequestActivity).get(PassportViewModel::class.java)
    }
    private var mNoSecuredialog: MissPasswordDialogFragment? = null
    private val mKeyguardManager: KeyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    private lateinit var mAuthenticalDialogFragment: FingerAuthDialogFragment
    override fun getLayoutResId(): Int {
        return R.layout.activity_auth_request
    }

    override fun onCreateBeforeBinding(savedInstanceState: Bundle?) {
        super.onCreateBeforeBinding(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 禁用横屏
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        /*mNoSecuredialog =
            AlertDialogUtil.INSTANCE.createDialog(this, false, "安全提示", "请先添加系统解锁密码").create()
*/
        lockContainer.setOnClickListener(this)
        if (mKeyguardManager.isKeyguardSecure) {
            launchFingerAuth()
        } else {
            MissPasswordDialogFragment.showSelf(supportFragmentManager)

        }
    }

    /***开始指纹识别*/
    private fun launchFingerAuth() {
        FingerAuthDialogFragment.showSelf(
            supportFragmentManager,
            FingerAuthDialogFragment.Stage.FINGERPRINT
        )
    }

    fun authencitatedCallback() {
        if (mPassportViewModel.loadPassport()) {
            MainActivity.startThis(this@AuthRequestActivity)
        }else {
            /**首次使用，创建passport*/
            GuideActivity.startThis(this@AuthRequestActivity)
        }
    }


}
