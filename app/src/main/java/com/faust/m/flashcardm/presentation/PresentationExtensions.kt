package com.faust.m.flashcardm.presentation

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText

fun View.setNoArgOnClickListener(listener: () -> Unit) {
    setOnClickListener {_ -> listener.invoke()}
}

fun AlertDialog.Builder.setNoArgPositiveButton(textId: Int, listener: () -> Unit): AlertDialog.Builder {
    return setPositiveButton(textId) { _, _ -> listener.invoke() }
}

fun TextInputEditText.setEditorActionListener(listener:
                                                  (textView: TextView, editorAction: EditorAction) -> Boolean) {
    setOnEditorActionListener { textView: TextView, actionId: Int, keyEvent: KeyEvent? ->
        listener.invoke(textView, EditorAction(actionId, keyEvent))
    }
}

fun Activity?.showKeyboard(view: View) = this?.run {
    if (view.requestFocus()) {
        (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
            ?.showSoftInput(view, 0)
    }
}

fun Activity?.hideKeyboard(view: View) = this?.run {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        ?.hideSoftInputFromWindow(view.windowToken, 0)
}

class EditorAction(private val actionId: Int, private val keyEvent: KeyEvent?) {

    private fun isActionDone() = EditorInfo.IME_ACTION_DONE == actionId

    private fun isEnterKeyDownEvent() =
        keyEvent?.run {
            KeyEvent.KEYCODE_ENTER == keyCode && KeyEvent.ACTION_DOWN == action
        } ?: false

    fun isDone(): Boolean = isActionDone() || isEnterKeyDownEvent()
}

fun <T> MutableLiveData<T>.notifyObserver() {
    postValue(this.value)
}

internal interface LiveDataObserver {

    fun <T> LiveData<T>.observeData(owner: LifecycleOwner, onChange: ((value: T) -> Unit)) {
        observe(owner, Observer<T> { t -> onChange.invoke(t) })
    }

    fun <Y, Z:Event<Y>> LiveData<Z>.observeEvent(owner: LifecycleOwner, onChange: ((value: Y) -> Unit)) {
        observe(owner, Observer<Z> { z -> z.getContentIfNotHandled()?.let { onChange.invoke(it) } })
    }
}

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     * This method should only be called if you intend to handle the content in case it hasn't been
     */
    fun getContentIfNotHandled(): T? {
        val result: T? = if (hasBeenHandled) null else content
        hasBeenHandled = true
        return result
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

/**
 * MutableLiveSet is a mutable list that can be used as a MutableLiveData. Observers will be called
 * on add or remove.
 * Only add(element) or remove(element) are surcharged. Any other add / remove method, like addAll,
 * will no trigger an observer call. Probably could be refactored, when I need it.
 */
class LiveMutableList<T> private constructor(private val inner: MutableList<T>) :
    LiveData<MutableList<T>>(inner), MutableList<T> by inner {

    constructor() : this(mutableListOf())

    override fun add(element: T): Boolean {
        val result = inner.add(element)
        if (result) postValue(value)
        return result
    }

    override fun remove(element: T): Boolean {
        val result = inner.remove(element)
        if (result) postValue(value)
        return result
    }
}

/**
 * MutableLiveSet is a mutable set that can be used as a liveData. Observers will be called
 * only when there is an addition to the set.
 * (Probably could be refactored to also trigger when an object is removed from the set, I'll see
 * to it when I actually need that functionality)
 */
class MutableLiveSet<T> private constructor(private val inner :HashSet<T>) :
    MutableLiveData<MutableSet<T>>(HashSet()), MutableSet<T> by inner {

    constructor(): this(HashSet())

    override fun add(element: T): Boolean {
        val result = inner.add(element)
        if (result)
            postValue(value)
        return result
    }
}

class MutableLiveEvent<T>: MutableLiveData<Event<T>>() {

    fun postEvent(event: T) {
        postValue(Event(event))
    }
}
