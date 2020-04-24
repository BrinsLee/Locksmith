package com.brins.locksmith.ui.activity

import android.os.Bundle
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.AUTO_FILL_FROM_WHERE
import com.brins.locksmith.data.AppConfig.AUTO_FILL_URL

class AutofillSelectorActivity : BaseActivity() {

    private var url: String? = null
    private var fromwhere: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_autofill_selector
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        url = intent.getStringExtra(AUTO_FILL_URL)
        fromwhere = intent.getStringExtra(AUTO_FILL_FROM_WHERE)
        
    }


}
