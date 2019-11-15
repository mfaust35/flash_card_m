package com.faust.m.flashcardm.presentation

import android.app.Dialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import org.jetbrains.anko.find

/**
 * Extend this class if you want to display a dialog fragment with 3 functionality:
 * Show edit text for user to input a value
 * Disable positive button when value is invalid
 * Display and error on EditText when value is invalid
 */
abstract class EditTextDialogFragment: DialogFragment() {

    protected lateinit var editText: TextInputEditText
    private var _dialog: AlertDialog? = null


    protected fun createDialog(confirmStringRes: Int,
                               editTextIdRes: Int,
                               customView: View,
                               builder: AlertDialog.Builder): AlertDialog {
        editText = customView.find(editTextIdRes)
        editText.setEditorActionListener(::onEditorAction)
        editText.addTextChangedListener(ValidationTextWatcher())

        // Complete dialog builder by adding button cancel and confirm
        val dialog = builder
            .setView(customView)
            .setNoArgPositiveButton(confirmStringRes, ::onPositiveButtonClicked)
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()
        _dialog = dialog

        // Use dialog window to focus on edit text and show soft input keyboard
        editText.requestFocus()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }

    private fun onEditorAction(textView: TextView, editorAction: EditorAction): Boolean {
        if (!editorAction.isDone()) return false
        if (onConfirm(textView.text.toString())) {
            dismiss()
            return true
        }
        return false
    }

    protected abstract fun onConfirm(value: String): Boolean

    private inner class ValidationTextWatcher: TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            editable?.toString()?.let { onDisplayValidationState(it) }
        }
    }

    protected abstract fun onDisplayValidationState(value: String)

    private fun onPositiveButtonClicked() {
        onConfirm(editText.text.toString())
    }


    protected fun enablePositiveButton(enable: Boolean) {
        _dialog?.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = enable
    }
}