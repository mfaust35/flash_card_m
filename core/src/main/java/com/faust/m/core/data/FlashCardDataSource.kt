package com.faust.m.core.data

import com.faust.m.core.domain.FlashCard

interface FlashCardDataSource {

    fun add(flashCard: FlashCard): Long

    fun getAllFlashCardForBooklet(bookletId: Long): List<FlashCard>
}