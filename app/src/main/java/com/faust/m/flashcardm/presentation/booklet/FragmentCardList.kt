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
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import com.faust.m.flashcardm.presentation.view_library_booklet.displayShortName
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_card_list.*
import kotlinx.android.synthetic.main.recycler_view_library_booklets.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin

class FragmentCardList: Fragment(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var libraryBookletBinding: RecyclerViewLibraryBookletsBinding
    private lateinit var viewModel: BookletViewModel


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result =
            inflater.inflate(R.layout.fragment_card_list, container, false)

        viewModel =
            getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        with(this.viewLifecycleOwner) {
            viewModel.booklet.observeData(this, ::onBookletChanged)
            viewModel.bookletCards.observeData(this, ::onCardsChanged)
            viewModel.cardRemovalStatus.observeData(this, ::onDeleteCardStateChanged)
        }

        return result
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

        // Initialize adapter
        bookletCardAdapter = BookletCardAdapter(onItemClick = ::onEditCard)
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(activity)
        recycler_view_cards.adapter = bookletCardAdapter

        fab_add_card.setNoArgOnClickListener(::onAddCardClicked)
        iv_info.setOnClickListener(::onBookletInfoClicked)
    }

    private fun onEditCard(card: BookletCard) = viewModel.startCardEdition(card)

    private fun onBookletChanged(booklet: LibraryBooklet) {
        libraryBookletBinding.booklet = booklet
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
        showEmptyRecyclerView(cards.isEmpty())
    }

    private fun showEmptyRecyclerView(show: Boolean) {
        tv_empty_recycler_view.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun onDeleteCardStateChanged(deleteCard: CardRemovalStatus) {
        if (deleteCard.state == CardRemovalStatus.State.SELECTING) {
            libraryBookletBinding.animateToCancelButton()
            fab_add_card.animateToConfirmDeleteFAB()
            bookletCardAdapter.switchMode(true, ::onSelectItem)
            bookletCardAdapter.notifyDataSetChanged()
        }
        else {
            libraryBookletBinding.animateToInfoButton()
            fab_add_card.animateToAddCardFAB()
            bookletCardAdapter.switchMode(false, ::onEditCard)
            if (deleteCard.state == CardRemovalStatus.State.DELETED) {
                bookletCardAdapter.notifyItemDeleted(deleteCard.position, deleteCard.bookletCards)
                showEmptyRecyclerView(deleteCard.bookletCards.isEmpty())
            }
            else {
                bookletCardAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onCancelDeleteClicked() = viewModel.stopRemoveCard()

    private fun onConfirmDeleteClicked() = viewModel.deleteSelectedBookletCards()

    private fun onSelectItem(card: BookletCard) =
        viewModel.switchBookletCardForRemoval(card)

    private fun onAddCardClicked() = viewModel.startCardAddition()


    fun makeFABAnimation(enable: Boolean) {
        fab_add_card.apply {
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
            show()
        }
    }

    private fun onInfoMenuClick(menuItem: MenuItem?): Boolean {
        return when (menuItem?.itemId) {
            R.id.menu_action_delete_card -> {
                viewModel.startRemoveCards()
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

    private fun RecyclerViewLibraryBookletsBinding.animateToCancelButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_cancel_white_24dp)
            it.clearAnimation()
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
            it.setNoArgOnClickListener(::onCancelDeleteClicked)
        }

    private fun RecyclerViewLibraryBookletsBinding.animateToInfoButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_info_white_24dp)
            it.clearAnimation()
            it.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.pop_up))
            it.setOnClickListener(::onBookletInfoClicked)
        }
}
