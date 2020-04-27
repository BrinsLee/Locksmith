package com.brins.locksmith.ui.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.adapter.MainPagerAdapter
import com.brins.locksmith.ui.base.BaseMainItemType
import com.brins.locksmith.ui.main.MainFragment
import com.brins.locksmith.ui.main.MineFragment
import com.brins.locksmith.ui.widget.KickBackAnimator
import com.brins.locksmith.utils.getScreenHeight
import com.brins.locksmith.utils.getStatusBarHeight
import com.brins.locksmith.viewmodel.card.SaveCardViewModel
import com.brins.locksmith.viewmodel.save.SavePasswordViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.widget_add_pass_view.*

class MainActivity : BaseActivity() {

    private var list = mutableListOf<Fragment>()
    private val adapter by lazy { MainPagerAdapter(supportFragmentManager, list) }
    private val mHandler = Handler()


    companion object {
        private val TAB_MAIN = 0
        //        private val TAB_FIND = 1
//        private val TAB_MESSAGE = 2
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
        list.add(MineFragment())
    }

    private fun initView() {
        viewpager.adapter = adapter
        viewpager.offscreenPageLimit = 3
        changeTab(0)
    }


    private fun showAnimation() {
        for (i in 0 until add_view_root.childCount) {
            val child = add_view_root.getChildAt(i)
            mHandler.postDelayed({
                add_view_root.visibility = View.VISIBLE
                child.visibility = View.VISIBLE
                val fadeAnim: ValueAnimator =
                    ObjectAnimator.ofFloat(child, "translationY", 600f, 0f)
                fadeAnim.duration = 300
                val kickAnimator = KickBackAnimator()
                kickAnimator.setDuration(150f)
                fadeAnim.setEvaluator(kickAnimator)
                fadeAnim.start()
            }, i * 50.toLong())
        }
    }

    private fun closeAnimation() {
        add_view_root.visibility = View.VISIBLE
        for (i in 0 until add_view_root.childCount) {
            val child = add_view_root.getChildAt(i)
            mHandler.postDelayed({
                val fadeAnim: ValueAnimator =
                    ObjectAnimator.ofFloat(child, "translationY", getScreenHeight() / 2f, 600f)
                fadeAnim.duration = 200
                val kickAnimator = KickBackAnimator()
                kickAnimator.setDuration(100f)
                fadeAnim.setEvaluator(kickAnimator)
                fadeAnim.start()
                fadeAnim.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) { // TODO Auto-generated method stub
                    }

                    override fun onAnimationRepeat(animation: Animator) { // TODO Auto-generated method stub
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        child.visibility = View.INVISIBLE
                        if (child.id == R.id.center_music_window_close) {
                            add_view_root.visibility = View.INVISIBLE

                        }
                    }

                    override fun onAnimationCancel(animation: Animator) { // TODO Auto-generated method stub
                    }
                })

            }, (add_view_root.childCount - i - 1) * 30.toLong())
        }
    }

    @OnClick(
        R.id.tab_add_ll, R.id.tab_add_btn,
        R.id.tab_main_ll, R.id.tab_main_btn, R.id.tab_main_tv,
        R.id.tab_find_ll, R.id.tab_find_btn, R.id.tab_find_tv,
        R.id.tab_message_btn, R.id.tab_message_ll, R.id.tab_message_tv,
        R.id.tab_my_ll, R.id.tab_my_btn, R.id.tab_my_tv, R.id.center_music_window_close,
        R.id.more_window_password,R.id.more_window_bank
    )
    fun onClickView(v: View) {
        when (v.id) {
            R.id.tab_add_ll, R.id.tab_add_btn -> showAnimation()
            R.id.tab_main_ll, R.id.tab_main_btn, R.id.tab_main_tv -> changeTab(TAB_MAIN)
/*            R.id.tab_find_ll, R.id.tab_find_btn, R.id.tab_find_tv ->
            R.id.tab_message_btn, R.id.tab_message_ll, R.id.tab_message_tv ->*/
            R.id.more_window_password -> {
                closeAnimation()
                EditPassActivity.startThis(this)
            }
            R.id.more_window_bank -> {
                closeAnimation()
                EditPassActivity.startThis(this, BaseMainItemType.ITEM_NORMAL_CARD)
            }
            R.id.tab_my_ll, R.id.tab_my_btn, R.id.tab_my_tv -> changeTab(TAB_MINE)
            R.id.center_music_window_close -> closeAnimation()
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
