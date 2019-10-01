package com.faust.m.flashcardm.presentation.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.provideViewModel
import kotlinx.android.synthetic.main.activity_library.*
import org.jetbrains.anko.toast

class LibraryActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletAdapter: BookletAdapter
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // Initialize adapter
        bookletAdapter = BookletAdapter(this, onItemClick = ::onBookletClicked)
        // Setup adapter in recyclerView
        recycler_view_booklet.layoutManager = LinearLayoutManager(this)
        recycler_view_booklet.adapter = bookletAdapter

        // Initialize viewModel
        viewModel = provideViewModel()
        // Setup observe data in viewModel
        viewModel.getAllBooklets().observe(this, ::onBookletsChanged)

        getString(R.string.action_settings)
    }

    private fun onBookletClicked(booklet: Booklet) {
        toast("On booklet clicked")
    }

    private fun onBookletsChanged(booklets: List<Booklet>) =
        bookletAdapter.replaceBooklets(booklets)
}
