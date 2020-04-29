package com.brins.locksmith.ui.dialog

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.brins.locksmith.R
import kotlinx.android.synthetic.main.dialog_loading.*
import kotlinx.android.synthetic.main.dialog_miss_password.*

class LoadingDialogFragment : BaseDialogFragment() {


    companion object{
        fun showSelf(manager: FragmentManager, content: String?) : LoadingDialogFragment {
            val dialog = LoadingDialogFragment()
            val bundle = Bundle()
            bundle.putString(CONTENT, content)
            dialog.arguments = bundle
            dialog.show(manager)
            return dialog
        }
    }

    override fun isCanceledOnTouchOutside(): Boolean {
        return true
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
    override fun getLayoutResId(): Int {
        return R.layout.dialog_loading
    }

    override fun onCreateViewAfterBinding(view: View) {
        super.onCreateViewAfterBinding(view)
        if (arguments != null) {
            if (!arguments!!.getString(CONTENT).isNullOrEmpty()) {
                tv_content.text = arguments!!.getString(CONTENT)
            }
        }
    }
}