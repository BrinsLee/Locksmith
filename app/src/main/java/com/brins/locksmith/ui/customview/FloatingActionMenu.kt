package com.brins.locksmith.ui.customview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import com.brins.locksmith.R
import com.brins.locksmith.data.customview.DestroySelfSpringListener
import com.brins.locksmith.data.customview.MenuItem
import com.brins.locksmith.data.customview.OnMenuActionListener
import com.brins.locksmith.utils.createWrapParams
import com.brins.locksmith.utils.getDimension
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tumblr.backboard.Actor
import com.tumblr.backboard.MotionProperty
import com.tumblr.backboard.imitator.Imitator
import com.tumblr.backboard.imitator.SpringImitator
import com.tumblr.backboard.performer.MapPerformer
import com.tumblr.backboard.performer.Performer
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.ArrayList

class FloatingActionMenu(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), ViewTreeObserver.OnGlobalLayoutListener {
    override fun onGlobalLayout() {
        if (mEnableFollowAnimation) {
            applyFollowAnimation()
        }
    }

    private fun applyFollowAnimation() {
        val springSystem = SpringSystem.create()

        // create the springs that control movement
        val springX = springSystem.createSpring()
        val springY = springSystem.createSpring()

        // bind circle movement to events
        Actor.Builder(springSystem, mFab)
            .addMotion(springX, Imitator.TRACK_DELTA, Imitator.FOLLOW_EXACT, MotionProperty.X)
            .addMotion(springY, Imitator.TRACK_DELTA, Imitator.FOLLOW_EXACT, MotionProperty.Y)
            .build()

        // add springs to connect between the views
        val followsX = arrayOfNulls<Spring>(mMenuItemCount)
        val followsY = arrayOfNulls<Spring>(mMenuItemCount)

        for (i in mFollowCircles!!.indices) {

            // create spring to bind views
            followsX[i] = springSystem.createSpring()
            followsY[i] = springSystem.createSpring()
            followsX[i]?.addListener(Performer(mFollowCircles!![i], View.TRANSLATION_X))
            followsY[i]?.addListener(Performer(mFollowCircles!![i], View.TRANSLATION_Y))

            // imitates another character
            val followX = SpringImitator(followsX[i]!!)
            val followY = SpringImitator(followsY[i]!!)

            //  imitate the previous character
            if (i == 0) {
                springX.addListener(followX)
                springY.addListener(followY)
            } else {
                followsX[i - 1]!!.addListener(followX)
                followsY[i - 1]!!.addListener(followY)
            }
        }
    }

    constructor(builder: Builder) : this(context = builder.context) {
        this.mMenuItems = builder.menuItems
        this.mFab = builder.fab!!
        this.mMenuItemCount = builder.menuItems.size
        this.mGravity = builder.gravity
        this.mActionListeners = builder.actionListeners
        this.mRevealColor = builder.revealColor
        this.mOnFabClickListener = builder.onFabClickListener
        this.mEnableFollowAnimation = builder.enableFollowAnimation
        this.mMargin = builder.mMargin
        initView()
    }

    companion object {
        private val TAG = FloatingActionMenu::class.java.simpleName

        const val ANIMATION_TYPE_BLOOM = 0
        const val ANIMATION_TYPE_TUMBLR = 1

        class Builder(val context: Context) {

            internal val menuItems = ArrayList<MenuItem>()

            internal var fab: FloatingActionButton? = null

            internal var gravity = Gravity.BOTTOM or Gravity.RIGHT

            internal var enableFollowAnimation = true

            @ColorRes
            internal var revealColor = android.R.color.holo_purple

            internal var onFabClickListener: OnFabClickListener? = null

            internal val actionListeners = ArrayList<OnMenuActionListener>()

            internal var mMargin: IntArray? = null

            fun build(): FloatingActionMenu {
                return FloatingActionMenu(this)
            }

            fun addMenuItem(
                @ColorRes bgColor: Int, icon: Int, label: String,
                @ColorRes textColor: Int = R.color.white, onClickListener: View.OnClickListener
            ): Builder {
                menuItems.add(MenuItem(bgColor, icon, label, textColor, onClickListener))
                return this
            }

            fun addFab(fab: FloatingActionButton): Builder {
                this.fab = fab
                return this
            }

            fun gravity(gravity: Int): Builder {
                this.gravity = gravity
                return this
            }

            fun onMenuActionListner(listener: OnMenuActionListener): Builder {
                actionListeners.add(listener)
                return this
            }

            fun revealColor(@ColorRes color: Int): Builder {
                this.revealColor = color
                return this
            }

            fun addMargin(margin: IntArray): Builder {
                this.mMargin = margin
                return this
            }

            fun enableFollowAnimation(enable: Boolean): Builder {
                this.enableFollowAnimation = enable
                return this
            }

            fun onFabClickListener(listener: OnFabClickListener): Builder {
                this.onFabClickListener = listener
                return this
            }

        }
    }


