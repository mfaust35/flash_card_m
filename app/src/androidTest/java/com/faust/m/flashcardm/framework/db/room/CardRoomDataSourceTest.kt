package com.faust.m.flashcardm.framework.db.room

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.core.domain.Card
import com.faust.m.core.domain.CardContent
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CardRoomDataSourceTest: BaseDaoTest() {

    private val bookletEntity = BookletEntity("My Second Booklet", 25)
    private val cardContent = CardContent("content", "recto")
    private val card = Card(bookletId = bookletEntity.id).add(cardContent)

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
        // Given two booklets in database and 3 cards
        bookletDao.add(BookletEntity("My first booklet", 10))
        bookletDao.add(BookletEntity("My Second Booklet", 25))
        cardDao.add(CardEntity(10))
        cardDao.add(CardEntity(25))
        cardDao.add(CardEntity(25))

        // I can retrieve the card count by booklet id
        assertThat(cardRoomDataSource.countCardsForBooklets(listOf(10)))
            .isEqualTo(HashMap<Long, Int>().also{ it[10] = 1 })
        assertThat(cardRoomDataSource.countCardsForBooklets(listOf(10, 25)))
            .isEqualTo(HashMap<Long, Int>().also {
                it[10] = 1
                it[25] = 2
            })
    }
}
