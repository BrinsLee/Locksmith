package com.brins.locksmith.ui.activity

import android.content.Intent
import android.os.Bundle
import com.brins.locksmith.R
import com.brins.locksmith.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.activity_edit_pass.*

class EditPassActivity : BaseActivity() {


    companion object {
        fun startThis(activity: BaseActivity) {
            val intent = Intent(activity, EditPassActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_edit_pass
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        header_layout.setPadding(0, getStatusBarHeight(this), 0, 0)
    }
}
