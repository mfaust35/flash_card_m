package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.*
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.State.DELETED
import com.faust.m.flashcardm.presentation.booklet.CardRemovalStatus.State.SELECTING
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_booklet.*
import kotlinx.android.synthetic.main.recycler_view_library_booklets.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin


class BookletActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var libraryBookletBinding: RecyclerViewLibraryBookletsBinding
    private lateinit var viewModel: BookletViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booklet)

        // Initialize LibraryBookletBinding
        libraryBookletBinding = with(LayoutInflater.from(insert_point.context)) {
            RecyclerViewLibraryBookletsBinding.inflate(this, insert_point, true)
        }.apply {
            displayShortName()
            booklet = LibraryBooklet.LOADING
        }

        // Initialize adapter
        bookletCardAdapter = BookletCardAdapter()
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(this)
        recycler_view_cards.adapter = bookletCardAdapter

        // Hide fragment add card
        makeFragmentTransaction(animated = false) { it.hide(fg_add_card) }

        viewModel = getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.booklet.observeData(this, ::onBookletChanged)
        viewModel.bookletCards.observeData(this, ::onCardsChanged)
        viewModel.cardEditionState.observeData(this, ::onCardEditionStateChanged)
        viewModel.cardRemovalStatus.observeData(this, ::onDeleteCardStateChanged)
        viewModel.loadData()

        fab_add_card.setNoArgOnClickListener(::onAddCardClicked)
        iv_info.setOnClickListener(::onBookletInfoClicked)
    }

    private fun onBookletChanged(booklet: LibraryBooklet) {
        libraryBookletBinding.booklet = booklet
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
    }

    private fun onCardEditionStateChanged(cardEditionState: CardEditionState) =
        when(cardEditionState) {
            EDIT, ADD -> showFragment()
            CLOSED -> hideFragment()
        }

    private fun onDeleteCardStateChanged(deleteCard: CardRemovalStatus) {
        if (deleteCard.state == SELECTING) {
            libraryBookletBinding.animateToCancelButton()
            fab_add_card.animateToConfirmDeleteFAB()
            bookletCardAdapter.switchMode(true, ::onSelectItem)
            bookletCardAdapter.notifyDataSetChanged()
        }
        else {
            libraryBookletBinding.animateToInfoButton()
            fab_add_card.animateToAddCardFAB()
            bookletCardAdapter.switchMode(false, ::onEditCard)
            if (deleteCard.state == DELETED) {
                bookletCardAdapter.notifyItemDeleted(deleteCard.position, deleteCard.bookletCards)
            }
            else {
                bookletCardAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun onEditCard(card: BookletCard) = viewModel.startCardEdition(card)

    private fun onCancelDeleteClicked() = viewModel.stopRemoveCard()

    private fun onConfirmDeleteClicked() = viewModel.deleteSelectedBookletCards()

    private fun onSelectItem(card: BookletCard) =
        viewModel.switchBookletCardForRemoval(card)

    private fun onAddCardClicked() = viewModel.startCardAddition()


    private fun onBookletInfoClicked(view: View) {
        PopupMenu(this, view).apply {
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


    private fun showFragment() {
        makeFragmentTransaction { it.show(fg_add_card) }
        makeFABAnimation(false)
    }

    private fun hideFragment() {
        makeFragmentTransaction { it.hide(fg_add_card) }
        makeFABAnimation(true)
    }

    private fun makeFragmentTransaction(animated: Boolean = true,
                                        transaction: ((ft: FragmentTransaction) -> Unit)) =
        supportFragmentManager.beginTransaction().let { ft ->
            if (animated) {
                ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            }
            transaction.invoke(ft)
            ft.commit()
        }

    private fun makeFABAnimation(enable: Boolean) =
        fab_add_card.apply {
            clearAnimation()
            startAnimation(
                loadAnimation(this@BookletActivity,
                    if(enable) R.anim.pop_up else R.anim.pop_down)
            )
            isEnabled = enable
        }


    override fun onBackPressed() {
        if (!viewModel.onBackPressed()) {
            super.onBackPressed()
        }
    }


    private fun FloatingActionButton.animateToAddCardFAB() {
        setImageResource(R.drawable.ic_library_add_black_24dp)
        clearAnimation()
        startAnimation(loadAnimation(this@BookletActivity, R.anim.pop_up))
        setNoArgOnClickListener(::onAddCardClicked)
    }

    private fun FloatingActionButton.animateToConfirmDeleteFAB() {
        setImageResource(R.drawable.ic_delete_forever_white_24dp)
        clearAnimation()
        startAnimation(loadAnimation(this@BookletActivity, R.anim.pop_up))
        setNoArgOnClickListener(::onConfirmDeleteClicked)
    }

    private fun RecyclerViewLibraryBookletsBinding.animateToCancelButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_cancel_white_24dp)
            it.clearAnimation()
            it.startAnimation(loadAnimation(this@BookletActivity, R.anim.pop_up))
            it.setNoArgOnClickListener(::onCancelDeleteClicked)
        }

    private fun RecyclerViewLibraryBookletsBinding.animateToInfoButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_info_white_24dp)
            it.clearAnimation()
            it.startAnimation(loadAnimation(this@BookletActivity, R.anim.pop_up))
            it.setOnClickListener(::onBookletInfoClicked)
        }

    private fun RecyclerViewLibraryBookletsBinding.displayShortName() =
        root.find<TextView>(R.id.recycler_view_booklet_name).let {
            it.maxLines = 1
            it.ellipsize = TextUtils.TruncateAt.END
        }
}
