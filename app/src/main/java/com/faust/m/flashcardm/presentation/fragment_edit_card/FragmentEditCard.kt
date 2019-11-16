package com.faust.m.flashcardm.presentation.fragment_edit_card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.core.domain.Card
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.*
import com.faust.m.flashcardm.presentation.booklet.BookletActivity
import com.faust.m.flashcardm.presentation.booklet.BookletViewModel
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState.ADD
import com.faust.m.flashcardm.presentation.fragment_edit_card.CardEditionState.EDIT
import com.faust.m.flashcardm.presentation.fragment_edit_card.FragmentEditCard.EditTextMatching.BACK
import com.faust.m.flashcardm.presentation.fragment_edit_card.FragmentEditCard.EditTextMatching.FRONT
import com.faust.m.flashcardm.presentation.review.ReviewActivity
import com.faust.m.flashcardm.presentation.review.ReviewViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit_card.*
import org.jetbrains.anko.AnkoLogger
import org.koin.android.ext.android.getKoin


class FragmentEditCard: Fragment(), LiveDataObserver, AnkoLogger {

    private lateinit var viewModel: ViewModelEditCard


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_edit_card, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        with(this.viewLifecycleOwner) {
            viewModel.cardEditionState.observeData(this, ::onCardEditionStateChanged)
            viewModel.cardToEdit.observeData(this, ::onCardChanged)
        }

        // Setup view listeners
        bt_close.setNoArgOnClickListener(::onClose)
        et_card_front.addTextChangedListener(ValidationTextWatcher(FRONT))
        et_card_back.addTextChangedListener(ValidationTextWatcher(BACK))
    }

    private fun initializeViewModel() {
        // I could not find any other way (apart from reflexion) to infer which viewModel
        // should be created
        // This solution seems cleaner than reflexion even if it means that each time this
        // fragment is used somewhere else we need to update this code.
        // The error should be pretty clear to understand and should help to update code
        activity?.let { activity ->
            viewModel = getKoin().get<BookletViewModelFactory>().let {
                when (activity) {
                    is ReviewActivity -> it.createViewModelFrom<ReviewViewModel>(this)
                    is BookletActivity -> it.createViewModelFrom<BookletViewModel>(this)
                    else -> throw IllegalStateException("Cannot infer which ViewModel to" +
                            " instantiate. If you want to use this fragment in an activity that" +
                            " is neither ReviewActivity nor BookletActivity, then you need" +
                            " to select which viewModel should be used")
                }
            }
        } ?: throw IllegalStateException("Cannot instantiate fragment if activity is null")
    }

    private fun onCardEditionStateChanged(state: CardEditionState) {
        when(state) {
            EDIT -> {
                bt_confirm.configureAsEdit()
                activity.showKeyboard(et_card_front)
            }
            ADD -> {
                bt_confirm.configureAsAdd()
                activity.showKeyboard(et_card_front)
            }
            else -> activity.hideKeyboard(et_card_front)
        }
    }

    private fun MaterialButton.configureAsEdit() {
        setNoArgOnClickListener(::onEditCardClicked)
        setText(R.string.confirm_edit_card)
    }

    private fun MaterialButton.configureAsAdd() {
        setNoArgOnClickListener(::onAddCardClicked)
        setText(R.string.confirm_new_card)
    }

    private fun onCardChanged(card: Card?) {
        card?.let {
            val frontValue = it.frontAsTextOrNull()
            updateText(frontValue, et_card_front)
            updateText(it.backAsTextOrNull(), et_card_back)
            frontValue?.let { value -> et_card_front.setSelection(value.length) }
        }
    }

    private fun updateText(text: String?, view: EditText) {
        text?.let {
            when { (it != view.text.toString()) -> view.setText(it) }
        } ?: view.setText("")
    }



    private fun onAddCardClicked() {
        et_card_front.clearFocus()
        et_card_back.clearFocus()
        viewModel.addCard(et_card_front.text.toString(), et_card_back.text.toString())
    }

    private fun onEditCardClicked() {
        viewModel.editCard(et_card_front.text.toString(), et_card_back.text.toString())
        onClose()
    }

    private fun onClose() {
        viewModel.stopCardEdition()
    }


    private var frontValid = false
    private var backValid = false
    enum class EditTextMatching { FRONT, BACK }

    private inner class ValidationTextWatcher(private val match: EditTextMatching): TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            (editable?.toString()?.isNotBlank() ?: false).let { valid ->
                when(match) {
                    FRONT -> frontValid = valid
                    BACK -> backValid = valid
                }
            }
            bt_confirm.isEnabled = frontValid && backValid
        }
    }
}
