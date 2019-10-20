package com.faust.m.flashcardm.presentation.booklet

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.EDIT
import com.faust.m.flashcardm.presentation.booklet.FragmentEditCard.EditTextMatching.BACK
import com.faust.m.flashcardm.presentation.booklet.FragmentEditCard.EditTextMatching.FRONT
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_edit_card.*
import org.koin.android.ext.android.getKoin


class FragmentEditCard: Fragment(), LiveDataObserver {

    private lateinit var viewModel: BookletViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_edit_card, container, false)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        with(this.viewLifecycleOwner) {
            viewModel.cardEditionState.observeData(this, ::onCardEditionStateChanged)
            viewModel.currentCard.observeData(this, ::onCardChanged)
        }

        return result
    }

    private fun onCardEditionStateChanged(state: CardEditionState) {
        when(state) {
            EDIT -> bt_confirm.configureAsEdit()
            else -> bt_confirm.configureAsAdd()
        }
    }

    private fun MaterialButton.configureAsAdd() {
        setNoArgOnClickListener(::onAddCardClicked)
        setText(R.string.confirm_new_card)
    }

    private fun MaterialButton.configureAsEdit() {
        setNoArgOnClickListener(::onEditCardClicked)
        setText(R.string.confirm_edit_card)
    }

    private fun onCardChanged(card: Card?) {
        card?.let {
            updateText(it.frontAsTextOrNull(), et_card_front)
            updateText(it.backAsTextOrNull(), et_card_back)
            if(et_card_front.requestFocus()) {
                activity?.run {
                    (this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)
                        ?.showSoftInput(et_card_front, 0)
                }
            }
        }
    }

    private fun updateText(text: String?, view: EditText) {
        text?.let {
            when { (it != view.text.toString()) -> view.setText(it) }
        } ?: view.setText("")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup view listeners
        bt_close.setOnClickListener(::onClose)
        et_card_front.addTextChangedListener(ValidationTextWatcher(FRONT))
        et_card_back.addTextChangedListener(ValidationTextWatcher(BACK))
    }

    private fun onAddCardClicked() {
        et_card_front.clearFocus()
        et_card_back.clearFocus()
        viewModel.addCard(et_card_front.text.toString(), et_card_back.text.toString())
    }

    private fun onEditCardClicked() {
        viewModel.editCard(et_card_front.text.toString(), et_card_back.text.toString())
        onClose(bt_close)
    }

    private fun onClose(v: View) {
        viewModel.stopCardEdition()
        activity?.let {
            (it.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)
                ?.hideSoftInputFromWindow(v.windowToken, 0)
        }
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
