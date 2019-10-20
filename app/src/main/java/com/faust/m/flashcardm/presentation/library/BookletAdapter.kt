package com.faust.m.flashcardm.presentation.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.databinding.RecyclerViewLibraryBookletsBinding
import org.jetbrains.anko.find

class BookletAdapter(booklets: Collection<LibraryBooklet>? = null,
                     var onItemClick: ((value: LibraryBooklet) -> Unit)? = null,
                     var onInfoClick: ((value: LibraryBooklet, infoView: View) -> Unit)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>() {

    private val booklets: MutableList<LibraryBooklet> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        with(LayoutInflater.from(parent.context)) {
            Holder(RecyclerViewLibraryBookletsBinding.inflate(this, parent, false))
        }

    override fun getItemCount(): Int = booklets.size

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bindBooklet(booklets[position])

    fun replaceBooklets(newBooklets: List<LibraryBooklet>) {
        booklets.clear()
        booklets.addAll(newBooklets)
        notifyDataSetChanged()
    }

    fun bookletRemoved(libraryBooklet: LibraryBooklet) {
        val indexOf = booklets.indexOf(libraryBooklet)
        booklets.remove(libraryBooklet)
        notifyItemRemoved(indexOf)
    }

    fun bookletAdded(booklet: AddedBooklet) {
        booklets.add(booklet.position, booklet.booklet)
        notifyItemInserted(booklet.position)
    }

    inner class Holder(private val binding: RecyclerViewLibraryBookletsBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bindBooklet(booklet: LibraryBooklet) {
            binding.booklet = booklet
            binding.executePendingBindings()

            itemView.setOnClickListener { onItemClick?.invoke(booklet) }
            itemView.find<ImageView>(R.id.iv_info).apply {
                this.setOnClickListener { onInfoClick?.invoke(booklet, this) }
            }
        }
    }
}