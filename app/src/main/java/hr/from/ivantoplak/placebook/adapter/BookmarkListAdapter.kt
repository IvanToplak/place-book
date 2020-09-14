package hr.from.ivantoplak.placebook.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.extensions.inflate
import hr.from.ivantoplak.placebook.model.BookmarkView
import kotlinx.android.synthetic.main.bookmark_item.view.*

class BookmarkListAdapter(
    private val bookmarkData: MutableList<BookmarkView>,
    private val bookmarkListAdapterListener: BookmarkListAdapterListener
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    interface BookmarkListAdapterListener {
        fun onMoveToBookmark(bookmark: BookmarkView)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.bookmarkNameTextView
        val categoryImageView: ImageView = view.bookmarkIcon

        init {
            view.setOnClickListener {
                val bookmarkView = itemView.tag as BookmarkView
                bookmarkListAdapterListener.onMoveToBookmark(bookmarkView)
            }
        }
    }

    fun setBookmarkData(bookmarks: List<BookmarkView>) {
        bookmarkData.clear()
        bookmarkData.addAll(bookmarks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.bookmark_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkViewData = bookmarkData[position]
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        holder.categoryImageView.setImageResource(bookmarkViewData.categoryResourceId)
    }

    override fun getItemCount(): Int = bookmarkData.size
}