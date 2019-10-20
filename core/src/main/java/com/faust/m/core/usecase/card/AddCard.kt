package com.faust.m.core.usecase.card

import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card

class AddCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.addCard(card)
}