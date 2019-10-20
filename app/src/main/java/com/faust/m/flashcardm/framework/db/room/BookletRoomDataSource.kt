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

    override fun renameBooklet(newName: String, bookletId: Long): Boolean = with (bookletDao) {
        updateName(newName, bookletId) == 1
    }

    override fun getAllBooklet(): List<Booklet> =
        bookletDao.getAllBooklets().map { it.toDomainModel() }

    override fun getBooklet(bookletId: Long): Booklet? =
        bookletDao.getBooklet(bookletId)?.toDomainModel()

    override fun delete(booklet: Booklet): Int = bookletDao.delete(booklet.toEntityModel())

    private fun BookletEntity.toDomainModel(): Booklet =
        Booklet(name, id)

    private fun Booklet.toEntityModel(): BookletEntity =
        BookletEntity(name, id)
}