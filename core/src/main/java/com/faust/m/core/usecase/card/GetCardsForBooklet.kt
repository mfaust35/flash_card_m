package com.faust.m.core.usecase.card

import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Card

class GetCardsForBooklet(private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long, filterReviewCard: Boolean = false): List<Card> =
        cardRepository.getAllCardsForBooklet(bookletId).run {
            when {
                filterReviewCard -> filter(Card::needReview)
                else -> this
            }
        }
}