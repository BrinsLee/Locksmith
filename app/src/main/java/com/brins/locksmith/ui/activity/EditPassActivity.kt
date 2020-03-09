package com.brins.locksmith.ui.activity

import android.content.Intent
import com.brins.locksmith.R

class EditPassActivity : BaseActivity() {


    companion object{
        fun startThis(activity: BaseActivity){
            val intent = Intent(activity, EditPassActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_edit_pass
    }
}
