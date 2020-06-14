package hr.from.ivantoplak.placebook.util.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
    ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        saveBytesToFile(context, bytes, filename)
    }
}

private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
    try {
        val outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
        outputStream.use { stream -> stream.write(bytes) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
    val filePath = File(context.filesDir, filename).absolutePath
    return BitmapFactory.decodeFile(filePath)
}

@Throws(IOException::class)
fun createUniqueImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
    val filename = "PlaceBook_" + timeStamp + "_"
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(filename, ".jpg", filesDir)
}

private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun decodeFileToSize(filePath: String, width: Int, height: Int): Bitmap {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(filePath, options)
    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, width, height)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(filePath, options)
}

fun decodeUriStreamToSize(uri: Uri, width: Int, height: Int, context: Context): Bitmap? {
    var image: Bitmap? = null
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            BitmapFactory.decodeStream(stream, null, options)

            val newStream = context.contentResolver.openInputStream(uri)
            newStream?.use { str ->
                options.inSampleSize =
                    calculateInSampleSize(options.outWidth, options.outHeight, width, height)
                options.inJustDecodeBounds = false
                image = BitmapFactory.decodeStream(str, null, options)
            }
        }
    } catch (e: Exception) {
    }
    return image
}