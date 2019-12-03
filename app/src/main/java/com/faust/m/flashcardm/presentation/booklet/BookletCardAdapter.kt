package com.faust.m.flashcardm.presentation.booklet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.databinding.RecyclerViewBookletCardsBinding


class BookletCardAdapter(var viewModel: BookletViewModel):
        ListAdapter<BookletCard, BookletCardAdapter.Holder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        with(LayoutInflater.from(parent.context)) {
            Holder(RecyclerViewBookletCardsBinding.inflate(this, parent, false))
        }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindCard(getItem(position))
    }

    inner class Holder(private val binding: RecyclerViewBookletCardsBinding):
            RecyclerView.ViewHolder(binding.root) {

        fun bindCard(cardData: BookletCard) {
            binding.card = cardData
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }
    }
}

private val diffCallback = object: DiffUtil.ItemCallback<BookletCard>() {

    override fun areItemsTheSame(oldItem: BookletCard, newItem: BookletCard): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: BookletCard, newItem: BookletCard): Boolean =
        !(oldItem.isSelected != newItem.isSelected ||
                oldItem.showRating != newItem.showRating ||
                oldItem.showSelection != newItem.showSelection ||
                oldItem.front != newItem.front ||
                oldItem.back != newItem.back)
}
