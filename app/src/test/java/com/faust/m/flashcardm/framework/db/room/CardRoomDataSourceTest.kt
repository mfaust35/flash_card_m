package com.faust.m.flashcardm.framework.db.room

import android.database.SQLException
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.CardContent
import com.faust.m.flashcardm.core.domain.CardContentType
import com.faust.m.flashcardm.core.domain.toRoster
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*
import java.util.concurrent.Callable


class CardRoomDataSourceTest {

    // DataSource to test
    private lateinit var cardRoomDataSource: CardRoomDataSource

    // Default CardEntity             -> id(5)
    //         + -- CardContentEntity -> id(+ -- 1)
    private val contentEntity = CardContentEntity(
        value = "Learn it",
        type = CardContentType.FRONT,
        cardId = 5,
        id = 1
    )
    private val cardEntity = CardEntity(
        rating = 2,
        nextReview = Date(30),
        updatedAt = Date(70),
        createdAt = Date(20),
        bookletId = 20,
        id = 5
    )
    // Corresponding default Card & CardContent
    private val cardContent = CardContent(
        value = "Learn it",
        type = CardContentType.FRONT,
        cardId = 5,
        id = 1
    )
    private val card = Card(
        rating = 2,
        nextReview = Date(30),
        updatedAt = Date(70),
        createdAt = Date(20),
        roster = mutableListOf(cardContent).toRoster(),
        bookletId = 20,
        id = 5
    )

    private val database: FlashRoomDatabase = mockk()
    private val cardDao: CardDao = mockk()
    private val cardContentDao: CardContentDao = mockk()

    @get:Rule
    val exception: ExpectedException = ExpectedException.none()

    @Before
    fun setup() {
        // Enable static to be able to run lambda in transaction
        mockkStatic("androidx.room.RoomDatabase")
        // Capture lambda from runInTransaction and run it
        val lambda = slot<Callable<Card>>()
        every { database.runInTransaction(capture(lambda)) } answers {
            lambda.captured.call()
        }

        every { database.cardDao() } returns cardDao
        every { database.cardContentDao() } returns cardContentDao
        cardRoomDataSource = CardRoomDataSource(database)
    }

    @After
    fun tearDown() {
        clearMocks(cardDao, cardContentDao, database)
    }


    @Test
    fun testDeleteCardsShouldReturnResultFromDaoDeleteAll() {
        // Given cardDao return 110 when delete cardEntity
        every { cardDao.deleteAll(cardEntity) } returns 110

        cardRoomDataSource.deleteCards(listOf(card)).let { result ->

            // Then method should return the result from cardDao
            assertThat(result).isEqualTo(110)
        }
    }

    @Test
    fun testUpdateCardContentShouldUpdateCardInCardDao() {
        givenCardDaoAndCardContentDaoReturnSuccessOnUpdate()

        cardRoomDataSource.updateCardWithContent(card)

        verifyAll {
            cardDao.update(cardEntity)
        }
    }


    @Test
    fun testUpdateCardContentShouldUpdateEveryCardContentInCardRoster() {
        givenCardDaoAndCardContentDaoReturnSuccessOnUpdate()

        cardRoomDataSource.updateCardWithContent(card)

        verifyAll {
            cardContentDao.updateAll(contentEntity)
        }
    }

    @Test
    fun `test AddCardWithContent When AddCardContentFail ShouldThrowExceptionBack`() {
        // If lambda throw exception here, RoomDatabase will catch it and rollback the transaction
        givenCardDaoReturnSuccessOnAdd()
        givenCardContentDaoThrowExceptionOnCreate()

        assertThatThrownBy { cardRoomDataSource.add(card) }.isInstanceOf(SQLException::class.java)
    }

    @Test
    fun testAddCardShouldReturnCardWithSameContent() {
        givenCardDaoAndCardContentDaoReturnSuccessOnAdd()

        cardRoomDataSource.add(card).let { result ->

            assertThat(result.roster).hasSize(card.roster.size)
        }
    }

    @Test
    fun testAddCardShouldReturnACardWithIdFromDao() {
        givenCardDaoAndCardContentDaoReturnSuccessOnAdd()

        cardRoomDataSource.add(card).let { result ->

            assertThat(result.id).isEqualTo(25)
        }
    }

    @Test
    fun testAddCardShouldReturnCardContentWithCardIdMatchingCard() {
        givenCardDaoAndCardContentDaoReturnSuccessOnAdd()

        cardRoomDataSource.add(card).let { result ->

            assertThat(result.first().cardId).isEqualTo(25)
        }
    }


    private fun givenCardDaoAndCardContentDaoReturnSuccessOnUpdate() {
        every { cardDao.update(cardEntity) } returns 1 // 1 card updated
        every { cardContentDao.updateAll(contentEntity) } returns 1 // 1 content updated
    }

    private fun givenCardDaoReturnSuccessOnAdd() {
        every { cardDao.add(cardEntity) } returns 10 // Card id
    }

    private fun givenCardContentDaoThrowExceptionOnCreate() {
        every { cardContentDao.add(any()) }.throws(SQLException())
    }

    private fun givenCardDaoAndCardContentDaoReturnSuccessOnAdd() {
        every { cardDao.add(cardEntity) } returns 25 // Card id
        every { cardContentDao.add(contentEntity.copy(cardId = 25)) } returns 32 // Content id
    }
}
