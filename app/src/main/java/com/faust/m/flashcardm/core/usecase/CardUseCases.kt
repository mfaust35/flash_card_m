package com.faust.m.flashcardm.core.usecase

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card

class CardUseCases private constructor(val addCard: AddCard,
                                       val deleteCards: DeleteCards,
                                       val getCardsForBooklet: GetCardsForBooklet,
                                       val updateCard: UpdateCard,
                                       val updateCardContent: UpdateCardContent) {

    constructor(cardRepository: CardRepository): this(
        AddCard(cardRepository),
        DeleteCards(cardRepository),
        GetCardsForBooklet(cardRepository),
        UpdateCard(cardRepository),
        UpdateCardContent(cardRepository)
    )
}

class AddCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.addCard(card)
}

class DeleteCards(private val cardRepository: CardRepository) {

    operator fun invoke(cards :List<Card>): Int = cardRepository.deleteCards(cards)
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
