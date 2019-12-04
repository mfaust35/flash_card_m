package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck
import com.faust.m.flashcardm.core.domain.FilterState

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = dataSource.add(card)

    fun updateCard(card: Card): Card = dataSource.update(card)

    fun updateCardWithContent(card: Card): Card = dataSource.updateCardWithContent(card)

    fun getLiveDeck(): LiveData<Deck> = dataSource.getLiveDeck()

    fun getLiveDeckForBooklet(bookletId: Long,
                              attachCardContent: Boolean = false,
                              filters: FilterState = FilterState()): LiveData<Deck> =
        dataSource.getLiveDeckForBooklet(bookletId, attachCardContent, filters)

    fun resetForReview(count: Int, bookletId: Long): Int = dataSource.resetForReview(count, bookletId)

    fun deleteCards(cards: List<Card>): Int = dataSource.deleteCards(cards)
}
