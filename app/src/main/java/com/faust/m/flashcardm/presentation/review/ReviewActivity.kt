package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.core.domain.Card
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.provideBookletViewModel
import org.jetbrains.anko.toast

class ReviewActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Init viewModel
        val bookletId = intent.getLongExtra(BOOKLET_ID, 0)
        viewModel = provideBookletViewModel(bookletId)
        // Setup observe data in viewModel
        viewModel.cards().observe(this, ::onCardsChanged)
    }

    private fun onCardsChanged(cards: List<Card>) {
        toast("There are ${cards.size} card on this booklet")
    }
}