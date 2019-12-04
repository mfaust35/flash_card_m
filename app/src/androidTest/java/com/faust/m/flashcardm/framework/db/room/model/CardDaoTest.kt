package com.faust.m.flashcardm.framework.db.room.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.faust.m.flashcardm.core.domain.CardContentType.BACK
import com.faust.m.flashcardm.core.domain.CardContentType.FRONT
import com.faust.m.flashcardm.framework.db.room.OneTimeObserverRule
import com.faust.m.flashcardm.framework.db.room.definition.FlashRoomDatabase
import com.faust.m.flashcardm.framework.db.room.observeOnce
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
class CardDaoTest: BaseDaoTest() {

    // Dao to test
    private lateinit var cardDao: CardDao
    private lateinit var cardContentDao: CardContentDao

    // Default BookletEntity               -> id(42)
    //         + -- CardEntity             -> id(+ -- 10)
    //              + -- CardContentEntity -> id(     + -- 25)
    private val bookletEntity =
        BookletEntity(
            name = "First Booklet",
            id = 42
        )
    private val cardEntity =
        CardEntity(
            rating = 1,
            nextReview = Date(30),
            updatedAt = Date(70),
            createdAt = Date(30),
            bookletId = 42,
            id = 10
        )
    private val cardContentEntity =
        CardContentEntity(
            value = "Learn it",
            type = FRONT,
            cardId = 10,
            id = 25
        )
    // Default triple Booklet (42) / Card (42-10) / CardContent in database (42-10-25)
    private val defaultTriple =
        Triple(bookletEntity, cardEntity, cardContentEntity)

    private lateinit var bookletDao: BookletDao

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val oneTimeRule = OneTimeObserverRule()

    override fun onDatabaseCreated(database: FlashRoomDatabase) = database.run {
        bookletDao = bookletDao()
        cardDao = cardDao()
        cardContentDao = cardContentDao()
    }


    @Test
    fun testGetAllCardShellForBookletIds() {
        given2BookletsAnd3CardsInDatabase()

        // The card shells can be retrieved
        assertThat(cardDao.getAllCardsShellsForBooklets(listOf(42)))
            .containsExactly(
                cardEntity
            )
        assertThat(cardDao.getAllCardsShellsForBooklets(listOf(42, 3)))
            .containsExactlyInAnyOrder(
                cardEntity,
                CardEntity(rating = 2, nextReview = Date(30),
                    updatedAt = Date(70), createdAt = Date(30), bookletId = 3, id = 3),
                CardEntity(rating = 3, nextReview = Date(30),
                    updatedAt = Date(70), createdAt = Date(30), bookletId = 3, id = 4)
            )
    }

    @Test
    fun testGetLiveCardContentForBookletShouldReturnCardContentForCardInBooklet() {
        defaultTriple.saveInDatabase()

        // When I get cardContent for the default bookletId
        cardContentDao.getLiveCardContentsForBooklet(bookletId = 42).observeOnce(oneTimeRule) { result ->

            // I should find the card content
            assertThat(result).containsExactly(cardContentEntity)
        }
    }

    @Test
    fun testGetLiveCardContentForBookletShouldNotReturnCardContentForCardNotInBooklet() {
        // Given the default triple Booklet (42) / Card (42-10) / CardContent in database (42-10-25)
        // along with another triple Booklet (1) / Card (1-4) / CardContent (1-4-2)
        defaultTriple.saveInDatabase()
        generateDistinctTriple().saveInDatabase()

        // When I get cardContent for the default bookletId
        cardContentDao.getLiveCardContentsForBooklet(bookletId = 42).observeOnce(oneTimeRule) { result ->

            // I should find ONLY the cardContent (42-10-25)
            assertThat(result).containsExactly(cardContentEntity)
        }
    }

    @Test
    fun testDeleteCardsShouldDeleteCardFromDatabase() {
        defaultTriple.saveInDatabase()
        // Test valid only if there is one card to delete:
        assertThat(countCardForDefaultBooklet()).isEqualTo(1)

        cardDao.deleteAll(cardEntity) // Method under test

        // There should be no more card
        assertThat(countCardForDefaultBooklet()).isEqualTo(0)
    }

    @Test
    fun testDeleteCardsShouldDeleteCardContentLinkedToCardDeletedFromDatabase() {
        defaultTriple.saveInDatabase()
        // Test valid only if there is one cardContent to delete:
        assertThat(countCardContentForDefaultBooklet()).isEqualTo(1)

        cardDao.deleteAll(cardEntity) // Method under test

        assertThat(countCardContentForDefaultBooklet()).isEqualTo(0)
    }

