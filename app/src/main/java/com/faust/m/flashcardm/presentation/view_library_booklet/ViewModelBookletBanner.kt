package com.faust.m.flashcardm.presentation.view_library_booklet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.faust.m.flashcardm.core.usecase.BookletOutline
import com.faust.m.flashcardm.core.usecase.BookletUseCases
import com.faust.m.flashcardm.presentation.library.BookletBannerData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * This viewModel extension is intended for use with a view ViewBookletBannerBinding.
 * To see an example of usage, look into the booklet package
 * fragment_layout should include a LinearLayout insert_point to include the bookletBannerBinding
 * ActivityViewModel should delegate the booklet to DelegateEditCard, like:
    class ActivityViewModel @JvmOverloads constructor(
        private val var1: Long,
        private val delegateEditCard: DelegateEditCard = DelegateEditCard(bookletId)
    ): ViewModel(),
        OtherInterface,
        ViewModelEditCard by delegateEditCard { ... }
 * Fragment should bind the bookletBannerBinding during onViewCreated()
 * Fragment should observe booklet from viewModel
 * Activity should loadData from viewModel during onCreate()
 */
interface ViewModelBookletBanner {

    val booklet: LiveData<BookletBannerData>

    fun loadData()
    fun postBookletUpdate()
}

class DelegateBookletBanner(private val bookletId: Long): ViewModelBookletBanner, KoinComponent {

    private val bookletUseCases: BookletUseCases by inject()


    // Booklet information used to display the top banner
    private val _booklet: MutableLiveData<BookletBannerData> = MutableLiveData()
    override val booklet: LiveData<BookletBannerData> = _booklet


    override fun loadData() {
        GlobalScope.launch {
            postBookletUpdate()
        }
    }


    override fun postBookletUpdate() {
        val bookletBannerData = bookletUseCases.getBooklet(bookletId)?.let { tBooklet ->
            val tOutlines =
                bookletUseCases.getBookletsOutlines(listOf(tBooklet))
            val tOutline = tOutlines[tBooklet.id] ?: BookletOutline.EMPTY
            BookletBannerData(tBooklet, tOutline)

        } ?: BookletBannerData.ERROR
        _booklet.postValue(bookletBannerData)
    }
}