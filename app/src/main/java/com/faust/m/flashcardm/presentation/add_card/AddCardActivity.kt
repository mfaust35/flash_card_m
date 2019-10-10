package com.faust.m.flashcardm.presentation.add_card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.activity_add_card.*
import org.koin.android.ext.android.getKoin

const val BOOKLET_ID = "booklet_id"

class AddCardActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: AddCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.getCard().observe(this, ::onCardChanged)

        // Setup view listeners
        bt_add_card.setOnClickListener(::onAddCardClicked)
        et_card_front.setOnFocusChangeListener(::onFocusChanged)
    }

    private fun onCardChanged(card: Card) {
        updateText(card.frontAsTextOrNull(), et_card_front)
        updateText(card.backAsTextOrNull(), et_card_back)
        et_card_front.requestFocus()
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
}