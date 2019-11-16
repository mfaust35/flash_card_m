package com.faust.m.flashcardm.presentation.library

import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.usecase.booklet.BookletOutline
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.framework.BookletUseCases
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.presentation.Event
import com.faust.m.flashcardm.presentation.MutableLiveEvent
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


const val DEFAULT_REVIEW_AHEAD_CARD_NUMBER = 20

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val bookletUseCases: BookletUseCases by inject()


    private val _booklets: MutableLibraryBooklets = MutableLibraryBooklets()
    val booklets: LiveData<MutableList<LibraryBooklet>> =
        Transformations.switchMap(getKoin().get<FlashViewModel>().bookletsState) {
            GlobalScope.launch {
                postLibraryBooklets()
            }
            _booklets
        }

    private val _bookletAdded: MutableLiveData<Event<AddedBooklet>> = MutableLiveData()
    val bookletAdded: LiveData<Event<AddedBooklet>> = _bookletAdded

    private val _bookletRemoved: MutableLiveEvent<BookletRemovalStatus> = MutableLiveEvent()
    val bookletRemoved: LiveData<Event<BookletRemovalStatus>> = _bookletRemoved

    private val _eventManageCardsForBooklet: MutableLiveEvent<Long> = MutableLiveEvent()
    val eventManageCardsForBooklet: LiveData<Event<Long>> = _eventManageCardsForBooklet

    private val _eventReviewBooklet: MutableLiveEvent<LibraryBooklet> = MutableLiveEvent()
    val eventReviewBooklet: LiveData<Event<LibraryBooklet>> = _eventReviewBooklet

    var selectedBooklet: LibraryBooklet? = null

    fun nameBooklet(newName: String) {
        selectedBooklet?.let { renameBooklet(newName, it) } ?: addBooklet(newName)
    }

    private fun renameBooklet(newName: String, libraryBooklet: LibraryBooklet) {
        GlobalScope.launch {
            when(bookletUseCases.renameBooklet(newName, libraryBooklet.id)) {
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
            bookletUseCases.addBooklet(Booklet(newName)).let {
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
                bookletUseCases.deleteBooklet(libraryBooklet.toBooklet()).let { result: Int ->
                    if (0 == result) {
                        warn { "Booklet $libraryBooklet not deleted" }
                        return@launch
                    }
                    verbose { "Booklet $libraryBooklet deleted" }
                    _booklets.removeSilent(libraryBooklet)
                    _bookletRemoved.postEvent(BookletRemovalStatus(
                        removedBooklet = libraryBooklet,
                        wasLast = _booklets.value.isNullOrEmpty())
                    )
                }
            }
        } ?: warn { "Could not find booklet to delete" }
    }

    fun reviewBooklet(booklet: LibraryBooklet) {
        selectedBooklet = booklet
        _eventReviewBooklet.postEvent(booklet)
    }

    fun addCardsToReviewAheadForCurrentBooklet(count: Int) {
        selectedBooklet?.let { tBooklet ->
            GlobalScope.launch {
                bookletUseCases.resetForReview(count, tBooklet.id)
                // Reload LibraryBooklet
                val newBooklets = loadLibraryBooklet()
                _booklets.postValue(newBooklets)
                // Ask to review the booklet for which we just added card to review
                newBooklets.find { nBooklet -> nBooklet.id == tBooklet.id }?.let {
                    _eventReviewBooklet.postEvent(it)
                }
            }
        }
    }

    fun maxCardCountToReviewAheadForCurrentBooklet() = selectedBooklet?.countReviewAheadCards ?: 0

    fun defaultCardCountToReviewAheadForCurrentBooklet() =
        maxCardCountToReviewAheadForCurrentBooklet().coerceAtMost(DEFAULT_REVIEW_AHEAD_CARD_NUMBER)

    fun manageCardsForCurrentBooklet() {
        selectedBooklet?.let {
            _eventManageCardsForBooklet.postEvent(it.id)
        } ?: warn { "Could not find booklet to add cards to" }
    }

    private fun postLibraryBooklets() = loadLibraryBooklet().let {
        _booklets.postValue(it)
    }

    private fun loadLibraryBooklet() = mutableListOf<LibraryBooklet>()
        .apply {
            val tBooklets: List<Booklet> = bookletUseCases.getBooklets()
            val tBookletsOutlines: LongSparseArray<BookletOutline> =
                bookletUseCases.getBookletsOutlines(tBooklets)

            for (tBooklet: Booklet in tBooklets) {
                val tOutline = tBookletsOutlines[tBooklet.id] ?: BookletOutline.EMPTY
                add(LibraryBooklet(tBooklet, tOutline))
            }
        }
}

class MutableLibraryBooklets: MutableLiveList<LibraryBooklet>() {

    fun addSilent(booklet: LibraryBooklet): Int {
        return value?.let {
            it.add(booklet)
            it.sortBy { booklet -> booklet.name.toLowerCase() }
            it.indexOf(booklet)
        } ?: -1
    }

    fun removeSilent(booklet: LibraryBooklet) = value?.remove(booklet)

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
                          val newCount: Int,
                          val inReviewCount: Int,
                          val learnedCount: Int,
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

        val LOADING = LibraryBooklet("Loading", 0, 0, 0, 0, 0, 0)
        val ERROR = LibraryBooklet("Error", 0, 0, 0, 0, 0, 0)
    }

    constructor(booklet: Booklet, bookletOutline: BookletOutline): this(
        booklet.name,
        bookletOutline.cardToReviewCount,
        bookletOutline.cardTotalCount,
        bookletOutline.cardNewCount,
        bookletOutline.cardInReviewCount,
        bookletOutline.cardLearnedCount,
        booklet.id
    )

    val color: Int = colors[kotlin.math.abs(hashCode() % 6)]

    fun toBooklet() = Booklet(name, id)

    fun isEmpty() = totalCardCount == 0

    fun isCompletedForToday() = cardToReviewCount == 0

    fun canReviewAhead() = totalCardCount > cardToReviewCount

    val countReviewAheadCards = totalCardCount - cardToReviewCount
}


/**
 * Wrapper class for observing booklet adding procedure
 */
data class AddedBooklet(val state: State,
                        val position: Int = 0,
                        val booklet: LibraryBooklet = LibraryBooklet.LOADING) {

    enum class State { EMPTY, ONGOING, FAIL, SUCCESS }
}

data class BookletRemovalStatus(val removedBooklet: LibraryBooklet,
                                val wasLast: Boolean)
