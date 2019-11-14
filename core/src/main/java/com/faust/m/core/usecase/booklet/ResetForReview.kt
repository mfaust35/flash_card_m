package com.faust.m.core.usecase.booklet

import com.faust.m.core.data.CardRepository

class ResetForReview(private val cardRepository: CardRepository) {

    operator fun invoke(count: Int, bookletId: Long) =
        cardRepository.resetForReview(count, bookletId)
}
