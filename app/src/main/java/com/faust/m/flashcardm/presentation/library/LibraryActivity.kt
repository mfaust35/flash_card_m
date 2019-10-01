package com.faust.m.flashcardm.presentation.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.ViewModelFactory
import kotlinx.android.synthetic.main.activity_library.*
import org.jetbrains.anko.toast
import org.koin.android.ext.android.getKoin

class LibraryActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletAdapter: BookletAdapter
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // Initialize adapter
        bookletAdapter = BookletAdapter(onItemClick = ::onBookletClicked)
        // Setup adapter in recyclerView
        recyclerViewBooklet.layoutManager = LinearLayoutManager(this)
        recyclerViewBooklet.adapter = bookletAdapter

        // Initialize viewModel
        getKoin().get<ViewModelFactory>().let {
            viewModel = ViewModelProvider(this, it).get(LibraryViewModel::class.java)
        }
        // Setup observe data in viewModel
        viewModel.getAllBooklets().observe(this, ::onBookletsChanged)
    }

    private fun onBookletClicked(booklet: Booklet) {
        toast("On booklet clicked")
    }

    private fun onBookletsChanged(booklets: List<Booklet>) =
        bookletAdapter.replaceBooklets(booklets)
}