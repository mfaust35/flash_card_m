package com.faust.m.flashcardm.core.usecase.card

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card

class AddCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.addCard(card)
}