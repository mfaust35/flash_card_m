package com.faust.m.flashcardm.presentation.booklet

import android.view.View
import androidx.lifecycle.*
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.NEW
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.TRAINING
import com.faust.m.flashcardm.core.usecase.BookletUseCases
import com.faust.m.flashcardm.core.usecase.CardUseCases
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.*
import com.faust.m.flashcardm.presentation.fragment_edit_card.DelegateEditCard
import com.faust.m.flashcardm.presentation.fragment_edit_card.ViewModelEditCard
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import com.faust.m.flashcardm.presentation.library.toBookletBanner
import com.faust.m.flashcardm.presentation.notifyObserver
import kotlinx.coroutines.CoroutineScope
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

    // Initialize the delegate for card edition with listeners for onCardEdited & onCardCreated
    init {
        delegateEditCard.onCardCreated = ::onCardCreated
        delegateEditCard.onCardEdited = ::onCardEdited
    }


    private val bookletUseCases: BookletUseCases by inject()
    private val cardUseCases: CardUseCases by inject()


    // List of cards in booklet
    private val _cards: MutableLiveList<Card> = MutableLiveList()

    // Booklet information used to display the top banner
    private val _bookletBannerData: MutableLiveData<BookletBannerData> = MutableLiveData()
    val bookletBannerData: LiveData<BookletBannerData> =
        Transformations.switchMap(bookletUseCases.getLiveOutlinedBooklet(bookletId))
        { outlineBooklet ->
            _bookletBannerData.postValue(outlineBooklet.toBookletBanner())
            _bookletBannerData
        }

    // Mirror cards in booklet with values that view can display
    // An update on _cards will automatically trigger an update on _bookletCards
    private val _bookletCards: MutableLiveList<BookletCard> = MutableLiveList()
    val bookletCards: LiveData<MutableList<BookletCard>> =
        Transformations.switchMap(_cards) {
            _bookletCards.postValue(
                it.map { c -> BookletCard(c, _showRatingLevel) }.toMutableList()
            )
            _bookletCards
        }

    // State of deletion process
    private val _cardRemovalStatus: MutableLiveData<CardRemovalStatus> = MutableLiveData()
    val cardRemovalStatus: LiveData<CardRemovalStatus> =_cardRemovalStatus

    // Display or hide an indicator of ratingLevel
    private var _showRatingLevel = false
    val showRatingLevel: Boolean
        get() = _showRatingLevel


    override fun parentScope(): CoroutineScope? = viewModelScope

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            cardUseCases.getCardsForBooklet(bookletId).let {
                _cards.postValue(it.toMutableList())
            }
        }
    }


    fun startCardEdition(card: BookletCard) =
        delegateEditCard.startCardEdition(_cards.value?.find { it.id == card.id })

    fun startRemoveCards() {
        _cardRemovalStatus.postValue(SELECTING)
        _bookletCards.postCopyForEach { it.copy(showSelection = true, isSelected = false) }
    }

    fun stopRemoveCard() {
        _cardRemovalStatus.postValue(OFF)
        _bookletCards.postCopyForEach { it.copy(showSelection = false, isSelected = false) }
    }

    private fun MutableLiveList<BookletCard>
            .postCopyForEach(transformation: (oldCard :BookletCard) -> BookletCard) {
        value?.let { oldBookletCards ->
            oldBookletCards
                .map { bookletCard ->
                    transformation(bookletCard)
                }
                .toMutableList()
                .let { _bookletCards.postValue(it) }
        }
    }

    /**
     * Mark or unmark bookletCard as selected and trigger a redraw of card
     */
    fun switchBookletCardForRemoval(bookletCard: BookletCard) {
        _bookletCards.value?.let { oldBookletCards ->
            val indexOfCard = oldBookletCards.indexOf(bookletCard)
            oldBookletCards
                .toMutableList()
                .apply { set(indexOfCard, bookletCard.switchSelection()) }
                .let {_bookletCards.postValue(it) }
        }
    }

    fun deleteSelectedBookletCards() {
        _bookletCards.value?.let { tBookletCards ->
            val idsToDelete = tBookletCards.filter { it.isSelected }.map { it.id }.toSet()
            val oldCards = _cards.value ?: return

            // Remove cards from _cards in memory
            val cardsToRemove = oldCards.filter { idsToDelete.contains(it.id) }
            oldCards
                .apply { removeAll(cardsToRemove) }
                .let { _cards.postValue(it) }

            // Remove cards from database
            viewModelScope.launch(Dispatchers.IO) {
                cardUseCases.deleteCards(cardsToRemove)
            }

            _cardRemovalStatus.postValue(DELETED)
        }
    }

    fun switchShowRatingLevel() {
        _showRatingLevel = !_showRatingLevel
        _bookletCards.postCopyForEach { it.copy(showRating = _showRatingLevel) }
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


    private fun onCardCreated(newCard: Card) {
        _cards.add(newCard)
    }

    private fun onCardEdited(cardEdited: Card) {
        _cards.updateCardValue(cardEdited)
    }

    private fun MutableLiveList<Card>.updateCardValue(updatedCard: Card) =
        value?.let { cardsValue ->
            cardsValue.find { it.id == updatedCard.id }?.let { cardFound ->
                val indexOf = cardsValue.indexOf(cardFound)
                cardsValue.remove(cardFound)
                val newCard = cardFound.copyWithoutContent()
                updatedCard.frontAsTextOrNull()?.let { newCard.addFrontAsText(it) }
                updatedCard.backAsTextOrNull()?.let { newCard.addBackAsText(it) }
                cardsValue.add(indexOf, newCard)
                notifyObserver()
            }
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
