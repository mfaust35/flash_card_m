package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.CurrentCard.State.RATING
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.fragment_review_actions.*
import org.koin.android.ext.android.getKoin

class FragmentReviewActions: Fragment(), LiveDataObserver {

    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_actions, container, false)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.getCurrentCard().observe(this.viewLifecycleOwner, ::onCurrentCardChanged)

        return result
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup button click listener
        bt_show_answer.setOnClickListener(::onShowAnswerClicked)
        bt_ask_again.setOnClickListener(::onAskAgainClicked)
        bt_i_knew.setOnClickListener(::onIKnewClicked)
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {
        if (currentCard == CurrentCard.EMPTY) {
            return
        }
        (view as ConstraintLayout).let {
            TransitionManager.beginDelayedTransition(it, AutoTransition().apply { duration = 100 })
            ConstraintSet().apply {
                clone(it)
                when (currentCard.state) {
                    ASKING -> {
                        setVisibility(R.id.bt_show_answer, View.VISIBLE)
                        setVisibility(R.id.bt_ask_again, View.GONE)
                        setVisibility(R.id.bt_i_knew, View.GONE)
                    }
                    RATING -> {
                        setVisibility(R.id.bt_show_answer, View.GONE)
                        setVisibility(R.id.bt_ask_again, View.VISIBLE)
                        setVisibility(R.id.bt_i_knew, View.VISIBLE)
                    }
                }
                applyTo(it)
            }
        }
    }

    private fun onShowAnswerClicked() {
        viewModel.switchCurrent()
    }

    private fun onAskAgainClicked() {
        viewModel.repeatCurrentCard()
    }

    private fun onIKnewClicked() {
        viewModel.switchCurrent()
    }
}
