package com.faust.m.flashcardm.framework

import com.faust.m.core.usecase.card.AddCard
import com.faust.m.core.usecase.card.GetCardsForBooklet
import com.faust.m.core.usecase.card.UpdateCard
import com.faust.m.core.usecase.card.UpdateCardContent

class CardUseCases(val addCard: AddCard,
                   val getCardsForBooklet: GetCardsForBooklet,
                   val updateCard: UpdateCard,
                   val updateCardContent: UpdateCardContent
)