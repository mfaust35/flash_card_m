package com.faust.m.flashcardm.presentation.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.RATING
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
    private var card: Card? = null

    fun getCurrentCard(): LiveData<CurrentCard> = currentCard

    fun switchCurrent() {
        currentCard.postValue(nextCard())
    }

    fun repeatCurrentCard() {
        card?.let { cardQueue.add(it) }
        currentCard.postValue(nextCard())
    }

    private fun loadQueue() {
        cardRepository
            .getAllCardsForBooklet(bookletId)
            .forEach { cardQueue.add(it) }
        currentCard.postValue(nextCard())
    }

    private fun nextCard(): CurrentCard {
        if (currentCard.value == null || currentCard.value?.state == RATING) {
            if (cardQueue.isNotEmpty()) {
                val nextCard = cardQueue.remove()
                card = nextCard
                nextCard.let {
                    return CurrentCard(
                        it.frontAsTextOrNull() ?: "??",
                        it.backAsTextOrNull() ?: "??",
                        ASKING
                    )
                }
            }
            else {
                return CurrentCard.EMPTY
            }
        }
        else {
            return currentCard.value?.copy(state = RATING) ?: CurrentCard.EMPTY
        }
    }
}

data class CurrentCard(val front: String, val back: String, val state: State) {

    companion object {
        val EMPTY = CurrentCard("--", "--", ASKING)
    }

    enum class State { ASKING, RATING }
}
