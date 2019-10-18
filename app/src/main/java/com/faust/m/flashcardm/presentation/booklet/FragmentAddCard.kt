package com.faust.m.flashcardm.presentation.booklet

import android.content.Context
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
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import kotlinx.android.synthetic.main.activity_add_card.bt_add_card
import kotlinx.android.synthetic.main.activity_add_card.et_card_back
import kotlinx.android.synthetic.main.activity_add_card.et_card_front
import kotlinx.android.synthetic.main.fragment_add_card.*
import org.jetbrains.anko.AnkoLogger
import org.koin.android.ext.android.getKoin


class FragmentAddCard: Fragment(), LiveDataObserver, AnkoLogger {

    private lateinit var viewModel: BookletViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_add_card, container, false)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.getCard().observeData(this.viewLifecycleOwner, ::onCardChanged)

        return result
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup view listeners
        bt_add_card.setNoArgOnClickListener(::onAddCardClicked)
        bt_close.setOnClickListener {
            viewModel.triggerStopAdd()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(it.getWindowToken(), 0)
        }
        et_card_front.setOnFocusChangeListener(::onFocusChanged)
        et_card_front.addTextChangedListener(ValidationTextWatcher())
        et_card_back.addTextChangedListener(ValidationTextWatcher())
    }

    private fun onCardChanged(card: Card) {
        updateText(card.frontAsTextOrNull(), et_card_front)
        updateText(card.backAsTextOrNull(), et_card_back)
        if(et_card_front.requestFocus()) {
            (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
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
        saveFront()
        saveBack()
        viewModel.addCard()
    }

    private fun saveFront() {
        viewModel.updateCardFront(et_card_front.text.toString())
    }

    private fun saveBack() {
        viewModel.updateCardBack(et_card_back.text.toString())
    }

    private fun onFocusChanged(view: View, hasFocus: Boolean) {
        if (hasFocus) return
        when(view) {
            et_card_front -> saveFront()
            et_card_back -> saveBack()
        }
    }

    private inner class ValidationTextWatcher: TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            (et_card_back.text.toString().isNotBlank() &&
                    et_card_front.text.toString().isNotBlank()).let { isValid ->
                bt_add_card.isEnabled = isValid
            }
        }
    }
}
