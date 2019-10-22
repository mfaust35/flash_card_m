package com.faust.m.flashcardm.framework

import com.faust.m.core.usecase.card.*

class CardUseCases(val addCard: AddCard,
                   val deleteCard: DeleteCard,
                   val getCardsForBooklet: GetCardsForBooklet,
                   val updateCard: UpdateCard,
                   val updateCardContent: UpdateCardContent
)