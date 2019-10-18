package com.faust.m.flashcardm.presentation.booklet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BookletViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import kotlinx.android.synthetic.main.activity_cards.*
import org.koin.android.ext.android.getKoin

class BookletActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletCardAdapter: BookletCardAdapter
    private lateinit var viewModel: CardsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        // Initialize adapter
        bookletCardAdapter = BookletCardAdapter()
        // Setup adapter in recyclerView
        recycler_view_cards.layoutManager = LinearLayoutManager(this)
        recycler_view_cards.adapter = bookletCardAdapter

        viewModel = getKoin().get<BookletViewModelFactory>().createViewModelFrom(this)
        viewModel.cards.observeData(this, ::onCardsChanged)
    }

    private fun onCardsChanged(cards: MutableList<BookletCard>) {
        bookletCardAdapter.replaceCards(cards)
    }
}