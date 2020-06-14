package hr.from.ivantoplak.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hr.from.ivantoplak.placebook.util.file.deleteFile
import hr.from.ivantoplak.placebook.util.image.saveBitmapToFile

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "") val placeId: String = "",
    @ColumnInfo(defaultValue = "") val name: String = "",
    @ColumnInfo(defaultValue = "") val address: String = "",
    @ColumnInfo(defaultValue = "0") val latitude: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val longitude: Double = 0.0,
    @ColumnInfo(defaultValue = "") val phone: String = "",
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "") val category: String = ""
) {
    companion object {
        fun generateImageFilename(id: Long): String = "bookmark$id.png"
    }

    fun setImage(image: Bitmap, context: Context) =
        saveBitmapToFile(context, image, generateImageFilename(id))

    fun deleteImage(context: Context) = deleteFile(context, generateImageFilename(id))
}