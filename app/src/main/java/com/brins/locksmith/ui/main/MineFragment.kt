package com.brins.locksmith.ui.main

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModelProvider
import butterknife.OnClick

import com.brins.locksmith.R
import com.brins.locksmith.ui.activity.BaseActivity
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.ui.base.BaseFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.jumpToWebActivity
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel


class MineFragment : BaseFragment() {

    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private lateinit var mSaveCardViewModel: SaveCardViewModel

    companion object {
        val HOW_TO_PROTECT = "file:////android_asset/how-protect.html"
        val PROLICY = "file:////android_asset/privacy-policy.html"
    }

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

    @OnClick(R.id.how_protect_rl,R.id.prolicy_rl)
    fun onClick(v: View) {
        when (v.id) {
            R.id.how_protect_rl -> {
                jumpToWebActivity(activity as BaseActivity, HOW_TO_PROTECT)
            }
            R.id.prolicy_rl -> {
                jumpToWebActivity(activity as BaseActivity, PROLICY)
            }

        }
    }
}
