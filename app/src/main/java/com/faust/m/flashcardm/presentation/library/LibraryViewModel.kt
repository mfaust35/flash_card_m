package com.faust.m.flashcardm.presentation.library

import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Booklet
import com.faust.m.core.usecase.booklet.BookletOutline
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.framework.BookletUseCases
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.presentation.Event
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.ONGOING
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.SUCCESS
import com.faust.m.flashcardm.presentation.notifyObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.jetbrains.anko.warn
import org.koin.core.KoinComponent
import org.koin.core.inject

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    companion object {
        const val EMPTY_BOOKLET: Long = -1
    }

    private val cardUseCases: BookletUseCases by inject()


    private val _booklets: MutableLibraryBooklets = MutableLibraryBooklets()
    val booklets: LiveData<MutableList<LibraryBooklet>> =
        Transformations.switchMap(getKoin().get<FlashViewModel>().bookletsState) {
            GlobalScope.launch {
                loadLibraryBooklets()
            }
            _booklets
        }

    private val _bookletAdded: MutableLiveData<Event<AddedBooklet>> = MutableLiveData()
    val bookletAdded: LiveData<Event<AddedBooklet>> = _bookletAdded

    private val _bookletRemoved: MutableLiveData<Event<LibraryBooklet>> = MutableLiveData()
    val bookletRemoved: LiveData<Event<LibraryBooklet>> = _bookletRemoved

    private val _eventAddCardToBooklet: MutableLiveData<Event<Long>> = MutableLiveData()
    val eventAddCardToBooklet: LiveData<Event<Long>> = _eventAddCardToBooklet

    private val _eventReviewBooklet: MutableLiveData<Event<Long>> = MutableLiveData()
    val eventReviewBooklet: LiveData<Event<Long>> = _eventReviewBooklet

    var selectedBooklet: LibraryBooklet? = null

    fun nameBooklet(newName: String) {
        selectedBooklet?.let { renameBooklet(newName, it) } ?: addBooklet(newName)
    }

    private fun renameBooklet(newName: String, libraryBooklet: LibraryBooklet) {
        GlobalScope.launch {
            when(cardUseCases.renameBooklet(newName, libraryBooklet.id)) {
                true -> {
                    verbose { "Booklet renamed oldName: ${libraryBooklet.name} | newName: $newName" }
                    val copy = libraryBooklet.copy(name = newName)
                    _booklets.value?.let {
                        it.remove(libraryBooklet)
                        it.add(copy)
                    }
                    _booklets.notifyObserver()
                }
                false ->
                    warn { "Cannot rename booklet oldName: ${libraryBooklet.name} | newName: $newName" }
            }
        }
    }

    private fun addBooklet(newName: String) {
        _bookletAdded.postValue(Event(AddedBooklet(state = ONGOING)))
        GlobalScope.launch {
            cardUseCases.addBooklet(Booklet(newName)).let {
                verbose { "Booklet $it added" }
                val newBooklet = LibraryBooklet(it, BookletOutline.EMPTY)
                val position = _booklets.addSilent(newBooklet)
                _bookletAdded.postValue(Event(AddedBooklet(SUCCESS, position, newBooklet)))
            }
        }
    }

    fun deleteCurrentBooklet() {
        selectedBooklet?.let { libraryBooklet ->
            GlobalScope.launch {
                cardUseCases.deleteBooklet(libraryBooklet.toBooklet()).let { result: Int ->
                    when(result) {
                        0 -> warn { "Booklet $libraryBooklet not deleted" }
                        else -> {
                            verbose { "Booklet $libraryBooklet deleted" }
                            _booklets.value?.remove(libraryBooklet)
                            _bookletRemoved.postValue(Event(libraryBooklet))
                        }
                    }
                }
            }
        } ?: warn { "Could not find booklet to delete" }
    }

    fun reviewBooklet(booklet: LibraryBooklet) {
        when(booklet.cardToReviewCount) {
            0 -> _eventReviewBooklet.postValue(Event(EMPTY_BOOKLET))
            else -> _eventReviewBooklet.postValue(Event(booklet.id))
        }
    }

    fun addCardsToCurrentBooklet() {
        selectedBooklet?.let {
            _eventAddCardToBooklet.postValue(Event(it.id))
        } ?: warn { "Could not find booklet to add cards to" }
    }

    private fun loadLibraryBooklets() = mutableListOf<LibraryBooklet>()
        .apply {
            val tBooklets: List<Booklet> = cardUseCases.getBooklets()
            val tBookletsOutlines: LongSparseArray<BookletOutline> =
                cardUseCases.getBookletsOutlines(tBooklets)

            for (tBooklet: Booklet in tBooklets) {
                val tOutline = tBookletsOutlines[tBooklet.id] ?: BookletOutline.EMPTY
                add(LibraryBooklet(tBooklet, tOutline))
            }
        }
        .also {
            _booklets.postValue(it)
        }
}

class MutableLibraryBooklets: MutableLiveList<LibraryBooklet>() {

    fun addSilent(booklet: LibraryBooklet): Int {
        return value?.let {
            it.add(booklet)
            it.sortBy(LibraryBooklet::name)
            it.indexOf(booklet)
        } ?: -1
    }

    override fun postValue(value: MutableList<LibraryBooklet>?) {
        value?.sortBy(LibraryBooklet::name)
        super.postValue(value)
    }
}

/**
 * Wrapper data class for easy access to the summary of cards linked to a booklet
 */
data class LibraryBooklet(val name: String,
                          val cardToReviewCount: Int,
                          val totalCardCount: Int,
                          val id: Long = 0) {

    companion object {
        private val colors = arrayOf (
            R.color.colorHighlight1,
            R.color.colorHighlight2,
            R.color.colorHighlight3,
            R.color.colorHighlight4,
            R.color.colorHighlight5,
            R.color.colorHighlight6
        )

        val LOADING = LibraryBooklet("Loading", 0, 0, 0)
    }

    constructor(booklet: Booklet, bookletOutline: BookletOutline): this(
        booklet.name,
        bookletOutline.cardToReviewCount,
        bookletOutline.cardTotalCount,
        booklet.id
    )

    val color: Int = colors[kotlin.math.abs(hashCode() % 6)]

    fun toBooklet() = Booklet(name, id)
}


/**
 * Wrapper class for observing booklet adding procedure
 */
data class AddedBooklet(val state: State,
                        val position: Int = 0,
                        val booklet: LibraryBooklet = LibraryBooklet.LOADING) {

    enum class State { EMPTY, ONGOING, FAIL, SUCCESS }
}
