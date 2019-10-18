package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import android.view.animation.AnimationUtils.loadAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import kotlinx.android.synthetic.main.activity_booklet.*
import org.koin.android.ext.android.getKoin


class BookletActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var viewModel: BookletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booklet)

        // Initialize adapter
        bookletCardAdapter = BookletCardAdapter()
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(this)
        recycler_view_cards.adapter = bookletCardAdapter

        // Hide fragment add card
        supportFragmentManager.beginTransaction().let { transaction ->
            transaction.hide(fg_add_card)
            transaction.commit()
        }

        viewModel = getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.cards.observeData(this, ::onCardsChanged)
        viewModel.eventStopAdd.observeEvent(this, ::onEventStopAdd)

        fab_add_card.setNoArgOnClickListener(::onFabAddCardClicked)
    }

    private fun onEventStopAdd(boolean: Boolean) {
        hideFragment()
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
    }

    private fun onFabAddCardClicked() {
        showFragment()
    }

    private fun showFragment() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        ft.show(fg_add_card)
        ft.commit()

        fab_add_card.clearAnimation()
        val animation = loadAnimation(this, R.anim.pop_down)
        fab_add_card.startAnimation(animation)
        fab_add_card.isEnabled = false

        viewModel.setupCard()
    }

    private fun hideFragment() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()

        ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        ft.hide(fg_add_card)
        ft.commit()

        fab_add_card.clearAnimation()
        val animation = loadAnimation(this, R.anim.pop_up)
        fab_add_card.startAnimation(animation)
        fab_add_card.isEnabled = true
    }

    override fun onBackPressed() {
        if (fg_add_card.isVisible)
            hideFragment()
        else
            super.onBackPressed()
    }
}