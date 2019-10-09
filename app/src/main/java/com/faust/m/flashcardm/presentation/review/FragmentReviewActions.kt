package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.ASKING
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.fragment_review_actions.*

class FragmentReviewActions: Fragment(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_actions, container, false)

        // Init View Model
        activity?.let { viewModel = ReviewActivity.initViewModel(it) }
        // Setup observe data in viewModel
        viewModel.getCurrentCard().observe(this.viewLifecycleOwner, ::onCurrentCardChanged)
        return result
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup button click listener
        bt_show_answer.setOnClickListener(::onShowAnswerClicked)
        bt_ask_again.setOnClickListener(::onShowAnswerClicked)
        bt_i_knew.setOnClickListener(::onShowAnswerClicked)
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {
        if (currentCard.state == ASKING) {
            view?.setBackgroundResource(R.color.colorAccent)
            bt_show_answer.visibility = View.VISIBLE
            bt_ask_again.visibility = View.GONE
            bt_i_knew.visibility = View.GONE
        } else {
            view?.setBackgroundResource(R.color.colorWhite)
            bt_show_answer.visibility = View.GONE
            bt_ask_again.visibility = View.VISIBLE
            bt_i_knew.visibility = View.VISIBLE
        }
    }

    private fun onShowAnswerClicked() {
        viewModel.switchCurrent()
    }
}
