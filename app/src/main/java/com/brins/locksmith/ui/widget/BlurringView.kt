package com.brins.locksmith.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import com.brins.locksmith.R
import com.brins.locksmith.utils.doBlur
import java.lang.Exception


/**
 * @author lipeilin
 * @date 2020/4/24
 */
class BlurringView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context) : this(context, null)

    private var defaultBlurRadius = 11
    private var defaultDownsampleFactor = 6
    private var defaultOverlayColor = Color.parseColor("#50FFFFFF")
    private var mRenderScript: RenderScript? = null
    private var mBlurScript: ScriptIntrinsicBlur? = null
    private var mDownsampleFactor = 0
    private var mDownsampleFactorChanged = false
    private var mOverlayColor = 0
    private lateinit var mBlurredView: View
    private var mBlurredViewWidth = 0
    private var mBlurredViewHeight: Int = 0
    private var mBlurringCanvas: Canvas? = null
    private var mBitmapToBlur: Bitmap? = null
    private var mBlurredBitmap: Bitmap? = null
    private var mBlurInput: Allocation? = null
    private var mBlurOutput: Allocation? = null


    init {
        initializeRenderScript(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.PxBlurringView)
        setBlurRadius(a.getInt(R.styleable.PxBlurringView_blur_radius, defaultBlurRadius))
        setDownsampleFactor(
            a.getInt(
                R.styleable.PxBlurringView_sample_factor,
                defaultDownsampleFactor
            )
        )
        setOverlayColor(a.getColor(R.styleable.PxBlurringView_overlay_color, defaultOverlayColor))
        a.recycle()
    }

    private fun setOverlayColor(color: Int) {
        mOverlayColor = color
    }

    private fun setDownsampleFactor(factor: Int) {
        if (factor <= 0) {
            throw IllegalArgumentException("Downsample factor must be greater than 0.")
        }

        if (mDownsampleFactor != factor) {
            mDownsampleFactor = factor
            mDownsampleFactorChanged = true
        }

    }

    private fun setBlurRadius(radius: Int) {
        mBlurScript!!.setRadius(radius.toFloat())
    }

    private fun initializeRenderScript(context: Context) {
        mRenderScript = RenderScript.create(context)
        mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript))

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBlurredView != null) {
            if (prepare()) {
                if (mBlurredView.background != null && mBlurredView.background is ColorDrawable){
                    mBitmapToBlur!!.eraseColor((mBlurredView.background as ColorDrawable).color)

                }else{
                    mBitmapToBlur!!.eraseColor(Color.TRANSPARENT)
                }
                mBlurredView.draw(mBlurringCanvas)
                blur()
                canvas.save()
                canvas.translate(mBlurredView.x - x, mBlurredView.y - y)
                canvas.scale(mDownsampleFactor.toFloat(), mDownsampleFactor.toFloat())
                canvas.drawBitmap(mBlurredBitmap, 0f, 0f, null)
                canvas.restore()
            }
            canvas.drawColor(mOverlayColor)
        }
    }

    private fun blur(){
        mBlurInput!!.copyTo(mBitmapToBlur)
        mBlurScript!!.setInput(mBlurInput)
        mBlurScript!!.forEach(mBlurOutput)
        mBlurOutput!!.copyTo(mBlurredBitmap)
    }

    private fun prepare(): Boolean {
        val width = mBlurredView?.width ?: 0
        val height = mBlurredView?.height ?: 0
        if (mBlurringCanvas == null || mDownsampleFactorChanged
            || mBlurredViewWidth != width || mBlurredViewHeight != height) {

            mDownsampleFactorChanged = false
            mBlurredViewWidth = width
            mBlurredViewHeight = height

            var scaledWidth = width / mDownsampleFactor
            var scaledHeight = height / mDownsampleFactor
            scaledWidth = scaledWidth - scaledWidth % 4 + 4
            scaledHeight = scaledHeight - scaledHeight % 4 + 4

            if (mBlurredBitmap == null
                || mBlurredBitmap?.width != scaledWidth
                || mBlurredBitmap?.height != scaledHeight) {

                mBitmapToBlur = Bitmap.createBitmap(
                    scaledWidth, scaledHeight,
                    Bitmap.Config.ARGB_8888
                )
                if (mBitmapToBlur == null) {
                    return false
                }
                mBlurredBitmap = Bitmap.createBitmap(
                    scaledWidth, scaledHeight,
                    Bitmap.Config.ARGB_8888
                )

                if (mBlurredBitmap == null) {
                    return false
                }
            }
            mBlurringCanvas = Canvas(mBitmapToBlur)
            mBlurringCanvas?.scale(1f / mDownsampleFactor, 1f / mDownsampleFactor)
            mBlurInput = Allocation.createFromBitmap(
                mRenderScript, mBitmapToBlur,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
            )
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput!!.type)

        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mRenderScript?.destroy()
    }

    fun setBlurredView(blurredView : View){
        mBlurredView = blurredView
    }
}
