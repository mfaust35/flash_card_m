package com.faust.m.flashcardm.presentation.review

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.faust.m.flashcardm.R
import org.jetbrains.anko.find

@BindingAdapter("card_text")
fun setCardText(view: ReviewCardView, value: String) {
    view.find<TextView>(R.id.tv_review_card_text).text = value
}

/**
 * This view set the default look for a card to review.
 * Not a big class now, but it should fluff up once I add pictures
 */
class ReviewCardView @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0)
    : ScrollView(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_review_card, this, true)
        setBackgroundResource(R.drawable.card_background)
        isFillViewport = true
        elevation = resources.getDimension(R.dimen.small_margin)
        translationZ = resources.getDimension(R.dimen.small_margin)
    }
}

