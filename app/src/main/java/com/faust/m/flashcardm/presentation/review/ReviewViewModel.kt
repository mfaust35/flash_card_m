package com.faust.m.flashcardm.presentation.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.usecase.CardUseCases
import com.faust.m.flashcardm.presentation.fragment_edit_card.DelegateEditCard
import com.faust.m.flashcardm.presentation.fragment_edit_card.ViewModelEditCard
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.RATING
import com.faust.m.flashcardm.presentation.view_library_booklet.DelegateBookletBanner
import com.faust.m.flashcardm.presentation.view_library_booklet.ViewModelBookletBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ReviewViewModel @JvmOverloads constructor(
    private val bookletId: Long,
    private val delegateEditCard: DelegateEditCard = DelegateEditCard(bookletId),
    private val delegateBookletBanner: ViewModelBookletBanner = DelegateBookletBanner(bookletId)
): ViewModel(),
    KoinComponent,
    ViewModelEditCard by delegateEditCard,
    ViewModelBookletBanner by delegateBookletBanner,
    AnkoLogger {

    // Initialize the delegate for card edition with a listener onCardEdited
    init { delegateEditCard.onCardEdited = ::onCardEdited }


    private val cardUseCases: CardUseCases by inject()


    // Current card used to make reviewCard on display
    private val _currentCard: MutableLiveData<Card?> = MutableLiveData()
    // Current reviewCard on display
    private val _reviewCard: MutableLiveData<ReviewCard> = MutableLiveData()
    val reviewCard: LiveData<ReviewCard> = _reviewCard

    // Queue of cards (only cards to review) for the booklet on review
    private var _cardQueue = LinkedList<Card>()


    override fun loadData() {
        delegateBookletBanner.loadData()
        GlobalScope.launch {
            cardUseCases
                .getCardsForBooklet(bookletId, filterReviewCard = true)
                .forEach { _cardQueue.add(it) }
            postCardUpdate()
        }
    }


    fun flipCurrentCard() {
        postCardUpdate()
    }

    fun validateCurrentCard() {
        _currentCard.value?.let {
            val cardToUpdate = it.copy(rating = it.rating + 1, lastSeen = Date())
            GlobalScope.launch {
                cardUseCases.updateCard(cardToUpdate).also { updatedCard: Card ->
                    warn { "Card updated $updatedCard" }
                    postBookletUpdate()
                    postCardUpdate()
                }
            }
        }
    }

    fun repeatCurrentCard() {
        _currentCard.value?.let { _cardQueue.add(it) }
        postCardUpdate()
    }

    fun startEditCard() =
        delegateEditCard.startCardEdition(_currentCard.value)


    private fun postCardUpdate() {
        val card =
            if (_reviewCard.value.isFinished()) {
                when {
                    _cardQueue.isNotEmpty() -> _cardQueue.remove()
                    else -> null
                }
            } else {
                _currentCard.value
            }
        _currentCard.postValue(card)

        val tReviewCard =
            if (null != card) reviewCardFrom(card, _reviewCard.value)
            else ReviewCard.EMPTY
        _reviewCard.postValue(tReviewCard)
    }

    private fun reviewCardFrom(card: Card, previousReviewCard: ReviewCard?): ReviewCard =
        if (previousReviewCard.isFinished()) {
            ReviewCard(card, ASKING, true)
        }
        else {
            previousReviewCard!!.copy(state = RATING, animate = true)
        }

    private fun onCardEdited(cardEdited: Card) {
        // Updating currentCard will trigger an update on _reviewCard
        // So I reset _reviewCard to a value which will lead to a good updated value
        GlobalScope.launch(Dispatchers.Main) {
            _currentCard.postValue(cardEdited)
            _reviewCard.value?.let {
                _reviewCard.postValue(it.copy(
                    front = cardEdited.frontAsTextOrNull() ?: "",
                    back = cardEdited.backAsTextOrNull() ?: "",
                    animate = false
                ))
            }
        }
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
