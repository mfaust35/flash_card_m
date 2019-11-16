package com.faust.m.flashcardm.core.data

import com.faust.m.flashcardm.core.LongSparseArrayList
import com.faust.m.flashcardm.core.domain.Card

interface CardDataSource {

    fun add(card: Card): Card

    fun update(card: Card): Card

    fun updateCardContent(card: Card): Card

    fun getAllCardsForBooklet(bookletId: Long): List<Card>

    /**
     * Return a sparseArray bookletId -> list<Card>
     */
    fun getAllCardShellsForBooklets(bookletIds: List<Long>): LongSparseArrayList<Card>

    fun countCardsForBooklets(bookletIds: List<Long>): Map<Long, Int> // booklet_id -> count

    fun resetForReview(count: Int, bookletId: Long)

    fun deleteCard(card: Card): Int
}