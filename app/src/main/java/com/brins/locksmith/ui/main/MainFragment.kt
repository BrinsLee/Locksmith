package com.brins.locksmith.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.brins.locksmith.R
import com.brins.locksmith.databinding.FragmentMainBinding
import com.brins.ncov_2019.ui.base.BaseDBFragment


class MainFragment : BaseDBFragment<FragmentMainBinding>() {


    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun initEventAndData() {

    }

}
