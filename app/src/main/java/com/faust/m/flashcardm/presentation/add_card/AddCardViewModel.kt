package com.faust.m.flashcardm.presentation.add_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject

class AddCardViewModel(private val bookletId: Long): ViewModel(), KoinComponent, AnkoLogger {

    private val cardRepository: CardRepository by inject()

    private val card: MutableLiveData<Card> = MutableLiveData(Card(bookletId = bookletId))

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
                cardRepository.addCard(it).also { newCard ->
                    verbose { "Created a new card: $newCard" }
                    card.postValue(Card(bookletId = bookletId))
                }
            }
        }
    }
}
