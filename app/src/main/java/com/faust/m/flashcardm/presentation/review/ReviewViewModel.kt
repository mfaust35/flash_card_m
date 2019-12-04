package com.faust.m.flashcardm.presentation.review

import androidx.lifecycle.*
import com.faust.m.flashcardm.core.domain.*
import com.faust.m.flashcardm.core.domain.FilterType.INFERIOR
import com.faust.m.flashcardm.core.usecase.BookletUseCases
import com.faust.m.flashcardm.core.usecase.CardUseCases
import com.faust.m.flashcardm.presentation.MutableLiveSet
import com.faust.m.flashcardm.presentation.fragment_edit_card.DelegateEditCard
import com.faust.m.flashcardm.presentation.fragment_edit_card.ViewModelEditCard
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import com.faust.m.flashcardm.presentation.library.toBookletBanner
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.RATING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ReviewViewModel @JvmOverloads constructor(
    private val bookletId: Long,
    private val delegateEditCard: DelegateEditCard = DelegateEditCard(bookletId)
): ViewModel(),
    KoinComponent,
    ViewModelEditCard by delegateEditCard,
    AnkoLogger {

    private val bookletUseCases: BookletUseCases by inject()
    private val cardUseCases: CardUseCases by inject()


    // Booklet information used to display the top banner
    private val _bookletBannerData: MutableLiveData<BookletBannerData> = MutableLiveData()
    val bookletBannerData: LiveData<BookletBannerData> =
        Transformations.switchMap(bookletUseCases.getLiveOutlinedBooklet(bookletId))
        { outlineBooklet ->
            _bookletBannerData.postValue(outlineBooklet.toBookletBanner())
            _bookletBannerData
        }

    // Current card used to make reviewCard on display
    private var _currentCard: Card? = null

    // Current deck of cards to review for booklet
    private val _liveDeck =
        cardUseCases.getLiveDeck(
            bookletId,
            attachCardContent = true,
            filterState = declareFilterState {
                with { Filter.Timestamp(Card::nextReview, Date(), INFERIOR) }
            })
    // List of id of cards that have been marked as reviewLater
    private val _liveCardsToRepeat: MutableLiveSet<Long> = MutableLiveSet()
    // List of id of cards that have been marked as know for today
    private val _liveCardKnown: MutableLiveSet<Long> = MutableLiveSet()

    // reviewCard: Receive data from _liveDeck for each updates on the deck of cards to review
    // and event from _cardsToRepeat when a new card has been marked for repeat later
    val reviewCard: MediatorLiveData<ReviewCard> = MediatorLiveData<ReviewCard>().apply {
        addSource(_liveDeck) { postReviewCards() }
        addSource(_liveCardsToRepeat) { postReviewCards() }
        addSource(_liveCardKnown) { postReviewCards() }
    }


    fun flipCurrentCard() =
        reviewCard.value?.copy(state = RATING, animate = true).let { reviewCard.postValue(it) }

    fun validateCurrentCard() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentCard?.let { card ->
                _liveCardKnown.add(card.id)
                card.incrementLearnedLevel()
                    .let { cardUseCases.updateCard(it) }
            }
        }
    }

    fun repeatCurrentCard() {
        _currentCard?.let { card -> _liveCardsToRepeat.add(card.id) }
    }

    fun startEditCard() {
        delegateEditCard.startCardEdition(_currentCard)
    }


    /*
     * Cranky mechanic of choosing which card to review in the deck and
     * how to update reviewCard value. I intend to change it once I add a `nextReviewDate` on Card.
     * Bug: For now, sometimes the interface display cards without animation (last card if there
     * are cards to repeat)
     */
    private fun postReviewCards() {
        val deck = _liveDeck.value ?: return
        val nextCardInLine = nextCardFromDeck(deck)
        when {
            // Order is important here
            deck.isCircling() -> animateUpdateReviewCardValueFrom(nextCardInLine)
            nextCardInLine.isSameAsCurrentCard() -> updateReviewCardValueFromCurrentCard()
            else -> animateUpdateReviewCardValueFrom(nextCardInLine)
        }
    }

    private fun nextCardFromDeck(deck: Deck): Card? {
        // If all cards left have been marked as "to repeat", then remove all card from cardsToRepeat
        // It will enable card to be reviewed once more during this "review session"
        val cardLeftBeforeSecondPass =
            deck.map { it.id }
                .toSet()
                .subtract(_liveCardKnown)
                .subtract(_liveCardsToRepeat)
                .size

        if (cardLeftBeforeSecondPass == 0) {
            _liveCardsToRepeat.clear()
        }
        return deck.firstOrNull { c ->
            !_liveCardsToRepeat.contains(c.id) && !_liveCardKnown.contains(c.id)
        }
    }

    private fun Deck.isCircling() = (size == 1)

    private fun Card?.isSameAsCurrentCard() = (null != this && id == _currentCard?.id)

    private fun updateReviewCardValueFromCurrentCard() = reviewCard.value?.let { reviewCardValue ->
        if (reviewCardValue.needUpdateFrom(_currentCard)) {
            reviewCardValue
                .copy(
                    front = _currentCard?.frontAsTextOrNull() ?: "",
                    back = _currentCard?.backAsTextOrNull() ?: "",
                    animate = false
                )
                .let { reviewCard.value = it }
        }
    }

    private fun ReviewCard?.needUpdateFrom(card: Card?): Boolean {
        return this != null && card != null &&
                (front != card.frontAsTextOrNull() || back != card.backAsTextOrNull())
    }

    private fun animateUpdateReviewCardValueFrom(card: Card?) {
        _currentCard = card
        reviewCardFrom(card, reviewCard.value).let { reviewCard.value = it }
    }

    private fun reviewCardFrom(card: Card?, previousReviewCard: ReviewCard?): ReviewCard =
        when {
            card == null -> ReviewCard.EMPTY
            previousReviewCard.isFinished() -> ReviewCard(card, ASKING, true)
            else -> previousReviewCard!!.copy(state = RATING, animate = true)
        }
}

/**
 * Class ReviewCard is used by ReviewActivity and its fragment to display information
 * related to a card
 */
data class ReviewCard(val front: String,
                      val back: String,
                      val state: State,
                      val animate: Boolean) {

    constructor(card: Card, state: State, animate: Boolean): this(
        card.frontAsTextOrNull() ?: "??",
        card.backAsTextOrNull() ?: "??",
        state,
        animate
    )

    companion object {
        val EMPTY = ReviewCard("--", "--", ASKING, false)
        val LOADING = ReviewCard("loading", "loading", ASKING, false)
    }

    enum class State { ASKING, RATING }
}

fun ReviewCard?.isFinished(): Boolean = ((this == null) || (state == RATING))
