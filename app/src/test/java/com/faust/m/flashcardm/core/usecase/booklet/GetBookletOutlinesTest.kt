package com.faust.m.flashcardm.core.usecase.booklet

import androidx.collection.LongSparseArray
import androidx.collection.set
import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.usecase.BookletOutline
import com.faust.m.flashcardm.core.usecase.GetBookletsOutlines
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

class GetBookletOutlinesTest {

    private val cardRepository: CardRepository = mockk()
    private val getBookletOutlines: GetBookletsOutlines = GetBookletsOutlines(cardRepository)

    private var bookletsToOutline = listOf<Booklet>()
    private var cardShellsForBooklet = LongSparseArray<MutableList<Card>>()


    @Before
    fun setup() {
        bookletsToOutline = listOf()
        cardShellsForBooklet = LongSparseArray()
    }


    @Test
    fun testGetBookletOutlineWithOnlyNewCardsShouldReturnOutlineWithCorrectCardCounts() {
        val nCard = 2 // New card count
        val rCard = 0 // InReview card count
        val lCard = 0 // Learned card count
        val trCard = 0 // Card that will have a special createAt date to ensure they need review
        testGetBookletOutlineWithCardCount(nCard, rCard, lCard, trCard)
    }

    private fun testGetBookletOutlineWithCardCount(nCard: Int, rCard: Int,
                                                   lCard: Int, trCard: Int) {
        val bookletId = 1L
        val total = nCard + rCard + lCard
        givenABookletWithXCardShellInRepository(bookletId, nCard, rCard, lCard, trCard)

        // When I get the bookletOutline for the one bookletToOutline
        getBookletOutlines.invoke(bookletsToOutline).apply {

            // The result map contain one BookletOutline for correct bookletId
            assertThat(size()).isEqualTo(1)
            // The BookletOutline card count correspond to the cardShells from cardRepository
            assertThat(this[bookletId])
                .`as`("BookletOutline contains correct card counts")
                .isEqualTo(BookletOutline(total, nCard, rCard, lCard, trCard))
        }
    }

    @Test
    fun testGetBookletOutlineWithOnlyInReviewCardsShouldReturnOutlineWithCorrectCardCounts() {
        val nCard = 0 // New card count
        val rCard = 2 // InReview card count
        val lCard = 0 // Learned card count
        val trCard = 0 // Card that will have a special createAt date to ensure they need review
        testGetBookletOutlineWithCardCount(nCard, rCard, lCard, trCard)
    }

    @Test
    fun testGetBookletOutlineWithOnlyLearnedCardsShouldReturnOutlineWithCorrectCardCounts() {
        val nCard = 0 // New card count
        val rCard = 0 // InReview card count
        val lCard = 2 // Learned card count
        val trCard = 0 // Card that will have a special createAt date to ensure they need review
        testGetBookletOutlineWithCardCount(nCard, rCard, lCard, trCard)
    }

    @Test
    fun testGetBookletOutlineWith100CardsShouldReturnOutlineWithCorrectCardCounts() {
        val nCard = 25 // New card count
        val rCard = 25 // InReview card count
        val lCard = 50 // Learned card count
        val trCard = 0 // Card that will have a special createAt date to ensure they need review
        testGetBookletOutlineWithCardCount(nCard, rCard, lCard, trCard)
    }

    @Test
    fun testGetBookletOutlineWithCardToReviewReturnOutlineWithCorrectCardCounts() {
        val nCard = 25 // New card count
        val rCard = 25 // InReview card count
        val lCard = 50 // Learned card count
        val trCard = 32 // Card that will have a special createAt date to ensure they need review
        testGetBookletOutlineWithCardCount(nCard, rCard, lCard, trCard)
    }


    private fun givenABookletWithXCardShellInRepository(bookletId: Long,
                                                        nCard: Int, rCard: Int,
                                                        lCard: Int, trCard: Int) {
        givenADefaultBookletForId(bookletId)
        givenAssociatedCardShells(bookletId, nCard, rCard, lCard, trCard)
        givenCardRepositoryReturnCardShellForBookletId(bookletId)
    }

    private fun givenADefaultBookletForId(bookletId: Long) =
        givenADefaultBookletListForIds(listOf(bookletId))

    private fun givenADefaultBookletListForIds(bookletIds: List<Long>) {
        bookletsToOutline = mutableListOf<Booklet>().apply {
            for (i in bookletIds) {
                add(Booklet("My $i unicorn", i))
            }
        }
    }

    private fun givenAssociatedCardShells(bookletId: Long,
                                          nCard: Int, rCard: Int, lCard: Int, trCard: Int) {
        val cardShells = mutableListOf<Card>()
        cardShellsForBooklet[bookletId] = cardShells
        // Populate cardShells
        var idCount = 1L
        var trCardCount = 0
        for (i in 0 until nCard) {
            cardShells.add(createCard(
                rating = 0,
                bookletId = bookletId,
                id = idCount++,
                needReview =  trCardCount++ < trCard))
        }
        for (i in 0 until rCard) {
            cardShells.add(createCard(
                rating = 2,
                bookletId = bookletId,
                id = idCount++,
                needReview =  trCardCount++ < trCard))
        }
        for (i in 0 until lCard) {
            cardShells.add(createCard(
                rating = 5,
                bookletId = bookletId,
                id = idCount++,
                needReview =  trCardCount++ < trCard))
        }
    }

    private fun createCard(rating: Int, bookletId: Long, id:Long, needReview: Boolean): Card =
        if (needReview) {
            Card(rating = rating, bookletId = bookletId, id = id)
        } else {
            Card(rating = rating, createdAt = Date(0), bookletId = bookletId, id = id)
        }


    private fun givenCardRepositoryReturnCardShellForBookletId(bookletId: Long) {
        every { cardRepository.getAllCardShellsForBooklets(listOf(bookletId)) }
            .returns(cardShellsForBooklet)
    }
}
