package com.faust.m.flashcardm.framework.db.room.model

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BookletDaoTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("My First Booklet", 5)

    private lateinit var bookletDao: BookletDao

    override fun onDatabaseCreated(database: FlashRoomDatabase) {
        bookletDao = database.bookletDao()
    }

    @Test
    fun testGetBookletsShouldReturnInsertedBooklet() {
        // When inserting a new booklet in the database
        bookletDao.add(bookletEntity)

        // The booklet can be retrieved
        bookletDao.getAllBooklets().apply {
            assertThat(size).`as`("Number of booklets in cursor").isEqualTo(1)
            assertThat(first()).`as`("Booklet from cursor").isEqualTo(bookletEntity)
        }
    }
}
