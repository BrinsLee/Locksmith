package com.brins.locksmith.ui.main

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.brins.locksmith.R
import com.brins.locksmith.adapter.BaseMainAdapter
import com.brins.locksmith.data.GeneralTitleItem
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.databinding.FragmentMainBinding
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.ui.base.BaseDBFragment
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.ui.base.BaseMainItemType.ITEM_TITLE_CARD
import com.brins.locksmith.ui.base.BaseMainItemType.ITEM_TITLE_CERTIFICATE
import com.brins.locksmith.utils.EventMessage
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.fragment_main.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainFragment : BaseDBFragment<FragmentMainBinding>(), View.OnClickListener,
    GeneralTitleItem.onExpendListener {


    private lateinit var mSavePasswordViewModel: SavePasswordViewModel
    private lateinit var mSaveCardViewModel: SaveCardViewModel

    private val mAdapter = BaseMainAdapter()
    private val mData: MutableList<in BaseData> = mutableListOf()
    private lateinit var mPassTitleItem: GeneralTitleItem
    private lateinit var mCardTitleItem: GeneralTitleItem
    private lateinit var mCertificate: GeneralTitleItem

    override fun getLayoutId(): Int {
        return R.layout.fragment_main
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSavePasswordViewModel = (activity as MainActivity).getSavePasswordViewModel()
        mSaveCardViewModel = (activity as MainActivity).getSaveCardViewModel()
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    override fun initEventAndData() {
        main_recycler.layoutManager =
            GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        mPassTitleItem = GeneralTitleItem(
            getString(R.string.password),
            type = BaseMainItemType.ITEM_TITLE_PASS
        ).setListener(this)
        mCardTitleItem = GeneralTitleItem(
            getString(R.string.bank),
            type = ITEM_TITLE_CARD
        ).setListener(this)
        mCertificate = GeneralTitleItem(
            getString(R.string.certificate),
            type = ITEM_TITLE_CERTIFICATE
        ).setListener(this)

        mAdapter.setEnableRefresh(false)
        main_recycler.setHasFixedSize(true)
        mAdapter.setOnLoadDataListener { _, _, onLoadDataCompleteCallback ->
            if (mData.isNotEmpty()) {
                mData.clear()
            }
            mData.add(mPassTitleItem)
            mData.addAll(mSavePasswordViewModel.loadPasswordItem())
            mData.add(mCardTitleItem)
            mData.addAll(mSaveCardViewModel.loadCardItem())
            mData.add(mCertificate)
            onLoadDataCompleteCallback.onLoadDataSuccess(mData as? List<BaseData>)
        }
        main_recycler.adapter = mAdapter
        mSavePasswordViewModel.mPassWordData.observe(this,
            Observer {
                if (mAdapter.data.isNotEmpty()) {
                    val mCardSize = mSaveCardViewModel.mCardData.value?.size ?: 0
                    val mPassWordSize = mData.size - mCardSize - 3
                    if (it.size > mPassWordSize){
                        //添加
                        mAdapter.addData(it.size, it[it.size - 1])
                        mData.add(it.size, it[it.size - 1])
                    }
                }
            })
        mSaveCardViewModel.mCardData.observe(this, Observer {
            if (mAdapter.data.isNotEmpty()) {
                mAdapter.addData(mSavePasswordViewModel.dataSize() + it.size + 1, it[it.size - 1])
                mData.add(mSavePasswordViewModel.dataSize() + it.size + 1, it[it.size - 1])
            }
        })
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
            ITEM_TITLE_CARD -> {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshPassWordData(message: EventMessage<*>) {
        when (message.code) {
            EventMessage.CODE_UPDATE_PASSWORD -> mAdapter.notifyItemChanged(message.data as Int + 1)
            EventMessage.CODE_UPDATE_BANK -> {
                Log.d(
                    "bank:",
                    "${message.data as Int},size: ${(mSavePasswordViewModel.mPassWordData.value?.size
                        ?: 0)}"
                )
                val pos = message.data as Int + (mSavePasswordViewModel.mPassWordData.value?.size
                    ?: 0) + 2
                mData[pos] = mSaveCardViewModel.mCardData.value?.get(message.data as Int)!!
                mAdapter.setNewData(mData as? List<BaseData>)
                mAdapter.notifyItemChanged(pos)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun deleteData(message: EventMessage<*>) {
        when (message.code) {
            EventMessage.CODE_DELETE_PASSWORD ->{
                val pos = message.data as Int + 1
                mData.removeAt(pos)
                mAdapter.remove(pos)
                mAdapter.notifyItemRemoved(pos)
            }
            EventMessage.CODE_DELETE_BANK -> {
                Log.d(
                    "bank:",
                    "${message.data as Int},size: ${(mSavePasswordViewModel.mPassWordData.value?.size
                        ?: 0)}"
                )
                val pos = message.data as Int + (mSavePasswordViewModel.mPassWordData.value?.size
                    ?: 0) + 2
                mData.removeAt(pos)
                mAdapter.remove(pos)
                mAdapter.notifyItemRemoved(pos)
            }
        }
    }
}


