package com.faust.m.flashcardm.presentation.add_card

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.activity_add_card.*
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf

const val BOOKLET_ID = "booklet_id"

class AddCardActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: AddCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        // Init viewModel
        val bookletId = intent.getLongExtra(BOOKLET_ID, 0)
        val viewModelFactory:AddCardViewModel.Factory = getKoin().get{ parametersOf(bookletId) }
        viewModel = ViewModelProvider(this, viewModelFactory).get(AddCardViewModel::class.java)
        // Setup observe data in viewModel
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
        viewModel.addCard()
    }

    private fun onFocusChanged(view: View, hasFocus: Boolean) {
        if (hasFocus) return
        when(view) {
            et_card_front -> viewModel.updateCardFront(et_card_front.text.toString())
            et_card_back -> viewModel.updateCardBack(et_card_back.text.toString())
        }
    }
}