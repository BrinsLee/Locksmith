package com.brins.locksmith.ui.dialog

import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.brins.locksmith.R

class LoadingDialogFragment : BaseDialogFragment() {


    companion object{
        fun showSelf(manager: FragmentManager) : LoadingDialogFragment {
            val dialog = LoadingDialogFragment()
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
}