package com.brins.locksmith.ui.activity

import android.app.KeyguardManager
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.brins.locksmith.R
import com.brins.locksmith.databinding.ActivityAuthRequestBinding
import com.brins.locksmith.ui.dialog.FingerAuthDialogFragment
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.main.MainViewModel
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import kotlinx.android.synthetic.main.activity_auth_request.*

class AuthRequestActivity : BaseActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        launchFingerAuth()
    }

    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProviders.of(this@AuthRequestActivity, InjectorUtil.getPassportModelFactory()).get(PassportViewModel::class.java)
    }

    private val mMainViewModel : MainViewModel by lazy {
        ViewModelProviders.of(this@AuthRequestActivity, InjectorUtil.getMainModelFactory()).get(MainViewModel::class.java)
    }
    private val binding by lazy { DataBindingUtil.setContentView<ActivityAuthRequestBinding>(this, R.layout.activity_auth_request) }

    private var mNoSecuredialog: MissPasswordDialogFragment? = null
    private val mKeyguardManager: KeyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }
    private lateinit var mAuthenticalDialogFragment: FingerAuthDialogFragment

    override fun getLayoutResId(): Int {
        return 0
    }

    override fun onCreateBeforeBinding(savedInstanceState: Bundle?) {
        super.onCreateBeforeBinding(savedInstanceState)
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        binding.mData = mMainViewModel.mDateLiveData.value
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
