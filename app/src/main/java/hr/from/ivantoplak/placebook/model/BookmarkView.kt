package hr.from.ivantoplak.placebook.model

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.util.image.loadBitmapFromFile

data class BookmarkView(
    val id: Long = 0L,
    val location: LatLng = LatLng(0.0, 0.0),
    val name: String = "",
    val phone: String = "",
    val categoryResourceId: Int = R.drawable.ic_other
) {
    fun getImage(context: Context): Bitmap? =
        loadBitmapFromFile(context, Bookmark.generateImageFilename(id))
}