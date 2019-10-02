package com.faust.m.flashcardm.presentation.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.core.domain.Booklet
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.inflate
import kotlin.math.abs

class BookletAdapter(context: Context,
                     booklets: Collection<Booklet>? = null,
                     var onItemClick: ((value: Booklet) -> Unit)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>(){

    private val booklets: MutableList<Booklet> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)

    private val colors = context.run { arrayOf (
        getColor(R.color.colorHighlight1),
        getColor(R.color.colorHighlight2),
        getColor(R.color.colorHighlight3),
        getColor(R.color.colorHighlight4),
        getColor(R.color.colorHighlight5),
        getColor(R.color.colorHighlight6)
        )
    }

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

        private var highlight: TextView = view.findViewById(R.id.recycler_view_booklet_highlight)
        private var name: TextView = view.findViewById(R.id.recycler_view_booklet_name)

        fun bindBooklet(booklet: Booklet) {
            booklet.name.subSequence(0, 1).let {
                highlight.text = it
                highlight.setBackgroundColor(colors[abs(booklet.hashCode() % 6)])
            }

/*            val uniqueIdMultiplier = getHighlightLetter().hashCode().div(6)
            val colorArrayIndex = getHighlightLetter().hashCode() - (uniqueIdMultiplier * 6)*/
            name.text = booklet.name
            view.setOnClickListener { onItemClick?.invoke(booklet) }
        }
    }
}