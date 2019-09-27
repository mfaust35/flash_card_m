package com.faust.m.core.data

import com.faust.m.core.domain.Booklet

class BookletRepository(val dataSource: BookletDataSource) {

    fun add(booklet: Booklet): Booklet = with(dataSource) {
        add(booklet).let { booklet.copy(id = it) }
    }

    fun getAllBooklet() = dataSource.getAllBooklet()
}