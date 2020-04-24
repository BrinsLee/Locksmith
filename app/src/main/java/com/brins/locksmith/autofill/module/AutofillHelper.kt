package com.brins.locksmith.autofill.module

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.autofill.Dataset

import android.view.View
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.brins.locksmith.R
import com.brins.locksmith.data.AppConfig.AUTO_FILL_FROM_WHERE
import com.brins.locksmith.data.AppConfig.AUTO_FILL_URL
import com.brins.locksmith.data.AppConfig.PACKAGE_NAME
import com.brins.locksmith.data.password.PassWordItem
import com.brins.locksmith.ui.activity.AutofillSelectorActivity
import com.brins.locksmith.ui.activity.MainActivity


object AutofillHelper {

    private var pendingIntentId = 0

    val TrustBrowsers = setOf(
        "org.mozilla.firefox",
        "org.mozilla.firefox_beta",
        "org.mozilla.focus",
        "com.android.chrome",
        "com.chrome.beta",
        "com.google.android.apps.chrome",
        "com.google.android.apps.chrome_dev",
        "com.android.browser",
        "com.opera.browser",
        "com.opera.browser.beta",
        "com.opera.mini.native",
        "com.opera.mini.native.beta",
        "com.UCMobile",
        "com.uc.browser.en",
        "com.sec.android.app.sbrowser",
        "com.sec.android.app.sbrowser.beta",
        "com.tencent.mtt",
        "com.microsoft.emmx"
    )

    fun newRemoteViews(
        packageName: String, remoteViewsText: String,
        @DrawableRes drawableId: Int
    ): RemoteViews {

        val presentation = RemoteViews(packageName, R.layout.item_dataset_service)
        return presentation
    }

    fun isValidHint(hint: String): Boolean {
        when (hint) {
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
            View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
            View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
            View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,
            View.AUTOFILL_HINT_EMAIL_ADDRESS,
            View.AUTOFILL_HINT_PHONE,
            View.AUTOFILL_HINT_NAME,
            View.AUTOFILL_HINT_PASSWORD,
            View.AUTOFILL_HINT_POSTAL_ADDRESS,
            View.AUTOFILL_HINT_POSTAL_CODE,
            View.AUTOFILL_HINT_USERNAME ->
                return true
            else ->
                return false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildVaultDataset(
        context: Context,
        autofillFields: AutofillFieldMetadataCollection,
        url: String,
        fromwhere: String
    ): Dataset {
         var intent = Intent(context, AutofillSelectorActivity::class.java)
 //        intent.putExtra("autofillFramework",true);
         intent.putExtra(AUTO_FILL_URL,url)
         intent.putExtra(AUTO_FILL_FROM_WHERE,fromwhere)
         var pendingItent = PendingIntent.getActivity(context, ++pendingIntentId, intent,
                 PendingIntent.FLAG_CANCEL_CURRENT)
        val presentation = newRemoteViews(
            PACKAGE_NAME, context.getString(R.string.auto_fill_prompt)
            , R.mipmap.ic_launcher
        )
        var datasetBuilder = Dataset.Builder()
            .setValue(autofillFields.autofillIds[0], AutofillValue.forText(""), presentation)
        datasetBuilder.setAuthentication(pendingItent.intentSender)
        //build a fake dataset
        return datasetBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildDataset(
        context: Context,
        autofillFields: AutofillFieldMetadataCollection,
        selectedItem: PassWordItem
    ): Dataset {
        val presentation = newRemoteViews(
            PACKAGE_NAME, context.getString(R.string.auto_fill_prompt)
            , R.mipmap.ic_launcher
        )
        var datasetBuilder = Dataset.Builder()
        for (hint in autofillFields.allAutofillHints) {
            when (hint) {
                View.AUTOFILL_HINT_USERNAME -> {
                    var usernameAutofillMetadataCollection = autofillFields.getFieldsForHint(hint)
                    for (autofillFieldMetadata in usernameAutofillMetadataCollection!!) {
                        datasetBuilder.setValue(
                            autofillFieldMetadata.autofillId,
                            AutofillValue.forText(selectedItem.accountName),
                            presentation
                        )
                    }
                }
                View.AUTOFILL_HINT_PASSWORD -> {
                    var passwordAutofillMetadataCollection = autofillFields.getFieldsForHint(hint)
                    for (autofillFieldMetadata in passwordAutofillMetadataCollection!!) {
                        datasetBuilder.setValue(
                            autofillFieldMetadata.autofillId,
                            AutofillValue.forText(selectedItem.password),
                            presentation
                        )
                    }
                }
                View.AUTOFILL_HINT_EMAIL_ADDRESS -> {
                    var usernameAutofillMetadataCollection = autofillFields.getFieldsForHint(hint)
                    for (autofillFieldMetadata in usernameAutofillMetadataCollection!!) {
                        datasetBuilder.setValue(
                            autofillFieldMetadata.autofillId,
                            AutofillValue.forText(selectedItem.accountName),
                            presentation
                        )
                    }
                }
                View.AUTOFILL_HINT_PHONE -> {
                    var usernameAutofillMetadataCollection = autofillFields.getFieldsForHint(hint)
                    for (autofillFieldMetadata in usernameAutofillMetadataCollection!!) {
                        datasetBuilder.setValue(
                            autofillFieldMetadata.autofillId,
                            AutofillValue.forText(selectedItem.accountName),
                            presentation
                        )
                    }
                }
            }
        }
        var dataset = datasetBuilder.build()
        return dataset
    }

}