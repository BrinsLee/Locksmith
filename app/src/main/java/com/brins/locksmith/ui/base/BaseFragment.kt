package com.brins.locksmith.ui.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.brins.locksmith.utils.EventBusUtils

abstract class BaseFragment : Fragment(){

    protected var mView: View? = null
    protected lateinit var mActivity: Activity
    protected lateinit var mContext: Context
    protected lateinit var mFragment: Fragment
    private var mUnBinder: Unbinder? = null
//    protected var mStarterCommon: StarterCommon? = null



    private var isInited = false
    private var isFragmentVisible = false
    private var isFirstLoad = true
    // 初始化控件已完成
    private var isPrepared = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
        mContext = context
//        mStarterCommon = StarterCommon(context)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isRegisterEventBus()) {

            EventBusUtils.register(this)
        }
        mFragment = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(getLayoutId(), null)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPrepared = true
        mUnBinder = ButterKnife.bind(this, view)
        initEventAndData()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            isFragmentVisible = true
            lazyLoad()
        } else {
            isFragmentVisible = false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!isInited && !hidden) {
            isInited = true
            initEventAndData()
        }
    }

    private fun lazyLoad() {
        if (!isFragmentVisible || !isPrepared || !isFirstLoad) {
            return
        }
        lazyLoadData()
        isFirstLoad = false
    }

    protected fun lazyLoadData() {

    }

    override fun getContext(): Context {
        return mContext
    }

    protected fun isRegisterEventBus(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnBinder?.unbind()
        isFirstLoad = true
        isPrepared = false
        isFragmentVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRegisterEventBus()) {
            EventBusUtils.unregister(this)
        }
    }

    /*protected fun showProgress() {
        if (mStarterCommon != null) {
            mStarterCommon!!.showUnBackProgressLoading("")
        }
    }

    protected fun hideProgress() {
        if (mStarterCommon != null) {
            mStarterCommon!!.dismissUnBackProgressLoading()
        }
    }*/

    protected abstract fun getLayoutId(): Int

    protected abstract fun initEventAndData()
}