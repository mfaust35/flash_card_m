package com.faust.m.core.usecase

import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet

class DeleteBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(booklet: Booklet): Int = bookletRepository.delete(booklet)
}