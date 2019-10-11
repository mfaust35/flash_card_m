package com.faust.m.flashcardm.framework

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FlashApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FlashApplication)
            modules(listOf(roomDatabaseModule, viewModelModule, useCases))
        }
    }
}
