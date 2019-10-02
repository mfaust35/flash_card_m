package com.faust.m.flashcardm.presentation.library

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.provideViewModel
import com.faust.m.flashcardm.presentation.setPositiveButton

class FragmentAddBooklet : DialogFragment() {

    private lateinit var editName: EditText
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

            AlertDialog.Builder(it)
                .setView(rootView)
                .setMessage(R.string.enter_new_booklet_name)
                .setPositiveButton(R.string.confirm_new_booklet, ::onConfirmNewBooklet)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun onConfirmNewBooklet() {
        viewModel.addBookletWithName(editName.text.toString())
    }
}