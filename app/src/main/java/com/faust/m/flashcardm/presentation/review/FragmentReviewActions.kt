package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver

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


        result.findViewById<Button>(R.id.bt_actions).setOnClickListener {
            viewModel.switchCurrent()
        }
        return result
    }

    private fun onCurrentCardChanged(currentCard: CurrentCard) {

    }
}
