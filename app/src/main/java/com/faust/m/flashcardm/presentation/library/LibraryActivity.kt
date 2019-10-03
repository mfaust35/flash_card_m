package com.faust.m.flashcardm.presentation.library

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.add_card.AddCardActivity
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.provideViewModel
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.activity_library.*

const val TAG_FRAGMENT_ADD_BOOKLET = "add_booklet"

class LibraryActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletAdapter: BookletAdapter
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // Initialize adapter
        bookletAdapter = BookletAdapter(this,
            onItemClick = ::onBookletClicked,
            onItemLongClick = ::onBookletLongClicked)
        // Setup adapter in recyclerView
        recycler_view_booklet.layoutManager = LinearLayoutManager(this)
        recycler_view_booklet.adapter = bookletAdapter

        // Init viewModel
        viewModel = provideViewModel()
        // Setup observe data in viewModel
        viewModel.getAllBooklets().observe(this, ::onBookletsChanged)

        // Setup click listeners
        fab_add_booklet.setOnClickListener(::onFabAddBookletClicked)
    }

    private fun onFabAddBookletClicked() {
        FragmentAddBooklet().show(supportFragmentManager, TAG_FRAGMENT_ADD_BOOKLET)
    }

    private fun onBookletClicked(booklet: Booklet) {
        Intent(this, AddCardActivity::class.java)
            .apply {
                putExtra(BOOKLET_ID, booklet.id)
            }
            .also {
                startActivity(it)
            }
    }

    private fun onBookletLongClicked(booklet: Booklet): Boolean {
        viewModel.deleteBooklet(booklet)
        return true
    }

    private fun onBookletsChanged(booklets: List<Booklet>) =
        bookletAdapter.replaceBooklets(booklets)
}
