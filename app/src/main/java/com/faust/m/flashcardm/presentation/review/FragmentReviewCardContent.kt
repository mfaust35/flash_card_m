package com.faust.m.flashcardm.presentation.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.FragmentReviewCardContentBinding
import com.faust.m.flashcardm.databinding.ViewBookletBannerBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.EndAnimationListener
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
    private var _rootWidth = 0f
    private val _translationXShown = 0f
    private val _translationXHidden: Float
        get() = _rootWidth.times(-1)
    private val _toXDeltaIn: Float
        get() = _rootWidth
    private val _animationDuration = 200L


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

        // Initialize listener on layout change to reset translationX on cards outside of screen
        translateCardsOutsideViewOnLayoutChange(cardBinding.root)

        return cardBinding.root
    }

    private fun translateCardsOutsideViewOnLayoutChange(root: View) =
        root.addOnLayoutChangeListener{ _, left, _, right, _, _, _, _, _ ->
            _rootWidth = (right - left).toFloat()
            if (tv_card_back.isNotDisplayed()) tv_card_back.translationX = _translationXHidden
            if (tv_card_front.isNotDisplayed()) tv_card_front.translationX = _translationXHidden
        }

    private fun ReviewCardView.isNotDisplayed() = this.translationX != _translationXShown

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
            RATING -> animateShowRating()
            ASKING -> animateShowAsking()
        }
    }

    private fun animateShowRating() {
        tv_card_front.run {
            // Need to manually remove animationListener because clearAnimation stop the animation
            // but the endAnimationListener still get called, which is not useful in this case
            animation?.setAnimationListener(null)
            clearAnimation()
            translationX = _translationXHidden
        }
        tv_card_back.translationX = _translationXShown
    }

    private fun animateShowAsking() =
        tv_card_front.startAnimation(
            TranslateAnimation(0f, _toXDeltaIn, 0f, 0f).apply {
                duration = _animationDuration
                fillAfter = true // Need fillAfter to avoid a glitch in the UI
                // But it will be removed immediately in the endAnimationListener
                // We do need to remove it in the listener or else the button on cardReview
                // is unclickable
                setEndAnimationListener {
                    tv_card_front.run {
                        animation?.setAnimationListener(null)
                        clearAnimation()
                        translationX = _translationXShown
                    }
                    tv_card_back.translationX = _translationXHidden
                }
            }
        )

    private fun TranslateAnimation.setEndAnimationListener(listener: () -> Unit) {
        setAnimationListener(EndAnimationListener(listener))
    }
}
