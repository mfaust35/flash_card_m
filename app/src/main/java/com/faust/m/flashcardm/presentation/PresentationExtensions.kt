package com.faust.m.flashcardm.presentation

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import org.koin.android.ext.android.getKoin

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.setOnClickListener(listener: () -> Unit) {
    setOnClickListener {_ -> listener.invoke()}
}

fun AlertDialog.Builder.setPositiveButton(textId: Int, listener: () -> Unit): AlertDialog.Builder {
    return setPositiveButton(textId) { _, _ -> listener.invoke() }
}

open class MutableLiveList<T>: MutableLiveData<MutableList<T>>() {

    fun add(value: T) {
        this.value?.add(value)
        postValue(this.value)
    }

    fun remove(value: T) {
        this.value?.remove(value)
        postValue(this.value)
    }
}

internal interface LiveDataObserver {

    class GenericObserver<T>(private val onChange: ((value: T) -> Unit)): Observer<T> {
        override fun onChanged(t: T) {
            onChange.invoke(t)
        }
    }

    fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChange: ((value: T) -> Unit)) {
        observe(owner, GenericObserver(onChange))
    }
}

inline fun <reified T: ViewModel> Activity.provideViewModel(): T {
    return getKoin().get<ViewModelFactory>().let {
        ViewModelProvider(this as ViewModelStoreOwner, it).get(T::class.java)
    }
}

inline fun <reified T: ViewModel> Fragment.provideViewModel(): T {
    return getKoin().get<ViewModelFactory>().let {
        ViewModelProvider(this.activity as ViewModelStoreOwner, it).get(T::class.java)
    }
}