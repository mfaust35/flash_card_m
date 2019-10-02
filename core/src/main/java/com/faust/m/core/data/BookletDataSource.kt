package com.faust.m.core.data

import com.faust.m.core.domain.Booklet

interface BookletDataSource {

    fun add(booklet: Booklet): Booklet

    fun getAllBooklet(): List<Booklet>

    fun delete(booklet: Booklet): Int
}