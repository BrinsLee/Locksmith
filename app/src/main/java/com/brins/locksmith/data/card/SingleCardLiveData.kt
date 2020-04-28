package com.brins.locksmith.data.card

import androidx.lifecycle.MutableLiveData

/**
 * @author lipeilin
 * @date 2020/4/28
 */

class SingleCardLiveData : MutableLiveData<ArrayList<CardItem>>() {

    companion object {
        private lateinit var sInstance: SingleCardLiveData

        fun get(): SingleCardLiveData {
            sInstance = if (::sInstance.isInitialized) sInstance else SingleCardLiveData()
            return sInstance
        }
    }
}