package com.faust.m.flashcardm.presentation.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.faust.m.flashcardm.R
import com.faust.m.flashcardm.presentation.inflate
import kotlin.math.abs

class BookletAdapter(context: Context,
                     booklets: Collection<LibraryBooklet>? = null,
                     var onItemClick: ((value: LibraryBooklet) -> Unit)? = null,
                     var onItemLongClick: ((value: LibraryBooklet) -> Boolean)? = null):
    RecyclerView.Adapter<BookletAdapter.Holder>(){

    private val booklets: MutableList<LibraryBooklet> =
        if (booklets.isNullOrEmpty()) mutableListOf() else ArrayList(booklets)

    private var selected: Long? = null

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

    fun replaceBooklets(newBooklets: List<LibraryBooklet>) {
        booklets.clear()
        booklets.addAll(newBooklets)
        notifyDataSetChanged()
    }

    fun setSelected(bookletId: Long?) {
        selected = bookletId
        notifyDataSetChanged()
    }

    inner class Holder(private val view: View): RecyclerView.ViewHolder(view) {

        private var highlight: TextView = view.findViewById(R.id.recycler_view_booklet_highlight)
        private var name: TextView = view.findViewById(R.id.recycler_view_booklet_name)
        private var count: TextView = view.findViewById(R.id.recycler_view_booklet_count)

        fun bindBooklet(booklet: LibraryBooklet) {
            highlight.text = booklet.totalCardCount.toString()
            DrawableCompat.wrap(highlight.background)
                .apply { setTint(colors[abs(booklet.hashCode() % 6)]) }
                .also { highlight.background = it }
            name.text = booklet.name
            count.text = booklet.cardToReviewCount.toString()

            when(booklet.id) {
                selected -> view.isSelected = true
                else -> view.isSelected = false
            }

            view.setOnClickListener { onItemClick?.invoke(booklet) }
            view.setOnLongClickListener { onItemLongClick?.invoke(booklet) ?: false }
        }
    }
}