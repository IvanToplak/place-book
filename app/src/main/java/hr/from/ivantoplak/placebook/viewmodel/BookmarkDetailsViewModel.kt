package hr.from.ivantoplak.placebook.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import hr.from.ivantoplak.placebook.coroutines.CoroutineContextProvider
import hr.from.ivantoplak.placebook.extensions.generateBookmarkImageFilename
import hr.from.ivantoplak.placebook.mappings.toBookmark
import hr.from.ivantoplak.placebook.mappings.toBookmarkDetailsViewData
import hr.from.ivantoplak.placebook.model.BookmarkDetailsViewData
import hr.from.ivantoplak.placebook.repository.BookmarkRepo
import hr.from.ivantoplak.placebook.util.image.BitmapImageProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "BookmarkViewModel"
private const val GET_BOOKMARK_ERROR_MESSAGE = "Error occurred while getting current bookmark data."

class BookmarkDetailsViewModel(
    private val bookmarkRepo: BookmarkRepo,
    private val bitmapImageProvider: BitmapImageProvider,
    private val coroutineContextProvider: CoroutineContextProvider
) : ViewModel() {
    private var bookmarkDetailsViewDataFlow: Flow<BookmarkDetailsViewData>? = null

    fun getBookmark(bookmarkId: Long): Flow<BookmarkDetailsViewData>? {
        if (bookmarkDetailsViewDataFlow == null) {
            bookmarkDetailsViewDataFlow = bookmarkRepo.getLiveBookmark(bookmarkId)
                .map { bookmark -> bookmark.toBookmarkDetailsViewData() }
                .flowOn(coroutineContextProvider.io())
                .catch { exception -> Log.e(TAG, GET_BOOKMARK_ERROR_MESSAGE, exception) }
        }
        return bookmarkDetailsViewDataFlow
    }

    suspend fun updateBookmark(bookmarkDetailsViewData: BookmarkDetailsViewData) {
        val dbBookmark = bookmarkRepo.getBookmark(bookmarkDetailsViewData.id)
        dbBookmark?.let { oldBookmark ->
            val newBookmark = bookmarkDetailsViewData.toBookmark(oldBookmark)
            bookmarkRepo.updateBookmark(newBookmark)
        }
    }

    fun getCategoryResourceId(category: String): Int = bookmarkRepo.getCategoryResourceId(category)

    fun getCategories(): List<String> = bookmarkRepo.getCategories()

    suspend fun deleteBookmark(bookmarkDetailsViewData: BookmarkDetailsViewData) =
        withContext(coroutineContextProvider.io()) {
            val bookmark = bookmarkRepo.getBookmark(bookmarkDetailsViewData.id)
            bookmark?.let {
                bitmapImageProvider.deleteImage(it.id.generateBookmarkImageFilename())
                bookmarkRepo.deleteBookmark(it)
            }
        }

    fun getImage(id: Long): Bitmap? =
        bitmapImageProvider.getImage(id.generateBookmarkImageFilename())

    fun setImage(image: Bitmap, id: Long) =
        bitmapImageProvider.setImage(image, id.generateBookmarkImageFilename())
}