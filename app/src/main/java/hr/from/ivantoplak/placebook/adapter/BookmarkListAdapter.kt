package hr.from.ivantoplak.placebook.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.extensions.inflate
import hr.from.ivantoplak.placebook.model.BookmarkView
import hr.from.ivantoplak.placebook.ui.MapsActivity
import kotlinx.android.synthetic.main.bookmark_item.view.*

class BookmarkListAdapter(
    private var bookmarkData: List<BookmarkView>,
    private val mapsActivity: MapsActivity
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    class ViewHolder(v: View, private val mapsActivity: MapsActivity) : RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.bookmarkNameTextView
        val categoryImageView: ImageView = v.bookmarkIcon
        init {
            v.setOnClickListener {
                val bookmarkView = itemView.tag as BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
    }

    fun setBookmarkData(bookmarks: List<BookmarkView>) {
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.bookmark_item), mapsActivity)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookmarkViewData = bookmarkData[position]
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        holder.categoryImageView.setImageResource(bookmarkViewData.categoryResourceId)
    }

    override fun getItemCount(): Int = bookmarkData.size
}