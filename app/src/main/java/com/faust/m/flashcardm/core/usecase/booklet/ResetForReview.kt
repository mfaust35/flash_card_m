package com.faust.m.flashcardm.core.usecase.booklet

import com.faust.m.flashcardm.core.data.CardRepository

class ResetForReview(private val cardRepository: CardRepository) {

    operator fun invoke(count: Int, bookletId: Long) =
        cardRepository.resetForReview(count, bookletId)
}
