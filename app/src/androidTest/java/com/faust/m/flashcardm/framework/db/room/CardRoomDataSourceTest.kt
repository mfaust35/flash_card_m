package com.faust.m.flashcardm.framework.db.room

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.CardContent
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.core.domain.toRoster
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class CardRoomDataSourceTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("My Second Booklet", 25)

    private lateinit var _database: FlashRoomDatabase
    private lateinit var cardRoomDataSource: CardRoomDataSource
    private lateinit var cardContentDao: CardContentDao
    private lateinit var cardDao: CardDao
    private lateinit var bookletDao: BookletDao

    override fun onDatabaseCreated(database: FlashRoomDatabase) = with(database) {
        _database = this
        cardRoomDataSource = CardRoomDataSource(database)
        bookletDao = bookletDao()
        cardDao = cardDao()
        cardContentDao = cardContentDao()
    }

    @Test
    fun updateCardContentShouldUpdateCardContentInDatabase() {
        givenABookletCardCardContent()

        whenIUpdateCardContent()

        // The card content value should be updated
        cardContentDao.getAllCardContentsForCard(1).run {
            assertThat(size).isEqualTo(1)
            firstOrNull()?.let {
                assertThat(it).isEqualTo(
                    CardContentEntity(
                        value = "new_value",
                        type = FRONT,
                        cardId = 1,
                        id = 3
                    )
                )
            }
        }
    }

    private fun givenABookletCardCardContent() {
        bookletDao.add(BookletEntity(
            name = "My Second Booklet",
            id =  25))
        cardDao.add(CardEntity(
            rating = 5,
            createdAt = Date(20),
            lastSeen = Date(30),
            bookletId = 25,
            id = 1))
        cardContentDao.add(CardContentEntity(
            value = "old_value",
            type = FRONT,
            cardId = 1,
            id = 3
        ))
    }

    private fun whenIUpdateCardContent() {
        val cardToUpdate = Card(
            createdAt = Date(3000),
            roster = mutableListOf(
                CardContent(value = "new_value", type = FRONT, cardId = 1, id = 3)
            ).toRoster(),
            bookletId = 25, id = 1
        )
        cardRoomDataSource.updateCardContent(cardToUpdate)
    }

    @Test
    fun updateCardContentShouldUpdateCreatedAtDateOfCard() {
        givenABookletCardCardContent()

        whenIUpdateCardContent()

        // Only the card createdAt should be updated
        cardDao.getAllCardsForBooklet(25).run {
            assertThat(size).isEqualTo(1)
            first().let {
                assertThat(it).isEqualTo(CardEntity(
                    rating = 5,
                    createdAt = Date(3000),
                    lastSeen = Date(30),
                    bookletId = 25,
                    id = 1
                ))
            }
        }
    }

    @Test
    fun addACardWithCardContentFailShouldRollBackTransactionAndThrowError() {
        // Given a booklet in database
        bookletDao.add(
            BookletEntity(
            name = "my first booklet",
            id = 2)
        )
        // Given database will throw an error during cardContent add
        val cardContentDao: CardContentDao = mockk()
        every { cardContentDao.add(any()) }.throws(RuntimeException("Random error"))
        val cardRoomDataSourceException = CardRoomDataSource(_database, cardDao, cardContentDao)

        // When I add a card in database
        var errorThrown = false
        val card = Card(
            rating = 2,
            lastSeen = Date(30),
            createdAt = Date(500),
            roster = mutableListOf(
                CardContent(value = "val", type = FRONT, cardId = 1, id = 0)
            ).toRoster(),
            bookletId = 2,
            id = 3
        )
        try {
            cardRoomDataSourceException.add(card)
        } catch (e: java.lang.RuntimeException) {
            errorThrown = true
        }

        // Then there was an error thrown
        assertThat(errorThrown).isTrue()
        // Then the card add has been roll back
        cardDao.getAllCardsForBooklet(2).run {
            assertThat(size).isEqualTo(0)
        }
    }

    @Test
    fun testAddCardShouldReturnABookletWithNoMoreContentCardThanTheCardToAdd() {
        // Given a booklet in database
        bookletDao.add(bookletEntity)

        val cardWithContent = Card(
            rating = 5,
            createdAt = Date(20),
            lastSeen = Date(30),
            roster = mutableListOf(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 1,
                id = 3
            )).toRoster(),
            bookletId = 25,
            id = 1
        )

        cardRoomDataSource.add(cardWithContent).let {
            assertThat(it.roster).containsExactly(CardContent(
                    value = "old_value",
                    type = FRONT,
                    cardId = 1,
                    id = 3
            ))
        }
    }

    @Test
    fun testAddCardWithNewCardContentShouldSetCardIdIntoCardContent() {
        // Given a booklet in database
        bookletDao.add(bookletEntity)

        val cardWithContent = Card(
            rating = 5,
            createdAt = Date(20),
            lastSeen = Date(30),
            roster = mutableListOf(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 0, // Card id is not set to the correct card yet
                id = 3
            )).toRoster(),
            bookletId = 25,
            id = 1
        )

        cardRoomDataSource.add(cardWithContent).let {
            assertThat(it.roster).containsExactly(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 1,
                id = 3
            ))
        }
    }
}
