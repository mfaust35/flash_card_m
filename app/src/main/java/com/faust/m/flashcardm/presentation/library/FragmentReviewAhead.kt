package com.faust.m.flashcardm.presentation.library

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.EditTextDialogFragment
import com.faust.m.flashcardm.presentation.library.FragmentReviewAhead.CountValidationState.*
import org.koin.android.ext.android.getKoin

class FragmentReviewAhead : EditTextDialogFragment() {

    private lateinit var viewModel: LibraryViewModel


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)

        val customView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_review_ahead, null)

        val builder =
            AlertDialog.Builder(requireContext()).setTitle(R.string.title_dialog_review_ahead)

        return createDialog(R.string.confirm_review_card,
            R.id.et_number_card_to_review_ahead, customView, builder)
    }

    override fun onResume() {
        super.onResume()
        // Display default value in editText
        viewModel.defaultCardCountToReviewAheadForCurrentBooklet().toString()
            .let { defaultCardCount ->
                editText.setText(defaultCardCount)
                editText.setSelection(0, defaultCardCount.length)
            }
    }

    override fun onConfirm(value: String): Boolean =
        // Double check that value is valid && ask viewModel to add cards for review
        value.validateCount() == VALID &&
        value.toIntOrNull()?.let { count ->
            viewModel.addCardsToReviewAheadForCurrentBooklet(count)
            true
        } ?: false

    /**
     * Depending on text value (valid or not), this method will:
     * - enable/disable positive button
     * - show/hide error on textView
     */
    override fun onDisplayValidationState(value: String) {
        value.validateCount().let { validationState ->
            // Enable button only if text is valid
            enablePositiveButton(validationState == VALID)
            // Show error if text invalid, hide error if text valid
            editText.error = when (validationState) {
                VALID -> null
                TOO_LOW -> getString(R.string.error_min_card_for_review_ahead)
                TOO_HIGH -> {
                    val max = viewModel.maxCardCountToReviewAheadForCurrentBooklet()
                    resources.getQuantityString(
                        R.plurals.error_max_card_for_review_ahead, max, max)
                }
            }
        }
    }

    private fun String?.validateCount(): CountValidationState {
        return this?.toIntOrNull()?.let {
            when {
                it < 1 -> TOO_LOW
                it > viewModel.maxCardCountToReviewAheadForCurrentBooklet() -> TOO_HIGH
                else -> VALID
            }
        } ?: TOO_LOW
    }

    private enum class CountValidationState { VALID, TOO_HIGH, TOO_LOW }
}
