package com.faust.m.flashcardm.presentation.booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Card
import com.faust.m.core.usecase.BookletOutline
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.framework.UseCases
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.*
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


enum class CardEditionState { EDIT, ADD, CLOSED }

class BookletViewModel(private val bookletId: Long): ViewModel(), KoinComponent, AnkoLogger {

    private val useCases: UseCases by inject()
    private val flashViewModel: FlashViewModel by inject()

    private val _booklet: MutableLiveData<LibraryBooklet> = MutableLiveData<LibraryBooklet>().apply {
        GlobalScope.launch {
            loadBooklet()
        }
    }
    val booklet: LiveData<LibraryBooklet> = _booklet

    private val _cards: MutableLiveList<Card> = MutableLiveList<Card>().apply {
        GlobalScope.launch {
            loadCards()
        }
    }
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


    private  fun loadBooklet() = useCases.getBooklet(bookletId)?.let { tBooklet ->
        val tOutlines = useCases.getBookletsOutlines(listOf(tBooklet))
        val tOutline = tOutlines[tBooklet.id] ?: BookletOutline.EMPTY
        _booklet.postValue(LibraryBooklet(tBooklet, tOutline))
    }

    private fun loadCards() =
        useCases.getCardsForBooklet(bookletId).apply {
            _cards.postValue(this.toMutableList())
        }

    fun addCard(front: String, back: String) =
        _currentCard.value?.let {
            it.addFrontAsText(front)
            it.addBackAsText(back)
            GlobalScope.launch {
                useCases.addCard(it).also { newCard ->
                    verbose { "Created a new card: $newCard" }
                    _cards.add(newCard)
                    _currentCard.postValue(Card(bookletId = bookletId))
                    flashViewModel.bookletsStateChanged()
                }
            }
        }

    fun editCard(front: String, back: String) =
        _currentCard.value?.copy(createdAt = Date())?.let {
            it.editFrontAsText(front)
            it.editBackAsText(back)
            GlobalScope.launch {
                useCases.updateCardContent(it).also { updatedCard ->
                    verbose { "Updated a card: $updatedCard" }
                    loadCards()
                    flashViewModel.bookletsStateChanged()
                }
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