    @Test
    fun testDeleteCardsShouldReturnDeletedCardCount() {
        defaultTriple.saveInDatabase()

        cardDao.deleteAll(cardEntity).let { result ->

            assertThat(result).isEqualTo(1)
        }
    }

    @Test
    fun testGetLiveCardFilteredViaQueryOnBookletIdShouldReturnOnlyCardsWithCorrectId() {
        // Given the default triple Booklet (42) / Card (42-10) / CardContent in database (42-10-25)
        // along with another triple Booklet (1) / Card (1-4) / CardContent (1-4-2)
        defaultTriple.saveInDatabase()
        generateDistinctTriple().saveInDatabase()

        // When I get live card via raw query WHERE booklet_id = ?
        val query = SimpleSQLiteQuery("SELECT * FROM $CardTableName WHERE booklet_id = ?", arrayOf(42))
        cardDao.getLiveCardsFilteredViaQuery(query).observeOnce(oneTimeRule) { result ->

            // I should find ONLY the cardEntity (42-10-25)
            assertThat(result).containsExactly(cardEntity)
        }
    }

    @Test
    fun testGetLiveCardsFilteredViaQueryOnRatingShouldReturnOnlyCardsWithCorrectRating() {
        // Given the default triple Booklet (42) / Card (42-10) / CardContent in database (42-10-25)
        // along with another triple Booklet (1) / Card (1-4) / CardContent (1-4-2)
        defaultTriple.saveInDatabase()
        generateDistinctTriple(withCardRating = 5).saveInDatabase()

        // When I get live cards via raw query WHERE rating = ?
        val query = SimpleSQLiteQuery("SELECT * FROM $CardTableName WHERE rating < ?", arrayOf(5))
        cardDao.getLiveCardsFilteredViaQuery(query).observeOnce(oneTimeRule) { result ->

            // I should find ONLY the cardEntity with rating < 5
            assertThat(result).containsExactly(cardEntity)
        }
    }

    private fun givenABookletInDatabase() {
        bookletDao.add(bookletEntity)
    }

    private fun given2BookletsAnd3CardsInDatabase() {
        defaultTriple.saveInDatabase()
        bookletDao.add(BookletEntity("", 3))

        cardDao.add(CardEntity(rating = 2, nextReview = Date(30),
            updatedAt = Date(70), createdAt = Date(30), bookletId = 3, id = 3))
        cardDao.add(CardEntity(rating = 3, nextReview = Date(30),
            updatedAt = Date(70), createdAt = Date(30), bookletId = 3, id = 4))
    }

    private fun countCardForDefaultBooklet(): Int {
        var result = -1
        val query =
            SimpleSQLiteQuery("SELECT * FROM $CardTableName WHERE booklet_id = ?", arrayOf(42))
        cardDao
            .getLiveCardsFilteredViaQuery(query)
            .observeOnce(oneTimeRule) { contentEntities ->
                result = contentEntities.size
            }
        return result
    }

    private fun countCardContentForDefaultBooklet(): Int {
        var result = -1
        cardContentDao
            .getLiveCardContentsForBooklet(42)
            .observeOnce(oneTimeRule) { contentEntities ->
                result = contentEntities.size
            }
        return result
    }

    private fun generateDistinctTriple(withCardRating: Int = 2): Triple<BookletEntity, CardEntity, CardContentEntity> {
        val distinctBookletEntity =
            BookletEntity(
                name = "The only second booklet",
                id = 1
            )
        val distinctCardEntity =
            CardEntity(
                rating = withCardRating,
                nextReview = Date(20000),
                updatedAt = Date(70000),
                createdAt = Date(30000),
                bookletId = 1,
                id = 4
            )
        val distinctCardContentEntity =
            CardContentEntity(
                value = "Forget it",
                type = BACK,
                cardId = 4,
                id = 2
            )
        return Triple(distinctBookletEntity, distinctCardEntity, distinctCardContentEntity)
    }

    private fun Triple<BookletEntity, CardEntity, CardContentEntity>.saveInDatabase():
            Triple<BookletEntity, CardEntity, CardContentEntity> {
        bookletDao.add(first)
        cardDao.add(second)
        cardContentDao.add(third)
        return this
    }
}
