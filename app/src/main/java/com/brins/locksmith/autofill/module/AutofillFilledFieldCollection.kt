package com.brins.locksmith.autofill.module

import android.content.ContentValues.TAG
import android.os.Build
import android.service.autofill.Dataset
import android.util.Log
import android.view.View
import android.view.autofill.AutofillValue
import androidx.annotation.RequiresApi
import java.util.HashMap


class AutofillFilledFieldCollection @JvmOverloads constructor(
         var datasetName: String? = null,
         val hintMap: HashMap<String, AutofillFilledField> = HashMap()
) {

    /**
     * Sets values for a list of autofillHints.
     */
    fun add(autofillField: AutofillFilledField) {
        autofillField.autofillHints.forEach { autofillHint ->
            hintMap[autofillHint] = autofillField
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun applyToFields(autofillFieldMetadataCollection: AutofillFieldMetadataCollection,
                      datasetBuilder: Dataset.Builder): Boolean {
        var setValueAtLeastOnce = false
        for (hint in autofillFieldMetadataCollection.allAutofillHints) {
            val autofillFields = autofillFieldMetadataCollection.getFieldsForHint(hint) ?: continue
            for (autofillField in autofillFields) {
                val autofillId = autofillField.autofillId
                val autofillType = autofillField.autofillType
                val savedAutofillValue = hintMap[hint]
                when (autofillType) {
                    View.AUTOFILL_TYPE_LIST -> {
                        savedAutofillValue?.textValue?.let {
                            val index = autofillField.getAutofillOptionIndex(it)
                            if (index != -1) {
                                datasetBuilder.setValue(autofillId, AutofillValue.forList(index))
                                setValueAtLeastOnce = true
                            }
                        }
                    }
                    View.AUTOFILL_TYPE_DATE -> {
                        savedAutofillValue?.dateValue?.let { date ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forDate(date))
                            setValueAtLeastOnce = true
                        }
                    }
                    View.AUTOFILL_TYPE_TEXT -> {
                        savedAutofillValue?.textValue?.let { text ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forText(text))
                            setValueAtLeastOnce = true
                        }
                    }
                    View.AUTOFILL_TYPE_TOGGLE -> {
                        savedAutofillValue?.toggleValue?.let { toggle ->
                            datasetBuilder.setValue(autofillId, AutofillValue.forToggle(toggle))
                            setValueAtLeastOnce = true
                        }
                    }
                    else -> Log.w(TAG, "Invalid autofill type - " + autofillType)
                }
            }
        }
        return setValueAtLeastOnce
    }

    @RequiresApi(Build.VERSION_CODES.O)
            /**
     * @param autofillHints List of autofill hints, usually associated with a View or set of Views.
     * @return whether any of the filled fields on the page have at least 1 autofillHint that is
     * in the provided autofillHints.
     */
    fun helpsWithHints(autofillHints: List<String>): Boolean {
        for (autofillHint in autofillHints) {
            hintMap[autofillHint]?.let { savedAutofillValue ->
                if (!savedAutofillValue.isNull()) {
                    return true
                }
            }
        }
        return false
    }
}
