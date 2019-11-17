package com.faust.m.flashcardm.presentation.view_library_booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.core.usecase.booklet.BookletOutline
import com.faust.m.flashcardm.framework.BookletUseCases
import com.faust.m.flashcardm.framework.FlashViewModel
import com.faust.m.flashcardm.presentation.library.LibraryBooklet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * This viewModel extension is intended for use with a view RecyclerViewLibraryBookletsBinding.
 * To see an example of usage, look into the booklet package
 * fragment_layout should include a LinearLayout insert_point to include the libraryBookletBinding
 * ActivityViewModel should delegate the booklet to DelegateEditCard, like:
    class ActivityViewModel @JvmOverloads constructor(
        private val var1: Long,
        private val delegateEditCard: DelegateEditCard = DelegateEditCard(bookletId)
    ): ViewModel(),
        OtherInterface,
        ViewModelEditCard by delegateEditCard { ... }
 * Fragment should bind the libraryBookletBinding during onViewCreated()
 * Fragment should observe booklet from viewModel
 * Activity should loadData from viewModel during onCreate()
 */
interface ViewModelLibraryBooklet {

    val booklet: LiveData<LibraryBooklet>

    fun loadData()
    fun postBookletUpdate()
}

class DelegateLibraryBooklet(private val bookletId: Long): ViewModelLibraryBooklet, KoinComponent {

    private val bookletUseCases: BookletUseCases by inject()
    private val flashViewModel: FlashViewModel by inject()


    // Booklet information used to display the top banner
    private val _booklet: MutableLiveData<LibraryBooklet> = MutableLiveData()
    override val booklet: LiveData<LibraryBooklet> = _booklet


    override fun loadData() {
        GlobalScope.launch {
            postBookletUpdate()
        }
    }


    override fun postBookletUpdate() {
        flashViewModel.bookletsStateChanged()
        val libraryBooklet = bookletUseCases.getBooklet(bookletId)?.let { tBooklet ->
            val tOutlines =
                bookletUseCases.getBookletsOutlines(listOf(tBooklet))
            val tOutline = tOutlines[tBooklet.id] ?: BookletOutline.EMPTY
            LibraryBooklet(tBooklet, tOutline)

        } ?: LibraryBooklet.ERROR
        _booklet.postValue(libraryBooklet)
    }
}