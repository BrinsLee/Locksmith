package com.brins.locksmith.viewmodel.passport


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brins.locksmith.BaseApplication
import com.brins.locksmith.data.AesEncryptedData
import com.brins.locksmith.data.PassWordItem
import org.bouncycastle.util.encoders.Hex
import tech.bluespace.id_guard.AccountItemOuterClass
import java.io.File
import java.io.IOException

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