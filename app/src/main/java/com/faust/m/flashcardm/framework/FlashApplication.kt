package com.faust.m.flashcardm.framework

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.presentation.notifyObserver
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

class FlashViewModel(application: Application): AndroidViewModel(application) {

    private val _bookletsState: MutableLiveData<Boolean> = MutableLiveData(true)

    val bookletsState: LiveData<Boolean>
        get() = _bookletsState

    fun bookletsStateChanged() {
        _bookletsState.notifyObserver()
    }
}
