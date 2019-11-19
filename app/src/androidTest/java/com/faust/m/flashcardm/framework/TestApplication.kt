package com.faust.m.flashcardm.framework

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * For Android instrumentation test, we need koin to inject dependencies different from the
 * real application ones. Since Koin is started during Application.onCreate, we need to override
 * Application and give our new TestApplication to a JUnitRunner
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin once and for all tests, without any module
        // Tests are expected to load any module they want AND to unload any module they load
        startKoin {
            androidLogger()
            androidContext(this@TestApplication)
            modules(listOf())
        }
    }
}

class TestApplicationJUnitRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}
