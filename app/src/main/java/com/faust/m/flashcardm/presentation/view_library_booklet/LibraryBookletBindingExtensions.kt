package com.faust.m.flashcardm.presentation.view_library_booklet

import android.text.TextUtils
import android.widget.TextView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import org.jetbrains.anko.find

internal fun RecyclerViewLibraryBookletsBinding.displayShortName() =
    root.find<TextView>(R.id.recycler_view_booklet_name).let {
        it.maxLines = 1
        it.ellipsize = TextUtils.TruncateAt.END
    }