package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = dataSource.add(card)

    fun updateCard(card: Card): Card = dataSource.update(card)

    fun updateCardContent(card: Card): Card = dataSource.updateCardContent(card)

    fun getLiveDeck(): LiveData<Deck> = dataSource.getLiveDeck()

    fun getLiveDeckForBooklet(bookletId: Long,
                              attachCardContent: Boolean = false,
                              filterToReviewCard: Boolean = false): LiveData<Deck> =
        dataSource.getLiveDeckForBooklet(bookletId, attachCardContent, filterToReviewCard)

    fun resetForReview(count: Int, bookletId: Long): Int = dataSource.resetForReview(count, bookletId)

    fun deleteCards(cards: List<Card>): Int = dataSource.deleteCards(cards)
}
