package com.faust.m.flashcardm.framework.db.room

import com.faust.m.core.data.BookletDataSource
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.framework.db.room.model.BookletDao
import com.faust.m.flashcardm.framework.db.room.model.BookletEntity

class BookletRoomDataSource(private val bookletDao: BookletDao): BookletDataSource {

    override fun add(booklet: Booklet): Booklet = with(bookletDao) {
        add(booklet.toEntityModel()).let {
            booklet.copy(id = it)
        }
    }

    override fun getAllBooklet(): List<Booklet> =
        bookletDao.getAllBooklets().map(::toEntityModel)

    override fun delete(booklet: Booklet): Int = bookletDao.delete(booklet.toEntityModel())

    private fun toEntityModel(bookletEntity: BookletEntity): Booklet =
        Booklet(bookletEntity.name, bookletEntity.id)

    private fun Booklet.toEntityModel(): BookletEntity =
        BookletEntity(name, id)
}