package com.faust.m.core.data

import com.faust.m.core.domain.Booklet

class BookletRepository(private val dataSource: BookletDataSource) {

    fun add(booklet: Booklet): Booklet = dataSource.add(booklet)

    fun renameBooklet(newName: String, bookletId: Long): Boolean =
        dataSource.renameBooklet(newName, bookletId)

    fun getAllBooklets() = dataSource.getAllBooklet()

    fun getBooklet(bookletId: Long) = dataSource.getBooklet(bookletId)

    fun delete(booklet: Booklet): Int = dataSource.delete(booklet)
}