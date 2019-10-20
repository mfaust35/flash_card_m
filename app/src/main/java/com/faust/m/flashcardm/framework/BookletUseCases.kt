package com.faust.m.flashcardm.framework

import com.faust.m.core.usecase.booklet.*

class BookletUseCases(val addBooklet: AddBooklet,
                      val deleteBooklet: DeleteBooklet,
                      val getBooklet: GetBooklet,
                      val getBooklets: GetBooklets,
                      val getBookletsOutlines: GetBookletsOutlines,
                      val renameBooklet: RenameBooklet
)