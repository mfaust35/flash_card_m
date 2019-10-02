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
                mutableListOf<Booklet>()
                    .apply {
                        addAll(bookletRepository.getAllBooklets())
                    }
                    .also {
                        post(it)
                    }
            }
        }
    }

    private fun post(bookletsToPost: MutableList<Booklet>) {
        bookletsToPost.sortBy(Booklet::name)
        booklets.postValue(bookletsToPost)
    }

    fun getAllBooklets(): LiveData<List<Booklet>> = booklets

    fun addBookletWithName(name: String) {
        GlobalScope.launch {
            val newBooklet = bookletRepository.add(Booklet(name))
            mutableListOf<Booklet>()
                .apply {
                    booklets.value?.let { addAll(it) }
                    add(newBooklet)
                }
                .also {
                    post(it)
                }
        }
    }
}
