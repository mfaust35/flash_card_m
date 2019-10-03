package com.faust.m.flashcardm.presentation

import android.app.Activity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.material.textfield.TextInputEditText
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

fun TextInputEditText.setEditorActionListener(listener:
                                                  (textView: TextView, editorAction: EditorAction) -> Boolean) {
    setOnEditorActionListener { textView: TextView, actionId: Int, keyEvent: KeyEvent? ->
        listener.invoke(textView, EditorAction(actionId, keyEvent))
    }
}

class EditorAction(private val actionId: Int, private val keyEvent: KeyEvent?) {

    private fun isActionDone() = EditorInfo.IME_ACTION_DONE == actionId

    private fun isEnterKeyDownEvent() =
        keyEvent?.run {
            KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == action
        } ?: false

    fun isDone(): Boolean = isActionDone() || isEnterKeyDownEvent()
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

fun <T> MutableLiveData<T>.notifyObserver() {
    postValue(this.value)
}

internal interface LiveDataObserver {

    fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChange: ((value: T) -> Unit)) {
        observe(owner, Observer<T> { t -> onChange.invoke(t) })
    }

    fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChange: () -> Unit) {
        observe(owner, Observer<T> { onChange.invoke() })
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