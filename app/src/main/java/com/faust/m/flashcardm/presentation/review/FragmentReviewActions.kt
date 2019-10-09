package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.provideBookletViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn

class FragmentReviewActions: Fragment(), LiveDataObserver, AnkoLogger {

    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_actions, container, false)

        // Init View Model
        warn { "Initializing viewModel in Fragment actions" }
        val bookletId = activity?.intent?.getLongExtra(BOOKLET_ID, 0) ?: 0
        viewModel = provideBookletViewModel(bookletId)
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
