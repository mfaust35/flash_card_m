package com.faust.m.flashcardm.core.usecase.booklet

import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.domain.Booklet

class DeleteBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(booklet: Booklet): Int = bookletRepository.delete(booklet)
}