    private lateinit var mFab: FloatingActionButton
    private var mRevealCircle: View? = null
    private var mMenuItems: ArrayList<MenuItem>? = null

    private var mFollowCircles: ArrayList<ImageButton>? = null

    private var mMenuItemViews: ArrayList<MenuItemView>? = null

    private var mContainerView: ViewGroup? = null

    private var mActionListeners: ArrayList<OnMenuActionListener>? = null

    private var mOnFabClickListener: OnFabClickListener? = null

    @ColorRes
    private var mRevealColor: Int = 0

    private var mMenuItemCount: Int = 0

    private var mGravity: Int = 0

    private var mMargin: IntArray? = null

    private val mMarginSize = 16

    private var mMenuOpen = false

    private val mRevealDuration = 600

    private val mTimeInterval = 70

    private var mAnimating = false

    private var mEnableFollowAnimation = true


    interface OnFabClickListener {
        fun onClcik()
    }

    private fun initView() {
        mContainerView = FrameLayout(context)
        mRevealCircle = generateRevealCircle()
        (mContainerView as FrameLayout).addView(mRevealCircle)
        val fablp = createWrapParams()
        fablp.gravity = mGravity
        mMargin?.let {
            fablp.rightMargin = it[2]
            fablp.bottomMargin = it[3]
        }
        if (mEnableFollowAnimation) {
            mFollowCircles = generateFollowCircles()
            for (i in mFollowCircles!!.indices.reversed()) {
                // note follow circles is not added in container view, is just added in this SpringFloatingActionMenu
                addView(mFollowCircles!![i])
            }
        }

        mMenuItemViews = generateMenuItemViews()
        mMenuItemViews?.let {
            for (menuItemView in it) {
                mContainerView!!.addView(menuItemView)
                addOnMenuActionListener(menuItemView)
            }
            it[0].bringToFront()
        }

        addView(mFab, fablp)


        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //add self to root view
        val rootView = (context as Activity).findViewById<View>(android.R.id.content) as ViewGroup
        rootView.addView(this)
        bringToFront()
        viewTreeObserver.addOnGlobalLayoutListener(this)

        mFab.setOnClickListener(OnClickListener {
            if (mAnimating) {
                return@OnClickListener
            }
            if (mOnFabClickListener != null) {
                mOnFabClickListener!!.onClcik()
            }
            if (mMenuOpen) {
                hideMenu()
            } else {
                showMenu()
            }
        })
    }

    private fun showMenu() {
        applyOpenAniamtion()
        hideFollowCircles()
        revealIn()
        for (listener in mActionListeners!!) {
            listener.onMenuOpen()
        }
        mMenuOpen = !mMenuOpen
    }

    private fun hideMenu() {
        applyCloseAnimation()
        showFollowCircles()
        revealOut()
        for (listener in mActionListeners!!) {
            listener.onMenuClose()
        }
        mMenuOpen = !mMenuOpen
    }

    private fun hideFollowCircles() {
        mFollowCircles?.let {
            for (view in it) {
                view.visibility = View.INVISIBLE
            }
        }

    }

    private fun showFollowCircles() {
        mFollowCircles?.let {
            for (view in it) {
                view.visibility = View.VISIBLE
            }
        }
    }

