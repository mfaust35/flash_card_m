package com.faust.m.core.data

import com.faust.m.core.domain.FlashCard

class FlashCardRepository(private val dataSource: FlashCardDataSource) {

    fun add(flashCard: FlashCard): FlashCard = with(dataSource) {
        add(flashCard).let { flashCard.copy(id = it) }
    }

    fun getAllFlashCardForBooklet(bookletId: Long): List<FlashCard> =
        dataSource.getAllFlashCardForBooklet(bookletId)
}