package com.faust.m.flashcardm.framework.db.room

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.BaseDaoTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BookletRoomDataSourceTest: BaseDaoTest() {

    private val booklet = Booklet("My Third Booklet", 0)
    private lateinit var bookletRoomDataSource: BookletRoomDataSource

    override fun onDatabaseCreated(database: FlashRoomDatabase) = with(database) {
        bookletRoomDataSource = BookletRoomDataSource(bookletDao())
    }

    @Test
    fun testSaveAndGetBookletShouldReturnBookletWithNewId() {
        // When I save a booklet with id 0
        bookletRoomDataSource.add(booklet)

        // I can retrieve the booklet with a new Id and same parameters
        bookletRoomDataSource.getAllBooklet().run {
            assertThat(size).`as`("Number of booklet in cursor").isEqualTo(1)
            assertThat(first().name)
                .`as`("Name of retrieved booklet")
                .isEqualTo("My Third Booklet")
            assertThat(first().id).`as`("New Id of booklet").isNotEqualTo(0)
        }
    }
}
