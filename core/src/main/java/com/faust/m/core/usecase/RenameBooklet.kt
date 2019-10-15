package com.faust.m.core.usecase

import com.faust.m.core.data.BookletRepository

class RenameBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(newName: String, bookletId: Long): Boolean =
        bookletRepository.renameBooklet(newName, bookletId)
}