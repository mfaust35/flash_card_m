package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Booklet

class BookletRepository(private val dataSource: BookletDataSource) {

    fun add(booklet: Booklet): Booklet = dataSource.add(booklet)

    fun renameBooklet(newName: String, bookletId: Long): Boolean =
        dataSource.renameBooklet(newName, bookletId)

    fun getLiveBooklet(bookletId: Long): LiveData<Booklet?> = dataSource.getLiveBooklet(bookletId)

    fun getLiveLibrary() = dataSource.getLiveLibrary()

    fun delete(booklet: Booklet): Int = dataSource.delete(booklet)
}