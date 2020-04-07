package com.brins.locksmith.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brins.locksmith.R
import com.brins.locksmith.adapter.BaseMainAdapter
import com.brins.locksmith.data.PassWordItem
import com.brins.locksmith.databinding.FragmentMainBinding
import com.brins.locksmith.ui.base.BaseDBFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.chad.library.adapter.base.OnLoadDataCompleteCallback
import com.chad.library.adapter.base.OnLoadDataListener
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseDBFragment<FragmentMainBinding>() {


    private val mSavePasswordViewModel: SavePasswordViewModel by lazy {
        ViewModelProvider(this@MainFragment, InjectorUtil.getPassWordFactory()).get(
            SavePasswordViewModel::class.java
        )
    }
    private val mAdapter = BaseMainAdapter()

    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun initEventAndData() {
        main_recycler.layoutManager = LinearLayoutManager(context)
        mSavePasswordViewModel.mPassWordData
            .observe(this@MainFragment,
                Observer<ArrayList<PassWordItem>> {
                    mAdapter.setOnLoadDataListener { _, _, onLoadDataCompleteCallback ->
                        onLoadDataCompleteCallback.onLoadDataSuccess(it as List<BaseData>)
                    }
                    main_recycler.adapter = mAdapter
                })

    }

}
