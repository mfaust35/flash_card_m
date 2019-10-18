package com.faust.m.flashcardm.presentation.booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.framework.UseCases
import com.faust.m.flashcardm.presentation.Event
import com.faust.m.flashcardm.presentation.notifyObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject

class BookletViewModel(private val bookletId: Long): ViewModel(), KoinComponent, AnkoLogger {

    private val useCases: UseCases by inject()
    private val flashViewModel: FlashViewModel by inject()

    private val _cards: MutableLiveData<MutableList<BookletCard>> =
        MutableLiveData<MutableList<BookletCard>>().apply {
            GlobalScope.launch {
                loadCards()
            }
        }
    val cards: LiveData<MutableList<BookletCard>> = _cards

    private val _eventStopAdd: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val eventStopAdd: LiveData<Event<Boolean>> = _eventStopAdd

    fun triggerStopAdd() {
        _eventStopAdd.postValue(Event(true))
    }

    private fun loadCards() = mutableListOf<BookletCard>()
        .apply {
            useCases.getCardsForBooklet(bookletId).forEach {
                add(BookletCard(it))
            }
        }
        .also {
            _cards.postValue(it)
        }

    private val card: MutableLiveData<Card> = MutableLiveData()

    fun setupCard() {
        GlobalScope.launch {
            card.postValue(Card(bookletId = bookletId))
        }
    }

    fun getCard(): LiveData<Card> = card

    fun updateCardFront(front: String) {
        card.value?.add(CardContent(front, "front"))
    }

    fun updateCardBack(back: String) {
        card.value?.add(CardContent(back, "back"))
    }

    fun addCard() {
        GlobalScope.launch {
            card.value?.let {
                useCases.addCard(it).also { newCard ->
                    _cards.value?.add(BookletCard(newCard))
                    _cards.notifyObserver()
                    verbose { "Created a new card: $newCard" }
                    card.postValue(Card(bookletId = bookletId))
                    flashViewModel.bookletsStateChanged()
                }
            }
        }
    }
}

data class BookletCard(val front: String,
                       val back: String,
                       val id: Long = 0) {

    constructor(card: Card): this(
        card.frontAsTextOrNull() ?: "~~",
        card.backAsTextOrNull() ?: "~~",
        card.id
    )
}