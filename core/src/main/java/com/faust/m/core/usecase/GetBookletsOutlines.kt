package com.faust.m.core.usecase

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

    private fun List<Card>.cardToReviewCount() = filter { it.rating < 5 }.size
}

data class BookletOutline(val cardTotalCount: Int, val cardToReviewCount: Int) {

    companion object {
        val EMPTY = BookletOutline(0, 0)
    }
}
