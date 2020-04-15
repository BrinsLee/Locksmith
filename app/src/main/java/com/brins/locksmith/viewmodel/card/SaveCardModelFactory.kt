package com.brins.locksmith.viewmodel.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brins.locksmith.viewmodel.passport.PassportRepository

/**
 * @author lipeilin
 * @date 2020/4/15
 */
class SaveCardModelFactory(private val repository: PassportRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SaveCardViewModel(repository) as T
    }
}