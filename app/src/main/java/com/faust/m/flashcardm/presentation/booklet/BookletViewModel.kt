package com.faust.m.flashcardm.presentation.booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.NEW
import com.faust.m.flashcardm.core.domain.Card.RatingLevel.TRAINING
import com.faust.m.flashcardm.framework.CardUseCases
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.State.*
import com.faust.m.flashcardm.presentation.fragment_edit_card.DelegateEditCard
import com.faust.m.flashcardm.presentation.fragment_edit_card.ViewModelEditCard
import com.faust.m.flashcardm.presentation.notifyObserver
import com.faust.m.flashcardm.presentation.view_library_booklet.DelegateLibraryBooklet
import com.faust.m.flashcardm.presentation.view_library_booklet.ViewModelLibraryBooklet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.koin.core.KoinComponent
import org.koin.core.inject


class BookletViewModel @JvmOverloads constructor(
    private val bookletId: Long,
    private val delegateEditCard: ViewModelEditCard = DelegateEditCard(bookletId),
    private val delegateLibraryBooklet: ViewModelLibraryBooklet = DelegateLibraryBooklet(bookletId)
): ViewModel(),
    KoinComponent,
    ViewModelEditCard by delegateEditCard,
    ViewModelLibraryBooklet by delegateLibraryBooklet,
    AnkoLogger {

    // Initialize the delegate for card edition with listeners for onCardEdited & onCardCreated
    init {
        delegateEditCard.onCardCreated = ::onCardCreated
        delegateEditCard.onCardEdited = ::onCardEdited
    }


    private val cardUseCases: CardUseCases by inject()


    // List of cards in booklet
    private val _cards: MutableLiveList<Card> = MutableLiveList()

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


    override fun loadData() {
        delegateLibraryBooklet.loadData()
        GlobalScope.launch {
            cardUseCases.getCardsForBooklet(bookletId).let {
                _cards.postValue(it.toMutableList())
            }
        }
    }


    fun startCardEdition(card: BookletCard) =
        delegateEditCard.startCardEdition(_cards.value?.find { it.id == card.id })

    fun startRemoveCards() {
        _bookletCards.value?.let { oldBookletCards ->
            oldBookletCards
                .map { bookletCard -> bookletCard.copy(isSelected = false) }
                .toMutableList()
                .let { copyBookletCards ->
                    _bookletCards.postValue(copyBookletCards)
                }
        }
        _cardRemovalStatus.postValue(CardRemovalStatus(SELECTING))
    }

    fun stopRemoveCard() {
        _cardRemovalStatus.postValue(CardRemovalStatus(OFF))
    }

    /**
     * Mark or unmark bookletCard as selected and trigger a redraw of card
     */
    fun switchBookletCardForRemoval(bookletCard: BookletCard) {
        bookletCard.isSelected = !bookletCard.isSelected
        _bookletCards.notifyObserver()
    }

    fun deleteSelectedBookletCards() {
        _bookletCards.value?.let { tBookletCards ->
            val position = mutableSetOf<Int>()
            val bookletCardsToRemove = mutableListOf<BookletCard>()
            val cardsToRemove = mutableListOf<Card>()
            tBookletCards.forEachIndexed { index, tBookletCard ->
                if (tBookletCard.isSelected) {
                    position.add(index)
                    bookletCardsToRemove.add(tBookletCard)
                    _cards.value?.let {
                        it.find { c -> c.id == tBookletCard.id }?.let { c ->
                            cardsToRemove.add(c)
                        }
                    }
                }
            }

            // Remove cards and bookletsCard from _cards and _bookletCards
            _cards.value?.run { removeAll(cardsToRemove) }
            tBookletCards.removeAll(bookletCardsToRemove)

            // Remove card from dataSource
            GlobalScope.launch {
                cardsToRemove.forEach { cardUseCases.deleteCard(it) }

                _cardRemovalStatus.postValue(CardRemovalStatus(DELETED, position, tBookletCards))
                postBookletUpdate()
            }
        }
    }

    fun switchShowRatingLevel() {
        _showRatingLevel = !_showRatingLevel
        _bookletCards.value?.let { tOldBCards ->
            val tNewBCards = mutableListOf<BookletCard>()
            tOldBCards.forEach { oldBCard ->
                tNewBCards.add(oldBCard.copy(showRating = _showRatingLevel))
            }
            _bookletCards.postValue(tNewBCards)
        }
    }

    override fun onBackPressed(): Boolean =
        when {
            delegateEditCard.onBackPressed() -> true
            _cardRemovalStatus.value?.state == SELECTING -> {
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
        postBookletUpdate()
    }

    private fun onCardEdited(cardEdited: Card) {
        _cards.updateCardValue(cardEdited)
        postBookletUpdate()
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
                       var isSelected: Boolean,
                       val id: Long = 0) {

    constructor(card: Card, showRating: Boolean): this(
        card.frontAsTextOrNull() ?: "",
        card.backAsTextOrNull() ?: "",
        card.ratingLevel().toColor(),
        showRating,
        false,
        card.id
    )

    val color: Int
        get() = if (showRating) _color else R.color.colorWhite
}

private fun Card.RatingLevel.toColor(): Int = when(this) {
        NEW -> R.color.colorNewCard
        TRAINING -> R.color.colorTrainingCard
        else -> R.color.colorFamiliarCard
    }

data class CardRemovalStatus(val state: State,
                             val position: Set<Int> = setOf(),
                             val bookletCards: List<BookletCard> = listOf()) {

    enum class State { OFF, DELETED, SELECTING }
}
