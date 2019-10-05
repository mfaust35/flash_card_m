package com.faust.m.flashcardm.presentation.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.BookletRepository
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.ONGOING
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.SUCCESS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.koin.core.KoinComponent
import org.koin.core.inject

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val bookletRepository: BookletRepository by inject()
    private val cardRepository: CardRepository by inject()

    private val booklets: MutableLiveBooklet by lazy {
        MutableLiveBooklet().apply {
            GlobalScope.launch {
                loadLibraryBooklets()
            }
        }
    }

    private val currentBooklet: MutableLiveData<LibraryBooklet?> = MutableLiveData()

    private val addBookletState: MutableLiveData<AddedBooklet?> = MutableLiveData()


    fun booklets(): LiveData<out List<LibraryBooklet>> = booklets

    fun currentBooklet(): LiveData<LibraryBooklet?> = currentBooklet

    fun addBookletState(): LiveData<AddedBooklet?> = addBookletState


    fun addBookletWithName(name: String) {
        addBookletState.postValue(AddedBooklet(state = ONGOING))
        GlobalScope.launch {
            bookletRepository.add(Booklet(name)).let {
                booklets.add(LibraryBooklet(it, 0))
                addBookletState.postValue(AddedBooklet(it.id, SUCCESS))
            }
        }
    }

    fun currentBooklet(booklet: LibraryBooklet) {
        currentBooklet.postValue(booklet)
        addBookletState.postValue(null)
    }

    fun deleteCurrentBooklet() {
        currentBooklet.value?.let {
            GlobalScope.launch {
                when {
                    bookletRepository.delete(it.toBooklet()) != 0 -> booklets.remove(it)
                }
            }
        }
    }

    private fun loadLibraryBooklets() =
        mutableListOf<LibraryBooklet>()
            .apply {
                val tBooklets = bookletRepository.getAllBooklets()
                val cardCounts =
                    cardRepository.countCardForBooklets(tBooklets.map(Booklet::id))
                tBooklets.forEach {
                    this.add(LibraryBooklet(it, cardCounts[it.id] ?: 0))
                }
            }
            .also {
                booklets.postValue(it)
            }
}

/**
 * This class ensure that the list is sorted before being posted
 */
class MutableLiveBooklet: MutableLiveList<LibraryBooklet>() {

    override fun postValue(value: MutableList<LibraryBooklet>?) {
        value?.sortBy(LibraryBooklet::name)
        super.postValue(value)
    }
}

/**
 * Wrapper data class for easy access to the card count of a booklet
 */
data class LibraryBooklet(val name: String, val cardCount: Int = 0, val id: Long = 0) {

    constructor(booklet: Booklet, cardCount: Int): this(booklet.name, cardCount, booklet.id)

    fun toBooklet() = Booklet(name, id)
}

/**
 * Wrapper class for observing booklet adding procedure
 */
data class AddedBooklet(val id: Long? = null, val state: State) {

    enum class State { ONGOING, FAIL, SUCCESS }
}