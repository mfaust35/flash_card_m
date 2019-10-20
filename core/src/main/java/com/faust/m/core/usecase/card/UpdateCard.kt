package com.faust.m.core.usecase.card

import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card

class UpdateCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCard(card)
}