package com.faust.m.flashcardm.presentation

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.faust.m.flashcardm.presentation.add_card.BOOKLET_ID

class BaseViewModelFactory: ViewModelProvider.Factory {

    inline fun <reified T: ViewModel> createViewModelFrom(activity: Activity): T {
        return ViewModelProvider(activity as ViewModelStoreOwner, this).get(T::class.java)
    }

    inline fun <reified T: ViewModel> createViewModelFrom(fragment: Fragment): T {
        fragment.activity?.let {
            return createViewModelFrom(it)
        } ?: throw IllegalStateException("Fragment ${javaClass.name} should have an activity")
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        try {
            return modelClass.newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}

class BookletViewModelFactory: ViewModelProvider.Factory {

    var bookletId: Long = -1

    inline fun <reified T: ViewModel> createViewModelFrom(activity: Activity): T {
        activity.intent.let {
            when {
                it.hasExtra(BOOKLET_ID) -> {
                    bookletId = it.getLongExtra(BOOKLET_ID, -1)
                    return ViewModelProvider(activity as ViewModelStoreOwner, this)
                        .get(T::class.java)
                }
                else ->
                    throw IllegalStateException("Activity must have a $BOOKLET_ID Long Extra")}
        }
    }

    inline fun <reified T: ViewModel> createViewModelFrom(fragment: Fragment): T {
        fragment.activity?.let {
            return createViewModelFrom(it)
        } ?: throw IllegalStateException("Fragment ${javaClass.name} should have an activity")
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        try {
            return modelClass.getConstructor(Long::class.java).newInstance(bookletId)
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}
