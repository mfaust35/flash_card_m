package com.faust.m.flashcardm.core.usecase.booklet

import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.domain.Booklet

class GetBooklets(private val bookletRepository: BookletRepository) {

    operator fun invoke(): List<Booklet> = bookletRepository.getAllBooklets()
}