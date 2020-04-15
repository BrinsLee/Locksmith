package com.brins.locksmith.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.utils.EventBusUtils
import com.brins.locksmith.utils.EventMessage
import com.brins.locksmith.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.activity_edit_pass.*
import kotlinx.android.synthetic.main.activity_edit_pass.header_layout
import kotlinx.android.synthetic.main.header.*

class EditPassActivity : BaseActivity() {


    private var mNote: String = ""
    private var mPassword: String = ""
    private var mName: String = ""
    private var mAccountName: String = ""
    private var mType = 0


    companion object {

        @JvmStatic
        val TYPE_FROM_CARD = 1
        @JvmStatic
        val TYPE_FROM_PASSWORD = 2

        val TYPE_FROM_WHERE = "TYPE_FROM_WHERE"

        private val passwordlength = 20
        fun startThis(activity: BaseActivity) {
            val intent = Intent(activity, EditPassActivity::class.java)
            activity.startActivity(intent)
        }

        fun startThis(activity: BaseActivity, fromWhere: Int) {
            val intent = Intent(activity, EditPassActivity::class.java)
            intent.putExtra(TYPE_FROM_WHERE, fromWhere)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutResId(): Int {
        mType = intent.getIntExtra(TYPE_FROM_WHERE, 0)
        return when (mType) {
            0, 1 -> R.layout.activity_edit_pass
            2 -> R.layout.activity_edit_card
            else -> R.layout.activity_edit_pass
        }
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        header_layout.setPadding(0, getStatusBarHeight(this), 0, 0)
        when (mType) {
            0, 1 -> title_tv.text = "密码"
            2 -> title_tv.text = "银行卡"
        }
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
            when (mType) {
                0, 1 -> {
                    mSavePasswordViewModel.savePassWord(mName, mAccountName, mPassword, mNote) {
                        EventBusUtils.sendEnvent(EventMessage(EventMessage.CODE_UPDATE_PASSWORD, null))
                        finish()
                    }
                }
                2 -> {
                    mSaveCardViewModel.savePassWord(mName,mAccountName,mPassword,mNote){
                        EventBusUtils.sendEnvent(EventMessage(EventMessage.CODE_UPDATE_PASSWORD, null))
                        finish()
                    }
                }
            }

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
