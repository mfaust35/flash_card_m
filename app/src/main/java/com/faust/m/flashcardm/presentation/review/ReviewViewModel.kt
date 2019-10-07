package com.faust.m.flashcardm.presentation.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.ASKING
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ReviewViewModel(private val bookletId: Long): ViewModel(), KoinComponent {

    private val cardRepository: CardRepository by inject()

    private val currentCard: MutableLiveData<CurrentCard> = MutableLiveData()

    private val cardQueue: Queue<Card> =
        LinkedList<Card>().apply {
            GlobalScope.launch {
                loadQueue()
            }
        }

    fun getCurrentCard(): LiveData<CurrentCard> = currentCard

    private fun loadQueue() {
        cardRepository
            .getAllCardsForBooklet(bookletId)
            .forEach { cardQueue.add(it) }
        if (cardQueue.isNotEmpty()) {
            cardQueue.remove()
                .let {
                    CurrentCard(
                        it.frontAsTextOrNull() ?: "??",
                        it.backAsTextOrNull() ?: "??",
                        ASKING)
                }
                .run {
                    currentCard.postValue(this)
                }
        }
        else {
            currentCard.postValue(CurrentCard("???", "???", ASKING))
        }
    }
}

data class CurrentCard(val front: String, val back: String, val state: State) {

    enum class State { ASKING, RATING }
}