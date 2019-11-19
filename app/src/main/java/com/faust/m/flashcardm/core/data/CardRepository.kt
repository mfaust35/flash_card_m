package com.faust.m.flashcardm.core.data

import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = dataSource.add(card)

    fun updateCard(card: Card): Card = dataSource.update(card)

    fun updateCardContent(card: Card): Card = dataSource.updateCardContent(card)

    fun getLiveDeck(): LiveData<Deck> = dataSource.getLiveDeck()

    fun getAllCardsForBooklet(bookletId: Long) = dataSource.getAllCardsForBooklet(bookletId)

    fun getAllCardShellsForBooklets(bookletIds: List<Long>): LongSparseArray<MutableList<Card>> =
        dataSource.getAllCardShellsForBooklets(bookletIds)

    fun resetForReview(count: Int, bookletId: Long): Int = dataSource.resetForReview(count, bookletId)

    fun countCardForBooklets(bookletIds: List<Long>): Map<Long, Int> =
        dataSource.countCardsForBooklets(bookletIds)

    fun deleteCard(card: Card): Int = dataSource.deleteCard(card)
}
