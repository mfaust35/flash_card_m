package com.faust.m.flashcardm.framework.db.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.domain.CardContent
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.core.domain.toRoster
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class CardRoomDataSourceLiveDataTest {

    // DataSource to test
    private lateinit var cardRoomDataSource: CardRoomDataSource

    // Default CardEntity             -> id(5)
    //         + -- CardContentEntity -> id(+ -- 1)
    private val cardEntity = CardEntity(
        rating = 2,
        lastSeen = Date(3000),
        createdAt = Date(20),
        bookletId = 20,
        id = 5
    )
    private val contentEntity = CardContentEntity(
        value = "Learn it",
        type = FRONT,
        cardId = 5,
        id = 1
    )
    // Corresponding default Card & CardContent
    private val card = Card(
        rating = 2,
        lastSeen = Date(3000),
        createdAt = Date(20),
        bookletId = 20,
        id = 5
    )
    private val cardContent = CardContent(
        value = "Learn it",
        type = FRONT,
        cardId = 5,
        id = 1
    )

    private val database: FlashRoomDatabase = mockk()
    private val cardDao: CardDao = mockk()
    private val cardContentDao: CardContentDao = mockk()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val oneTimeRule = OneTimeObserverRule()

    @Before
    fun setup() {
        every { database.cardDao() } returns cardDao
        every { database.cardContentDao() } returns cardContentDao
        cardRoomDataSource = CardRoomDataSource(database)
    }

    @After
    fun tearDown() {
        clearMocks(cardDao, cardContentDao, database)
    }


    @Test
    fun testGetLiveDeckForBookletShouldReturnDeck() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()

        cardRoomDataSource
            .getLiveDeckForBooklet(20, attachCardContent = false, filterToReviewCard = false)
            .observeOnce(oneTimeRule) { result ->

                // Then result should contain a deck with default card
                assertThat(result).containsExactly(card)
            }
    }

    @Test
    fun testGetLiveDeckForBookletWithAttachTrueShouldReturnDeckWithRoster() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()
        givenCardContentDaoReturnListWithDefaultCardContentEntityForBooklet20()

        cardRoomDataSource
            .getLiveDeckForBooklet(20, attachCardContent = true)
            .observeOnce(oneTimeRule) { result ->

                // Then result should contain a deck with one card with values matching the cardEntity
                // and a roster with value matching the cardContentEntity
                val expectedRoster = mutableListOf(cardContent).toRoster()
                assertThat(result).containsExactly(card.copy(roster = expectedRoster))
            }
    }

    @Test
    fun testGetLiveDeckForBookletWithFilterTrueShouldReturnDeckWithoutFilteredCard() {
        givenCardDaoReturnListWithCardEntityToFilterForBookletId20()

        cardRoomDataSource
            .getLiveDeckForBooklet(20, filterToReviewCard = true)
            .observeOnce(oneTimeRule) { result ->

                // Then result should not contain any card
                assertThat(result).hasSize(0)
            }
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


    private fun givenCardDaoReturnListWithDefaultCardEntityForBookletId20() {
        every { cardDao.getLiveCardsForBooklet(20) } returns
                MutableLiveData(listOf(cardEntity))
    }

    private fun givenCardDaoReturnListWithCardEntityToFilterForBookletId20() {
        val cardEntityToFilter = CardEntity(
            rating = 5,
            lastSeen = Date(321),
            createdAt = Date(322),
            bookletId = 20,
            id = 12)
        every { cardDao.getLiveCardsForBooklet(20) } returns
                MutableLiveData(listOf(cardEntityToFilter))
    }

    private fun givenCardContentDaoReturnListWithDefaultCardContentEntityForBooklet20() {
        every { cardContentDao.getLiveCardContentsForBooklet(20) } returns
                MutableLiveData(listOf(contentEntity))
    }
}