package com.brins.locksmith.ui.activity

import android.app.Activity
import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.View
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.brins.locksmith.BaseApplication.Companion.context
import com.brins.locksmith.R
import com.brins.locksmith.adapter.AutofillAdapter
import com.brins.locksmith.autofill.module.AutofillFieldMetadata
import com.brins.locksmith.autofill.module.AutofillFieldMetadataCollection
import com.brins.locksmith.autofill.module.AutofillHelper
import com.brins.locksmith.data.AppConfig.AUTO_FILL_FROM_WHERE
import com.brins.locksmith.data.AppConfig.AUTO_FILL_URL
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.ui.dialog.MissPasswordDialogFragment
import com.brins.locksmith.utils.InjectorUtil
import com.brins.locksmith.utils.getStatusBarHeight
import com.brins.locksmith.viewmodel.passport.PassportViewModel
import com.chad.library.adapter.base.model.BaseData
import kotlinx.android.synthetic.main.activity_autofill_selector.*


class AutofillSelectorActivity : BaseActivity() {

    private var url: String? = null
    private var fromwhere: String? = null
    private val matchItems: MutableList<in BaseData> =
        arrayListOf()
    private val mData: MutableList<in BaseData> = arrayListOf()
    private val mAllAdapter = AutofillAdapter(this, mData)
    private val mMatchAdapter = AutofillAdapter(this, matchItems)
    private var mSelectItem: PassWordItem? = null
    private val autofillFields = AutofillFieldMetadataCollection()

    private val mPassportViewModel: PassportViewModel by lazy {
        ViewModelProvider(
            this@AutofillSelectorActivity,
            InjectorUtil.getPassportModelFactory()
        ).get(
            PassportViewModel::class.java
        )
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_autofill_selector
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        toolbar.setPadding(0, getStatusBarHeight(this), 0, 0)
        url = intent.getStringExtra(AUTO_FILL_URL)
        fromwhere = intent.getStringExtra(AUTO_FILL_FROM_WHERE)
        if (mPassportViewModel.loadPassport()) {
            showLoading(getString(R.string.loading))
            mSavePasswordViewModel.mPassWordData.observe(this, Observer {
                hideLoading()
                mData.clear()
                mData.addAll(it)
                mAllAdapter.notifyDataSetChanged()
                matchItems.addAll(mSavePasswordViewModel.getMatchItems(url ?: ""))
                mMatchAdapter.notifyDataSetChanged()
            })
            recycler_match.layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
            recycler_match.adapter = mMatchAdapter
            mMatchAdapter.onItemClickListener = object : AutofillAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(view: View?, position: Int) {
                    mSelectItem = matchItems[position] as PassWordItem
                    setResponseIntent()
                }
            }
            recycler_all.layoutManager =
                GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
            recycler_all.adapter = mAllAdapter
            mAllAdapter.onItemClickListener = object : AutofillAdapter.OnItemClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onItemClick(view: View?, position: Int) {
                    mSelectItem = mData[position] as PassWordItem
                    setResponseIntent()
                }
            }
            initData()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setResponseIntent() {
        mSelectItem?.let {
            val replyIntent = Intent()
            val assistStructure: AssistStructure =
                intent.getParcelableExtra(AutofillManager.EXTRA_ASSIST_STRUCTURE)
            traverseStructure(assistStructure)
            val dataset: Dataset =
                AutofillHelper.buildDataset(this, autofillFields, it)
            replyIntent.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
            setResult(Activity.RESULT_OK, replyIntent)
            finish()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun traverseStructure(assistStructure: AssistStructure) {
        val nodes = assistStructure.windowNodeCount
        for (i in 0 until nodes) {
            val windowNode = assistStructure.getWindowNodeAt(i)
            val viewNode = windowNode.rootViewNode
            traverseNode(viewNode)

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun traverseNode(viewNode: AssistStructure.ViewNode) {
        if (viewNode.className == null)
            return
        if (viewNode.autofillHints == null) {
            if (viewNode.className.contains("EditText") || viewNode.htmlInfo?.tag == "input") {
                var idEntry = viewNode.idEntry ?: ""
                var hint = viewNode.hint ?: ""
                if (idEntry.contains("username")
                    || hint.contains(getString(R.string.auto_hint_phone))
                    || hint.contains(getString(R.string.auto_hint_email))
                    || hint.contains(getString(R.string.auto_hint_username))
                    || hint.contains(getString(R.string.auto_hint_account))
                    || hint.contains(getString(R.string.auto_hint_member))
                ) {
                    val hints = arrayOf(View.AUTOFILL_HINT_USERNAME)
                    val autofillFieldMetadata = AutofillFieldMetadata(viewNode, hints)
                    autofillFields.add(autofillFieldMetadata)
                } else if (idEntry.contains("password") || idEntry.contains("pwd") || hint.contains(
                        getString(R.string.auto_hint_password)
                    )
                ) {

                    val hints = arrayOf(View.AUTOFILL_HINT_PASSWORD)
                    val autofillFieldMetadata = AutofillFieldMetadata(viewNode, hints)
                    autofillFields.add(autofillFieldMetadata)
                }
            }
        } else {
            viewNode.autofillHints?.let { autofillHints ->
                if (viewNode.className.contains("EditText") || viewNode.htmlInfo?.tag == "input" || autofillHints.isNotEmpty()) {
                    autofillFields.add(AutofillFieldMetadata(viewNode))
                }
            }
        }
        val childrenSize = viewNode.childCount
        for (i in 0 until childrenSize) {
            traverseNode(viewNode.getChildAt(i))
        }
    }

    override fun onStart() {
        super.onStart()
        if (mKeyguardManager.isKeyguardSecure) {
            launchFingerAuth()
        } else {
            MissPasswordDialogFragment.showSelf(supportFragmentManager)
        }
    }

    private fun initData() {
        if (mSavePasswordViewModel.hasPassword()) {
            hideLoading()
            mData.addAll(mSavePasswordViewModel.mPassWordData.value!!)
            mAllAdapter.notifyDataSetChanged()

        } else {
            mSavePasswordViewModel.loadPasswordItemAsync(this)
        }
    }

}
