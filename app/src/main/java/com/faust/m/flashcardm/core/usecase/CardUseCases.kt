package com.faust.m.flashcardm.core.usecase

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.Deck

class CardUseCases private constructor(val addCard: AddCard,
                                       val deleteCards: DeleteCards,
                                       val getLiveDeck: GetLiveDeck,
                                       val updateCard: UpdateCard,
                                       val updateCardContent: UpdateCardContent) {

    constructor(cardRepository: CardRepository): this(
        AddCard(cardRepository),
        DeleteCards(cardRepository),
        GetLiveDeck(cardRepository),
        UpdateCard(cardRepository),
        UpdateCardContent(cardRepository)
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
                        attachCardContent: Boolean = false,
                        filterToReviewCard: Boolean = false): LiveData<Deck> =
        cardRepository.getLiveDeckForBooklet(bookletId, attachCardContent, filterToReviewCard)
}

class UpdateCard(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCard(card)
}

class UpdateCardContent(private val cardRepository: CardRepository) {

    operator fun invoke(card: Card): Card = cardRepository.updateCardContent(card)
}
