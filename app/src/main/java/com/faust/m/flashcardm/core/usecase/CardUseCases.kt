package com.faust.m.flashcardm.core.usecase

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card

class CardUseCases private constructor(val addCard: AddCard,
                                       val deleteCard: DeleteCard,
                                       val getCardsForBooklet: GetCardsForBooklet,
                                       val updateCard: UpdateCard,
                                       val updateCardContent: UpdateCardContent) {

    constructor(cardRepository: CardRepository): this(
        AddCard(cardRepository),
        DeleteCard(cardRepository),
        GetCardsForBooklet(cardRepository),
        UpdateCard(cardRepository),
        UpdateCardContent(cardRepository)
    )
}

class AddCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.addCard(card)
}

class DeleteCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Int = cardRepository.deleteCard(card)
}

class GetCardsForBooklet(private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long, filterReviewCard: Boolean = false): List<Card> =
        cardRepository.getAllCardsForBooklet(bookletId).run {
            when {
                filterReviewCard -> filter(Card::needReview)
                else -> this
            }
        }
}

class UpdateCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCard(card)
}

class UpdateCardContent(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCardContent(card)
}
