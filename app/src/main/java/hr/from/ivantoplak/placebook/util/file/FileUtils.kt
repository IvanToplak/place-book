package hr.from.ivantoplak.placebook.util.file

import android.content.Context
import java.io.File

fun deleteFile(context: Context, filename: String) {
    val dir = context.filesDir
    val file = File(dir, filename)
    file.delete()
}