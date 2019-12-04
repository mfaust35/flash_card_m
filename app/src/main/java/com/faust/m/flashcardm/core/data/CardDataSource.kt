package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck
import com.faust.m.flashcardm.core.domain.FilterState

interface CardDataSource {

    fun add(card: Card): Card

    fun update(card: Card): Card

    fun updateCardWithContent(card: Card): Card

    fun getLiveDeck(): LiveData<Deck>

    fun getLiveDeckForBooklet(bookletId: Long,
                              attachCardContent: Boolean = false,
                              filterState: FilterState = FilterState()): LiveData<Deck>

    fun resetForReview(count: Int, bookletId: Long): Int

    fun deleteCards(cards: List<Card>): Int
}
