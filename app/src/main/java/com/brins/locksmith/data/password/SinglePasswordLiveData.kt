package com.brins.locksmith.data.password

import androidx.lifecycle.MutableLiveData

/**
 * @author lipeilin
 * @date 2020/4/27
 */
class SinglePasswordLiveData : MutableLiveData<ArrayList<PassWordItem>>() {

    companion object {
        private lateinit var sInstance: SinglePasswordLiveData

        fun get(): SinglePasswordLiveData {
            sInstance = if (::sInstance.isInitialized) sInstance else SinglePasswordLiveData()
            return sInstance
        }
    }
}