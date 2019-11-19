package com.faust.m.flashcardm.core.data

import androidx.lifecycle.LiveData
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.domain.Library

interface BookletDataSource {

    fun add(booklet: Booklet): Booklet

    fun renameBooklet(newName: String, bookletId: Long): Boolean

    fun getLiveBooklet(bookletId: Long): LiveData<Booklet?>

    fun getLiveLibrary(): LiveData<Library>

    fun getAllBooklet(): List<Booklet>

    fun getBooklet(bookletId: Long): Booklet?

    fun delete(booklet: Booklet): Int
}