package com.brins.locksmith.ui.main

import android.content.Context
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.brins.locksmith.R
import com.brins.locksmith.adapter.BaseMainAdapter
import com.brins.locksmith.data.GeneralTitleItem
import com.brins.locksmith.databinding.FragmentMainBinding
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.ui.base.BaseDBFragment
import com.brins.locksmith.utils.EventMessage
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class MainFragment : BaseDBFragment<FragmentMainBinding>(), View.OnClickListener,
    GeneralTitleItem.onExpendListener {


    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private val mAdapter = BaseMainAdapter()
    private val mData: MutableList<in BaseData> = mutableListOf()

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
            if (mData.isNotEmpty()) {
                mData.clear()
            }
            mData.add(GeneralTitleItem(getString(R.string.password)).setListener(this))
            mData.addAll(mSavePasswordViewModel.loadPasswordItem())
            onLoadDataCompleteCallback.onLoadDataSuccess(mData as? List<BaseData>)
        }
        main_recycler.adapter = mAdapter
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshPassWordData(message: EventMessage<*>) {
        when (message.code) {
            EventMessage.CODE_UPDATE_PASSWORD -> initEventAndData()

        }
    }

    override fun onClick(v: View) {

    }

    override fun onExpend(view: View, expend: Boolean) {
        if (mSavePasswordViewModel.hasPassword()) {
            if (expend) {
                for (password in mSavePasswordViewModel.mPassWordData.value!!) {
                    mData.remove(password)
                }
            } else {
                mData.addAll(mSavePasswordViewModel.mPassWordData.value!!)
            }
            mAdapter.setNewData(mData as? List<BaseData>)
            mAdapter.notifyDataSetChanged()
        }
    }

}
