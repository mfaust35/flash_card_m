package com.faust.m.core.data

import com.faust.m.core.domain.Booklet

class BookletRepository(val dataSource: BookletDataSource) {

    fun add(booklet: Booklet): Booklet = dataSource.add(booklet)

    fun getAllBooklet() = dataSource.getAllBooklet()
}