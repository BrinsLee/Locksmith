package com.brins.locksmith.ui.activity

import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.autofill.service.LockSmithAutofillService.Companion.AUTO_FILL_PASSWORD
import com.brins.locksmith.autofill.service.LockSmithAutofillService.Companion.AUTO_FILL_UESRNAME
import com.brins.locksmith.autofill.service.LockSmithAutofillService.Companion.AUTO_FILL_URL
import com.brins.locksmith.data.AppConfig.APPNAME
import com.brins.locksmith.data.AppConfig.LOCATION
import com.brins.locksmith.data.AppConfig.NOTE
import com.brins.locksmith.data.AppConfig.PHONE
import com.brins.locksmith.data.AppConfig.USERNAME
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.ui.dialog.CustomPopupWindow
import com.brins.locksmith.utils.EventBusUtils
import com.brins.locksmith.utils.EventMessage
import com.brins.locksmith.utils.TimeUtils
import com.brins.locksmith.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.activity_edit_card.*
import kotlinx.android.synthetic.main.activity_edit_card.nested_root
import kotlinx.android.synthetic.main.activity_edit_pass.*
import kotlinx.android.synthetic.main.activity_edit_pass.account_edit_et
import kotlinx.android.synthetic.main.activity_edit_pass.header_layout
import kotlinx.android.synthetic.main.activity_edit_pass.name_edit_et
import kotlinx.android.synthetic.main.activity_edit_pass.note_edit_et
import kotlinx.android.synthetic.main.activity_edit_pass.password_edit_et
import kotlinx.android.synthetic.main.header.*


class EditPassActivity : BaseActivity() {


    private var mNote: String = ""
    private var mPassword: String = ""
    private var mName: String = ""
    private var mAccountName: String = ""

    private var mType = 0
    private var mPos = -1
    private var isOpenEye = false


    companion object {


        val TYPE_FROM_WHERE = "TYPE_FROM_WHERE"
        val DATA_POS = "DATA_POS"


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
        window.setFlags(FLAG_SECURE, FLAG_SECURE)
        mType = intent.getIntExtra(TYPE_FROM_WHERE, 0)
        mPos = intent.getIntExtra(DATA_POS, -1)
        return when (mType) {
            0, BaseMainItemType.ITEM_NORMAL_PASS -> R.layout.activity_edit_pass
            BaseMainItemType.ITEM_NORMAL_CARD -> R.layout.activity_edit_card
            else -> R.layout.activity_edit_pass
        }
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        header_layout.setPadding(0, getStatusBarHeight(this), 0, 0)
        mPassportViewModel.loadPassport()
        initView()
        ButterKnife.bind(this)
        setListener()
    }

