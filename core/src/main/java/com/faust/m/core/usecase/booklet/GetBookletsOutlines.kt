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
                    append(key, BookletOutline(cardShells.size, cardShells.cardToReviewCount()))
                }
        }

    /**
     * Return a number of card to review depending on the rating of the cards as well as the
     * last_seen and create_at date. To be eligible for review, a card must have a rating
     * inferior to 5 (5 means the card is learned), and it must have not have been reviewed today
     */
    private fun List<Card>.cardToReviewCount() =
        filter(Card::needReview).size
}

data class BookletOutline(val cardTotalCount: Int, val cardToReviewCount: Int) {

    companion object {
        val EMPTY = BookletOutline(0, 0)
    }
}
