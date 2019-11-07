package com.faust.m.core.usecase.booklet

import androidx.collection.LongSparseArray
import androidx.collection.forEach
import com.faust.m.core.data.CardRepository
import com.faust.m.core.domain.Booklet
import com.faust.m.core.domain.Card

class GetBookletsOutlines(private val cardRepository: CardRepository) {

    /**
     * Return a map of bookletId -> BookletOutline
     */
    operator fun invoke(booklets: List<Booklet>): LongSparseArray<BookletOutline> =
        LongSparseArray<BookletOutline>().apply {
            cardRepository
                .getAllCardShellsForBooklets(booklets.map(Booklet::id))
                .forEach { key, cardShells ->
                    append(key, BookletOutline(
                        cardShells.size,
                        // It is ineffective to parse the list 3 times, but it makes small code
                        // and the size of list are not expected to be over 100,
                        // so the performance impact is low
                        cardShells.countNewCard(),
                        cardShells.countInReviewCard(),
                        cardShells.countLearnedCards(),
                        cardShells.countToReviewCard()
                    ))
                }
        }

    private fun List<Card>.countNewCard() =
        filter { c -> c.hasRatingLevel(Card.RatingLevel.NEW) }.size

    private fun List<Card>.countInReviewCard() =
        filter { c -> c.hasRatingLevel(Card.RatingLevel.TRAINING) }.size

    private fun List<Card>.countLearnedCards() =
        filter { c -> c.hasRatingLevel(Card.RatingLevel.FAMILIAR) }.size

    private fun List<Card>.countToReviewCard() =
        filter(Card::needReview).size
}

data class BookletOutline(val cardTotalCount: Int,
                          val cardNewCount: Int,
                          val cardInReviewCount: Int,
                          val cardLearnedCount: Int,
                          val cardToReviewCount: Int) {

    companion object {
        val EMPTY = BookletOutline(0, 0, 0, 0, 0)
    }
}
