
package com.brins.locksmith.autofill.module

import android.app.assist.AssistStructure
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
class AutofillFilledField(viewNode: AssistStructure.ViewNode) {
    var textValue: String? = null
    var dateValue: Long? = null
    var toggleValue: Boolean? = null
    var autofillHints = arrayOf("")

    init {

        if(viewNode.autofillHints!=null){
            autofillHints = viewNode.autofillHints.filter(AutofillHelper::isValidHint).toTypedArray()
        }

        viewNode.autofillValue?.let {
            if (it.isList) {
                val index = it.listValue
                viewNode.autofillOptions?.let { autofillOptions ->
                    if (autofillOptions.size > index) {
                        textValue = autofillOptions[index].toString()
                    }
                }
            } else if (it.isDate) {
                dateValue = it.dateValue
            } else if (it.isText) {
                // Using toString of AutofillValue.getTextValue in order to save it to
                // SharedPreferences.
                textValue = it.textValue.toString()
            } else {
            }
        }
    }
    constructor(viewNode: AssistStructure.ViewNode,hints : Array<String>) : this(viewNode){
        autofillHints = hints

    }

    fun isNull(): Boolean {
        return textValue == null && dateValue == null && toggleValue == null
    }
}
