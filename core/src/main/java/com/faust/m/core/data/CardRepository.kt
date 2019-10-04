package com.faust.m.core.data

import com.faust.m.core.domain.Card

class CardRepository(private val dataSource: CardDataSource) {

    fun addCard(card: Card): Card = dataSource.add(card)

    fun getAllCardsForBooklet(bookletId: Long) = dataSource.getAllCardsForBooklet(bookletId)

    fun countCardForBooklet(bookletId: Long): Int = dataSource.countCardsForBooklet(bookletId)
}
