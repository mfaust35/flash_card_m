package com.faust.m.core.data

import com.faust.m.core.domain.Card

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = with(dataSource) {
        add(card).let { card.copy(id = it) }
    }

    fun getAllCardsForBooklet(bookletId: Long) = dataSource.getAllCardsForBooklet(bookletId)
}
