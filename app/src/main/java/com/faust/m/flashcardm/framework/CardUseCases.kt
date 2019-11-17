package com.faust.m.flashcardm.framework

import com.faust.m.flashcardm.core.usecase.card.*

class CardUseCases(val addCard: AddCard,
                   val deleteCard: DeleteCard,
                   val getCardsForBooklet: GetCardsForBooklet,
                   val updateCard: UpdateCard,
                   val updateCardContent: UpdateCardContent
)