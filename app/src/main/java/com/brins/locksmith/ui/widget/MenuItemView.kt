package com.brins.locksmith.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.brins.locksmith.data.customview.MenuItem
import com.brins.locksmith.data.customview.OnMenuActionListener
import com.brins.locksmith.utils.dpToPx
import com.facebook.rebound.SpringSystem
import com.tumblr.backboard.imitator.ToggleImitator
import com.tumblr.backboard.performer.MapPerformer
import com.tumblr.backboard.performer.Performer

class MenuItemView(context: Context, var mMenuItem: MenuItem) : LinearLayout(context),
    OnMenuActionListener {

    companion object {
        private val TAG = MenuItemView::class.java.simpleName
    }

    var mBtn: ImageButton? = null
    var mLabel: TextView? = null
    private val mGapSize = 4
    private val mTextSize = 14
    var mDiameter: Int = 0
    private var mAlphaAnimation = true

    init {
        val resources = resources
        val diameterPX = dpToPx(mMenuItem.diameter.toFloat(), resources)
        this.mDiameter = diameterPX
        mBtn = ImageButton(context)
        val btnLp = LayoutParams(diameterPX, diameterPX)
        btnLp.gravity = Gravity.CENTER_HORIZONTAL
        btnLp.bottomMargin = dpToPx(mGapSize.toFloat(), resources)
        val ovalShape = OvalShape()
        val shapeDrawable = ShapeDrawable(ovalShape)
        shapeDrawable.paint.color = ContextCompat.getColor(context, mMenuItem.bgColor)
        mBtn?.let {
            it.layoutParams = btnLp
            it.background = shapeDrawable
            it.setImageResource(mMenuItem.icon)
            it.isClickable = false
        }
        addView(mBtn)

        mLabel = TextView(context)
        val labelLp = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        labelLp.gravity = Gravity.CENTER_HORIZONTAL
        mLabel?.let {
            it.layoutParams = labelLp
            it.text = mMenuItem.label
            it.setTextColor(ContextCompat.getColor(context, mMenuItem.textColor))
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat())
        }
        addView(mLabel)

        orientation = VERTICAL
        if (mAlphaAnimation) {
            alpha = 0f
        }

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                applyPressAnimation()
                val parent = parent as ViewGroup
                parent.clipChildren = false
                parent.clipToPadding = false
                clipChildren = false
                clipToPadding = false
            }
        })

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyPressAnimation() {
        val springSystem = SpringSystem.create()
        val spring = springSystem.createSpring()
        spring.addListener(Performer(mBtn, View.SCALE_X))
        spring.addListener(Performer(mBtn, View.SCALE_Y))
        mBtn!!.setOnTouchListener(object : ToggleImitator(spring, 1.0, 1.2) {
            override fun imitate(event: MotionEvent) {
                super.imitate(event)
                when (event.action) {
                    MotionEvent.ACTION_UP -> callOnClick()
                }
            }
        })
        spring.currentValue = 1.0

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            mBtn!!.measuredWidth.coerceAtLeast(mLabel!!.measuredWidth),
            mBtn!!.measuredHeight + mLabel!!.measuredHeight + dpToPx(4f, resources)
        )

    }

    fun showLabel() {
        val springSystem = SpringSystem.create()
        val spring = springSystem.createSpring()
        spring.addListener(MapPerformer(mLabel!!, View.SCALE_X, 0f, 1f))
        spring.addListener(MapPerformer(mLabel!!, View.SCALE_Y, 0f, 1f))
        spring.currentValue = 0.0
        Handler().postDelayed({ spring.endValue = 1.0 }, 200)
    }

    /***禁用透明度*/
    fun disableAlphaAnimation() {
        mAlphaAnimation = false
        alpha = 1f
    }

    override fun onMenuOpen() {
        showLabel()
        if (mAlphaAnimation) {
            animate().alpha(1f).setDuration(120).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    this@MenuItemView.setOnClickListener(mMenuItem.onClickListener)

                }
            })
        } else {
            this@MenuItemView.setOnClickListener(mMenuItem.onClickListener)
        }
    }

    override fun onMenuClose() {
        if (mAlphaAnimation) {
            animate().alpha(0f).setDuration(120).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Log.d(TAG, "now disable clickListener")
                    this@MenuItemView.setOnClickListener(null)
                }
            })
        } else {
            this@MenuItemView.setOnClickListener(null)
        }
    }
}