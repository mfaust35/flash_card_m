package com.faust.m.flashcardm.presentation.booklet

import android.view.View
import androidx.lifecycle.*
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.NEW
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.TRAINING
import com.faust.m.flashcardm.core.domain.Deck
import com.faust.m.flashcardm.core.usecase.BookletUseCases
import com.faust.m.flashcardm.core.usecase.CardUseCases
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.*
import com.faust.m.flashcardm.presentation.fragment_edit_card.DelegateEditCard
import com.faust.m.flashcardm.presentation.fragment_edit_card.ViewModelEditCard
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import com.faust.m.flashcardm.presentation.library.toBookletBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.koin.core.KoinComponent
import org.koin.core.inject


class BookletViewModel @JvmOverloads constructor(
    private val bookletId: Long,
    private val delegateEditCard: ViewModelEditCard = DelegateEditCard(bookletId)
): ViewModel(),
    KoinComponent,
    ViewModelEditCard by delegateEditCard,
    AnkoLogger {


    private val bookletUseCases: BookletUseCases by inject()
    private val cardUseCases: CardUseCases by inject()


    // Booklet information used to display the top banner
    private val _bookletBannerData: MutableLiveData<BookletBannerData> = MutableLiveData()
    val bookletBannerData: LiveData<BookletBannerData> =
        Transformations.switchMap(bookletUseCases.getLiveOutlinedBooklet(bookletId))
        { outlineBooklet ->
            _bookletBannerData.postValue(outlineBooklet.toBookletBanner())
            _bookletBannerData
        }


    // List of cards in booklet
    private val _cards: LiveData<Deck> =
        cardUseCases.getLiveDeck(
            bookletId,
            attachCardContent = true,
            filterToReviewCard = false)

    // State of deletion process
    private val _cardRemovalStatus: MutableLiveData<CardRemovalStatus> = MutableLiveData()
    val cardRemovalStatus: LiveData<CardRemovalStatus> =_cardRemovalStatus

    // Card display data
    val bookletCards: MutableLiveData<MutableList<BookletCard>> = MediatorLiveData<MutableList<BookletCard>>()
        .apply {
            addSource(_cards) { deck ->
                this.value = deck
                    .map { c -> c.toBookletCard(showRatingLevel) }
                    .toMutableList()
            }
    }

    // Display or hide an indicator of ratingLevel
    private var _showRatingLevel = false
    val showRatingLevel: Boolean
        get() = _showRatingLevel


    fun onCardClicked(card: BookletCard) {
        when (cardRemovalStatus.value) {
            SELECTING -> switchBookletCardForRemoval(card)
            else -> startCardEdition(card)
        }
    }

    private fun startCardEdition(bookletCard: BookletCard) =
        delegateEditCard.startCardEdition(_cards.value?.find { it.id == bookletCard.id })

    fun startRemoveCards() {
        _cardRemovalStatus.postValue(SELECTING)
        bookletCards.postCopyForEach { it.copy(showSelection = true, isSelected = false) }
    }

    fun stopRemoveCard() {
        _cardRemovalStatus.postValue(OFF)
        bookletCards.postCopyForEach { it.copy(showSelection = false, isSelected = false) }
    }

    private fun MutableLiveData<MutableList<BookletCard>>
            .postCopyForEach(transformation: (oldBookletCard :BookletCard) -> BookletCard) {
        value?.let { oldBookletCards ->
            oldBookletCards
                .map { bookletCard ->
                    transformation(bookletCard)
                }
                .toMutableList()
                .let { this.postValue(it) }
        }
    }

    /**
     * Mark bookletCard as selected or not and trigger a redraw of card
     */
    fun switchBookletCardForRemoval(bookletCard: BookletCard) {
        bookletCards.value?.let { oldBookletCards ->
            oldBookletCards
                .toMutableList()
                .replaceBookletCardWithCopy(bookletCard, bookletCard.switchSelection())
                .let { bookletCards.postValue(it) }
        }
    }

    private fun MutableList<BookletCard>.replaceBookletCardWithCopy(oldValue: BookletCard, copy :BookletCard) :
            MutableList<BookletCard> = this.apply {
        val indexOfCard = indexOf(oldValue)
        set(indexOfCard, copy)
    }

    fun deleteSelectedBookletCards() {
        bookletCards.value?.let { tBookletCards ->
            val idsToDelete = tBookletCards.filter { it.isSelected }.map { it.id }.toSet()
            val cardsToRemove = _cards.value?.filter { idsToDelete.contains(it.id) } ?: return

            // Remove cards from database
            viewModelScope.launch(Dispatchers.IO) {
                cardUseCases.deleteCards(cardsToRemove)
            }

            _cardRemovalStatus.postValue(DELETED)
        }
    }

    fun switchShowRatingLevel() {
        _showRatingLevel = !_showRatingLevel
        bookletCards.postCopyForEach { it.copy(showRating = _showRatingLevel) }
    }

    override fun onBackPressed(): Boolean =
        when {
            delegateEditCard.onBackPressed() -> true
            _cardRemovalStatus.value == SELECTING -> {
                stopRemoveCard()
                true
            }
            _showRatingLevel -> {
                switchShowRatingLevel()
                true
            }
            else -> false
        }
}

data class BookletCard(val front: String,
                       val back: String,
                       val _color: Int,
                       val showRating: Boolean,
                       val showSelection: Boolean,
                       val isSelected: Boolean,
                       val id: Long = 0) {

    constructor(card: Card, showRating: Boolean): this(
        card.frontAsTextOrNull() ?: "",
        card.backAsTextOrNull() ?: "",
        card.ratingLevel().toColor(),
        showRating,
        false,
        false,
        card.id
    )

    val color: Int
        get() = if (showRating) _color else R.color.colorWhite

    val selectedVisibility: Int
        get() = if (showSelection) View.VISIBLE else View.GONE

    fun switchSelection(): BookletCard = copy(isSelected = !isSelected)
}

private fun Card.RatingLevel.toColor(): Int = when(this) {
        NEW -> R.color.colorNewCard
        TRAINING -> R.color.colorTrainingCard
        else -> R.color.colorFamiliarCard
    }

enum class CardRemovalStatus { OFF, DELETED, SELECTING }

private fun Card.toBookletCard(showRating: Boolean): BookletCard = BookletCard(this, showRating)