    private fun applyOpenAniamtion() {
        val firstItem = mMenuItemViews!![0]

        //make start position at center
        mMenuItemViews?.let {
            for (itemView in it) {
                itemView.disableAlphaAnimation()
                itemView.x = firstItem.left.toFloat()
                itemView.y = firstItem.y
                itemView.scaleX = 0f
                itemView.scaleY = 0f
            }
        }

        val springSystem = SpringSystem.create()

        val springScaleX = springSystem.createSpring()
        val springScaleY = springSystem.createSpring()

        springScaleX.addListener(MapPerformer(firstItem, View.SCALE_X, 0f, 1f))
        springScaleY.addListener(MapPerformer(firstItem, View.SCALE_Y, 0f, 1f))
        val destroySelfSpringListener = DestroySelfSpringListener(this, mContainerView!!, true)
        springScaleX.addListener(destroySelfSpringListener)
        springScaleY.addListener(destroySelfSpringListener)
        springScaleX.endValue = 1.0
        springScaleY.endValue = 1.0

        for (i in 1 until mMenuItemCount) {
            val menuItemView = mMenuItemViews!![i]
            Handler().postDelayed({
                val springScaleX = springSystem.createSpring()
                val springScaleY = springSystem.createSpring()

                springScaleX.addListener(MapPerformer(menuItemView, View.SCALE_X, 0f, 1f))
                springScaleY.addListener(MapPerformer(menuItemView, View.SCALE_Y, 0f, 1f))
                val destroySelfSpringListener =
                    DestroySelfSpringListener(this@FloatingActionMenu, mContainerView!!, true)
                springScaleX.addListener(destroySelfSpringListener)
                springScaleY.addListener(destroySelfSpringListener)
                springScaleX.endValue = 1.0
                springScaleY.endValue = 1.0

                val springX = springSystem.createSpring()
                val springY = springSystem.createSpring()

                springX.addListener(
                    MapPerformer(
                        menuItemView,
                        View.X,
                        firstItem.left.toFloat(),
                        menuItemView.left.toFloat()
                    )
                )
                springY.addListener(
                    MapPerformer(
                        menuItemView,
                        View.Y,
                        firstItem.top.toFloat(),
                        menuItemView.top.toFloat()
                    )
                )
                springX.addListener(destroySelfSpringListener)
                springY.addListener(destroySelfSpringListener)
                springX.endValue = 1.0
                springY.endValue = 1.0
            }, (mTimeInterval * (i - 1)).toLong())
        }
    }


    private fun applyCloseAnimation() {
        val alphaDuration = 130
        val springSystem = SpringSystem.create()
        val firstItem = mMenuItemViews!![0]
        val springScaleX = springSystem.createSpring()
        val springScaleY = springSystem.createSpring()
        springScaleX.addListener(MapPerformer(firstItem, View.SCALE_X, 1f, 0f))
        springScaleY.addListener(MapPerformer(firstItem, View.SCALE_Y, 1f, 0f))

        val destroySelfSpringListener = DestroySelfSpringListener(this, mContainerView!!, false)
        springScaleX.addListener(destroySelfSpringListener)
        springScaleY.addListener(destroySelfSpringListener)
        springScaleX.endValue = 1.0
        springScaleY.endValue = 1.0
        firstItem.animate()
            .alpha(160f)

        for (i in mMenuItemCount - 1 downTo 1) {
            val menuItemView = mMenuItemViews!![i]
            Handler().postDelayed({
                val springScaleX = springSystem.createSpring()
                val springScaleY = springSystem.createSpring()

                springScaleX.addListener(MapPerformer(menuItemView, View.SCALE_X, 1f, 0f))
                springScaleY.addListener(MapPerformer(menuItemView, View.SCALE_Y, 1f, 0f))
                val destroySelfSpringListener =
                    DestroySelfSpringListener(this@FloatingActionMenu, mContainerView!!, false)
                springScaleX.addListener(destroySelfSpringListener)
                springScaleY.addListener(destroySelfSpringListener)
                springScaleX.endValue = 1.0
                springScaleY.endValue = 1.0

                val springX = springSystem.createSpring()
                val springY = springSystem.createSpring()

                springX.addListener(
                    MapPerformer(
                        menuItemView,
                        View.X,
                        menuItemView.left.toFloat(),
                        firstItem.left.toFloat()
                    )
                )
                springY.addListener(
                    MapPerformer(
                        menuItemView,
                        View.Y,
                        menuItemView.top.toFloat(),
                        firstItem.top.toFloat()
                    )
                )
                springX.addListener(destroySelfSpringListener)
                springY.addListener(destroySelfSpringListener)
                springX.endValue = 1.0
                springY.endValue = 1.0

                menuItemView.animate()
                    .alpha(0f).duration = alphaDuration.toLong()

            }, mTimeInterval * (mMenuItemCount - i - 1).toLong())
        }

    }

