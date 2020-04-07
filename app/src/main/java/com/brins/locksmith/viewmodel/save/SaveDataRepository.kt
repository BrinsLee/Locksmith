package com.brins.locksmith.viewmodel.save

class SaveDataRepository private constructor(){

    companion object{

        private lateinit var instance: SaveDataRepository

        fun getInstance(): SaveDataRepository {
            if (!::instance.isInitialized) {
                synchronized(SaveDataRepository::class.java) {
                    if (!::instance.isInitialized) {
                        instance = SaveDataRepository()
                    }
                }
            }
            return instance
        }
    }
}