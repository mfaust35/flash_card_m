package com.faust.m.core.data

import com.faust.m.core.domain.Card

interface CardDataSource {

    fun add(card: Card): Long

    fun getAllCardsForBooklet(bookletId: Long): List<Card>
}