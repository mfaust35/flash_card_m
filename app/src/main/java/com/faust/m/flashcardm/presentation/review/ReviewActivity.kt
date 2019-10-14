package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import org.koin.android.ext.android.getKoin

class ReviewActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.getCurrentCard().observeData(this, ::onCurrentCardChanged)
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {
        if (currentCard == CurrentCard.EMPTY) {
            finish()
        }
    }
}