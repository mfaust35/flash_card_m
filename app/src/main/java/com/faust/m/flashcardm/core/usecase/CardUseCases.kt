package com.faust.m.flashcardm.core.usecase

import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.FilterState

class CardUseCases private constructor(val addCard: AddCard,
                                       val deleteCards: DeleteCards,
                                       val getLiveDeck: GetLiveDeck,
                                       val updateCard: UpdateCard,
                                       val updateCardWithContent: UpdateCardWithContent) {

    constructor(cardRepository: CardRepository): this(
        AddCard(cardRepository),
        DeleteCards(cardRepository),
        GetLiveDeck(cardRepository),
        UpdateCard(cardRepository),
        UpdateCardWithContent(cardRepository)
    )
}

class AddCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.addCard(card)
}

class DeleteCards(private val cardRepository: CardRepository) {

    operator fun invoke(cards: List<Card>): Int = cardRepository.deleteCards(cards)
}

class GetLiveDeck(private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long,
                        attachCardContent: Boolean,
                        filterState: FilterState = FilterState()) =
        cardRepository.getLiveDeckForBooklet(bookletId, attachCardContent, filterState)
}

class UpdateCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCard(card)
}

class UpdateCardWithContent(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCardWithContent(card)
}
