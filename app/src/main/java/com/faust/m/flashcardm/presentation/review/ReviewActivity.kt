package com.faust.m.flashcardm.presentation.review

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.provideBookletViewModel

class ReviewActivity: AppCompatActivity() {

    companion object {
        internal fun initViewModel(reviewActivity: Activity): ReviewViewModel {
            // Init View Model
            val bookletId = reviewActivity.intent?.getLongExtra(BOOKLET_ID, 0) ?: 0
            return reviewActivity.provideBookletViewModel(bookletId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
    }
}