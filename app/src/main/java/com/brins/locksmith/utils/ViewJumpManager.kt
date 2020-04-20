package com.brins.locksmith.utils

import android.content.Intent
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.ui.activity.EditPassActivity
import com.brins.locksmith.ui.activity.EditPassActivity.Companion.DATA_POS
import com.brins.locksmith.ui.activity.EditPassActivity.Companion.TYPE_FROM_WHERE

/**
 * @author lipeilin
 * @date 2020/4/20
 */
fun jumpToEditActivity(pos: Int, from: Int) {
    val context = BaseApplication.context
    val intent = Intent(context, EditPassActivity::class.java)
    intent.putExtra(DATA_POS, pos)
    intent.putExtra(TYPE_FROM_WHERE, from)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}