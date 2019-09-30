package com.faust.m.flashcardm.presentation

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

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