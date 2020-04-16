package com.brins.locksmith.viewmodel.passport


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brins.locksmith.BaseApplication

class PassportViewModel(private val repository: PassportRepository) : ViewModel() {


    companion object{
        private val path = BaseApplication.context.applicationInfo.dataDir + "/account_file/"
        private val TAG = this::class.java.simpleName
    }
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