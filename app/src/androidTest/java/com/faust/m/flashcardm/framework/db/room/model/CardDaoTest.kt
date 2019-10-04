package com.faust.m.flashcardm.framework.db.room.model

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CardDaoTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("First Booklet", 42)
    private val cardEntity = CardEntity(42, 10)
    private val cardContentEntity =
        CardContentEntity("Learn it", "text", 10, 25)

    private lateinit var bookletDao: BookletDao
    private lateinit var cardDao: CardDao
    private lateinit var cardContentDao: CardContentDao


    override fun onDatabaseCreated(database: FlashRoomDatabase) = database.run {
        bookletDao = bookletDao()
        cardDao = cardDao()
        cardContentDao = cardContentDao()
    }

    @Test
    fun testGetAllCardsShouldReturnInsertedCard() {
        givenABookletInDatabase()

        // When inserting a new cardEntity in the database
        cardDao.add(cardEntity)

        // The cardEntity can be retrieved
        cardDao.getAllCards().apply {
            assertThat(size).`as`("Number of cards in cursor").isEqualTo(1)
            assertThat(first()).`as`("Card from cursor").isEqualTo(cardEntity)
        }
    }

    private fun givenABookletInDatabase() {
        bookletDao.add(bookletEntity)
    }

    @Test
    fun testGetCardContentsForCardShouldReturnInsertedCardContent() {
        // Given a booklet and some cardEntity content in the database
        givenABookletInDatabase()
        cardDao.add(cardEntity)

        // When inserting a new cardContentEntity in the database
        cardContentDao.add(cardContentEntity)

        // The cardEntity can be retrieved
        cardContentDao.getAllCardContentsForCard(cardEntity.id).apply {
            assertThat(size).`as`("Number of card contents in cursor").isEqualTo(1)
            assertThat(first())
                .`as`("Card content from cursor")
                .isEqualTo(cardContentEntity)
        }
    }


    @Test
    fun testCountCardForBookletsReturnCount() {
        // Given booklets in the database
        bookletDao.add(BookletEntity("", 2))
        bookletDao.add(BookletEntity("", 3))

        // When inserting 3 cards in the database
        cardDao.add(CardEntity(bookletId = 2))
        cardDao.add(CardEntity(bookletId = 3))
        cardDao.add(CardEntity(bookletId = 3))

        // The counts can be retrieved
        assertThat(cardDao.countCardsForBooklets(listOf(2)))
            .isEqualTo(listOf(CardCountEntity(2, 1)))
        assertThat(cardDao.countCardsForBooklets(listOf(2, 3)))
            .isEqualTo(listOf(CardCountEntity(2, 1), CardCountEntity(3, 2)))
    }
}
