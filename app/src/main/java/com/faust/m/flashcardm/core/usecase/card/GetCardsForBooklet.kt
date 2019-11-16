package com.faust.m.flashcardm.core.usecase.card

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card

class GetCardsForBooklet(private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long, filterReviewCard: Boolean = false): List<Card> =
        cardRepository.getAllCardsForBooklet(bookletId).run {
            when {
                filterReviewCard -> filter(Card::needReview)
                else -> this
            }
        }
}