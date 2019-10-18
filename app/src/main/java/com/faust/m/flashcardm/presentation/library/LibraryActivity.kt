package com.faust.m.flashcardm.presentation.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BOOKLET_ID
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.about.AboutActivity
import com.faust.m.flashcardm.presentation.booklet.BookletActivity
import com.faust.m.flashcardm.presentation.review.ReviewActivity
import com.faust.m.flashcardm.presentation.setNoArgOnClickListener
import kotlinx.android.synthetic.main.activity_library.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.startActivity
import org.koin.android.ext.android.getKoin

const val TAG_FRAGMENT_ADD_BOOKLET = "add_booklet"

class LibraryActivity: AppCompatActivity(), LiveDataObserver {

    private lateinit var bookletAdapter: BookletAdapter
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize adapter
        bookletAdapter = BookletAdapter(
            onItemClick = ::onBookletClicked,
            onInfoClick = ::onBookletInfoClicked
        )
        // Setup adapter in recyclerView
        recycler_view_booklet.layoutManager = LinearLayoutManager(this)
        recycler_view_booklet.adapter = bookletAdapter

        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)
        viewModel.booklets.observeData(this, ::onBookletsChanged)
        viewModel.stateAddBooklet.observeData(this, ::onStateAddedBookletChanged)
        viewModel.eventAddCardToBooklet.observeEvent(this, ::onEvenAddCardToBooklet)
        viewModel.eventReviewBooklet.observeEvent(this, ::onEventReviewBooklet)

        fab_add_booklet.setNoArgOnClickListener(::onFabAddBookletClicked)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_library, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        return when (item.itemId) {
            R.id.menu_action_show_about -> {
                startActivity<AboutActivity>()
                true
            }
            else -> false
        }
    }

    private fun onBookletInfoClicked(booklet: LibraryBooklet, view: View) {
        viewModel.selectedBooklet = booklet

        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.menu_booklet, this.menu)
            setOnMenuItemClickListener(::onInfoMenuClick)
            show()
        }
    }

    private fun onInfoMenuClick(menuItem: MenuItem?): Boolean {
        return when (menuItem?.itemId) {
            R.id.menu_action_delete -> {
                viewModel.deleteCurrentBooklet()
                true
            }
            R.id.menu_action_add_card -> {
                viewModel.addCardsToCurrentBooklet()
                true
            }
            R.id.menu_action_rename_booklet -> {
                showFragmentNameBooklet()
                true
            }
            else -> false
        }
    }

    private fun showFragmentNameBooklet() =
        FragmentNameBooklet().show(supportFragmentManager, TAG_FRAGMENT_ADD_BOOKLET)


    private fun onBookletClicked(booklet: LibraryBooklet) {
        viewModel.reviewBooklet(booklet)
    }

    private fun onBookletsChanged(booklets: MutableList<LibraryBooklet>) {
        bookletAdapter.replaceBooklets(booklets)
        when {
            booklets.isEmpty() -> tv_empty_recycler_view.visibility = View.VISIBLE
            else -> tv_empty_recycler_view.visibility = View.GONE
        }
    }

    private fun onStateAddedBookletChanged(addedBooklet: AddedBooklet?) {
        bookletAdapter.setSelected(addedBooklet?.id)
    }

    private fun onEvenAddCardToBooklet(bookletId: Long) {
        startActivity<BookletActivity>(
            BOOKLET_ID to bookletId
        )
    }

    private fun onEventReviewBooklet(bookletId: Long) {
        when(bookletId) {
            LibraryViewModel.EMPTY_BOOKLET ->
                activity_library_main_view.longSnackbar(R.string.empty_booklet_for_review)
            else -> startActivity<ReviewActivity>(
                BOOKLET_ID to bookletId
            )
        }
    }

    private fun onFabAddBookletClicked() {
        viewModel.selectedBooklet = null
        showFragmentNameBooklet()
    }
}
