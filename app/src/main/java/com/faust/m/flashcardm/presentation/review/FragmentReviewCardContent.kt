package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.transition.TransitionManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.view_library_booklet.displayShortName
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.RATING
import kotlinx.android.synthetic.main.fragment_review_card_content.*
import kotlinx.android.synthetic.main.recycler_view_library_booklets.*
import org.koin.android.ext.android.getKoin

class FragmentReviewCardContent: Fragment(), LiveDataObserver {

    private lateinit var libraryBookletBinding: RecyclerViewLibraryBookletsBinding
    private lateinit var viewModel: ReviewViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_review_card_content, container, false)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        with(this.viewLifecycleOwner) {
            viewModel.reviewCard.observeData(this, ::onCurrentCardChanged)
            viewModel.booklet.observeData(this, ::onBookletChanged)
        }

        return result
    }

    private fun onBookletChanged(booklet: LibraryBooklet) {
        libraryBookletBinding.booklet = booklet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize LibraryBookletBinding
        libraryBookletBinding = with(LayoutInflater.from(insert_point.context)) {
            RecyclerViewLibraryBookletsBinding.inflate(this, insert_point, true)
        }.apply {
            displayShortName()
            booklet = LibraryBooklet.LOADING
        }

        iv_info.setOnClickListener(::onBookletInfoClicked)
    }


    private fun onBookletInfoClicked(view: View) {
        PopupMenu(activity, view).apply {
            menuInflater.inflate(R.menu.menu_review, this.menu)
            setOnMenuItemClickListener(::onInfoMenuClick)
            show()
        }
    }

    private fun onInfoMenuClick(menuItem: MenuItem?): Boolean {
        return when (menuItem?.itemId) {
            R.id.menu_action_edit_card -> {
                viewModel.startEditCard()
                true
            }
            else -> false
        }
    }

    private fun onCurrentCardChanged(reviewCard: ReviewCard) {
        if (reviewCard == ReviewCard.EMPTY) {
            return
        }
        tv_card_back.text = reviewCard.back
        tv_card_front.text = reviewCard.front
        if (!reviewCard.animate) {
            return
        }
        if (reviewCard.state == RATING) {
            animateView { it.setVisibility(R.id.tv_card_back, View.VISIBLE) }
        } else {
            tv_card_front.visibility = View.INVISIBLE
            tv_card_back.visibility = View.INVISIBLE
            animateView { it.setVisibility(R.id.tv_card_front, View.VISIBLE) }
        }
    }

    private fun animateView(constraint: (constraintSet: ConstraintSet) -> Unit) {
        (view as ConstraintLayout).let {
            TransitionManager.beginDelayedTransition(it)
            ConstraintSet().apply {
                clone(it)
                constraint.invoke(this)
                applyTo(it)
            }
        }
    }
}