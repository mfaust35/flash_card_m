package com.faust.m.flashcardm.core.usecase

import androidx.collection.LongSparseArray
import androidx.collection.forEach
import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.domain.Card

class BookletUseCases private constructor(val addBooklet: AddBooklet,
                                          val deleteBooklet: DeleteBooklet,
                                          val getBooklet: GetBooklet,
                                          val getBooklets: GetBooklets,
                                          val getBookletsOutlines: GetBookletsOutlines,
                                          val renameBooklet: RenameBooklet,
                                          val resetForReview: ResetForReview) {

    constructor(bookletRepository: BookletRepository,
                cardRepository: CardRepository): this(
        AddBooklet(bookletRepository),
        DeleteBooklet(bookletRepository),
        GetBooklet(bookletRepository),
        GetBooklets(bookletRepository),
        GetBookletsOutlines(cardRepository),
        RenameBooklet(bookletRepository),
        ResetForReview(cardRepository)
    )
}

class AddBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(booklet: Booklet): Booklet = bookletRepository.add(booklet)
}

class DeleteBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(booklet: Booklet): Int = bookletRepository.delete(booklet)
}

class GetBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(bookletId: Long): Booklet? = bookletRepository.getBooklet(bookletId)
}

class GetBooklets(private val bookletRepository: BookletRepository) {

    operator fun invoke(): List<Booklet> = bookletRepository.getAllBooklets()
}

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

class RenameBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(newName: String, bookletId: Long): Boolean =
        bookletRepository.renameBooklet(newName, bookletId)
}

class ResetForReview(private val cardRepository: CardRepository) {

    operator fun invoke(count: Int, bookletId: Long) =
        cardRepository.resetForReview(count, bookletId)
}
