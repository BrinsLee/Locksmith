package com.brins.locksmith.ui.activity

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.brins.locksmith.R
import com.brins.locksmith.data.customview.OnMenuActionListener
import com.brins.locksmith.ui.widget.FloatingActionMenu
import com.brins.locksmith.ui.widget.MoreWindow
import com.brins.locksmith.utils.getDimension
import com.brins.locksmith.utils.getStatusBarHeight
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton.SIZE_MINI
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

/*    private val frameAnimRes = intArrayOf(
        R.mipmap.compose_anim_1,
        R.mipmap.compose_anim_2,
        R.mipmap.compose_anim_3,
        R.mipmap.compose_anim_4,
        R.mipmap.compose_anim_5,
        R.mipmap.compose_anim_6,
        R.mipmap.compose_anim_7,
        R.mipmap.compose_anim_8,
        R.mipmap.compose_anim_9,
        R.mipmap.compose_anim_10,
        R.mipmap.compose_anim_11,
        R.mipmap.compose_anim_12,
        R.mipmap.compose_anim_13,
        R.mipmap.compose_anim_14,
        R.mipmap.compose_anim_15,
        R.mipmap.compose_anim_15,
        R.mipmap.compose_anim_16,
        R.mipmap.compose_anim_17,
        R.mipmap.compose_anim_18,
        R.mipmap.compose_anim_19
    )*/
    private var springFloatingActionMenu: FloatingActionMenu? = null

    private val frameDuration = 20
    private var frameAnim: AnimationDrawable? = null
    private var frameReverseAnim: AnimationDrawable? = null
    var mMoreWindow: MoreWindow? = null


    companion object {
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

/*        actionbar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_menu_black)
            it.setDisplayHomeAsUpEnabled(true)
        }
        navigator.setNavigationItemSelectedListener {
            it.isChecked = true
            drawable.closeDrawers()
            true
        }*/
        toolbar.setPadding(0, getStatusBarHeight(this),0,0)
/*        createFabFrameAnim()
        createFabReverseFrameAnim()
        initFab()*/
    }

    private fun showMoreWindow(view: View) {
        if (null == mMoreWindow) {
            mMoreWindow = MoreWindow(this)
        }
        mMoreWindow!!.showMoreWindow(view, 100)
    }


    /***初始化浮动按钮*/
   /* private fun initFab() {
        val fab = FloatingActionButton(this)

        fab.size = SIZE_MINI
        fab.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        fab.setImageDrawable(frameAnim)
        springFloatingActionMenu = FloatingActionMenu.Companion.Builder(this)
            .addFab(fab)
            .addMenuItem(
                R.color.password,
                R.drawable.ic_password_fab,
                getString(R.string.passport),
                onClickListener = View.OnClickListener {
                    springFloatingActionMenu!!.hide()
                    EditPassActivity.startThis(this@MainActivity)
                }
            )
            .addMenuItem(
                R.color.bank_card,
                R.drawable.ic_bank_card,
                getString(R.string.bank),
                onClickListener = this@MainActivity
            )
            .addMenuItem(
                R.color.password,
                R.drawable.ic_password_fab,
                getString(R.string.passport),
                onClickListener = this@MainActivity
            )
            .addMenuItem(
                R.color.bank_card,
                R.drawable.ic_bank_card,
                getString(R.string.bank),
                onClickListener = this@MainActivity
            )
            .revealColor(R.color.colorPrimary)
            .gravity(Gravity.RIGHT or Gravity.BOTTOM)
            .addMargin(
                intArrayOf(
                    0,
                    0,
                    getDimension(this, R.dimen.fab_margin),
                    getDimension(this, R.dimen.fab_margin)
                )
            )
            .onMenuActionListner(object : OnMenuActionListener {
                override fun onMenuOpen() {
                    fab.setImageDrawable(frameAnim)
                    frameReverseAnim?.stop()
                    frameAnim?.start()
                }

                override fun onMenuClose() {
                    fab.setImageDrawable(frameReverseAnim)
                    frameAnim?.stop()
                    frameReverseAnim?.start()
                }

            })
            .build()
    }*/

    /***创建浮动按钮重置的帧动画*/
    /*private fun createFabReverseFrameAnim() {
        frameReverseAnim = AnimationDrawable()
        val resources = resources
        frameReverseAnim?.let {
            it.isOneShot = true
            for (i in frameAnimRes.indices.reversed()) {
                it.addFrame(resources.getDrawable(frameAnimRes[i], null), frameDuration)
            }
        }

    }

    *//***创建浮动按钮打开的帧动画*//*
    private fun createFabFrameAnim() {
        frameAnim = AnimationDrawable()
        val resources = resources
        frameAnim?.let {
            it.isOneShot = true
            for (res in frameAnimRes) {
                it.addFrame(resources.getDrawable(res, null), frameDuration)
            }
        }
    }*/

    @OnClick(R.id.tab_photo_ll,R.id.tab_photo_btn)
     fun onClick(v: View) {
        showMoreWindow(v)
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
}
