package com.faust.m.core.usecase.booklet

import com.faust.m.core.data.BookletRepository
import com.faust.m.core.domain.Booklet

class GetBooklets(private val bookletRepository: BookletRepository) {

    operator fun invoke(): List<Booklet> = bookletRepository.getAllBooklets()
}