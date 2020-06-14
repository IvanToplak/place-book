package hr.from.ivantoplak.placebook.adapter

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.model.BookmarkView
import hr.from.ivantoplak.placebook.ui.PlaceInfo
import kotlinx.android.synthetic.main.content_bookmark_info.view.*

class BookmarkInfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    private val contents = context.layoutInflater.inflate(R.layout.content_bookmark_info, null)

    override fun getInfoContents(marker: Marker?): View {
        marker?.let {
            contents.title.text = it.title
            contents.phone.text = it.snippet
            it.tag?.let { tag ->
                when (tag) {
                    is PlaceInfo -> contents.photo.setImageBitmap(tag.image)
                    is BookmarkView -> contents.photo.setImageBitmap(tag.getImage(context))
                }
            }
        }
        return contents
    }

    override fun getInfoWindow(p0: Marker?): View? = null
}