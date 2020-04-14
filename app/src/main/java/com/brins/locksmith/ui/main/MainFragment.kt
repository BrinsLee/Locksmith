package com.brins.locksmith.ui.main

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.brins.locksmith.R
import com.brins.locksmith.adapter.BaseMainAdapter
import com.brins.locksmith.data.PassWordItem
import com.brins.locksmith.databinding.FragmentMainBinding
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.ui.activity.MainActivity_ViewBinding
import com.brins.locksmith.ui.base.BaseDBFragment
import com.brins.locksmith.ui.widget.GridSpacingItemDecoration
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.dip2px
import com.brins.locksmith.utils.dpToPx
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.chad.library.adapter.base.OnLoadDataCompleteCallback
import com.chad.library.adapter.base.OnLoadDataListener
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseDBFragment<FragmentMainBinding>() {


    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private val mAdapter = BaseMainAdapter()

    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSavePasswordViewModel = (activity as MainActivity).getSavePasswordViewModel()
    }

    override fun initEventAndData() {
        main_recycler.layoutManager =
            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
/*        main_recycler.addItemDecoration(
            GridSpacingItemDecoration(
                4,
                dip2px(10f), true
            )
        )*/
        mAdapter.setEnableRefresh(true)
        main_recycler.setHasFixedSize(true)
        mAdapter.setOnLoadDataListener { _, _, onLoadDataCompleteCallback ->
            val data = mSavePasswordViewModel.loadPasswordItem()
            onLoadDataCompleteCallback.onLoadDataSuccess(data as List<BaseData>?)
        }
        main_recycler.adapter = mAdapter
    }

}
