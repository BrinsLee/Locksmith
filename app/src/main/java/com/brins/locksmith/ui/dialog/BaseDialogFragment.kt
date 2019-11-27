package com.brins.locksmith.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import java.util.*

abstract class BaseDialogFragment : DialogFragment() {

    val TAG = this.javaClass.simpleName
    private var mMapKey: Int = 0

    companion object {
        private val sWaitShowDialogMap = HashMap<Int, LinkedList<BaseDialogFragment>>()
        private val sShowingDialogMap = HashMap<Int, BaseDialogFragment>()
        val DEFAULT_DIM_AMOUNT = 0.6f
    }

    fun show(fragmentManager: FragmentManager): BaseDialogFragment {
        return show(fragmentManager, true)
    }

    private fun show(fragmentManager: FragmentManager, isCheck: Boolean): BaseDialogFragment {
        try {
            mMapKey = fragmentManager.hashCode()
            fragmentManager.executePendingTransactions()
            val fragment = fragmentManager.findFragmentByTag(TAG)
            if (fragment != null && fragment.isAdded) {
                return fragment as BaseDialogFragment
            }
            var isCanShow = true
            if (isCheck) {
                val showDialog = sShowingDialogMap.get(mMapKey)
                if (showDialog != null && showDialog.isAdded) {
                    isCanShow = false
                    var linkedList = sWaitShowDialogMap[mMapKey]
                    if (linkedList == null) {
                        linkedList = LinkedList()
                        sWaitShowDialogMap[mMapKey] = linkedList
                    }
                    linkedList.add(this)
                }
            }
            if (isCanShow) {
                val ft = fragmentManager.beginTransaction()
                ft.add(this, TAG)
                ft.commitAllowingStateLoss()
                sShowingDialogMap[mMapKey] = this
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        sShowingDialogMap.remove(mMapKey)
        val linkedList = sWaitShowDialogMap[mMapKey]
        if (linkedList != null && !linkedList.isEmpty()) {
            val dialogFragment = linkedList.poll()
            dialogFragment?.show(fragmentManager!!, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(getLayoutResId(), container, false)
        if (isInterceptKeyCodeBack()) {
            dialog?.setOnKeyListener { _, keyCode, event ->
                keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateViewAfterBinding(view)
    }

    override fun onStart() {
        super.onStart()
        initParams()
    }

    private fun initParams() {
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                //去除背景
                window.setBackgroundDrawableResource(android.R.color.transparent)
                //设置布局属性
                val layoutParams = window.attributes
                layoutParams.width = getDialogWidth()
                layoutParams.height = getDialogHeight()
                layoutParams.dimAmount = getDimAmount()
                layoutParams.gravity = getGravity()
                window.attributes = layoutParams
                if (getDialogAnimResId() > 0) {
                    window.setWindowAnimations(getDialogAnimResId())
                }
            }
            //设置是否点击外面可以取消
            dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside())
        }
    }

    protected abstract fun getLayoutResId(): Int

    protected open fun onCreateViewAfterBinding(view: View) {}

    protected open fun isCanceledOnTouchOutside(): Boolean {
        return true
    }

    protected open fun isInterceptKeyCodeBack(): Boolean {
        return false
    }

    protected open fun getDialogWidth(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    protected open fun getDialogHeight(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    protected open fun getGravity(): Int {
        return Gravity.CENTER
    }

    protected open fun getDimAmount(): Float {
        return DEFAULT_DIM_AMOUNT
    }


    protected open fun getDialogAnimResId(): Int {
        return 0
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }
}