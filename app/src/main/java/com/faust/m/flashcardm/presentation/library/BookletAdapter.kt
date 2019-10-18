package com.faust.m.flashcardm.presentation.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewBookletBinding
import org.jetbrains.anko.find

class BookletAdapter(booklets: Collection<LibraryBooklet>? = null,
                     var onItemClick: ((value: LibraryBooklet) -> Unit)? = null,
                     var onInfoClick: ((value: LibraryBooklet, infoView: View) -> Unit)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>(){

    private val booklets: MutableList<LibraryBooklet> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)

    private var selected: Long? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        with(LayoutInflater.from(parent.context)) {
            Holder(RecyclerViewBookletBinding.inflate(this, parent, false))
        }

    override fun getItemCount(): Int = booklets.size

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bindBooklet(booklets[position])

    fun replaceBooklets(newBooklets: List<LibraryBooklet>) {
        booklets.clear()
        booklets.addAll(newBooklets)
        notifyDataSetChanged()
    }

    fun setSelected(bookletId: Long?) {
        selected = bookletId
        notifyDataSetChanged()
    }

    inner class Holder(private val binding: RecyclerViewBookletBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bindBooklet(booklet: LibraryBooklet) {
            binding.booklet = booklet
            binding.executePendingBindings()

            when(booklet.id) {
                selected -> itemView.isSelected = true
                else -> itemView.isSelected = false
            }

            itemView.setOnClickListener { onItemClick?.invoke(booklet) }
            itemView.find<ImageView>(R.id.iv_info).apply {
                this.setOnClickListener { onInfoClick?.invoke(booklet, this) }
            }
        }
    }
}