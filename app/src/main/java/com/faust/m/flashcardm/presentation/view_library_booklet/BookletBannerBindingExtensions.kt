package com.faust.m.flashcardm.presentation.view_library_booklet

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.ViewBookletBannerBinding
import org.jetbrains.anko.find

internal fun ViewBookletBannerBinding.displayShortName() {
    root.find<TextView>(R.id.recycler_view_booklet_name).let {
        it.maxLines = 1
        it.ellipsize = TextUtils.TruncateAt.END
    }
}

internal fun ViewBookletBannerBinding.displayCardCount() {
    root.find<View>(R.id.view_advancement).visibility = View.VISIBLE
}