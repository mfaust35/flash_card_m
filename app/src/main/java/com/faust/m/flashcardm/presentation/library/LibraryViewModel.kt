package com.faust.m.flashcardm.presentation.library

import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.faust.m.core.domain.Booklet
import com.faust.m.core.usecase.BookletOutline
import com.faust.m.flashcardm.framework.UseCases
import com.faust.m.flashcardm.presentation.MutableLiveList
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.ONGOING
import com.faust.m.flashcardm.presentation.library.AddedBooklet.State.SUCCESS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.verbose
import org.jetbrains.anko.warn
import org.koin.core.KoinComponent
import org.koin.core.inject

class LibraryViewModel: ViewModel(), KoinComponent, AnkoLogger {

    private val useCases: UseCases by inject()


    private val booklets: MutableLiveBooklet by lazy {
        MutableLiveBooklet().apply {
            GlobalScope.launch {
                loadLibraryBooklets()
            }
        }
    }

    private val currentBooklet: MutableLiveData<LibraryBooklet?> = MutableLiveData()

    private val addBookletState: MutableLiveData<AddedBooklet?> = MutableLiveData()


    fun booklets(): LiveData<out List<LibraryBooklet>> = booklets

    fun currentBooklet(): LiveData<LibraryBooklet?> = currentBooklet

    fun addBookletState(): LiveData<AddedBooklet?> = addBookletState


    fun addBookletWithName(name: String) {
        addBookletState.postValue(AddedBooklet(state = ONGOING))
        GlobalScope.launch {
            useCases.addBooklet(Booklet(name)).let {
                verbose { "Booklet $it added" }
                booklets.add(LibraryBooklet(it, BookletOutline.EMPTY))
                addBookletState.postValue(AddedBooklet(it.id, SUCCESS))
            }
        }
    }

    fun currentBooklet(booklet: LibraryBooklet) {
        currentBooklet.postValue(booklet)
        addBookletState.postValue(null)
    }

    fun deleteCurrentBooklet() {
        currentBooklet.value?.let { libraryBooklet ->
            GlobalScope.launch {
                useCases.deleteBooklet(libraryBooklet.toBooklet()).let {result: Int ->
                    when(result) {
                        0 -> warn { "Booklet $libraryBooklet not deleted" }
                        else -> {
                            verbose { "Booklet $libraryBooklet deleted" }
                            booklets.remove(libraryBooklet)
                        }
                    }
                }
            }
        } ?: warn { "Could not find booklet to delete" }
    }

    private fun loadLibraryBooklets() = mutableListOf<LibraryBooklet>()
        .apply {
            val tBooklets: List<Booklet> = useCases.getBooklets()
            val tBookletsOutlines: LongSparseArray<BookletOutline> =
                useCases.getBookletsOutlines(tBooklets)

            for (tBooklet: Booklet in tBooklets) {
                val tOutline = tBookletsOutlines[tBooklet.id] ?: BookletOutline.EMPTY
                add(LibraryBooklet(tBooklet, tOutline))
            }
        }
        .also {
            booklets.postValue(it)
        }
}

/**
 * This class ensure that the list is sorted before being posted
 */
class MutableLiveBooklet: MutableLiveList<LibraryBooklet>() {

    override fun postValue(value: MutableList<LibraryBooklet>?) {
        value?.sortBy(LibraryBooklet::name)
        super.postValue(value)
    }
}

/**
 * Wrapper data class for easy access to the summary of cards linked to a booklet
 */
data class LibraryBooklet(val name: String,
                          val cardToReviewCount: Int,
                          val totalCardCount: Int,
                          val id: Long = 0) {

    constructor(booklet: Booklet, bookletOutline: BookletOutline): this(
        booklet.name,
        bookletOutline.cardToReviewCount,
        bookletOutline.cardTotalCount,
        booklet.id
    )

    fun toBooklet() = Booklet(name, id)
}

/**
 * Wrapper class for observing booklet adding procedure
 */
data class AddedBooklet(val id: Long? = null, val state: State) {

    enum class State { ONGOING, FAIL, SUCCESS }
}