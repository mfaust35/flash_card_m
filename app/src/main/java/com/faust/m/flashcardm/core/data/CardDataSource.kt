package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck

interface CardDataSource {

    fun add(card: Card): Card

    fun update(card: Card): Card

    fun updateCardContent(card: Card): Card

    fun getLiveDeck(): LiveData<Deck>

    fun getLiveDeckForBooklet(bookletId: Long): LiveData<Deck>

    fun getAllCardsForBooklet(bookletId: Long): List<Card>

    fun resetForReview(count: Int, bookletId: Long): Int

    fun deleteCard(card: Card): Int
}