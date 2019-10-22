package com.faust.m.flashcardm.presentation.booklet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewBookletCardsBinding
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.find
import org.jetbrains.anko.info

class BookletCardAdapter(cards: Collection<BookletCard>? = null,
                         var onItemClick: ((value: BookletCard) -> Unit)? = null):
        RecyclerView.Adapter<BookletCardAdapter.Holder>(), AnkoLogger {

    private val cards: MutableList<BookletCard> =
        if (cards.isNullOrEmpty()) mutableListOf() else ArrayList(cards)

    var deleteMode: Boolean = false
    var idSelected: MutableList<Long> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        with(LayoutInflater.from(parent.context)) {
            Holder(RecyclerViewBookletCardsBinding.inflate(this, parent, false))
        }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindCard(cards[position])
    }

    fun replaceCards(newCards: List<BookletCard>) {
        cards.clear()
        cards.addAll(newCards)
        notifyDataSetChanged()
    }

    fun notifyItemDeleted(positions: List<Int>) {

        var size = cards.size

        val cardsToRemove =
            cards.filterIndexed { index, bookletCard -> positions.contains(index) }
        cards.removeAll(cardsToRemove)
/*
        positions.forEach {
            notifyItemRemoved(it)
        }
*/
        var count = 0
        info { "Size is $size" }
        for (i in 0 until cards.size) {
            if (positions.contains(i))
                notifyItemRemoved(i - (count++))
            /*else
                notifyItemChanged(i)*/
        }
        size = cards.size
        //notifyDataSetChanged()
        notifyItemRangeChanged(0, size)
    }

    inner class Holder(private val binding: RecyclerViewBookletCardsBinding):
            RecyclerView.ViewHolder(binding.root) {

        fun bindCard(card: BookletCard) {
            binding.card = card
            binding.executePendingBindings()

            itemView.find<CheckBox>(R.id.cb_selected).isChecked = idSelected.contains(card.id)

            itemView.find<CheckBox>(R.id.cb_selected).visibility =
                if (deleteMode) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onItemClick?.invoke(card) }
        }
    }
}