package com.faust.m.flashcardm.presentation.library

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.LiveDataObserver
import com.faust.m.flashcardm.presentation.add_card.AddCardActivity
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID
import com.faust.m.flashcardm.presentation.review.ReviewActivity
import com.faust.m.flashcardm.presentation.setOnClickListener
import kotlinx.android.synthetic.main.activity_library.*
import org.koin.android.ext.android.getKoin

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
        registerForContextMenu(recycler_view_booklet)

        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)
        viewModel.booklets.observeData(this, ::onBookletsChanged)
        viewModel.stateAddBooklet.observeData(this, ::onStateAddedBookletChanged)
        viewModel.eventAddCardToBooklet.observeEvent(this, ::onEvenAddCardToBooklet)

        fab_add_booklet.setOnClickListener(::onFabAddBookletClicked)
    }

    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?,
                                     menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_library, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onContextItemSelected(item)
        return when (item.itemId) {
            R.id.menu_action_delete -> {
                viewModel.deleteCurrentBooklet()
                true
            }
            R.id.menu_action_add_card -> {
                viewModel.addCardsToCurrentBooklet()
                true
            }
            else -> false
        }
    }


    private fun onBookletClicked(booklet: LibraryBooklet) {
        Intent(this, ReviewActivity::class.java)
            .apply { putExtra(BOOKLET_ID, booklet.id) }
            .also { startActivity(it) }
    }

    private fun onBookletLongClicked(booklet: LibraryBooklet): Boolean {
        viewModel.selectedBooklet = booklet
        return false
    }

    private fun onBookletsChanged(booklets: MutableList<LibraryBooklet>) {
        bookletAdapter.replaceBooklets(booklets)
    }

    private fun onStateAddedBookletChanged(addedBooklet: AddedBooklet?) {
        bookletAdapter.setSelected(addedBooklet?.id)
    }

    private fun onEvenAddCardToBooklet(bookletId: Long) {
        Intent(this, AddCardActivity::class.java).run {
            putExtra(BOOKLET_ID, bookletId)
            startActivity(this)
        }
    }

    private fun onFabAddBookletClicked() {
        FragmentAddBooklet().show(supportFragmentManager, TAG_FRAGMENT_ADD_BOOKLET)
    }
}
