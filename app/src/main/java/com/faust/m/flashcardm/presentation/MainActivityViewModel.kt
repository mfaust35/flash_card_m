package com.faust.m.flashcardm.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class MainActivityViewModel: ViewModel(), KoinComponent {

    private val bookletRepository: BookletRepository by inject()

    private val allBooklets =
        MutableLiveData<List<Booklet>>().apply {
            refreshBooklets()
        }

    fun getAllBooklets(): LiveData<List<Booklet>> = allBooklets

    private fun refreshBooklets() {
        GlobalScope.launch {
            allBooklets.postValue(bookletRepository.getAllBooklet())
        }
    }
}