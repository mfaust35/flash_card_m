package com.faust.m.flashcardm.presentation.library

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.EditorAction
import com.faust.m.flashcardm.presentation.provideViewModel
import com.faust.m.flashcardm.presentation.setEditorActionListener
import com.faust.m.flashcardm.presentation.setPositiveButton
import com.google.android.material.textfield.TextInputEditText

class FragmentAddBooklet : DialogFragment() {

    private lateinit var editName: TextInputEditText
    private lateinit var viewModel: LibraryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init viewModel
        viewModel = provideViewModel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create dialog
        return activity?.let {
            val rootView =
                requireActivity().layoutInflater.inflate(R.layout.dialog_add_booklet, null)
            editName = rootView.findViewById(R.id.et_booklet_name)
            editName.setEditorActionListener(::onEditorAction)

            AlertDialog.Builder(it)
                .setTitle(R.string.title_dialog_add_booklet)
                .setView(rootView)
                .setPositiveButton(R.string.confirm_new_booklet, ::onPositiveButtonClicked)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun onPositiveButtonClicked() {
        addBookletWithName(editName.text.toString())
    }

    private fun addBookletWithName(name: String) {
        viewModel.addBookletWithName(name)
    }

    private fun onEditorAction(textView: TextView, editorAction: EditorAction): Boolean {
        return when {
            editorAction.isDone() -> {
                addBookletWithName(textView.text.toString())
                dismiss()
                true
            }
            else -> false
        }
    }
}