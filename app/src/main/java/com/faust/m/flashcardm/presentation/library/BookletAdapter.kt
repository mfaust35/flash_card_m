package com.faust.m.flashcardm.presentation.library

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.inflate

class BookletAdapter(booklets: Collection<Booklet>? = null,
                     var onItemClick: ((value: Booklet) -> Unit)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>(){

    private val booklets: MutableList<Booklet> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(parent.inflate(R.layout.recycler_view_booklet, false))

    override fun getItemCount(): Int = booklets.size

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bindBooklet(booklets[position])

    fun replaceBooklets(newBooklets: List<Booklet>) {
        booklets.clear()
        booklets.addAll(newBooklets)
        notifyDataSetChanged()
    }

    inner class Holder(private val view: View): RecyclerView.ViewHolder(view) {

        private var name: TextView = view.findViewById(R.id.recycler_view_booklet_name)

        fun bindBooklet(booklet: Booklet) {
            name.text = booklet.name
            view.setOnClickListener { onItemClick?.invoke(booklet) }
        }
    }
}