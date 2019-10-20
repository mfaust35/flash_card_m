package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.CLOSED
import com.faust.m.flashcardm.presentation.booklet.CardEditionState.OPEN
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import kotlinx.android.synthetic.main.activity_booklet.*
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin


class BookletActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var viewModel: BookletViewModel

    private lateinit var libraryBookletBinding: RecyclerViewLibraryBookletsBinding


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
        viewModel.cards.observeData(this, ::onCardsChanged)
        viewModel.cardEditionState.observeData(this, ::onCardEditionState)

        fab_add_card.setNoArgOnClickListener(::onFabAddCardClicked)
    }

    private fun onBookletChanged(booklet: LibraryBooklet) {
        libraryBookletBinding.booklet = booklet
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
    }

    private fun onCardEditionState(cardEditionState: CardEditionState) =
        when(cardEditionState) {
            OPEN -> showFragment()
            CLOSED -> hideFragment()
        }

    private fun onFabAddCardClicked() {
        viewModel.startCardEdition()
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


    override fun onBackPressed() =
        when(fg_add_card.isVisible) {
            true -> viewModel.stopCardEdition()
            else -> super.onBackPressed()
        }
}

private fun RecyclerViewLibraryBookletsBinding.displayShortName() =
    root.find<TextView>(R.id.recycler_view_booklet_name).let {
        it.maxLines = 1
        it.ellipsize = TextUtils.TruncateAt.END
    }
