package com.brins.locksmith.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.adapter.MainPagerAdapter
import com.brins.locksmith.ui.main.MainFragment
import com.brins.locksmith.ui.main.PasswordFragment
import com.brins.locksmith.ui.widget.MoreWindow
import com.brins.locksmith.utils.getStatusBarHeight
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.view_common_toolbar.*

class MainActivity : BaseActivity() {

    var mMoreWindow: MoreWindow? = null
    private var list = mutableListOf<Fragment>()
    private val adapter by lazy { MainPagerAdapter(supportFragmentManager, list) }


    companion object {
        private val TAB_MAIN = 0
        private val TAB_FIND = 1
        private val TAB_MESSAGE = 2
        private val TAB_MINE = 3
        fun startThis(activity: AppCompatActivity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }


    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        ButterKnife.bind(this)
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0)
        initData()
        initView()
    }

    private fun initData() {
        list.add(MainFragment())
        list.add(PasswordFragment())
        list.add(PasswordFragment())
        list.add(PasswordFragment())
    }

    private fun initView() {
        viewpager.adapter = adapter
        viewpager.offscreenPageLimit = 3
        changeTab(0)
    }

    private fun showMoreWindow(view: View) {
        if (null == mMoreWindow) {
            mMoreWindow = MoreWindow(this)
        }
        mMoreWindow!!.showMoreWindow(view, 100)
    }


    @OnClick(
        R.id.tab_add_ll, R.id.tab_add_btn,
        R.id.tab_main_ll, R.id.tab_main_btn, R.id.tab_main_tv,
        R.id.tab_find_ll, R.id.tab_find_btn, R.id.tab_find_tv,
        R.id.tab_message_btn, R.id.tab_message_ll, R.id.tab_message_tv,
        R.id.tab_my_ll, R.id.tab_my_btn, R.id.tab_my_tv
    )
    fun onClickView(v: View) {
        when (v.id) {
            R.id.tab_add_ll, R.id.tab_add_btn -> showMoreWindow(v)
            R.id.tab_main_ll, R.id.tab_main_btn, R.id.tab_main_tv -> changeTab(TAB_MAIN)
            R.id.tab_find_ll, R.id.tab_find_btn, R.id.tab_find_tv -> changeTab(TAB_FIND)
            R.id.tab_message_btn, R.id.tab_message_ll, R.id.tab_message_tv -> changeTab(TAB_MESSAGE)
            R.id.tab_my_ll, R.id.tab_my_btn, R.id.tab_my_tv -> changeTab(TAB_MINE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawable.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeTab(position: Int) {
        tab_main_btn.isSelected = false
        tab_main_tv.isSelected = false
        tab_find_btn.isSelected = false
        tab_find_tv.isSelected = false
        tab_message_btn.isSelected = false
        tab_message_tv.isSelected = false
        tab_my_btn.isSelected = false
        tab_my_tv.isSelected = false

        when (position) {
            0 -> {
                tab_main_btn.isSelected = true
                tab_main_tv.isSelected = true
                toolbar.text = getString(R.string.main_tab_title)
            }
            1 -> {
                tab_find_tv.isSelected = true
                tab_find_btn.isSelected = true
                toolbar.text = getString(R.string.find_tab_title)
            }
            2 -> {
                tab_message_btn.isSelected = true
                tab_message_tv.isSelected = true
                toolbar.text = getString(R.string.message_tab_title)

            }
            3 -> {
                tab_my_btn.isSelected = true
                tab_my_tv.isSelected = true
                toolbar.text = getString(R.string.my_tab_title)
            }
        }
        viewpager.currentItem = position
    }

    fun getSavePasswordViewModel(): SavePasswordViewModel {
        return mSavePasswordViewModel
    }

    fun getSaveCardViewModel(): SaveCardViewModel {
        return mSaveCardViewModel
    }
}
