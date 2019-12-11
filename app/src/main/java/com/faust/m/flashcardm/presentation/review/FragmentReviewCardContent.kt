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
import com.faust.m.flashcardm.databinding.FragmentReviewCardContentBinding
import com.faust.m.flashcardm.databinding.ViewBookletBannerBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.ASKING
import com.faust.m.flashcardm.presentation.review.ReviewCard.State.RATING
import com.faust.m.flashcardm.presentation.view_library_booklet.displayCardCount
import com.faust.m.flashcardm.presentation.view_library_booklet.displayShortName
import kotlinx.android.synthetic.main.fragment_review_card_content.*
import kotlinx.android.synthetic.main.view_booklet_banner.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin

class FragmentReviewCardContent: Fragment(), LiveDataObserver {

    private lateinit var bookletBannerBinding: ViewBookletBannerBinding
    private lateinit var viewModel: ReviewViewModel
    private lateinit var cardBinding: FragmentReviewCardContentBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Initialize main view binding
        cardBinding =
            FragmentReviewCardContentBinding.inflate(inflater, container, false)
                .apply { card = ReviewCard.LOADING }

        // Initialize BookletBannerBinding and insert it into the main view
        val insertPoint = cardBinding.root.find<ViewGroup>(R.id.insert_point)
        bookletBannerBinding =
            ViewBookletBannerBinding.inflate(inflater, insertPoint, true)
                .apply {
                    displayShortName()
                    displayCardCount()
                    booklet = BookletBannerData.LOADING
                }

        return cardBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init viewModel
        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        with(viewLifecycleOwner) {
            viewModel.reviewCard.observeData(this, ::onCurrentCardChanged)
            viewModel.bookletBannerData.observeData(this, ::onBookletChanged)
        }

        cardBinding.viewModel = viewModel
        iv_info.setOnClickListener(::onBookletInfoClicked)
    }


    private fun onBookletChanged(booklet: BookletBannerData) {
        bookletBannerBinding.booklet = booklet
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
        cardBinding.card = reviewCard
        if (!reviewCard.animate) {
            return
        }
        when(reviewCard.state) {
            RATING -> animateView { it.setVisibility(R.id.tv_card_back, View.VISIBLE) }
            ASKING -> {
                tv_card_front.visibility = View.INVISIBLE
                tv_card_back.visibility = View.INVISIBLE
                animateView { it.setVisibility(R.id.tv_card_front, View.VISIBLE) }
            }
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
