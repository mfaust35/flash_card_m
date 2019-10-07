package com.faust.m.flashcardm.presentation.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.presentation.MutableLiveList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class ReviewViewModel(private val bookletId: Long): ViewModel(), KoinComponent {

    private val cardRepository: CardRepository by inject()

    private val cards: MutableLiveList<Card> by lazy {
        MutableLiveList<Card>().apply {
            GlobalScope.launch {
                loadBookletCards()
            }
        }
    }

    fun cards(): LiveData<out List<Card>> = cards

    private fun loadBookletCards() =
        mutableListOf<Card>()
            .apply {
                this.addAll(cardRepository.getAllCardsForBooklet(bookletId))
            }
            .also {
                cards.postValue(it)
            }
}