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
import com.faust.m.flashcardm.presentation.setEditorActionListener
import com.faust.m.flashcardm.presentation.setPositiveButton
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.getKoin

class FragmentNameBooklet : DialogFragment() {

    private lateinit var editName: TextInputEditText
    private var _dialog: AlertDialog? = null
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getKoin().get<BaseViewModelFactory>().createViewModelFrom(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create dialog
        return activity?.let {
            val rootView =
                requireActivity().layoutInflater.inflate(R.layout.dialog_add_booklet, null)
            editName = rootView.findViewById(R.id.et_booklet_name)
            editName.setEditorActionListener(::onEditorAction)
            editName.addTextChangedListener(ValidationTextWatcher())

            val titleStringId =
                if (viewModel.selectedBooklet == null) R.string.title_dialog_add_booklet
                else R.string.title_dialog_rename_booklet
            // Build dialog
            _dialog = AlertDialog.Builder(it)
                .setTitle(titleStringId)
                .setView(rootView)
                .setPositiveButton(R.string.confirm_new_booklet, ::onPositiveButtonClicked)
                .create()

            // Use dialog window to focus on edit text and show soft input keyboard
            editName.requestFocus()
            _dialog?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)

            _dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun onPositiveButtonClicked() {
        nameBooklet(editName.text.toString())
    }

    private fun nameBooklet(name: String) {
        viewModel.nameBooklet(name)
    }

    private fun onEditorAction(textView: TextView, editorAction: EditorAction): Boolean {
        return when {
            editorAction.isDone() -> {
                nameBooklet(textView.text.toString())
                dismiss()
                true
            }
            else -> false
        }
    }

    override fun onStart() {
        super.onStart()

        val text = viewModel.selectedBooklet?.name ?: ""
        editName.setText(text)
        editName.setSelection(text.length)
    }

    inner class ValidationTextWatcher: TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            _dialog?.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled =
                editable?.toString()?.isNotBlank() ?: true
        }
    }
}