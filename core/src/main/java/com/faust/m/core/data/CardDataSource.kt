package com.faust.m.core.data

import com.faust.m.core.domain.Card

interface CardDataSource {

    fun add(card: Card): Card

    fun getAllCardsForBooklet(bookletId: Long): List<Card>

    fun countCardsForBooklet(bookletId: Long): Int
}