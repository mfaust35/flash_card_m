package com.faust.m.flashcardm.core.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.faust.m.flashcardm.core.data.BookletRepository
import com.faust.m.flashcardm.core.data.CardRepository
import com.faust.m.flashcardm.core.domain.Booklet
import com.faust.m.flashcardm.core.domain.Deck
import com.faust.m.flashcardm.core.domain.Library

class BookletUseCases private constructor(val addBooklet: AddBooklet,
                                          val deleteBooklet: DeleteBooklet,
                                          val getLiveOutlinedLibrary: GetLiveOutlinedLibrary,
                                          val getLiveOutlinedBooklet: GetLiveOutlinedBooklet,
                                          val renameBooklet: RenameBooklet,
                                          val resetForReview: ResetForReview) {

    constructor(bookletRepository: BookletRepository,
                cardRepository: CardRepository): this(
        AddBooklet(bookletRepository),
        DeleteBooklet(bookletRepository),
        GetLiveOutlinedLibrary(bookletRepository, cardRepository),
        GetLiveOutlinedBooklet(bookletRepository, cardRepository),
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

class GetLiveOutlinedLibrary(private val bookletRepository: BookletRepository,
                             private val cardRepository: CardRepository) {

    operator fun invoke(): LiveData<OutlinedLibrary> =
        MediatorOutlinedLibrary(
            bookletRepository.getLiveLibrary(),
            cardRepository.getLiveDeck()
        )
}

class GetLiveOutlinedBooklet(private val bookletRepository: BookletRepository,
                             private val cardRepository: CardRepository) {

    operator fun invoke(bookletId: Long): LiveData<OutlinedBooklet> =
        MediatorOutlinedBooklet(
            bookletRepository.getLiveBooklet(bookletId),
            cardRepository.getLiveDeckForBooklet(bookletId)
        )
}

class RenameBooklet(private val bookletRepository: BookletRepository) {

    operator fun invoke(newName: String, bookletId: Long): Boolean =
        bookletRepository.renameBooklet(newName, bookletId)
}

class ResetForReview(private val cardRepository: CardRepository) {

    operator fun invoke(count: Int, bookletId: Long): Int =
        cardRepository.resetForReview(count, bookletId)
}


private class MediatorOutlinedLibrary(private val librarySource: LiveData<Library>,
                                      private val deckSource: LiveData<Deck>)
    : MediatorLiveData<OutlinedLibrary>() {

    init {
        addSource(librarySource) { combineNotNullValues() }
        addSource(deckSource) { combineNotNullValues() }
    }

    private fun combineNotNullValues() {
        val library = librarySource.value ?: return
        val decks = deckSource.value?.mapDecksByBookletId() ?: return

        value = library
            .map { booklet ->
                val outline = decks[booklet.id].toBookletOutlineOrEmpty()
                OutlinedBooklet(booklet, outline)
            }
            .toOutlinedLibrary()
    }
}

private class MediatorOutlinedBooklet(private val bookletSource: LiveData<Booklet?>,
                                      private val deckSource: LiveData<Deck>)
    : MediatorLiveData<OutlinedBooklet>() {

    init {
        addSource(bookletSource) { combineNotNullValues() }
        addSource(deckSource) { combineNotNullValues() }
    }

    private fun combineNotNullValues() {
        val booklet = bookletSource.value ?: return
        val deck = deckSource.value ?: return

        value = OutlinedBooklet(booklet, deck.toBookletOutlineOrEmpty())
    }
}

data class BookletOutline(val cardTotalCount: Int,
                          val cardNewCount: Int,
                          val cardInReviewCount: Int,
                          val cardLearnedCount: Int,
                          val cardToReviewCount: Int) {

    companion object {
        val EMPTY = BookletOutline(0, 0, 0, 0, 0)
    }

    constructor(deck: Deck): this(
        deck.count(),
        deck.countNewCard(),
        deck.countTrainingCard(),
        deck.countFamiliarCard(),
        deck.countToReviewCard()
    )
}


data class OutlinedBooklet(val booklet: Booklet, val outline: BookletOutline)

fun Deck?.toBookletOutlineOrEmpty(): BookletOutline =
    if (this == null) BookletOutline.EMPTY else BookletOutline(this)


class OutlinedLibrary(outlinedBooklets: MutableList<OutlinedBooklet>)
    : MutableList<OutlinedBooklet> by outlinedBooklets

fun List<OutlinedBooklet>.toOutlinedLibrary() = OutlinedLibrary(this.toMutableList())
