package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.ViewBookletBannerBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.SELECTING
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import com.faust.m.flashcardm.presentation.view_library_booklet.displayShortName
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_card_list.*
import kotlinx.android.synthetic.main.view_booklet_banner.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin

class FragmentCardList: Fragment(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var bookletBannerBinding: ViewBookletBannerBinding
    private lateinit var viewModel: BookletViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_card_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init viewModel
        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        with(this.viewLifecycleOwner) {
            viewModel.bookletBannerData.observeData(this, ::onBookletChanged)
            viewModel.bookletCards.observeData(this, ::onCardsChanged)
            viewModel.cardRemovalStatus.observeData(this, ::onDeleteCardStateChanged)
        }

        // Initialize BookletBannerBinding
        bookletBannerBinding = with(LayoutInflater.from(insert_point.context)) {
            ViewBookletBannerBinding.inflate(this, insert_point, true)
        }.apply {
            displayShortName()
            booklet = BookletBannerData.LOADING
        }

        // Initialize adapter
        bookletCardAdapter = BookletCardAdapter(onItemClick = ::onEditCard)
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(activity)
        recycler_view_cards.adapter = bookletCardAdapter

        fab_add_card.setNoArgOnClickListener(::onAddCardClicked)
        iv_info.setOnClickListener(::onBookletInfoClicked)
    }

    private fun onEditCard(cardData: BookletCard) = viewModel.startCardEdition(cardData)

    private fun onBookletChanged(booklet: BookletBannerData) {
        bookletBannerBinding.booklet = booklet
    }

    private fun onCardsChanged(cardsData: MutableList<BookletCard>) {
        bookletCardAdapter.submitList(cardsData)
        showEmptyRecyclerView(cardsData.isEmpty())
    }

    private fun showEmptyRecyclerView(show: Boolean) {
        iv_empty_recycler_view.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun onDeleteCardStateChanged(deleteCard: CardRemovalStatus) {
        when (deleteCard) {
            SELECTING -> animateUIToSelectingStateOn()
            else -> animateUIToSelectionStateOff()
        }
    }

    private fun animateUIToSelectingStateOn() {
        bookletBannerBinding.animateToCancelButton()
        fab_add_card.animateToConfirmDeleteFAB()
        bookletCardAdapter.onItemClick = ::onSelectItem
    }

    private fun animateUIToSelectionStateOff() {
        bookletBannerBinding.animateToInfoButton()
        fab_add_card.animateToAddCardFAB()
        bookletCardAdapter.onItemClick = ::onEditCard
    }

    private fun onCancelDeleteClicked() = viewModel.stopRemoveCard()

    private fun onConfirmDeleteClicked() = viewModel.deleteSelectedBookletCards()

    private fun onSelectItem(cardData: BookletCard) =
        viewModel.switchBookletCardForRemoval(cardData)

    private fun onAddCardClicked() = viewModel.startCardAddition()


    fun makeFABAnimation(enable: Boolean) {
        fab_add_card.apply {
            if (isEnabled == enable) return // Prevent animation if same state is requested
            clearAnimation()
            val animId = if (enable) R.anim.pop_up else R.anim.pop_down
            startAnimation(AnimationUtils.loadAnimation(activity, animId))
            isEnabled = enable
        }
    }

    private fun onBookletInfoClicked(view: View) {
        PopupMenu(activity, view).apply {
            menuInflater.inflate(R.menu.menu_card, this.menu)
            setOnMenuItemClickListener(::onInfoMenuClick)
            menu.findItem(R.id.menu_action_switch_show_rating_level).setTitle(
                if (viewModel.showRatingLevel) R.string.menu_action_hide_learning_stage
                else R.string.menu_action_show_learning_stage
            )
            show()
        }
    }

    private fun onInfoMenuClick(menuItem: MenuItem?): Boolean {
        return when (menuItem?.itemId) {
            R.id.menu_action_delete_card -> {
                viewModel.startRemoveCards()
                true
            }
            R.id.menu_action_switch_show_rating_level -> {
                viewModel.switchShowRatingLevel()
                true
            }
            else -> false
        }
    }

    private fun FloatingActionButton.animateToAddCardFAB() {
        setImageResource(R.drawable.ic_library_add_black_24dp)
        clearAnimation()
        startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
        setNoArgOnClickListener(::onAddCardClicked)
    }

    private fun FloatingActionButton.animateToConfirmDeleteFAB() {
        setImageResource(R.drawable.ic_delete_forever_white_24dp)
        clearAnimation()
        startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
        setNoArgOnClickListener(::onConfirmDeleteClicked)
    }

    private fun ViewBookletBannerBinding.animateToCancelButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_cancel_white_24dp)
            it.clearAnimation()
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
            it.setNoArgOnClickListener(::onCancelDeleteClicked)
        }

    private fun ViewBookletBannerBinding.animateToInfoButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_info_white_24dp)
            it.clearAnimation()
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
            it.setOnClickListener(::onBookletInfoClicked)
        }
}
