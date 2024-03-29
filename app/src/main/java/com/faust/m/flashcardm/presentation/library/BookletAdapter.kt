package com.faust.m.flashcardm.presentation.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.ViewBookletBannerBinding
import com.faust.m.flashcardm.presentation.library.BookletAdapter.ChangeSet.Insert
import com.faust.m.flashcardm.presentation.library.BookletAdapter.ChangeSet.Remove
import org.jetbrains.anko.find

class BookletAdapter(booklets: Collection<BookletBannerData>? = null,
                     var onItemClick: ((value: BookletBannerData) -> Unit)? = null,
                     var onInfoClick: ((value: BookletBannerData, infoView: View) -> Unit)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>() {

    private val booklets: MutableList<BookletBannerData> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        with(LayoutInflater.from(parent.context)) {
            Holder(ViewBookletBannerBinding.inflate(this, parent, false))
        }

    override fun getItemCount(): Int = booklets.size

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bindBooklet(booklets[position])


    fun replaceBooklets(newBooklets: List<BookletBannerData>) {
        val changeSet = diff(booklets, newBooklets)

        booklets.clear()
        booklets.addAll(newBooklets)

        when (changeSet) {
            is Insert -> notifyItemInserted(changeSet.index)
            is Remove -> notifyItemRemoved(changeSet.index)
            else -> notifyDataSetChanged()
        }
    }


    inner class Holder(private val binding: ViewBookletBannerBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bindBooklet(booklet: BookletBannerData) {
            binding.booklet = booklet
            binding.executePendingBindings()

            itemView.setOnClickListener { onItemClick?.invoke(booklet) }
            itemView.find<ImageView>(R.id.iv_info).apply {
                this.setOnClickListener { onInfoClick?.invoke(booklet, this) }
            }
        }
    }


    private fun diff(oldBooklets: List<BookletBannerData>, newBooklets: List<BookletBannerData>): ChangeSet {
        val oldSize = oldBooklets.size
        val newSize = newBooklets.size

        if (isRemove(oldSize, newSize)) {
            var indexRemoved = newSize
            for ((index, booklet) in newBooklets.withIndex()) {
                if (oldBooklets[index] != booklet) {
                    indexRemoved = index
                    break
                }
            }
            return Remove(indexRemoved)
        }
        else if (isInsert(oldSize, newSize)) {
            var indexInserted = oldSize
            for ((index, booklet) in oldBooklets.withIndex()) {
                if (newBooklets[index] != booklet) {
                    indexInserted = index
                    break
                }
            }
            return Insert(indexInserted)
        }
        else {
            return ChangeSet.Update
        }
    }

    private fun isInsert(oldSize: Int, newSize: Int) = (oldSize < newSize)
    private fun isRemove(oldSize: Int, newSize: Int) = (oldSize > newSize)

    sealed class ChangeSet {
        data class Insert(val index: Int): ChangeSet()
        data class Remove(val index: Int): ChangeSet()
        object Update : ChangeSet()
    }
}