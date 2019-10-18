package com.faust.m.flashcardm.presentation.booklet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.databinding.RecyclerViewBookletCardsBinding

class BookletCardAdapter(cards: Collection<BookletCard>? = null):
        RecyclerView.Adapter<BookletCardAdapter.Holder>(){

    private val cards: MutableList<BookletCard> =
        if (cards.isNullOrEmpty()) mutableListOf() else ArrayList(cards)

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

    inner class Holder(private val binding: RecyclerViewBookletCardsBinding):
            RecyclerView.ViewHolder(binding.root) {

        fun bindCard(card: BookletCard) {
            binding.card = card
            binding.executePendingBindings()
        }
    }
}