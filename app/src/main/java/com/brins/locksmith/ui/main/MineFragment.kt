package com.brins.locksmith.ui.main

import androidx.lifecycle.ViewModelProvider

import com.brins.locksmith.R
import com.brins.locksmith.ui.base.BaseFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel


class MineFragment: BaseFragment() {

    private val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this@MineFragment, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_mine
    }

    override fun initEventAndData() {

    }

}
