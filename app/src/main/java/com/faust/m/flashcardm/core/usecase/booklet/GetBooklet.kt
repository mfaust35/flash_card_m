package com.faust.m.flashcardm.core.usecase.booklet

import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.domain.Booklet

class GetBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(bookletId: Long): Booklet? = bookletRepository.getBooklet(bookletId)
}