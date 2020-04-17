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
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.ui.base.BaseMainItemType.ITEM_TITLE_CARD
import com.brins.locksmith.utils.EventMessage
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class MainFragment : BaseDBFragment<FragmentMainBinding>(), View.OnClickListener,
    GeneralTitleItem.onExpendListener {


    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private lateinit var mSaveCardViewModel: SaveCardViewModel

    private val mAdapter = BaseMainAdapter()
    private val mData: MutableList<in BaseData> = mutableListOf()
    private lateinit var mPassTitleItem : GeneralTitleItem
    private lateinit var mCardTitleItem : GeneralTitleItem

    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSavePasswordViewModel = (activity as MainActivity).getSavePasswordViewModel()
        mSaveCardViewModel = (activity as MainActivity).getSaveCardViewModel()
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
        mPassTitleItem = GeneralTitleItem(
            getString(R.string.password),
            type = BaseMainItemType.ITEM_TITLE_PASS
        ).setListener(this)
        mCardTitleItem = GeneralTitleItem(
            getString(R.string.bank),
            type = ITEM_TITLE_CARD
        ).setListener(this)
        mAdapter.setEnableRefresh(true)
        main_recycler.setHasFixedSize(true)
        mAdapter.setOnLoadDataListener { _, _, onLoadDataCompleteCallback ->
            if (mData.isNotEmpty()) {
                mData.clear()
            }
            mData.add(mPassTitleItem)
            mData.addAll(mSavePasswordViewModel.loadPasswordItem())
            mData.add(mCardTitleItem)
            mData.addAll(mSaveCardViewModel.loadCardItem())
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

    override fun onExpend(view: View, expend: Boolean, type: Int) {
        when (type) {
            BaseMainItemType.ITEM_TITLE_PASS -> {
                if (mSavePasswordViewModel.hasPassword()) {
                    if (expend) {
                        for (password in mSavePasswordViewModel.mPassWordData.value!!) {
                            mData.remove(password)
                        }
                    } else {
                        for ((i, password) in mSavePasswordViewModel.mPassWordData.value!!.withIndex()) {
                            mData.add(i + 1, password)
                        }
                    }
                }
            }
            BaseMainItemType.ITEM_TITLE_CARD -> {
                if (mSaveCardViewModel.hasPassword()) {
                    if (expend) {
                        for (password in mSaveCardViewModel.mCardData.value!!) {
                            mData.remove(password)
                        }
                    } else {
                        for ((i, password) in mSaveCardViewModel.mCardData.value!!.withIndex()) {
                            mData.add(i + 1 + mData.indexOf(mCardTitleItem), password)
                        }
                    }
                }
            }
        }

        mAdapter.setNewData(mData as? List<BaseData>)
        mAdapter.notifyDataSetChanged()
    }
}


