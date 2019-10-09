package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.provideBookletViewModel
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.RATING
import kotlinx.android.synthetic.main.fragment_review_card_content.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

class FragmentReviewCardContent: Fragment(), LiveDataObserver, AnkoLogger {

    private lateinit var viewModel: ReviewViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_card_content, container, false)

        warn { "Initializing viewModel in Fragment card content" }
        // Init View Model
        val bookletId = activity?.intent?.getLongExtra(BOOKLET_ID, 0) ?: 0
        viewModel = provideBookletViewModel(bookletId)
        // Setup observe data in viewModel
        viewModel.getCurrentCard().observe(this.viewLifecycleOwner, ::onCurrentCardChanged)

        return result
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {
        tv_card_front.text = currentCard.front
        tv_card_back.text = currentCard.back

        (view as ConstraintLayout).let {
            TransitionManager.beginDelayedTransition(it)
            ConstraintSet().apply {
                clone(it)
                when (currentCard.state) {
                    ASKING -> setVisibility(R.id.tv_card_back, View.INVISIBLE)
                    RATING -> setVisibility(R.id.tv_card_back, View.VISIBLE)
                }
                applyTo(it)
            }
        }
    }
}