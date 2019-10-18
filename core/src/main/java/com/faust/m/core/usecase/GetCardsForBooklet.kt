package com.faust.m.core.usecase

import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card

class GetCardsForBooklet(private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long): List<Card> =
        cardRepository.getAllCardsForBooklet(bookletId)
}