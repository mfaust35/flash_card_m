package com.faust.m.core.usecase

import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card

class UpdateCardContent(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCardContent(card)
}