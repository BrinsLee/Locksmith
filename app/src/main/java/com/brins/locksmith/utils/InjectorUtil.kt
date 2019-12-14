package com.brins.locksmith.utils

import com.brins.locksmith.viewmodel.main.MainModelFactory
import com.brins.locksmith.viewmodel.passport.PassportModelFactory
import com.brins.locksmith.viewmodel.passport.PassportRepository


object InjectorUtil {

//    private fun getPlaceRepository() = PlaceRepository.getInstance(CoolWeatherDatabase.getPlaceDao(), CoolWeatherNetwork.getInstance())

    private fun getPassportRepository() = PassportRepository.getInstance()

//    fun getChooseAreaModelFactory() = ChooseAreaModelFactory(getPlaceRepository())

    fun getPassportModelFactory() = PassportModelFactory(getPassportRepository())

    fun getMainModelFactory() = MainModelFactory(getPassportRepository())

}