package com.faust.m.core.usecase

import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet

class GetBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(bookletId: Long): Booklet? = bookletRepository.getBooklet(bookletId)
}