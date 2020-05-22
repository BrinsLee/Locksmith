package com.brins.locksmith.ui.dialog

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.brins.locksmith.R

/**
 * @author lipeilin
 * @date 2020/5/22
 */
class CustomPopupWindow(builder: Builder) {

    private val mContext: Activity? = builder.context
    private val contentview: View? =
        LayoutInflater.from(mContext).inflate(builder.contentviewid, null)
    private val mPopupWindow: PopupWindow =
        PopupWindow(contentview, builder.width, builder.height, builder.fouse)
    private var mDismissListener: DismissListener = DismissListener()

    init {
        //需要跟 setBackGroundDrawable 结合
        mPopupWindow.isOutsideTouchable = builder.outsidecancel
        mPopupWindow.isFocusable = true
//        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.animationStyle = builder.animstyle
//        mPopupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.popupwindow_bg_color)));
        //        mPopupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.popupwindow_bg_color)));
        val lp = mContext!!.window.attributes
        lp.alpha = builder.bgAlpha
        mContext.window.attributes = lp
        mContext.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        mPopupWindow.setOnDismissListener(mDismissListener)
    }

    companion object {
        private val mPopupDismissListener: PopupDismissListener? = null
        private var lastClickTime = 0L
        private const val FAST_CLICK_DELAY_TIME = 1000
        const val REQUESTCODE = 1001

        class Builder {
            internal var contentviewid = 0
            internal var width = 0
            internal var height = 0
            internal var fouse = false
            internal var outsidecancel = false
            internal var animstyle = 0
            internal var context: Activity? = null
            var bgAlpha = 0.5f
            fun setContext(context: Activity?): Builder {
                this.context = context
                return this
            }

            fun setBgAlpha(alpha: Float): Builder {
                bgAlpha = alpha
                return this
            }

            fun setContentView(contentviewid: Int): Builder {
                this.contentviewid = contentviewid
                return this
            }

            fun setwidth(width: Int): Builder {
                this.width = width
                return this
            }

            fun setheight(height: Int): Builder {
                this.height = height
                return this
            }

            fun setFouse(fouse: Boolean): Builder {
                this.fouse = fouse
                return this
            }

            fun setOutSideCancel(outsidecancel: Boolean): Builder {
                this.outsidecancel = outsidecancel
                return this
            }

            fun setAnimationStyle(animstyle: Int): Builder {
                this.animstyle = animstyle
                return this
            }

            fun build(): CustomPopupWindow {
                return CustomPopupWindow(this)
            }
        }

        fun showOperatePopupWindow(activity: Activity, listener: OptionListener)
                : CustomPopupWindow? {
            if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
                return null
            }
            lastClickTime = System.currentTimeMillis()
            val customPopupWindow = Builder()
                .setContext(activity)
                .setContentView(R.layout.bottom_popup_window)
                .setOutSideCancel(true)
                .setFouse(true)
                .setheight(ConstraintLayout.LayoutParams.WRAP_CONTENT)
                .setwidth(ConstraintLayout.LayoutParams.MATCH_PARENT)
                .build()
                .showAtLocation(R.layout.activity_edit_pass, Gravity.BOTTOM, 0, 0)
            customPopupWindow!!.getItemView(R.id.delete_item_ll)!!.setOnClickListener {
                listener.onDelete()
                customPopupWindow.dismiss()
            }
            customPopupWindow.getItemView(R.id.cancel_layout)!!.setOnClickListener {
                customPopupWindow.dismiss()
                listener.onCancel()
            }
            return customPopupWindow
        }
    }

    fun getView(viewId: Int): View? {
        return contentview!!.findViewById(viewId)
    }

    fun dismiss() {
        mPopupWindow?.dismiss()
    }

    /**
     * 根据id获取view
     *
     * @param viewid
     * @return
     */
    fun getItemView(viewid: Int): View? {
        return if (mPopupWindow != null) {
            contentview!!.findViewById(viewid)
        } else null
    }

    /**
     * 根据父布局，显示位置
     *
     * @param rootviewid
     * @param gravity
     * @param x
     * @param y
     * @return
     */
    fun showAtLocation(rootviewid: Int, gravity: Int, x: Int, y: Int): CustomPopupWindow? {
        if (mPopupWindow != null) {
            val rootview =
                LayoutInflater.from(mContext).inflate(rootviewid, null)
            mPopupWindow.showAtLocation(rootview, gravity, x, y)
        }
        return this
    }

    /**
     * 根据id获取view ，并显示在该view的位置
     *
     * @param targetviewId
     * @param gravity
     * @param offx
     * @param offy
     * @return
     */
    fun showAsLaction(
        targetviewId: Int,
        gravity: Int,
        offx: Int,
        offy: Int
    ): CustomPopupWindow? {
        if (mPopupWindow != null) {
            val targetview =
                LayoutInflater.from(mContext).inflate(targetviewId, null)
            mPopupWindow.showAsDropDown(targetview, gravity, offx, offy)
        }
        return this
    }

    /**
     * 显示在 targetview 的不同位置
     *
     * @param targetview
     * @param gravity
     * @param offx
     * @param offy
     * @return
     */
    fun showAsLaction(
        targetview: View?,
        gravity: Int,
        offx: Int,
        offy: Int
    ): CustomPopupWindow? {
        mPopupWindow?.showAsDropDown(targetview, gravity, offx, offy)
        return this
    }

    /**
     * 根据id设置焦点监听
     *
     * @param viewid
     * @param listener
     */
    fun setOnFocusListener(viewid: Int, listener: OnFocusChangeListener?) {
        val view = getItemView(viewid)
        view!!.onFocusChangeListener = listener
    }

    inner class DismissListener : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            val lp: WindowManager.LayoutParams = mContext!!.window.attributes
            lp.alpha = 1.0f
            mContext.window.attributes = lp
            mContext.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            if (CustomPopupWindow.mPopupDismissListener != null) {
                CustomPopupWindow.mPopupDismissListener.onDismiss()
            }
        }
    }

    interface PopupDismissListener {
        fun onDismiss()
    }

    interface OptionListener {
        fun onDelete()
        fun onCancel()
    }

}