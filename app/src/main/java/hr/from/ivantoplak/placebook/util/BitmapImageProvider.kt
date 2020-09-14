package hr.from.ivantoplak.placebook.util

import android.graphics.Bitmap

interface BitmapImageProvider {

    fun setImage(image: Bitmap, fileName: String)
    fun getImage(fileName: String): Bitmap?
    fun deleteImage(fileName: String)
}