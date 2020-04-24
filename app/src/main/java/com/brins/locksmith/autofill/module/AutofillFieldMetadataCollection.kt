
package com.brins.locksmith.autofill.module

import android.view.autofill.AutofillId

data class AutofillFieldMetadataCollection @JvmOverloads constructor(
        val autofillIds: ArrayList<AutofillId> = ArrayList<AutofillId>(),
        val allAutofillHints: ArrayList<String> = ArrayList<String>(),
        val focusedAutofillHints: ArrayList<String> = ArrayList<String>()
) {

    private val autofillHintsToFieldsMap = HashMap<String, MutableList<AutofillFieldMetadata>>()
    var saveType = 0
        private set

    fun add(autofillFieldMetadata: AutofillFieldMetadata) {
        saveType = saveType or autofillFieldMetadata.saveType
        autofillIds.add(autofillFieldMetadata.autofillId)
        val hintsList = autofillFieldMetadata.autofillHints
        allAutofillHints.addAll(hintsList)
        if (autofillFieldMetadata.isFocused) {
            focusedAutofillHints.addAll(hintsList)
        }
        autofillFieldMetadata.autofillHints.forEach {
            val fields = autofillHintsToFieldsMap[it] ?: ArrayList()
            autofillHintsToFieldsMap[it] = fields
            fields.add(autofillFieldMetadata)
        }
    }

    fun getFieldsForHint(hint: String): MutableList<AutofillFieldMetadata>? {
        return autofillHintsToFieldsMap[hint]
    }
}
