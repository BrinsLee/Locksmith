package com.brins.locksmith.ui.dialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.brins.locksmith.R
import kotlinx.android.synthetic.main.dialog_miss_password.*
import kotlin.system.exitProcess

class MissPasswordDialogFragment : BaseDialogFragment(), View.OnClickListener {


    override fun onClick(v: View?) {
        dismiss()
        exitProcess(0)
    }

    override fun getLayoutResId(): Int {
        return R.layout.dialog_miss_password
    }

    companion object {

        fun showSelf(manager: FragmentManager, content: String = "") {
            val dialog = MissPasswordDialogFragment()
            val bundle = Bundle()
            bundle.putString(CONTENT, content)
            dialog.arguments = bundle
            dialog.show(manager)
        }
    }

    override fun isCanceledOnTouchOutside(): Boolean {
        return false
    }

    override fun isInterceptKeyCodeBack(): Boolean {
        return true
    }

    override fun getDialogAnimResId(): Int {
        return R.style.CustomCenterDialogAnim
    }

    override fun getDialogWidth(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun getDialogHeight(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    override fun onCreateViewAfterBinding(view: View) {
        super.onCreateViewAfterBinding(view)
        if (arguments != null) {
            if (arguments!!.getString(CONTENT).isNotEmpty()) {
                content.text = arguments!!.getString(CONTENT)
            }
        }
        btnOk.setOnClickListener(this)
    }
}