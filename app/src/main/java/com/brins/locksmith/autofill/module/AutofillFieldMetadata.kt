package com.brins.locksmith.autofill.module

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.SaveInfo
import android.view.View
import android.view.autofill.AutofillId
import androidx.annotation.RequiresApi

/**
 * @author lipeilin
 * @date 2020/4/22
 */
@RequiresApi(Build.VERSION_CODES.O)
class AutofillFieldMetadata(view: AssistStructure.ViewNode) {

    var saveType = 0
        private set
    var autofillHints = arrayOf("")
    val autofillId: AutofillId
    val autofillType: Int
    val autofillOptions: Array<CharSequence>?
    val isFocused: Boolean


    init {
        if(view.autofillHints!=null){
            autofillHints = view.autofillHints.filter(AutofillHelper::isValidHint).toTypedArray()
        }
        autofillId = view.autofillId
        autofillType = view.autofillType
        autofillOptions = view.autofillOptions
        isFocused = view.isFocused
        updateSaveTypeFromHints()
    }
    constructor(view: AssistStructure.ViewNode, hints:Array<String>) : this(view){
        autofillHints = hints
    }

    fun getAutofillOptionIndex(value: CharSequence): Int {
        return if (autofillOptions != null) {
            autofillOptions.indexOf(value)
        } else {
            -1
        }
    }

    private fun updateSaveTypeFromHints() {
        saveType = 0
        for (hint in autofillHints) {
            when (hint) {
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
                View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
                View.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
                View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_CREDIT_CARD
                }
                View.AUTOFILL_HINT_EMAIL_ADDRESS -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS
                }
                View.AUTOFILL_HINT_PHONE, View.AUTOFILL_HINT_NAME -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_GENERIC
                }
                View.AUTOFILL_HINT_PASSWORD -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_PASSWORD
                    saveType = saveType and SaveInfo.SAVE_DATA_TYPE_EMAIL_ADDRESS.inv()
                    saveType = saveType and SaveInfo.SAVE_DATA_TYPE_USERNAME.inv()
                }
                View.AUTOFILL_HINT_POSTAL_ADDRESS,
                View.AUTOFILL_HINT_POSTAL_CODE -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_ADDRESS
                }
                View.AUTOFILL_HINT_USERNAME -> {
                    saveType = saveType or SaveInfo.SAVE_DATA_TYPE_USERNAME
                }
            }
        }
    }
}