package hr.from.ivantoplak.placebook.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.extensions.inflate
import hr.from.ivantoplak.placebook.model.BookmarkViewData
import kotlinx.android.synthetic.main.bookmark_item.view.*

class BookmarkListAdapter(
    private val bookmarks: MutableList<BookmarkViewData>,
    private val bookmarkListAdapterListener: BookmarkListAdapterListener
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    interface BookmarkListAdapterListener {
        fun onMoveToBookmark(bookmarkViewData: BookmarkViewData)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.bookmarkNameTextView
        val categoryImageView: ImageView = view.bookmarkIcon

        init {
            view.setOnClickListener {
                val bookmarkView = itemView.tag as BookmarkViewData
                bookmarkListAdapterListener.onMoveToBookmark(bookmarkView)
            }
        }
    }

    fun setBookmarkData(bookmarks: List<BookmarkViewData>) {
        this.bookmarks.clear()
        this.bookmarks.addAll(bookmarks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.bookmark_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkViewData = bookmarks[position]
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        holder.categoryImageView.setImageResource(bookmarkViewData.categoryResourceId)
    }

    override fun getItemCount(): Int = bookmarks.size
}