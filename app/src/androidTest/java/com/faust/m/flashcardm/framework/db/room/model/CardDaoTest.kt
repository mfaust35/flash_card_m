package com.faust.m.flashcardm.framework.db.room.model

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class CardDaoTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("First Booklet", 42)
    private val cardEntity = CardEntity(1, Date(30), Date(30), 42, 10)
    private val cardContentEntity =
        CardContentEntity("Learn it", FRONT, 10, 25)

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
        given2BookletsAnd3CardsInDatabase()

        // The counts can be retrieved
        assertThat(cardDao.countCardsForBooklets(listOf(2)))
            .containsExactly(
                CardCountEntity(2, 1)
            )
        assertThat(cardDao.countCardsForBooklets(listOf(2, 3)))
            .containsExactlyInAnyOrder(
                CardCountEntity(2, 1),
                CardCountEntity(3, 2)
            )
    }

    private fun given2BookletsAnd3CardsInDatabase() {
        bookletDao.add(BookletEntity("", 2))
        bookletDao.add(BookletEntity("", 3))

        cardDao.add(CardEntity(rating = 2, lastSeen = Date(30), createdAt = Date(30), bookletId = 2, id = 2))
        cardDao.add(CardEntity(rating = 2, lastSeen = Date(30), createdAt = Date(30), bookletId = 3, id = 3))
        cardDao.add(CardEntity(rating = 3, lastSeen = Date(30), createdAt = Date(30), bookletId = 3, id = 4))
    }

    @Test
    fun testGetAllCardShellForBookletIds() {
        given2BookletsAnd3CardsInDatabase()

        // The card shells can be retrieved
        assertThat(cardDao.getAllCardsShellsForBooklets(listOf(2)))
            .containsExactly(
                CardEntity(rating = 2, lastSeen = Date(30), createdAt = Date(30), bookletId = 2, id = 2)
            )
        assertThat(cardDao.getAllCardsShellsForBooklets(listOf(2, 3)))
            .containsExactlyInAnyOrder(
                CardEntity(rating = 2, lastSeen = Date(30), createdAt = Date(30), bookletId = 2, id = 2),
                CardEntity(rating = 2, lastSeen = Date(30), createdAt = Date(30), bookletId = 3, id = 3),
                CardEntity(rating = 3, lastSeen = Date(30), createdAt = Date(30), bookletId = 3, id = 4)
            )
    }

    @Test
    fun testUpdateCreatedAtShouldUpdateFieldCreatedAtOnly() {
        // Given a booklet / card in database
        bookletDao.add(BookletEntity(
            name = "My first booklet",
            id = 24))
        cardDao.add(CardEntity(
            rating = 4,
            lastSeen = Date(20),
            createdAt = Date(300),
            bookletId = 24,
            id = 3
        ))

        // When I update createdAt
        cardDao.updateCreatedAt(Date(4000), 3)

        // The new card contain a new createdAt value
        cardDao.getAllCardsForBooklet(24).run {
            assertThat(size).isEqualTo(1)
            first(). let {
                assertThat(it).isEqualTo(CardEntity(
                    rating = 4,
                    lastSeen = Date(20),
                    createdAt = Date(4000),
                    bookletId = 24,
                    id = 3
                ))
            }
        }
    }
}
