package com.faust.m.flashcardm.presentation.booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.framework.UseCases
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class CardsViewModel(private val bookletId: Long): ViewModel(), KoinComponent {

    private val useCases: UseCases by inject()

    private val _cards: MutableLiveData<MutableList<BookletCard>> =
        MutableLiveData<MutableList<BookletCard>>().apply {
            GlobalScope.launch {
                loadCards()
            }
        }
    val cards: LiveData<MutableList<BookletCard>> = _cards

    private fun loadCards() = mutableListOf<BookletCard>()
        .apply {
            useCases.getCardsForBooklet(bookletId).forEach {
                add(BookletCard(it))
            }
        }
        .also {
            _cards.postValue(it)
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