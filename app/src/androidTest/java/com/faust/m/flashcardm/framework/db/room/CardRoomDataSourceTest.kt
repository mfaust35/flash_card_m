package com.faust.m.flashcardm.framework.db.room

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class CardRoomDataSourceTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("My Second Booklet", 25)
    private val cardContent = CardContent("content", FRONT)
    private val card =
        Card(rating = 0, lastSeen = Date(), bookletId = bookletEntity.id).add(cardContent)

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
    fun testGetCardWithCardContentShouldReturnCompleteCard() {
        givenABookletInDatabase()

        // When I insert a card with cardContent
        cardRoomDataSource.add(card)

        // I can retrieve the card with the same attributes
        cardRoomDataSource.getAllCardsForBooklet(card.bookletId).apply {
            assertThat(size).`as`("Number of cards in cursor").isEqualTo(1)
            first().also {
                assertThat(it.bookletId)
                    .`as`("BookletId from card in cursor")
                    .isEqualTo(25)
                assertThat(it.content[FRONT]?.size)
                    .`as`("List of recto content from card in database")
                    .isEqualTo(1)
                assertThat(it.content[FRONT]?.first()?.value)
                    .`as`("First cardContent in card from database")
                    .isEqualTo("content")
            }
        }
    }

    private fun givenABookletInDatabase() {
        bookletDao.add(bookletEntity)
    }

    @Test
    fun testCountCardForBookletsReturnCount() {
        given2BookletsAnd3CardsInDatabase()

        // I can retrieve the card count by booklet id
        assertThat(cardRoomDataSource.countCardsForBooklets(listOf(10)))
            .isEqualTo(HashMap<Long, Int>().also{ it[10] = 1 })
        assertThat(cardRoomDataSource.countCardsForBooklets(listOf(10, 25)))
            .isEqualTo(HashMap<Long, Int>().also {
                it[10] = 1
                it[25] = 2
            })
    }

    private fun given2BookletsAnd3CardsInDatabase() {
        bookletDao.add(BookletEntity("My first booklet", 10))
        bookletDao.add(BookletEntity("My Second Booklet", 25))
        cardDao.add(CardEntity(5, Date(20), Date(20),10, 1))
        cardDao.add(CardEntity(5, Date(1000), Date(1000),25, 3))
        cardDao.add(CardEntity(5, Date(30000), Date(30000),25, 5))
    }

    @Test
    fun getAllCardShellsForBookletIdsShouldReturnCardShellForAllBooklets() {
        given2BookletsAnd3CardsInDatabase()

        // When I get all cardShells for 2 different booklets
        cardRoomDataSource.getAllCardShellsForBooklets(listOf(10, 25)).let {
            // The map contains a list of cardShell for each booklet
            assertThat(it[10])
                .`as`("CardShells for bookletId = 10")
                .containsExactly(
                    Card(5, Date(20), bookletId = 10, id = 1)
                )
            assertThat(it[25])
                .`as`("CardShells for bookletId = 25")
                .containsExactlyInAnyOrder(
                    Card(5, Date(1000), bookletId = 25, id = 3),
                    Card(5, Date(30000), bookletId = 25, id = 5)
                )
        }
    }

    @Test
    fun getAllCardShellsForBookletIdsShouldNoEntryIfCardListIsEmpty() {
        given2BookletsAnd3CardsInDatabase()

        // When I get all cardShells for a non existing booklet
        cardRoomDataSource.getAllCardShellsForBooklets(listOf(20)).let {
            // There is no entry in the map for non existing booklet
            assertThat(it[20]).isNull()
        }
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
        val cardToUpdate: Card = Card(createdAt = Date(3000), bookletId = 25, id = 1).apply {
            content[FRONT] = mutableListOf(
                CardContent(value = "new_value", type = FRONT, cardId = 1, id = 3)
            )
        }
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
            bookletId = 2,
            id = 3
        ).apply {
            content[FRONT] = mutableListOf(
                CardContent(value = "val", type = FRONT, cardId = 1, id = 0)
            )
        }
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
            bookletId = 25,
            id = 1
        ).apply {
            content[FRONT] = mutableListOf(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 1,
                id = 3
            ))
        }

        cardRoomDataSource.add(cardWithContent).let {
            assertThat(it.content[FRONT]).containsExactly(CardContent(
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
            bookletId = 25,
            id = 1
        ).apply {
            content[FRONT] = mutableListOf(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 0, // Card id is not set to the correct card yet
                id = 3
            ))
        }

        cardRoomDataSource.add(cardWithContent).let {
            assertThat(it.content[FRONT]).containsExactly(CardContent(
                value = "old_value",
                type = FRONT,
                cardId = 1,
                id = 3
            ))
        }
    }

    @Test
    fun testResetCardForReviewShouldMakeCardsAvailableForReview() {
        givenABookletAnd4CardsNotForReviewWithDifferentRating()

        // When I reset 2 cards for review
        cardRoomDataSource.resetForReview(2, 25)

        // There should be 2 card for review
        cardRoomDataSource.getAllCardsForBooklet(25).filter(Card::needReview).run {
            assertThat(size).`as`("There should be 2 card for review").isEqualTo(2)
        }
    }

    private fun givenABookletAnd4CardsNotForReviewWithDifferentRating() {
        bookletDao.add(bookletEntity)
        cardDao.add(CardEntity(4, Date(), Date(20),25, 1))
        cardDao.add(CardEntity(2, Date(), Date(20),25, 2))
        cardDao.add(CardEntity(1, Date(), Date(20),25, 3))
        cardDao.add(CardEntity(3, Date(), Date(20),25, 4))

        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assumeTrue("There should be no card to review yet", size == 0)
            }
    }

    @Test
    fun testResetCardForReviewShouldResetLowRatingCardFirst() {
        givenABookletAnd4CardsNotForReviewWithDifferentRating()

        // When I reset cards for review
        cardRoomDataSource.resetForReview(2, 25)

        // The cards for review should be the one with lowest rating
        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assertThat(this.map { it.rating }).containsExactlyInAnyOrder(1, 2)
            }
    }

    @Test
    fun testResetCardForReviewWhenNotEnoughCardShouldResetAsManyCardsAsPossible() {
        givenABookletAnd4CardsNotForReviewWithDifferentRating()

        // When I try to reset more card than possible
        cardRoomDataSource.resetForReview(5, 25)

        // All cards should be reset
        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assertThat(size).`as`("All card should need review").isEqualTo(4)
            }
    }

    @Test
    fun testResetCardForReviewShouldResetRatingUnder5IfNecessaryToMakeEnoughCardsForReview() {
        // Given a booklet with 2 card (one with rating 5, so unelectable for review normally)
        bookletDao.add(bookletEntity)
        cardDao.add(CardEntity(5, Date(), Date(20),25, 1))
        cardDao.add(CardEntity(2, Date(), Date(20),25, 2))

        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assumeTrue("There should be no card to review yet", size == 0)
            }

        // When I try to reset 2 cards for review
        cardRoomDataSource.resetForReview(2, 25)

        // The card with rating 5 should have dropped to 4 (rating 2 should stay the same)
        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assertThat(this.map { it.rating }).containsExactlyInAnyOrder(2, 4)
            }
    }

    @Test
    fun testResetCardForReviewShouldResetOnlyCardThatAreNotForReviewNow() {
        // Given a booklet with 3 cards (one already in review state)
        bookletDao.add(bookletEntity)
        cardDao.add(CardEntity(3, Date(30), Date(20),25, 1))
        cardDao.add(CardEntity(4, Date(), Date(20),25, 2))
        cardDao.add(CardEntity(4, Date(), Date(20),25, 3))

        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assumeTrue("There should be one card for review already", size == 1)
            }

        // When I try to reset 1 card for review
        cardRoomDataSource.resetForReview(1, 25)

        // There should be 2 cards for review now (the previous one,
        // plus the one that was just changed)
        cardRoomDataSource
            .getAllCardsForBooklet(25)
            .filter(Card::needReview)
            .run {
                assertThat(size).`as`("There should be 2 cards to review yet").isEqualTo(2)
            }
    }
}
