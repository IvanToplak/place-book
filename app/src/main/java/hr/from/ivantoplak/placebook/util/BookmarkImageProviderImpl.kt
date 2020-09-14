package hr.from.ivantoplak.placebook.util

import android.content.Context
import android.graphics.Bitmap
import hr.from.ivantoplak.placebook.util.file.deleteFile
import hr.from.ivantoplak.placebook.util.image.loadBitmapFromFile
import hr.from.ivantoplak.placebook.util.image.saveBitmapToFile

class BookmarkImageProviderImpl(private val context: Context) : BitmapImageProvider {

    override fun setImage(image: Bitmap, fileName: String) =
        saveBitmapToFile(context, image, fileName)

    override fun getImage(fileName: String): Bitmap? =
        loadBitmapFromFile(context, fileName)

    override fun deleteImage(fileName: String) = deleteFile(context, fileName)
}