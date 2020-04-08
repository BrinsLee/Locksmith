package com.brins.locksmith.viewmodel.save

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brins.locksmith.data.PassWordItem

/**
 * @author lipeilin
 * @date 2020/4/8
 */
class SinglePasswordData private constructor() : MutableLiveData<ArrayList<PassWordItem>>() {

    companion object {
        var mInstance: SinglePasswordData? = null

        fun getInstance(): SinglePasswordData {
            if (mInstance == null) {
                mInstance = SinglePasswordData()
            }
            return mInstance!!
        }
    }

}