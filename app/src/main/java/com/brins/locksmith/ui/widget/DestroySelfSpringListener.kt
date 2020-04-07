package com.brins.locksmith.data.customview

import android.view.View
import android.view.ViewGroup
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringListener

class DestroySelfSpringListener(
    private val mSpringMenu: ViewGroup?,
    private val view: View,
    private val mInOpen: Boolean
) :
    SpringListener {
    override fun onSpringUpdate(spring: Spring) {

    }

    override fun onSpringAtRest(spring: Spring) {
        val spring = spring
        spring.removeAllListeners()
        spring.destroy()
        if (mSpringMenu != null && !mInOpen) {
            mSpringMenu.removeView(view)
        }
    }

    override fun onSpringActivate(spring: Spring) {

        if (mSpringMenu != null && mSpringMenu.indexOfChild(view) == -1) {
            mSpringMenu.addView(view)
        }
    }

    override fun onSpringEndStateChange(spring: Spring) {

    }

    companion object {
        private val TAG = "DestroySelfSpringListener"
    }
}
