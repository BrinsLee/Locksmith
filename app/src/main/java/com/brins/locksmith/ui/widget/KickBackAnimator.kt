package com.brins.locksmith.ui.widget

import android.animation.TypeEvaluator

class KickBackAnimator : TypeEvaluator<Float> {
    private val s = 1.70158f
    var mDuration = 0f
    fun setDuration(duration: Float) {
        mDuration = duration
    }

    override fun evaluate(
        fraction: Float,
        startValue: Float,
        endValue: Float
    ): Float {
        val t = mDuration * fraction
        val c = endValue - startValue
        val d = mDuration
        return calculate(t, startValue, c, d)
    }

    fun calculate(
        t: Float,
        b: Float,
        c: Float,
        d: Float
    ): Float {
        var t = t
        return c * ((t / d - 1.also { t = it.toFloat() }) * t * ((s + 1) * t + s) + 1) + b
    }
}