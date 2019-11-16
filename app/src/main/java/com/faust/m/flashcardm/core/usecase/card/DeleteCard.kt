package com.faust.m.flashcardm.core.usecase.card

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card

class DeleteCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Int = cardRepository.deleteCard(card)
}