package com.faust.m.flashcardm.framework.db.room

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SupportSQLiteQuery
import com.faust.m.flashcardm.core.domain.*
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.core.domain.Filter.Numeric
import com.faust.m.flashcardm.core.domain.FilterType.EQUAL
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.CardContentDao
import com.faust.m.flashcardm.framework.db.room.model.CardContentEntity
import com.faust.m.flashcardm.framework.db.room.model.CardDao
import com.faust.m.flashcardm.framework.db.room.model.CardEntity
import io.mockk.*
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
        nextReview = Date(3000),
        updatedAt = Date(22),
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
        nextReview = Date(3000),
        updatedAt = Date(22),
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
    private val filterState: FilterState = mockk()
    private val query: SupportSQLiteQuery = mockk()
    private val slotFilterDeclaration = slot<FilterDeclaration>()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val oneTimeRule = OneTimeObserverRule()

    @Before
    fun setup() {
        setupCardRoomDataSourceFromDatabase()
        setupMockkFilterStateToReturnQuery()
    }


    @After
    fun tearDown() {
        clearMocks(cardDao, cardContentDao, database)
    }


    @Test
    fun testGetLiveDeckForBookletShouldReturnDeck() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()

        cardRoomDataSource
            .getLiveDeckForBooklet(20, attachCardContent = false, filterState = filterState)
            .observeOnce(oneTimeRule) { result ->

                // Then result should contain a deck with default cardEntity transformed to card
                assertThat(result).containsExactly(card)
            }
    }

    @Test
    fun testGetLiveDeckForBookletWithAttachCardContentTrueShouldReturnDeckWithRoster() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()
        givenCardContentDaoReturnListWithDefaultCardContentEntityForBooklet20()

        cardRoomDataSource
            .getLiveDeckForBooklet(20, attachCardContent = true, filterState = filterState)
            .observeOnce(oneTimeRule) { result ->

                // Then result should contain a deck with one card with values matching the cardEntity
                // and a roster with value matching the cardContentEntity
                val expectedRoster = mutableListOf(cardContent).toRoster()
                assertThat(result).containsExactly(card.copy(roster = expectedRoster))
            }
    }

    @Test
    fun testGetLiveDeckForBookletShouldAddBookletIdFilterToFilterState() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()

        cardRoomDataSource.getLiveDeckForBooklet(20, filterState = filterState)

        // Then the filter that was added to filterState is a bookletIdFilter
        slotFilterDeclaration.verifyFilterAdded { filter: Filter ->
            assertThat(filter).isEqualTo(Numeric(Card::bookletId, 20L, EQUAL))
        }
    }

    @Test
    fun testGetLiveDeckForBookletShouldAddFilterToFilterStateBeforeBuildingRoomQuery() {
        givenCardDaoReturnListWithDefaultCardEntityForBookletId20()

        // When I get liveDeckForBooklet
        cardRoomDataSource.getLiveDeckForBooklet(20, filterState = filterState)

        // Then filterState + bookletIdFilter happens before filterState.toRoomQuery
        verifyOrder {
            filterState + any()
            filterState.toRoomQuery()
        }
    }


    private fun setupCardRoomDataSourceFromDatabase() {
        every { database.cardDao() } returns cardDao
        every { database.cardContentDao() } returns cardContentDao
        cardRoomDataSource = CardRoomDataSource(database)
    }

    private fun setupMockkFilterStateToReturnQuery() {
        mockkStatic("com.faust.m.flashcardm.framework.db.room.CardRoomFilterKt")
        every { filterState + capture(slotFilterDeclaration) } returns filterState
        every { filterState.toRoomQuery() } returns query
    }

    private fun givenCardDaoReturnListWithDefaultCardEntityForBookletId20() {
        every { cardDao.getLiveCardsFilteredViaQuery(query) } returns
                MutableLiveData(listOf(cardEntity))
    }

    private fun givenCardContentDaoReturnListWithDefaultCardContentEntityForBooklet20() {
        every { cardContentDao.getLiveCardContentsForBooklet(20) } returns
                MutableLiveData(listOf(contentEntity))
    }

    private fun CapturingSlot<FilterDeclaration>.verifyFilterAdded(verify: (actualFilter : Filter) -> Unit) {
        verify(captured())
    }
}