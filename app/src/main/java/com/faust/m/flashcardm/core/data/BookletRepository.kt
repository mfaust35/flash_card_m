package com.faust.m.flashcardm.core.data

import com.faust.m.flashcardm.core.domain.Booklet

class BookletRepository(private val dataSource: BookletDataSource) {

    fun add(booklet: Booklet): Booklet = dataSource.add(booklet)

    fun renameBooklet(newName: String, bookletId: Long): Boolean =
        dataSource.renameBooklet(newName, bookletId)

    fun getLiveLibrary() = dataSource.getLiveLibrary()

    fun getAllBooklets() = dataSource.getAllBooklet()

    fun getBooklet(bookletId: Long) = dataSource.getBooklet(bookletId)

    fun delete(booklet: Booklet): Int = dataSource.delete(booklet)
}