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
        bookletCardAdapter = BookletCardAdapter(onItemClick = ::onBookletCardClicked)
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(this)
        recycler_view_cards.adapter = bookletCardAdapter

        // Hide fragment add card
        makeFragmentTransaction(animated = false) { it.hide(fg_add_card) }

        viewModel = getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.booklet.observeData(this, ::onBookletChanged)
        viewModel.bookletCards.observeData(this, ::onCardsChanged)
        viewModel.cardEditionState.observeData(this, ::onCardEditionState)
        viewModel.cardDeleteState.observeData(this, ::onDeleteCardStateChanged)
        viewModel.loadData()

        fab_add_card.setNoArgOnClickListener(::onFabAddCardClicked)
        iv_info.setOnClickListener(::onInfoClicked)
    }

    private fun onBookletCardClicked(card: BookletCard) {
        viewModel.startCardEdition(card)
    }

    private fun onBookletChanged(booklet: LibraryBooklet) {
        libraryBookletBinding.booklet = booklet
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
    }

    private fun onCardEditionState(cardEditionState: CardEditionState) =
        when(cardEditionState) {
            EDIT, ADD -> showFragment()
            CLOSED -> hideFragment()
        }

    private fun onDeleteCardStateChanged(deleteCard: DeleteCard) {
        if (deleteCard.state == DeleteCard.State.DELETING) {
            libraryBookletBinding.displayAsCancelButton()
            bookletCardAdapter.deleteMode = true
            bookletCardAdapter.idSelected = mutableListOf()
            bookletCardAdapter.onItemClick = ::onItemClickWhenDelete
            bookletCardAdapter.notifyDataSetChanged()
            fab_add_card.displayAsDelete()
            fab_add_card.setNoArgOnClickListener(::onFabDeleteClicked)
            iv_info.setNoArgOnClickListener(::onCancelDeleteClicked)
        }
        else if (deleteCard.state == DeleteCard.State.DELETED) {
            libraryBookletBinding.displayAsNormalButton()
            bookletCardAdapter.deleteMode = false
            bookletCardAdapter.onItemClick = ::onBookletCardClicked
            bookletCardAdapter.notifyItemDeleted(deleteCard.position)
            fab_add_card.displayAsAdd()
            fab_add_card.setNoArgOnClickListener(::onFabAddCardClicked)
            iv_info.setOnClickListener(::onInfoClicked)
        }
        else {
            libraryBookletBinding.displayAsNormalButton()
            bookletCardAdapter.deleteMode = false
            bookletCardAdapter.onItemClick = ::onBookletCardClicked
            bookletCardAdapter.notifyDataSetChanged()
            fab_add_card.displayAsAdd()
            fab_add_card.setNoArgOnClickListener(::onFabAddCardClicked)
            iv_info.setOnClickListener(::onInfoClicked)
        }
    }

    private fun onCancelDeleteClicked() {
        viewModel.cancelDelete()
    }

    private fun onFabDeleteClicked() {
        viewModel.deleteTheseItems()
    }

    private fun onItemClickWhenDelete(card: BookletCard) {
        val ids = viewModel.itemClickForDeletion(card)
        bookletCardAdapter.idSelected = ids
        bookletCardAdapter.notifyDataSetChanged()
    }

    private fun onFabAddCardClicked() {
        viewModel.startCardAddition()
    }

    private fun onInfoClicked(view: View) {
        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.menu_card, this.menu)
            setOnMenuItemClickListener(::onInfoMenuClick)
            show()
        }
    }

    private fun onInfoMenuClick(menuItem: MenuItem?): Boolean {
        return when (menuItem?.itemId) {
            R.id.menu_action_delete_card -> {
                viewModel.startDeleteCards()
                true
            }
            else -> false
        }
    }

    private fun showFragment() {
        makeFragmentTransaction { it.show(fg_add_card) }
        makeAnimationEnable(false)
    }

    private fun hideFragment() {
        makeFragmentTransaction { it.hide(fg_add_card) }
        makeAnimationEnable(true)
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

    private fun makeAnimationEnable(enable: Boolean) =
        fab_add_card.apply {
            clearAnimation()
            startAnimation(
                loadAnimation(this@BookletActivity,
                    if(enable) R.anim.pop_up else R.anim.pop_down)
            )
            isEnabled = enable
        }


    override fun onBackPressed() {
        if (fg_add_card.isVisible) {
            viewModel.stopCardEdition()
        } else {
            if (!viewModel.onBackPressed())
                super.onBackPressed()
        }
    }

    private fun FloatingActionButton.displayAsAdd() {
        setImageResource(R.drawable.ic_library_add_black_24dp)
        apply {
            clearAnimation()
            startAnimation(
                loadAnimation(this@BookletActivity,
                    R.anim.pop_up)
            )
        }
    }

    private fun FloatingActionButton.displayAsDelete() {
        setImageResource(android.R.drawable.ic_menu_delete)
        apply {
            clearAnimation()
            startAnimation(
                loadAnimation(this@BookletActivity,
                    R.anim.pop_up)
            )
        }
    }

    private fun RecyclerViewLibraryBookletsBinding.displayAsCancelButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            apply {
                it.clearAnimation()
                it.startAnimation(
                    loadAnimation(this@BookletActivity,
                        R.anim.pop_up)
                )
            }
        }

    private fun RecyclerViewLibraryBookletsBinding.displayAsNormalButton() =
        root.find<ImageView>(R.id.iv_info).let {
            it.setImageResource(R.drawable.ic_info_white_24dp)
            apply {
                it.clearAnimation()
                it.startAnimation(
                    loadAnimation(this@BookletActivity,
                        R.anim.pop_up)
                )
            }
        }
}

private fun RecyclerViewLibraryBookletsBinding.displayShortName() =
    root.find<TextView>(R.id.recycler_view_booklet_name).let {
        it.maxLines = 1
        it.ellipsize = TextUtils.TruncateAt.END
    }
