package com.brins.locksmith.autofill.service

import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.brins.locksmith.BaseApplication.Companion.context
import com.brins.locksmith.R
import com.brins.locksmith.autofill.module.*
import com.brins.locksmith.data.AppConfig.FROMAPP
import com.brins.locksmith.data.AppConfig.FROMWEBSITE
import com.brins.locksmith.ui.activity.EditPassActivity
import com.brins.locksmith.ui.activity.Main2Activity
import com.brins.locksmith.ui.activity.MainActivity
import com.brins.locksmith.utils.AccountIconUtil


/**
 * @author lipeilin
 * @date 2020/4/22
 */
@RequiresApi(Build.VERSION_CODES.O)
class LockSmithAutofillService : AutofillService() {


    companion object {
        var fillContext: FillContext? = null
        lateinit var autofillFields: AutofillFieldMetadataCollection
    }

    private var mPackageName: String? = null
    private var webDomain: String? = null
    private var uri: String? = null

    private val autofillFields = AutofillFieldMetadataCollection()
    private val filledAutofillFieldCollection = AutofillFilledFieldCollection()
    private var fromwhere = FROMWEBSITE


    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        //获取屏幕快照数据
        val structure = request.fillContexts[request.fillContexts.size - 1].structure
        val fields: MutableList<ViewNode> = mutableListOf()
        val packageName = structure.activityComponent.packageName
        traverseStructure(structure, fields)
        when (fromwhere) {
            FROMAPP -> {

            }
            FROMWEBSITE -> {
                if (!AutofillHelper.TrustBrowsers.contains(webDomain)) {
                    return
                }
            }
        }

        if (uri.isNullOrBlank()
            || uri.equals("androidapp//:com.brins.locksmith")
            || equals("androidapp://android")
        ) {
            return
        }
        if (autofillFields.autofillIds.size > 0) {
            val responseBuilder = FillResponse.Builder()
            responseBuilder.addDataset(
                AutofillHelper.buildVaultDataset(
                    this,
                    autofillFields,
                    uri!!,
                    fromwhere
                )
            )
            AddSaveInfo(responseBuilder, autofillFields.autofillIds)
            callback.onSuccess(responseBuilder.build())

        } else {
            return
        }

    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val structure = request.fillContexts[request.fillContexts.size - 1].structure
        val fields: MutableList<ViewNode> = mutableListOf()
        val packageName = structure.activityComponent.packageName
        traverseStructure(structure, fields)
        val intent = Intent(context, EditPassActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        intent.putExtra("autofillFrameworkType", autofillFields.saveType)
        intent.putExtra(
            "autofillFrameworkUrl", uri
                ?.replace("https://", "")
                ?.replace("http://", "")
        )
        var username: String
        var password: String
        for (hint in autofillFields.allAutofillHints) {
            val autofillFields = autofillFields.getFieldsForHint(hint) ?: continue
            for (autofillField in autofillFields) {
                val autofillId = autofillField.autofillId
                val autofillType = autofillField.autofillType
                val savedAutofillValue = filledAutofillFieldCollection.hintMap[hint]
                when (autofillType) {
                    SaveInfo.SAVE_DATA_TYPE_USERNAME -> {
                        when (hint) {
                            "username" -> {
                                username = savedAutofillValue?.textValue ?: ""
                                intent.putExtra("autofillFrameworkUsername", username)
                            }
                            "password" -> {
                                password = savedAutofillValue?.textValue ?: ""
                                intent.putExtra("autofillFrameworkPassword", password)
                            }
                        }
                    }
                    SaveInfo.SAVE_DATA_TYPE_PASSWORD -> {
                        when (hint) {
                            "username" -> {
                                username = savedAutofillValue?.textValue ?: ""
                                intent.putExtra("autofillFrameworkUsername", username)
                            }
                            "password" -> {
                                password = savedAutofillValue?.textValue ?: ""
                                intent.putExtra("autofillFrameworkPassword", password)
                            }
                        }

                    }
                    else -> {
                        Log.d("Invalid autofill type", "$autofillType")
                    }

                }
            }
        }
        startActivity(intent)
        if (!AccountIconUtil.saveApkIcons(uri ?: "")) {
            Toast.makeText(
                this,
                "Fail to save the app icon: not such apk find on your devices",
                Toast.LENGTH_LONG
            ).show()
        }
        callback.onSuccess()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun AddSaveInfo(
        responseBuilder: FillResponse.Builder,
        autofillIds: ArrayList<AutofillId>
    ) {
        val type = autofillFields.saveType
        val autofillId = autofillIds.toTypedArray()
        responseBuilder.setSaveInfo(
            SaveInfo.Builder(type, autofillId)
                .setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
                .build()
        )
    }

    private fun traverseStructure(
        structure: AssistStructure,
        fields: MutableList<AssistStructure.ViewNode>
    ) {
        val nodes = structure.windowNodeCount
        for (i in 0 until nodes) {
            val windowNode = structure.getWindowNodeAt(i)
            val viewNode = windowNode.rootViewNode
            traverseNode(viewNode, fields)

        }

    }

    private fun traverseNode(
        viewNode: ViewNode,
        fields: MutableList<ViewNode>
    ) {
        if (viewNode.className == null)
            return
        if (viewNode.autofillHints == null) {
            if (viewNode.className.contains("EditText") || viewNode.htmlInfo?.tag == "input") {
                setPackageAndDomain(viewNode)
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
                    filledAutofillFieldCollection.add(AutofillFilledField(viewNode, hints))
                    fields.add(viewNode)
                    autofillFields.add(autofillFieldMetadata)
                } else if (idEntry.contains("password") || idEntry.contains("pwd") || hint.contains(
                        getString(R.string.auto_hint_password)
                    )
                ) {
                    val hints = arrayOf(View.AUTOFILL_HINT_PASSWORD)
                    val autofillFieldMetadata = AutofillFieldMetadata(viewNode, hints)
                    fields.add(viewNode)
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
            traverseNode(viewNode.getChildAt(i), fields)
        }
    }

    private fun setPackageAndDomain(viewNode: ViewNode) {
        if (mPackageName.isNullOrBlank() && !viewNode.idPackage.isNullOrBlank()) {
            mPackageName = viewNode.idPackage
            Log.d("packageName", packageName)
        }
        if (webDomain.isNullOrBlank() && !viewNode.webDomain.isNullOrBlank()) {
            webDomain = viewNode.webDomain
            Log.d("webDomain", webDomain)
        }
        if (uri.isNullOrBlank()) {
            if (!webDomain.isNullOrBlank()) {
                uri = "http://$webDomain"
            } else {
                uri = mPackageName
                fromwhere = FROMAPP
            }
        }
    }


}