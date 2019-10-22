package com.faust.m.flashcardm.presentation.booklet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewBookletCardsBinding
import org.jetbrains.anko.find

class BookletCardAdapter(cards: Collection<BookletCard>? = null,
                         var onItemClick: ((value: BookletCard) -> Unit)? = null):
        RecyclerView.Adapter<BookletCardAdapter.Holder>() {

    private val cards: MutableList<BookletCard> =
        if (cards.isNullOrEmpty()) mutableListOf() else ArrayList(cards)

    var showRemoveMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        with(LayoutInflater.from(parent.context)) {
            Holder(RecyclerViewBookletCardsBinding.inflate(this, parent, false))
        }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindCard(cards[position])
    }

    fun switchMode(isRemoveMode: Boolean, newOnItemClick: (value: BookletCard) -> Unit) {
        showRemoveMode = isRemoveMode
        onItemClick = newOnItemClick
    }

    fun replaceCards(newCards: List<BookletCard>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    fun notifyItemDeleted(positions: Set<Int>, newCards: List<BookletCard>) {
        val previousSize = cards.size

        cards.clear()
        cards.addAll(newCards)

        // Animated remove card
        var offset = 0
        (0 until previousSize).forEach {
            if (positions.contains(it)) {
                notifyItemRemoved(it - offset++)
            }
        }

        // Update the rest
        notifyItemRangeChanged(0, cards.size)
    }

    inner class Holder(private val binding: RecyclerViewBookletCardsBinding):
            RecyclerView.ViewHolder(binding.root) {

        fun bindCard(card: BookletCard) {
            binding.card = card
            binding.executePendingBindings()

            itemView.find<CheckBox>(R.id.cb_selected).let {
                when {
                    showRemoveMode -> {
                        it.visibility = View.VISIBLE
                        it.isChecked = card.isSelected
                    }
                    else -> it.visibility = View.GONE
                }
            }

            itemView.setOnClickListener { onItemClick?.invoke(card) }
        }
    }
}