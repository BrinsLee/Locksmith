package com.brins.locksmith.viewmodel.card

/**
 * @author lipeilin
 * @date 2020/4/15
 */
class SaveCardRepository private constructor() {
    companion object {
        private lateinit var instance: SaveCardRepository

        fun getInstance(): SaveCardRepository {
            if (!::instance.isInitialized) {
                synchronized(SaveCardRepository::class.java) {
                    if (!::instance.isInitialized) {
                        instance = SaveCardRepository()
                    }
                }
            }
            return instance
        }
    }
}