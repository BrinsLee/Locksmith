package com.brins.locksmith.ui.main

import android.content.Context
import androidx.lifecycle.ViewModelProvider

import com.brins.locksmith.R
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.ui.base.BaseFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel


class MineFragment: BaseFragment() {

    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private lateinit var mSaveCardViewModel: SaveCardViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSavePasswordViewModel = (activity as MainActivity).getSavePasswordViewModel()
        mSaveCardViewModel = (activity as MainActivity).getSaveCardViewModel()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_mine
    }

    override fun initEventAndData() {

    }

}
