package com.faust.m.flashcardm.core.usecase.booklet

import com.faust.m.flashcardm.core.data.BookletRepository

class RenameBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(newName: String, bookletId: Long): Boolean =
        bookletRepository.renameBooklet(newName, bookletId)
}