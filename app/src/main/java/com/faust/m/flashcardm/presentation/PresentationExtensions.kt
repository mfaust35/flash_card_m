package com.faust.m.flashcardm.presentation

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText

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
