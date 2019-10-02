package com.faust.m.flashcardm.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.presentation.MutableLiveList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val bookletRepository: BookletRepository by inject()

    private val booklets:  MutableLiveList<Booklet> by lazy {
        MutableLiveList<Booklet>().apply {
            GlobalScope.launch {
                mutableListOf<Booklet>()
                    .apply {
                        addAll(bookletRepository.getAllBooklets())
                        sortBy(Booklet::name)
                    }
                    .also { booklets.postValue(it) }
            }
        }
    }

    fun getAllBooklets(): LiveData<out List<Booklet>> = booklets

    fun addBookletWithName(name: String) {
        GlobalScope.launch {
            val newBooklet = bookletRepository.add(Booklet(name)).also {
                verbose { "Created a new booklet: $it" }
            }
            booklets.add(newBooklet)
        }
    }

    fun deleteBooklet(booklet: Booklet) {
        GlobalScope.launch {
            when {
                bookletRepository.delete(booklet) != 0 -> booklets.remove(booklet)
            }
        }
    }
}
