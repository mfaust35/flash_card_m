package com.faust.m.flashcardm.core.data

import com.faust.m.flashcardm.core.domain.Booklet

interface BookletDataSource {

    fun add(booklet: Booklet): Booklet

    fun renameBooklet(newName: String, bookletId: Long): Boolean

    fun getAllBooklet(): List<Booklet>

    fun getBooklet(bookletId: Long): Booklet?

    fun delete(booklet: Booklet): Int
}