package com.brins.locksmith.viewmodel.passport


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PassportViewModel(private val repository: PassportRepository) : ViewModel() {


    private val mUserIdLiveData: MutableLiveData<ByteArray> = MutableLiveData()


    fun loadPassport(): Boolean {
        if (repository.isPassportValid()) {
            return true
        }
        if (!repository.loadDeviceSecretKey()) {
            return false
        }
        return repository.initPassport()

    }

    fun createPassport() = repository.createPassport()

}