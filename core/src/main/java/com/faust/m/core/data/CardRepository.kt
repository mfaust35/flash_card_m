package com.faust.m.core.data

import androidx.collection.LongSparseArray
import com.faust.m.core.domain.Card

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = dataSource.add(card)

    fun updateCard(card: Card): Card = dataSource.update(card)

    fun updateCardContent(card: Card): Card = dataSource.updateCardContent(card)

    fun getAllCardsForBooklet(bookletId: Long) = dataSource.getAllCardsForBooklet(bookletId)

    fun getAllCardShellsForBooklets(bookletIds: List<Long>): LongSparseArray<MutableList<Card>> =
        dataSource.getAllCardShellsForBooklets(bookletIds)

    fun resetForReview(count: Int, bookletId: Long) = dataSource.resetForReview(count, bookletId)

    fun countCardForBooklets(bookletIds: List<Long>): Map<Long, Int> =
        dataSource.countCardsForBooklets(bookletIds)

    fun deleteCard(card: Card): Int = dataSource.deleteCard(card)
}
