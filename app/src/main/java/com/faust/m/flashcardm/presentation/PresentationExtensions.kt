package com.faust.m.flashcardm.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
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