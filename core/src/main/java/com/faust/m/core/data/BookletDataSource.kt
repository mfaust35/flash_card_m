package com.faust.m.core.data

import com.faust.m.core.domain.Booklet

interface BookletDataSource {

    fun add(booklet: Booklet): Long

    fun getAllBooklet(): List<Booklet>
}