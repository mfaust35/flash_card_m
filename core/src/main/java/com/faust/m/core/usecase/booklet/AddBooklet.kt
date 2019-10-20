package com.faust.m.core.usecase.booklet

import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet

class AddBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(booklet: Booklet): Booklet = bookletRepository.add(booklet)
}