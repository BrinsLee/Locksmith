package com.brins.locksmith.viewmodel.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brins.locksmith.viewmodel.passport.PassportRepository

class SavePasswordModelFactory (private val repository: PassportRepository)
    : ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SavePasswordViewModel(repository) as T
    }
}