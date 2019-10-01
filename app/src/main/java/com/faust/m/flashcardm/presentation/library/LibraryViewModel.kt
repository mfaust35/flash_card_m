package com.faust.m.flashcardm.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

// Shorten MutableLiveData<List<Booklet>> into MLDBooklets
private class MLDBooklets : MutableLiveData<List<Booklet>>()

class LibraryViewModel: ViewModel(), KoinComponent {

    private val bookletRepository: BookletRepository by inject()

    private val booklets: MLDBooklets by lazy {
        MLDBooklets().apply {
            GlobalScope.launch {
                bookletRepository.getAllBooklets().let {
                    //mutableListOf().addAll(it)
                    val add = mutableListOf<Booklet>()
                    add.add(Booklet("My first booklet", 10))
                    booklets.postValue(add)
                }
            }
        }
    }

    fun getAllBooklets(): LiveData<List<Booklet>> = booklets
}
