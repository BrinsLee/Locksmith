package com.brins.locksmith.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatEditText

/**
 * @author lipeilin
 * @date 2020/4/20
 */
class DrawableEditText : AppCompatEditText {
    private var mListener: onDrawableClickListener? = null

    interface onDrawableClickListener {
        fun onDrawableClick(view: View?, location: Int)
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    fun setListener(mListener: onDrawableClickListener?) {
        this.mListener = mListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> if (mListener != null) {
                val drawables: Array<Drawable> = compoundDrawables
                for (drawable in drawables) {
                    if (drawable != null) {
                        if (event.rawX <= left + drawable.bounds.width()) {
                            mListener!!.onDrawableClick(this, LEFT)
                            return true
                        }
                        if (event.rawX >= right - drawable.bounds.width()) {
                            mListener!!.onDrawableClick(this, RIGHT)
                            return true
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        const val TOP = 1
        const val LEFT = 2
        const val RIGHT = 3
        const val BOTTOM = 4
    }

    fun getDrawable(@IntRange(from = 0, to = 3) location: Int): Drawable {
        val drawables: Array<Drawable> = compoundDrawables
        return drawables[location]
    }
}
