package com.brins.locksmith.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.data.PassWordItem
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.getStatusBarHeight
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import kotlinx.android.synthetic.main.activity_edit_pass.*

class EditPassActivity : BaseActivity() {


    private var mNote: String = ""
    private var mPassword: String = ""
    private var mName: String = ""
    private var mAccountName: String = ""
    private val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this@EditPassActivity, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }

    companion object {
        private val passwordlength = 20
        fun startThis(activity: BaseActivity) {
            val intent = Intent(activity, EditPassActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_edit_pass
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        header_layout.setPadding(0, getStatusBarHeight(this), 0, 0)
        ButterKnife.bind(this)
        setListener()
    }


    private fun setListener() {

    }

    @OnClick(R.id.return_img, R.id.save_account)
    fun onClick(v: View) {
        when (v.id) {
            R.id.return_img -> finish()
            R.id.save_account -> {
                saveAccount()
            }
        }
    }

    private fun saveAccount() {
        if (isInfoComplete()) {
            mSavePasswordViewModel.savePassWord(mName, mAccountName, mPassword, mNote, this)
        } else {

        }
    }

    /***检查信息是否完整*/
    private fun isInfoComplete(): Boolean {
        mName = name_edit_et.text.toString()
        mAccountName = account_edit_et.text.toString()
        mPassword = password_edit_et.text.toString()
        return if (mName.isNullOrEmpty() || mAccountName.isNullOrEmpty() || mPassword.isNullOrEmpty()) {
            //todo 自定义Toast显示信息不全
            false
        } else {
            mNote = note_edit_et.text.toString()
            if (mNote.isEmpty()) mNote = ""
            true
        }
    }

}
