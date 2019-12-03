package com.faust.m.flashcardm.presentation.library

import androidx.lifecycle.*
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.usecase.BookletOutline
import com.faust.m.flashcardm.core.usecase.BookletUseCases
import com.faust.m.flashcardm.core.usecase.OutlinedBooklet
import com.faust.m.flashcardm.core.usecase.OutlinedLibrary
import com.faust.m.flashcardm.presentation.Event
import com.faust.m.flashcardm.presentation.MutableLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.jetbrains.anko.warn
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


const val DEFAULT_REVIEW_AHEAD_CARD_NUMBER = 20

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val bookletUseCases: BookletUseCases by inject()


    private val _booklets: MutableLiveData<List<BookletBannerData>> = MutableLiveData()
    val booklets: LiveData<List<BookletBannerData>> =
        Transformations.switchMap(bookletUseCases.getLiveOutlinedLibrary()) { outlinedLibrary ->
            _booklets.postValue(outlinedLibrary.toSortedBookletBanners())
            _booklets
        }

    private val _eventManageCardsForBooklet: MutableLiveEvent<Long> = MutableLiveEvent()
    val eventManageCardsForBooklet: LiveData<Event<Long>> = _eventManageCardsForBooklet

    private val _eventReviewBooklet: MutableLiveEvent<BookletBannerData> = MutableLiveEvent()
    val eventReviewBooklet: LiveData<Event<BookletBannerData>> = _eventReviewBooklet

    var selectedBooklet: BookletBannerData? = null


    fun nameBooklet(newName: String) {
        selectedBooklet?.let { renameBooklet(newName, it) } ?: addBooklet(newName)
    }

    private fun renameBooklet(newName: String, booklet: BookletBannerData) {
        viewModelScope.launch(Dispatchers.IO) {
            when(bookletUseCases.renameBooklet(newName, booklet.id)) {
                true ->
                    verbose { "Booklet renamed oldName: ${booklet.name} | newName: $newName" }
                false ->
                    warn { "Cannot rename booklet oldName: ${booklet.name} | newName: $newName" }
            }
        }
    }

    private fun addBooklet(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newBooklet = bookletUseCases.addBooklet(Booklet(newName))
            verbose { "Booklet $newBooklet added" }
        }
    }

    fun deleteCurrentBooklet() {
        selectedBooklet?.let { bookletBannerData ->
            viewModelScope.launch(Dispatchers.IO) {
                bookletUseCases.deleteBooklet(bookletBannerData.toBooklet()).let { result: Int ->
                    if (0 == result) {
                        warn { "Booklet $bookletBannerData not deleted" }
                        return@launch
                    }
                    verbose { "Booklet $bookletBannerData deleted" }
                }
            }
        } ?: warn { "Could not find booklet to delete" }
    }

    fun reviewBooklet(booklet: BookletBannerData) {
        selectedBooklet = booklet
        _eventReviewBooklet.postEvent(booklet)
    }

    fun addCardsToReviewAheadForCurrentBooklet(count: Int) {
        selectedBooklet?.let { tBooklet ->
            viewModelScope.launch(Dispatchers.IO) {
                val actualResetCount = bookletUseCases.resetForReview(count, tBooklet.id)
                if (actualResetCount > 0) {
                    _eventReviewBooklet.postEvent(tBooklet.copy(cardToReviewCount = actualResetCount))
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
}

/**
 * Wrapper data class for easy access to the summary of cards linked to a booklet
 */
data class BookletBannerData(val name: String,
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

        val LOADING = BookletBannerData("Loading", 0, 0, 0, 0, 0, 0)
        val ERROR = BookletBannerData("Error", 0, 0, 0, 0, 0, 0)
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

fun OutlinedLibrary.toSortedBookletBanners(): MutableList<BookletBannerData> {
    val result = mutableListOf<BookletBannerData>()
    this.forEach { outlinedBooklet ->
        result.add(outlinedBooklet.toBookletBanner())
    }
    result.sortBy { it.name.toLowerCase(Locale.getDefault()) }
    return result
}

fun OutlinedBooklet.toBookletBanner(): BookletBannerData = BookletBannerData(booklet, outline)