    private fun initView() {
        when (mType) {
            0, BaseMainItemType.ITEM_NORMAL_PASS -> {
                title_tv.text = "密码"
                if (mPos != -1) {
                    more_operate.visibility = View.VISIBLE
                    val data = mSavePasswordViewModel.mPassWordData.value?.get(mPos)
                    data?.let {
                        name_edit_et.setText(it.getAppName())
                        account_edit_et.setText(it.generalItems[USERNAME])
                        password_edit_et.setText(it.getPasswordData())
                        note_edit_et.setText(it.generalItems[NOTE])
                        create_date_tv.text =
                            "创建日期：${TimeUtils.getDateByCurrentTime(it.meta!!.creationDate * 1000)}"

                    }
                } else {
                    create_date_tv.text = TimeUtils.getDateByCurrentTime(System.currentTimeMillis());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        name_edit_et.setText(intent.getStringExtra(AUTO_FILL_URL))
                        account_edit_et.setText(intent.getStringExtra(AUTO_FILL_UESRNAME))
                        password_edit_et.setText(intent.getStringExtra(AUTO_FILL_PASSWORD))
                    }

                }

            }
            BaseMainItemType.ITEM_NORMAL_CARD -> {
                title_tv.text = "银行卡"
                if (mPos != -1) {
                    more_operate.visibility = View.VISIBLE
                    val data = mSaveCardViewModel.mCardData.value?.get(mPos)
                    data?.let {
                        name_edit_et.setText(it.getAppName())
                        account_edit_et.setText(it.generalItems[USERNAME])
                        password_edit_et.setText(it.getPasswordData())
                        phone_edit_et.setText(it.generalItems[PHONE])
                        location_edit_et.setText(it.generalItems[LOCATION])
                        note_edit_et.setText(it.generalItems[NOTE])

                    }
                }
            }
        }
        nested_root.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener{
            override fun onScrollChange(
                v: NestedScrollView?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                header_layout.y = scrollY.toFloat()
            }

        })
    }


    private fun setListener() {
        if (mPos == -1) {
            showSoftInputFromWindow(name_edit_et)
            nested_root.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                header_layout.y = scrollY.toFloat()
            })
        }
    }

    @OnClick(
        R.id.return_img,
        R.id.save_account,
        R.id.more_operate
    )
    fun onClickView(v: View) {
        when (v.id) {
            R.id.return_img -> finish()
            R.id.save_account -> {
                saveAccount()
            }
            R.id.more_operate -> {
                CustomPopupWindow.showOperatePopupWindow(this, object :
                    CustomPopupWindow.OptionListener {
                    override fun onDelete() {
                        deleteAccount()
                    }

                    override fun onCancel() {

                    }
                })
            }
        }
    }

    fun onVisibleClick(v: View) {
        if (!isOpenEye) {
            iv_password_visible.isSelected = true
            isOpenEye = true
            password_edit_et.transformationMethod =
                HideReturnsTransformationMethod.getInstance()

        } else {
            iv_password_visible.isSelected = false
            isOpenEye = false
            password_edit_et.transformationMethod =
                PasswordTransformationMethod.getInstance()

        }
    }

    fun onCardVisibleClick(v: View) {
        if (!isOpenEye) {
            iv_password_visible_card.isSelected = true
            isOpenEye = true
            password_edit_et.transformationMethod =
                HideReturnsTransformationMethod.getInstance()

        } else {
            iv_password_visible_card.isSelected = false
            isOpenEye = false
            password_edit_et.transformationMethod =
                PasswordTransformationMethod.getInstance()

        }
    }

    private fun showSoftInputFromWindow(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

    }

    private fun saveAccount() {
        if (isInfoComplete()) {
            when (mType) {
                0, BaseMainItemType.ITEM_NORMAL_PASS -> {
                    if (mPos == -1) {
                        mSavePasswordViewModel.savePassWord(mName, mAccountName, mPassword, mNote) {
                            finish()
                        }
                    } else {
                        val data = mSavePasswordViewModel.mPassWordData.value?.get(mPos)
                        data?.let {
                            it.generalItems[APPNAME] = mName
                            it.generalItems[USERNAME] = mAccountName
                            it.generalItems[NOTE] = mNote
                            it.setPasswordData(mPassword)
                            it.setAppName(mName)
                            it.accountName = mAccountName
                            it.mNote = mNote
                            mSavePasswordViewModel.updatePassWord(it) {
                                EventBusUtils.sendEnvent(
                                    EventMessage(
                                        EventMessage.CODE_UPDATE_PASSWORD,
                                        mPos
                                    )
                                )
                                finish()
                            }

                        }
                    }
                }
                BaseMainItemType.ITEM_NORMAL_CARD -> {
                    if (mPos == -1) {
                        mSaveCardViewModel.saveCard(
                            mName,
                            mAccountName,
                            mPassword,
                            mNote,
                            location_edit_et.text?.trim().toString(),
                            phone_edit_et.text?.trim().toString()
                        ) {
                            finish()
                        }
                    } else {
                        val data = mSaveCardViewModel.mCardData.value?.get(mPos)
                        data?.let {
                            it.generalItems[APPNAME] = mName
                            it.generalItems[USERNAME] = mAccountName
                            it.setPasswordData(mPassword)
                            it.generalItems[NOTE] = mNote
                            it.generalItems[PHONE] = phone_edit_et.text.toString()
                            it.generalItems[LOCATION] = location_edit_et.text.toString()
                            it.setAppName(mName)
                            it.accountName = mAccountName
                            it.mNote = mNote
                            it.location = location_edit_et.text.toString()
                            it.phone = phone_edit_et.text.toString()
                            mSaveCardViewModel.updateCard(it) {
                                EventBusUtils.sendEnvent(
                                    EventMessage(
                                        EventMessage.CODE_UPDATE_BANK,
                                        mPos
                                    )
                                )
                                finish()
                            }

                        }
                    }
                }
            }

        } else {

        }
    }

    private fun deleteAccount(){
        when(mType){
            0, BaseMainItemType.ITEM_NORMAL_PASS -> {
                mSavePasswordViewModel.deletePassword(mPos){
                    EventBusUtils.sendEnvent(
                        EventMessage(
                            EventMessage.CODE_DELETE_PASSWORD,
                            mPos
                        )
                    )
                    finish()
                }
            }
            BaseMainItemType.ITEM_NORMAL_CARD -> {
                mSaveCardViewModel.deleteCard(mPos){
                    EventBusUtils.sendEnvent(
                        EventMessage(
                            EventMessage.CODE_DELETE_BANK,
                            mPos
                        )
                    )
                    finish()
                }
            }
        }
    }

    /***检查信息是否完整*/
    private fun isInfoComplete(): Boolean {
        mName = name_edit_et.text.toString()
        mAccountName = account_edit_et.text.toString()
        mPassword = password_edit_et.text.toString()
        return if (mName.isNullOrEmpty() || mAccountName.isNullOrEmpty() || mPassword.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.information_not_complete), Toast.LENGTH_SHORT)
                .show()
            false
        } else {
            mNote = note_edit_et.text.toString()
            if (mNote.isEmpty()) mNote = ""
            true
        }
    }

}
