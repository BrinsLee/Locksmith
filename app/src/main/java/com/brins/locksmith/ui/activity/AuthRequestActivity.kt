package com.brins.locksmith.ui.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.brins.locksmith.R
import com.brins.locksmith.databinding.ActivityAuthRequestBinding
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.main.MainViewModel
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import kotlinx.android.synthetic.main.activity_auth_request.*

class AuthRequestActivity : BaseActivity(), View.OnClickListener, ViewModelStoreOwner {

    //    private var mFingerDialog: FingerAuthDialogFragment? = null
    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProvider(this@AuthRequestActivity, InjectorUtil.getPassportModelFactory()).get(
            PassportViewModel::class.java
        )
    }

    private val mMainViewModel: MainViewModel by lazy {
        ViewModelProvider(this@AuthRequestActivity, InjectorUtil.getMainModelFactory()).get(
            MainViewModel::class.java
        )
    }
    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityAuthRequestBinding>(
            this,
            R.layout.activity_auth_request
        )
    }


    override fun getLayoutResId(): Int {
        return 0
    }


    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        binding.mData = mMainViewModel.mDateLiveData.value
        fingerprint_icon.setOnClickListener(this)
        if (mKeyguardManager.isKeyguardSecure) {
            launchFingerAuth()
        } else {
            MissPasswordDialogFragment.showSelf(supportFragmentManager)
        }
    }

    /***开始指纹识别*/


    public override fun authencitatedCallback() {
        super.authencitatedCallback()
        if (mPassportViewModel.loadPassport()) {
            MainActivity.startThis(this@AuthRequestActivity)
        } else {
            /**首次使用，创建passport*/
            GuideActivity.startThis(this@AuthRequestActivity)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.fingerprint_icon -> {
                launchFingerAuth()
            }
            R.id.cancel -> mFingerDialog?.dismissAllowingStateLoss()

            R.id.usePassword -> {
                onClickUsePassword()
            }
        }
    }



}
