package com.faust.m.flashcardm.framework

import com.faust.m.core.usecase.*

class UseCases(val getBookletsOutlines: GetBookletsOutlines,
               val addBooklet: AddBooklet,
               val deleteBooklet: DeleteBooklet,
               val getBooklets: GetBooklets,
               val updateCard: UpdateCard,
               val renameBooklet: RenameBooklet,
               val getCardsToReviewForBooklet: GetCardsToReviewForBooklet,
               val getCardsForBooklet: GetCardsForBooklet,
               val addCard: AddCard,
               val getBooklet: GetBooklet
)