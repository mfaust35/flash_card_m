package com.faust.m.flashcardm.presentation.library

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.BaseViewModelFactory
import com.faust.m.flashcardm.presentation.EditTextDialogFragment
import com.faust.m.flashcardm.presentation.library.FragmentNameBooklet.NameValidationState.TOO_SHORT
import com.faust.m.flashcardm.presentation.library.FragmentNameBooklet.NameValidationState.VALID
import org.koin.android.ext.android.getKoin

class FragmentNameBooklet : EditTextDialogFragment() {

    private lateinit var viewModel: LibraryViewModel


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)

        val customView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_add_booklet, null)

        var titleStringId = R.string.title_dialog_add_booklet
        var positiveButtonStringId = R.string.confirm_new_booklet
        if (viewModel.selectedBooklet != null) {
            titleStringId = R.string.title_dialog_rename_booklet
            positiveButtonStringId = R.string.confirm_rename_booklet
        }

        val builder =
            AlertDialog.Builder(requireContext())
                .setTitle(titleStringId)

        return createDialog(positiveButtonStringId,
            R.id.et_booklet_name, customView, builder)
    }

    override fun onResume() {
        super.onResume()
        // Display the name of selected booklet if any
        viewModel.selectedBooklet?.name?.let { text ->
            editText.setText(text)
            editText.setSelection(text.length)
        }
    }

    override fun onConfirm(value: String): Boolean {
        // Check if value is valid
        if (value.validateName() != VALID) return false
        // Name booklet
        viewModel.nameBooklet(value)
        return true
    }

    override fun onDisplayValidationState(value: String) {
        value.validateName().let { validationState ->
            enablePositiveButton(validationState == VALID)
            editText.error = when (validationState) {
                VALID -> null
                TOO_SHORT -> getString(R.string.error_booklet_name_must_not_be_null)
            }
        }
    }

    private fun String?.validateName(): NameValidationState =
        when {
            this == null -> TOO_SHORT
            this.isNotBlank() -> VALID
            else -> TOO_SHORT
        }

    enum class NameValidationState { VALID, TOO_SHORT }
}