    private fun revealIn() {
        mRevealCircle!!.visibility = View.VISIBLE
        mRevealCircle!!.animate()
            .scaleX(100f)
            .scaleY(100f)
            .setDuration(mRevealDuration.toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mAnimating = false
                    animation.removeAllListeners()
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    mAnimating = true
                }
            })
            .start()
    }

    private fun revealOut() {
        mRevealCircle!!.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(mRevealDuration.toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mRevealCircle!!.visibility = View.INVISIBLE
                    animation.removeAllListeners()
                    mAnimating = false
                }

                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    mAnimating = true
                }
            })
            .start()
    }

    private fun addOnMenuActionListener(menuItemView: OnMenuActionListener) {
        this.mActionListeners!!.add(menuItemView)
    }

    private fun generateMenuItemViews(): ArrayList<MenuItemView>? {
        val menuItemViews = ArrayList<MenuItemView>(mMenuItems!!.size)
        for (item in mMenuItems!!) {
            val menuItemView = MenuItemView(context, item)
            menuItemView.layoutParams = createWrapParams()
            //            menuItemView.setOnClickListener(item.getOnClickListener());
            menuItemViews.add(menuItemView)
        }
        return menuItemViews
    }

    private fun generateFollowCircles(): ArrayList<ImageButton>? {
        val diameter = getDimension(context, R.dimen.fab_size_normal)
        val circles = ArrayList<ImageButton>(mMenuItems!!.size)
        for (item in mMenuItems!!) {
            val circle = ImageButton(context)
            val ovalShape = OvalShape()
            val shapeDrawable = ShapeDrawable(ovalShape)
            shapeDrawable.paint.color = ContextCompat.getColor(context, item.bgColor)
            circle.background = shapeDrawable
            circle.setImageResource(item.icon)
            val lp = LayoutParams(diameter, diameter)
            circle.layoutParams = lp
            circles.add(circle)
        }

        return circles
    }

    private fun generateRevealCircle(): View {
        val diameter = getDimension(context, R.dimen.fab_size_normal)
        val view = View(context)
        val ovalShape = OvalShape()
        val shapeDrawable = ShapeDrawable(ovalShape)
        shapeDrawable.paint.color = ContextCompat.getColor(context, mRevealColor)
        view.background = shapeDrawable
        val lp = LayoutParams(diameter, diameter)
        view.layoutParams = lp
        view.isClickable = true
        view.visibility = View.INVISIBLE
        return view
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val activity = context as Activity
        val fabWidth = mFab.measuredWidth
        val fabHeight = mFab.measuredHeight

        var fabX = right - fabWidth - getDimension(context, R.dimen.fab_margin)
        var fabY = bottom - fabHeight - getDimension(context, R.dimen.fab_margin)
        val fabCenterX = fabX + fabWidth / 2
        val fabCenterY = fabY + fabHeight / 2

        mFollowCircles?.let {
            for (circle in it) {
                val x = fabCenterX - circle.width / 2
                val y = fabCenterY - circle.height / 2
                circle.layout(x, y, x + circle.measuredWidth, y + circle.measuredHeight)
            }
        }
        mRevealCircle?.let {
            val x = fabCenterX - it.width / 2
            val y = fabCenterY - it.height / 2
            it.layout(
                x,
                y,
                x + it.measuredWidth,
                y + it.measuredHeight
            )
        }

        //layout menu items
        layoutMenuItems(left, top, right, bottom)

    }

    private fun layoutMenuItems(left: Int, top: Int, right: Int, bottom: Int) {
        mMenuItemViews?.let {
            val itemHeight = it[0].measuredHeight
            val itemWidth = it[0].measuredWidth
            val itemDiameter = it[0].mDiameter
            val itemRadius = itemDiameter / 2
            val ringRadius = (itemDiameter * 1.6).toInt()
            val containerWidth = measuredWidth
            val containerHeight = measuredHeight

            val ringCenterX = containerWidth / 2
            val ringCenterY = containerHeight / 2

            //layout first item at container center
            val firstX = containerWidth / 2 - itemRadius
            val firstY = containerHeight / 2 - itemRadius
            val firstItem = it[0]
            firstItem.layout(firstX, firstY, firstX + itemWidth, firstY + itemHeight)

            val arcunit = 2 * Math.PI / (mMenuItemCount - 1)

            for (i in 0 until mMenuItemCount - 1) {
                val item = it[i + 1]
                val arc = arcunit * i
                val x = (ringCenterX + ringRadius * Math.sin(arc) - itemRadius).toInt()
                val y =
                    (ringCenterY.toDouble() - ringRadius * Math.cos(arc) - itemRadius.toDouble()).toInt()
                item.layout(x, y, x + item.measuredWidth, y + item.measuredHeight)
            }
        }
    }
}