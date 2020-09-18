package hr.from.ivantoplak.placebook.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import hr.from.ivantoplak.placebook.coroutines.CoroutineContextProvider
import hr.from.ivantoplak.placebook.extensions.generateBookmarkImageFilename
import hr.from.ivantoplak.placebook.mappings.toBookmarkViewData
import hr.from.ivantoplak.placebook.model.Bookmark
import hr.from.ivantoplak.placebook.model.BookmarkViewData
import hr.from.ivantoplak.placebook.repository.BookmarkRepo
import hr.from.ivantoplak.placebook.util.image.BitmapImageProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "MapsViewModel"
private const val GET_BOOKMARKS_ERROR_MESSAGE = "Error occurred while retrieving all bookmarks."

class MapsViewModel(
    private val bookmarkRepo: BookmarkRepo,
    private val bitmapImageProvider: BitmapImageProvider,
    private val coroutineContextProvider: CoroutineContextProvider
) : ViewModel() {

    private var bookmarksFlow: Flow<List<BookmarkViewData>>? = null

    suspend fun addBookmarkFromPlace(place: Place, image: Bitmap?) =
        withContext(coroutineContextProvider.io()) {
            val bookmark = Bookmark(
                placeId = place.id ?: "",
                name = place.name ?: "",
                longitude = place.latLng?.longitude ?: 0.0,
                latitude = place.latLng?.latitude ?: 0.0,
                phone = place.phoneNumber ?: "",
                address = place.address ?: "",
                category = getPlaceCategory(place)
            )

            val newBookmark = bookmarkRepo.addBookmark(bookmark)
            if (image != null && newBookmark != null) {
                bitmapImageProvider.setImage(image, newBookmark.id.generateBookmarkImageFilename())
            }
            Log.i(TAG, "New bookmark ${newBookmark?.id} added to the database.")
        }

    suspend fun addBookmark(latLng: LatLng): Bookmark? {
        val bookmark = Bookmark(
            name = "Untitled",
            longitude = latLng.longitude,
            latitude = latLng.latitude,
            category = "Other"
        )
        return bookmarkRepo.addBookmark(bookmark)
    }

    fun getBookmarks(): Flow<List<BookmarkViewData>>? {
        if (bookmarksFlow == null) {
            bookmarksFlow = bookmarkRepo.allBookmarks()
                .map { bookmarks -> bookmarks.toBookmarkViewDataList() }
                .flowOn(coroutineContextProvider.io())
                .catch { exception -> Log.e(TAG, GET_BOOKMARKS_ERROR_MESSAGE, exception) }
        }
        return bookmarksFlow
    }

    private fun getPlaceCategory(place: Place): String {
        var category = bookmarkRepo.getDefaultCategory()
        place.types?.let { placeTypes ->
            if (placeTypes.isNotEmpty()) {
                category = bookmarkRepo.placeTypeToCategory(placeTypes[0])
            }
        }
        return category
    }

    private fun List<Bookmark>.toBookmarkViewDataList(): List<BookmarkViewData> =
        map { bookmark -> bookmark.toBookmarkViewData(bookmarkRepo.getCategoryResourceId(bookmark.category)) }

}