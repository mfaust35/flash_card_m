package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.RATING
import kotlinx.android.synthetic.main.fragment_review_card_content.*

class FragmentReviewCardContent: Fragment(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_card_content, container, false)

        // Init View Model
        activity?.let { viewModel = ReviewActivity.initViewModel(it) }
        // Setup observe data in viewModel
        viewModel.getCurrentCard().observe(this.viewLifecycleOwner, ::onCurrentCardChanged)

        return result
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {
        if (currentCard.state == RATING) {
            animateView { it.setVisibility(R.id.tv_card_back, View.VISIBLE) }
        } else {
            tv_card_front.visibility = View.INVISIBLE
            tv_card_back.visibility = View.INVISIBLE
            tv_card_back.text = currentCard.back
            tv_card_front.text = currentCard.front
            animateView { it.setVisibility(R.id.tv_card_front, View.VISIBLE) }
        }
    }

    private fun animateView(listener: TransitionListenerAdapter? = null,
                            constraint: (constraintSet: ConstraintSet) -> Unit) {
        (view as ConstraintLayout).let {
            val transition = AutoTransition().apply {
                listener?.let { l -> addListener(l) }
            }
            TransitionManager.beginDelayedTransition(it, transition)
            ConstraintSet().apply {
                clone(it)
                constraint.invoke(this)
                applyTo(it)
            }
        }
    }
}