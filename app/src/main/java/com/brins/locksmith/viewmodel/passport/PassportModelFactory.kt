package com.brins.locksmith.viewmodel.passport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PassportModelFactory(private val repository: PassportRepository) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PassportViewModel(repository) as T
    }
}