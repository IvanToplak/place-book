package hr.from.ivantoplak.placebook.model

import android.content.Context
import android.graphics.Bitmap
import hr.from.ivantoplak.placebook.util.image.loadBitmapFromFile
import hr.from.ivantoplak.placebook.util.image.saveBitmapToFile

data class BookmarkDetailsView(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val category: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val placeId: String = ""
) {
    fun getImage(context: Context): Bitmap? =
        loadBitmapFromFile(context, Bookmark.generateImageFilename(id))

    fun setImage(context: Context, image: Bitmap) =
        saveBitmapToFile(context, image, Bookmark.generateImageFilename(id))

}