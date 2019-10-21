package com.faust.m.flashcardm.presentation.booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Card
import com.faust.m.core.usecase.booklet.BookletOutline
import com.faust.m.flashcardm.framework.BookletUseCases
import com.faust.m.flashcardm.framework.CardUseCases
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.*
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import com.faust.m.flashcardm.presentation.notifyObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


enum class CardEditionState { EDIT, ADD, CLOSED }

class BookletViewModel(private val bookletId: Long): ViewModel(), KoinComponent, AnkoLogger {

    private val bookletUseCases: BookletUseCases by inject()
    private val cardUseCases: CardUseCases by inject()
    private val flashViewModel: FlashViewModel by inject()

    private val _booklet: MutableLiveData<LibraryBooklet> = MutableLiveData()
    val booklet: LiveData<LibraryBooklet> = _booklet

    private val _cards: MutableLiveList<Card> = MutableLiveList()

    private val _bookletCards: MutableLiveList<BookletCard> = MutableLiveList()
    val bookletCards: LiveData<MutableList<BookletCard>> =
        Transformations.switchMap(_cards) {
            _bookletCards.postValue(it.map { c -> BookletCard(c) }.toMutableList())
            _bookletCards
        }

    private val _currentCard: MutableLiveData<Card?> = MutableLiveData()
    val currentCard: LiveData<Card?> = _currentCard

    private val _cardEditionState: MutableLiveData<CardEditionState> = MutableLiveData(CLOSED)
    val cardEditionState: LiveData<CardEditionState> = _cardEditionState


    fun loadData() = GlobalScope.launch {
        cardUseCases.getCardsForBooklet(bookletId).let {
            _cards.postValue(it.toMutableList())
        }
        _booklet.postValue(loadBooklet())
    }

    private fun loadBooklet(): LibraryBooklet =
        bookletUseCases.getBooklet(bookletId)?.let { tBooklet ->
            val tOutlines = bookletUseCases.getBookletsOutlines(listOf(tBooklet))
            val tOutline = tOutlines[tBooklet.id] ?: BookletOutline.EMPTY
            LibraryBooklet(tBooklet, tOutline)
        } ?: LibraryBooklet.ERROR

    fun addCard(front: String, back: String) =
        _currentCard.value?.let {
            it.addFrontAsText(front)
            it.addBackAsText(back)
            GlobalScope.launch {
                cardUseCases.addCard(it).also { newCard ->
                    verbose { "Created a new card: $newCard" }
                    _cards.add(newCard)
                    _currentCard.postValue(Card(bookletId = bookletId))
                    flashViewModel.bookletsStateChanged()
                    _booklet.postValue(loadBooklet())
                }
            }
        }

    fun editCard(front: String, back: String) =
        _currentCard.value?.copy(createdAt = Date())?.let { cardToUpdate ->
            cardToUpdate.editFrontAsText(front)
            cardToUpdate.editBackAsText(back)
            GlobalScope.launch {
                cardUseCases.updateCardContent(cardToUpdate).also { updatedCard ->
                    verbose { "Updated a card: $updatedCard" }
                    flashViewModel.bookletsStateChanged()
                    _cards.updateCardValue(updatedCard)
                    _booklet.postValue(loadBooklet())
                }
            }
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

    fun stopCardEdition() {
        _cardEditionState.postValue(CLOSED)
        _currentCard.postValue(null)
    }

    fun startCardAddition() {
        _cardEditionState.postValue(ADD)
        when { _currentCard.value == null -> _currentCard.postValue(Card(bookletId = bookletId)) }
    }

    fun startCardEdition(card: BookletCard) {
        _currentCard.postValue(_cards.value?.find { it.id == card.id })
        _cardEditionState.postValue(EDIT)
    }
}

data class BookletCard(val front: String,
                       val back: String,
                       val id: Long = 0) {

    constructor(card: Card): this(
        card.frontAsTextOrNull() ?: "",
        card.backAsTextOrNull() ?: "",
        card.id
    )
}
