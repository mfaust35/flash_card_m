package com.faust.m.flashcardm.core.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.domain.Deck
import com.faust.m.flashcardm.framework.db.room.OneTimeObserverRule
import com.faust.m.flashcardm.framework.db.room.observeOnce
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class BookletUseCaseTest {

    private val bookletRepo: BookletRepository = mockk()
    private val cardRepo: CardRepository = mockk()
    private val bookletUseCases = BookletUseCases(bookletRepo, cardRepo)

    private val bookletId = 2L
    private lateinit var bookletMockk: Booklet
    private lateinit var deckMockk: Deck

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val oneTimeRule = OneTimeObserverRule()

    @Test
    fun testGetOutlinedBookletShouldContainBookletFromRepo() {
        // Given bookletRepository return a LiveMock booklet:
        bookletMockk = mockk()
        every { bookletRepo.getLiveBooklet(bookletId) } returns MutableLiveData(bookletMockk)

        // Given cardRepository returns a LiveMock deck without any cards:
        deckMockk = mockkDeck(0, 0, 0, 0, 0)
        every { cardRepo.getLiveDeckForBooklet(bookletId) } returns MutableLiveData(deckMockk)

        // When I get liveOutlinedBooklet for bookletId
        bookletUseCases.getLiveOutlinedBooklet(bookletId).observeOnce(oneTimeRule) { result ->

            // Then outlinedBooklet contains the booklet
            assertThat(result.booklet).isEqualTo(bookletMockk)
        }
    }

    @Test
    fun testGetOutlinedBookletShouldContainOutlineWithCountFromDeck() {
        // Given bookletRepository return a LiveMock booklet:
        bookletMockk = mockk()
        every { bookletRepo.getLiveBooklet(bookletId) } returns MutableLiveData(bookletMockk)

        // Given cardRepository returns a LiveMock deck without card counts:
        val total = 10
        val new = 2
        val training = 3
        val familiar = 5
        val toReview = 4
        deckMockk = mockkDeck(total, new, training, familiar, toReview)
        every { cardRepo.getLiveDeckForBooklet(bookletId) } returns MutableLiveData(deckMockk)

        // When I get liveOutlinedBooklet for bookletId
        bookletUseCases.getLiveOutlinedBooklet(bookletId).observeOnce(oneTimeRule) { result ->

            // Then outlinedBooklet contains a bookletOutline with correct counts
            assertThat(result.outline).isEqualTo(
                BookletOutline(total, new, training, familiar, toReview)
            )
        }
    }


    private fun mockkDeck(total: Int, new: Int, training: Int, familiar: Int, toReview: Int) =
        mockk<Deck> {
            every { count() } returns total
            every { countNewCard() } returns new
            every { countTrainingCard() } returns training
            every { countFamiliarCard() } returns familiar
            every { countToReviewCard() } returns toReview
        }
}
