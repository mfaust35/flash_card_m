package com.faust.m.flashcardm.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.data.CardRepository
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
    private val cardRepository: CardRepository by inject()

    private val booklets:  MutableLiveList<LibraryBooklet> by lazy {
        MutableLiveList<LibraryBooklet>().apply {
            GlobalScope.launch {
                mutableListOf<LibraryBooklet>()
                    .apply {
                        addAll(
                            bookletRepository.getAllBooklets().map {
                                LibraryBooklet(it.name, cardRepository.countCardForBooklet(it.id), it.id)
                            }
                        )
                        sortBy(LibraryBooklet::name)
                    }
                    .also { booklets.postValue(it) }
            }
        }
    }

    private val currentBooklet: MutableLiveData<LibraryBooklet> = MutableLiveData()

    fun getAllBooklets(): LiveData<out List<LibraryBooklet>> = booklets

    fun addBookletWithName(name: String) {
        GlobalScope.launch {
            val newBooklet = bookletRepository.add(Booklet(name)).run {
                verbose { "Created a new booklet: $this" }
                LibraryBooklet(this, 0)
            }
            booklets.add(newBooklet)
        }
    }

    fun currentBooklet(booklet: LibraryBooklet) {
        currentBooklet.postValue(booklet)
    }

    fun currentBooklet(): LiveData<LibraryBooklet> = currentBooklet

    fun deleteCurrentBooklet() {
        currentBooklet.value?.let {
            GlobalScope.launch {
                when {
                    bookletRepository.delete(it.toBooklet()) != 0 -> booklets.remove(it)
                }
            }
        }
    }
}

data class LibraryBooklet(val name: String, val cardCount: Int = 0, val id: Long = 0) {

    constructor(booklet: Booklet, cardNumber: Int): this(booklet.name, cardNumber, booklet.id)

    fun toBooklet() = Booklet(name, id)
}
