package com.brins.locksmith.data.customview

import android.view.View
import androidx.annotation.ColorRes

class MenuItem {
    @ColorRes
    var bgColor: Int = 0
    var icon: Int = 0
    var label: String? = null
    @ColorRes
    var textColor = android.R.color.white
    var diameter = 50
    var onClickListener: View.OnClickListener? = null

    constructor(
        bgColor: Int,
        icon: Int,
        label: String,
        textColor: Int,
        onClickListener: View.OnClickListener
    ) {
        this.bgColor = bgColor
        this.icon = icon
        this.label = label
        this.textColor = textColor
        this.onClickListener = onClickListener
    }

    constructor(
        bgColor: Int,
        icon: Int,
        label: String,
        textColor: Int,
        diameter: Int,
        onClickListener: View.OnClickListener
    ) {
        this.bgColor = bgColor
        this.icon = icon
        this.label = label
        this.textColor = textColor
        this.diameter = diameter
        this.onClickListener = onClickListener
    }

    constructor(bgColor: Int) {
        this.bgColor = bgColor
    }

    constructor(bgColor: Int, icon: Int, label: String) {
        this.bgColor = bgColor
        this.icon = icon
        this.label = label
    }
}