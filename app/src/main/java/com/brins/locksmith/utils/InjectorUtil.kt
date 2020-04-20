package com.brins.locksmith.utils

import com.brins.locksmith.viewmodel.card.SaveCardModelFactory
import com.brins.locksmith.viewmodel.main.MainModelFactory
import com.brins.locksmith.viewmodel.passport.PassportModelFactory
import com.brins.locksmith.viewmodel.passport.PassportRepository
import com.brins.locksmith.viewmodel.save.SavePasswordModelFactory
import com.brins.locksmith.viewmodel.save.SaveDataRepository


object InjectorUtil {

//    private fun getPlaceRepository() = PlaceRepository.getInstance(CoolWeatherDatabase.getPlaceDao(), CoolWeatherNetwork.getInstance())

    private fun getPassportRepository() = PassportRepository.getInstance()


//    fun getChooseAreaModelFactory() = ChooseAreaModelFactory(getPlaceRepository())

    fun getPassportModelFactory() = PassportModelFactory(getPassportRepository())

    fun getMainModelFactory() = MainModelFactory(getPassportRepository())

    fun getPassWordFactory() = SavePasswordModelFactory(getPassportRepository())

    fun getCardFactory() = SaveCardModelFactory(getPassportRepository())




}