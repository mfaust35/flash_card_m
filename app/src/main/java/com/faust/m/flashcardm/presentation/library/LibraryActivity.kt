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
const val TAG_FRAGMENT_REVIEW_AHEAD = "review_ahead"

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
        viewModel.eventManageCardsForBooklet.observeEvent(this, ::onEvenManageCardsForBooklet)
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

    private fun onBookletInfoClicked(booklet: BookletBannerData, view: View) {
        viewModel.selectedBooklet = booklet

        PopupMenu(this, view).apply {
            menuInflater.inflate(R.menu.menu_booklet, this.menu)
            // Disable the option to review ahead if we can't add more cards to review
            menu.findItem(R.id.menu_action_review_ahead).isEnabled = booklet.canReviewAhead()
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
            R.id.menu_action_review_ahead -> {
                showFragmentReviewAhead()
                true
            }
            R.id.menu_action_manage_cards -> {
                viewModel.manageCardsForCurrentBooklet()
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

    private fun showFragmentReviewAhead() =
        FragmentReviewAhead().show(supportFragmentManager, TAG_FRAGMENT_REVIEW_AHEAD)

    private fun onBookletClicked(booklet: BookletBannerData) {
        viewModel.reviewBooklet(booklet)
    }

    private fun onBookletsChanged(booklets: MutableList<BookletBannerData>) {
        bookletAdapter.replaceBooklets(booklets)
        showEmptyRecyclerView(booklets.isEmpty())
    }

    private fun showEmptyRecyclerView(show: Boolean) {
        iv_empty_recycler_view.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun onEvenManageCardsForBooklet(bookletId: Long) =
        startActivity<BookletActivity>(BOOKLET_ID to bookletId)

    private fun onEventReviewBooklet(booklet: BookletBannerData) {
        when {
            booklet.isEmpty() -> {
                // Empty booklet, user probably wants to add new cards,
                // show snackbar with action add
                activity_library_main_view.longSnackbar(
                    R.string.empty_booklet_for_review_message,
                    R.string.empty_booklet_for_review_action
                ) { startActivity<BookletActivity>(BOOKLET_ID to booklet.id)}
            }
            booklet.isCompletedForToday() -> {
                // Every card reviewed for today, user might want to review more,
                // show snackbar with action reviewAhead
                activity_library_main_view.longSnackbar(
                    R.string.completed_for_today_booklet_for_review_message,
                    R.string.completed_for_today_booklet_for_review_action
                ) { showFragmentReviewAhead() }
            }
            else -> // Start reviewing booklet
                startActivity<ReviewActivity>(BOOKLET_ID to booklet.id)
        }
    }

    private fun onFabAddBookletClicked() {
        viewModel.selectedBooklet = null
        showFragmentNameBooklet()
    }
}
