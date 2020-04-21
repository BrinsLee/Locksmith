package com.brins.locksmith.ui.activity

import android.app.KeyguardManager
import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.brins.locksmith.R
import com.brins.locksmith.databinding.ActivityAuthRequestBinding
import com.brins.locksmith.ui.dialog.FingerAuthDialogFragment
import com.brins.locksmith.ui.dialog.FingerAuthDialogFragment.Companion.AUTH_REQUEST_CODE
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.main.MainViewModel
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import kotlinx.android.synthetic.main.activity_auth_request.*

class AuthRequestActivity : BaseActivity(), View.OnClickListener, ViewModelStoreOwner {

    private var mFingerDialog: FingerAuthDialogFragment? = null
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

    private var mNoSecuredialog: MissPasswordDialogFragment? = null
    private val mWallpaperManager: WallpaperManager by lazy { WallpaperManager.getInstance(this) }
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
        fingerprint_icon.setOnClickListener(this)
        if (mKeyguardManager.isKeyguardSecure) {
            launchFingerAuth()
        } else {
            MissPasswordDialogFragment.showSelf(supportFragmentManager)

        }
    }

    /***开始指纹识别*/
    private fun launchFingerAuth() {
        mFingerDialog = FingerAuthDialogFragment.showSelf(
            supportFragmentManager,
            FingerAuthDialogFragment.Stage.FINGERPRINT
            , this
        )
    }

    fun authencitatedCallback() {
        mFingerDialog?.dismiss()
        if (mPassportViewModel.loadPassport()) {
            MainActivity.startThis(this@AuthRequestActivity)
        } else {
            /**首次使用，创建passport*/
            GuideActivity.startThis(this@AuthRequestActivity)
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel -> mFingerDialog?.dismissAllowingStateLoss()

            R.id.usePassword -> {
                val intent: Intent? = mKeyguardManager.createConfirmDeviceCredentialIntent(
                    "Authentication required",
                    "PASSWORD"
                )
                if (intent != null) {
                    startActivityForResult(intent, FingerAuthDialogFragment.AUTH_REQUEST_CODE)
                }
            }

            R.id.fingerprint_icon -> {
                launchFingerAuth()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTH_REQUEST_CODE && resultCode == RESULT_OK) {
            authencitatedCallback()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
