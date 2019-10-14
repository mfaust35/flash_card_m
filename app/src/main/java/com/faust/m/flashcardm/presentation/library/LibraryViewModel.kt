package com.faust.m.flashcardm.presentation.library

import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Booklet
import com.faust.m.core.usecase.BookletOutline
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.framework.UseCases
import com.faust.m.flashcardm.presentation.Event
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.ONGOING
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.SUCCESS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.jetbrains.anko.warn
import org.koin.core.KoinComponent
import org.koin.core.inject

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val useCases: UseCases by inject()


    private val _booklets: MutableLibraryBooklets = MutableLibraryBooklets()
    val booklets: LiveData<MutableList<LibraryBooklet>> =
        Transformations.switchMap(getKoin().get<FlashViewModel>().bookletsState) {
            GlobalScope.launch {
                loadLibraryBooklets()
            }
            _booklets
        }

    private val _stateAddBooklet: MutableLiveData<AddedBooklet?> = MutableLiveData()
    val stateAddBooklet: LiveData<AddedBooklet?> = _stateAddBooklet

    private val _eventAddCardToBooklet: MutableLiveData<Event<Long>> = MutableLiveData()
    val eventAddCardToBooklet: LiveData<Event<Long>> = _eventAddCardToBooklet

    var selectedBooklet: LibraryBooklet? = null
        set(value) {
            _stateAddBooklet.postValue(null)
            field = value
        }


    fun addBookletWithName(name: String) {
        _stateAddBooklet.postValue(AddedBooklet(state = ONGOING))
        GlobalScope.launch {
            useCases.addBooklet(Booklet(name)).let {
                verbose { "Booklet $it added" }
                _booklets.add(LibraryBooklet(it, BookletOutline.EMPTY))
                _stateAddBooklet.postValue(AddedBooklet(it.id, SUCCESS))
            }
        }
    }

    fun deleteCurrentBooklet() {
        selectedBooklet?.let { libraryBooklet ->
            GlobalScope.launch {
                useCases.deleteBooklet(libraryBooklet.toBooklet()).let {result: Int ->
                    when(result) {
                        0 -> warn { "Booklet $libraryBooklet not deleted" }
                        else -> {
                            verbose { "Booklet $libraryBooklet deleted" }
                            _booklets.remove(libraryBooklet)
                        }
                    }
                }
            }
        } ?: warn { "Could not find booklet to delete" }
    }

    fun addCardsToCurrentBooklet() {
        selectedBooklet?.let {
            _eventAddCardToBooklet.postValue(Event(it.id))
        } ?: warn { "Could not find booklet to add cards to" }
    }

    private fun loadLibraryBooklets() = mutableListOf<LibraryBooklet>()
        .apply {
            val tBooklets: List<Booklet> = useCases.getBooklets()
            val tBookletsOutlines: LongSparseArray<BookletOutline> =
                useCases.getBookletsOutlines(tBooklets)

            for (tBooklet: Booklet in tBooklets) {
                val tOutline = tBookletsOutlines[tBooklet.id] ?: BookletOutline.EMPTY
                add(LibraryBooklet(tBooklet, tOutline))
            }
        }
        .also {
            _booklets.postValue(it)
        }
}

class MutableLibraryBooklets: MutableLiveData<MutableList<LibraryBooklet>>() {

    override fun postValue(value: MutableList<LibraryBooklet>?) {
        value?.sortBy(LibraryBooklet::name)
        super.postValue(value)
    }

    fun add(value: LibraryBooklet) {
        this.value?.add(value)
        postValue(this.value)
    }

    fun remove(value: LibraryBooklet) {
        this.value?.remove(value)
        postValue(this.value)
    }
}

/**
 * Wrapper data class for easy access to the summary of cards linked to a booklet
 */
data class LibraryBooklet(val name: String,
                          val cardToReviewCount: Int,
                          val totalCardCount: Int,
                          val id: Long = 0) {

    constructor(booklet: Booklet, bookletOutline: BookletOutline): this(
        booklet.name,
        bookletOutline.cardToReviewCount,
        bookletOutline.cardTotalCount,
        booklet.id
    )

    fun toBooklet() = Booklet(name, id)
}

/**
 * Wrapper class for observing booklet adding procedure
 */
data class AddedBooklet(val id: Long? = null, val state: State) {

    enum class State { ONGOING, FAIL, SUCCESS }
}