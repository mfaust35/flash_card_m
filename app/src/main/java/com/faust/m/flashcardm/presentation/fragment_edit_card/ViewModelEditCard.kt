package com.faust.m.flashcardm.presentation.fragment_edit_card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.core.usecase.CardUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


enum class CardEditionState { EDIT, ADD, CLOSED }

/**
 * This viewModel extension is intended for use with the fragment FragmentEditCard.
 * To see an example of usage, look into the booklet package
 * Activity should observe cardEditionState and show or hide the fragment according to the state
 * activity_layout should include the fragment
 * Activity should hide the fragment during onCreate()
 * ActivityViewModel should delegate the card edition to DelegateEditCard, like:
    class ActivityViewModel @JvmOverloads constructor(
        private val var1: Long,
        private val delegateEditCard: DelegateEditCard = DelegateEditCard(bookletId)
    ): ViewModel(),
        OtherInterface,
        ViewModelEditCard by delegateEditCard { ... }
 */
interface ViewModelEditCard {

    // TODO: Maybe could simplify this code by having one object with card + state
    val cardToEdit: LiveData<Card?>
    val cardEditionState: LiveData<CardEditionState>

    var onCardCreated: ((newCard: Card) -> Unit)?
    var onCardEdited: ((editedCard: Card) -> Unit)?

    fun parentScope(): CoroutineScope?

    fun addCard(front: String, back: String)
    fun editCard(front: String, back: String)
    fun startCardAddition()
    fun startCardEdition(card: Card?)
    fun stopCardEdition()

    fun onBackPressed(): Boolean
}


class DelegateEditCard(private val bookletId: Long): ViewModelEditCard, KoinComponent, AnkoLogger {

    private val cardUseCases: CardUseCases by inject()


    private val _cardEditionState: MutableLiveData<CardEditionState> =
        MutableLiveData(CardEditionState.CLOSED)
    override val cardEditionState: LiveData<CardEditionState> = _cardEditionState

    private val _cardToEdit: MutableLiveData<Card?> = MutableLiveData()
    override val cardToEdit: LiveData<Card?> = _cardToEdit

    override var onCardCreated: ((newCard: Card) -> Unit)? = null
    override var onCardEdited: ((editedCard: Card) -> Unit)? = null


    override fun parentScope(): CoroutineScope? = null

    override fun addCard(front: String, back: String) {
        _cardToEdit.value?.let {
            it.addFrontAsText(front)
            it.addBackAsText(back)
            parentScope()?.launch(Dispatchers.IO) {
                cardUseCases.addCard(it).also { newCard ->
                    verbose { "Created a new card: $newCard" }
                    _cardToEdit.postValue(Card(bookletId = bookletId))
                    onCardCreated?.invoke(newCard)
                }
            }
        }
    }

    override fun editCard(front: String, back: String) {
        _cardToEdit.value?.copy(createdAt = Date())?.let { cardToUpdate ->
            cardToUpdate.editFrontAsText(front)
            cardToUpdate.editBackAsText(back)
            parentScope()?.launch(Dispatchers.IO) {
                cardUseCases.updateCardContent(cardToUpdate).also { updatedCard ->
                    verbose { "Updated a card: $updatedCard" }
                    onCardEdited?.invoke(updatedCard)
                }
            }
        }
    }

    override fun startCardAddition() {
        _cardEditionState.postValue(CardEditionState.ADD)
        when (_cardToEdit.value) {
            null -> _cardToEdit.postValue(Card(bookletId = bookletId))
        }
    }

    override fun startCardEdition(card: Card?) {
        _cardToEdit.postValue(card)
        _cardEditionState.postValue(CardEditionState.EDIT)
    }

    override fun stopCardEdition() {
        _cardEditionState.postValue(CardEditionState.CLOSED)
        _cardToEdit.postValue(null)
    }

    override fun onBackPressed(): Boolean {
        if (_cardEditionState.value == CardEditionState.EDIT || _cardEditionState.value == CardEditionState.ADD) {
            stopCardEdition()
            return true
        }
        return false
    }

}