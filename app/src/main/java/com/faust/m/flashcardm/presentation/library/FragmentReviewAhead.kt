package com.faust.m.flashcardm.presentation.library

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.EditorAction
import com.faust.m.flashcardm.presentation.library.FragmentReviewAhead.CountValidationState.*
import com.faust.m.flashcardm.presentation.setEditorActionListener
import com.faust.m.flashcardm.presentation.setNoArgPositiveButton
import com.google.android.material.textfield.TextInputEditText
import org.jetbrains.anko.find
import org.koin.android.ext.android.getKoin

const val DEFAULT_REVIEW_AHEAD_CARD_NUMBER = 20

class FragmentReviewAhead : DialogFragment() {

    private lateinit var editNumber: TextInputEditText
    private var _dialog: AlertDialog? = null
    private lateinit var viewModel: LibraryViewModel
    private var maxCardCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create dialog
        return activity?.let {
            val rootView =
                it.layoutInflater.inflate(R.layout.dialog_review_ahead, null)
            editNumber = rootView.find(R.id.et_number_card_to_review_ahead)
            editNumber.setEditorActionListener(::onEditorAction)
            // TODO: could probably refactor this as it is duplicated code from FragmentNameBooklet
            editNumber.addTextChangedListener(ValidationTextWatcher())

            // Build dialog
            _dialog = AlertDialog.Builder(it)
                .setTitle(R.string.title_dialog_review_ahead)
                .setView(rootView)
                .setNoArgPositiveButton(R.string.confirm_review_card, ::onPositiveButtonClicked)
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .create()

            // Use dialog window to focus on edit text and show soft input keyboard
            editNumber.requestFocus()
            _dialog?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            _dialog
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun onPositiveButtonClicked() {
        editNumber.text.toString().toIntOrNull()?.let {
            viewModel.addCardsToReviewAheadForCurrentBooklet(it)
        }
    }

    private fun onEditorAction(textView: TextView, editorAction: EditorAction): Boolean {
        textView.text.toString().let { count ->
            if (editorAction.isDone() && count.validationState() == VALID) {
                viewModel.addCardsToReviewAheadForCurrentBooklet(count.toInt())
                dismiss()
                return true
            }
            return false
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.selectedBooklet?.let { maxCardCount = it.totalCardCount - it.cardToReviewCount}
        maxCardCount
            .coerceAtMost(DEFAULT_REVIEW_AHEAD_CARD_NUMBER)
            .toString()
            .let { defaultCardCount ->
                editNumber.setText(defaultCardCount)
                editNumber.setSelection(0, defaultCardCount.length)
            }
    }


    private inner class ValidationTextWatcher: TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            val validationState = editable?.toString().validationState()
            // Enable button if text is valid
            _dialog?.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = (validationState == VALID)
            // Show error if text invalid, hide error if text valid
            editNumber.error = when (editable?.toString().validationState()) {
                VALID -> null
                TOO_LOW -> getString(R.string.error_min_card_for_review_ahead)
                TOO_HIGH -> resources.getQuantityString(
                        R.plurals.error_max_card_for_review_ahead, maxCardCount, maxCardCount)
            }
        }
    }

    private fun String?.validationState(): CountValidationState {
        return this?.toIntOrNull()?.let {
            when {
                it < 1 -> TOO_LOW
                it > maxCardCount -> TOO_HIGH
                else -> VALID
            }
        } ?: TOO_LOW
    }

    private enum class CountValidationState { VALID, TOO_HIGH, TOO_LOW }
}
