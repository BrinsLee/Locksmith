package com.brins.locksmith.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import com.brins.locksmith.R
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_chrome_import.*

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
            ImportPassword().execute()
        } else {
            /**首次使用，创建passport*/
            Toast.makeText(this, getString(R.string.load_passport_fail), Toast.LENGTH_SHORT).show()
            GuideActivity.startThis(this@ChromeImportActivity)
        }
    }

    inner class ImportPassword : AsyncTask<String, String, ArrayList<PassWordItem>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mSavePasswordViewModel.loadPasswordItem()
        }

        override fun doInBackground(vararg params: String?): ArrayList<PassWordItem> {
            val bundle = intent.extras
            val uri: Uri = bundle.get("android.intent.extra.STREAM") as Uri
            val filename_array = uri.toString().split("/")
            val uri_inputStream = contentResolver.openInputStream(uri)
            return mSavePasswordViewModel.loadPasswordItem(uri_inputStream) { s: String ->
                publishProgress(s)
            }
        }

        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)
            Toast.makeText(this@ChromeImportActivity, values[0], Toast.LENGTH_SHORT).show()

        }

        override fun onPostExecute(result: ArrayList<PassWordItem>) {
            result.forEach{
                mSavePasswordViewModel.savePassWord(it)
            }
            hideLoading()
            val intent = Intent(this@ChromeImportActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
