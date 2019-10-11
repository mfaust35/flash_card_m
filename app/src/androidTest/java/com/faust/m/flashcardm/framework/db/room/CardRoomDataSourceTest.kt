package com.faust.m.flashcardm.framework.db.room

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class CardRoomDataSourceTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("My Second Booklet", 25)
    private val cardContent = CardContent("content", "recto")
    private val card =
        Card(rating = 0, lastSeen = Date(), bookletId = bookletEntity.id).add(cardContent)

    private lateinit var cardRoomDataSource: CardRoomDataSource
    private lateinit var cardDao: CardDao
    private lateinit var bookletDao: BookletDao

    override fun onDatabaseCreated(database: FlashRoomDatabase) = with(database) {
        cardRoomDataSource = CardRoomDataSource(cardDao(), cardContentDao())
        bookletDao = bookletDao()
        cardDao = cardDao()
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
                assertThat(it.content["recto"]?.size)
                    .`as`("List of recto content from card in database")
                    .isEqualTo(1)
                assertThat(it.content["recto"]?.first()?.value)
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
        cardDao.add(CardEntity(5, Date(20),10, 1))
        cardDao.add(CardEntity(5, Date(1000),25, 3))
        cardDao.add(CardEntity(5, Date(30000),25, 5))
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
}
