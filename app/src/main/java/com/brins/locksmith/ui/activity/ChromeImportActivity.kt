package com.brins.locksmith.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.brins.locksmith.R
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment

class ChromeImportActivity : BaseActivity() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_chrome_import
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        if (mKeyguardManager.isKeyguardSecure) {
            launchFingerAuth()
        } else {
            MissPasswordDialogFragment.showSelf(supportFragmentManager)
        }
    }

    public override fun authencitatedCallback() {
        super.authencitatedCallback()
        if (mPassportViewModel.loadPassport()) {
            showLoading(getString(R.string.loading))

        } else {
            /**首次使用，创建passport*/
            Toast.makeText(this, getString(R.string.load_passport_fail),Toast.LENGTH_SHORT).show()
            GuideActivity.startThis(this@ChromeImportActivity)
        }
    }

}
