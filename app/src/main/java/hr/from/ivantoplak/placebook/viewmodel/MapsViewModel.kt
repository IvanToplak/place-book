package hr.from.ivantoplak.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import hr.from.ivantoplak.placebook.model.Bookmark
import hr.from.ivantoplak.placebook.model.BookmarkView
import hr.from.ivantoplak.placebook.repository.BookmarkRepo

private const val TAG = "MapsViewModel"

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
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
        image?.let { newBookmark?.setImage(it, getApplication()) }
        Log.i(TAG, "New bookmark ${newBookmark?.id} added to the database.")
    }

    fun addBookmark(latLng: LatLng): Bookmark? {
        val bookmark = Bookmark(
            name = "Untitled",
            longitude = latLng.longitude,
            latitude = latLng.latitude,
            category = "Other"
        )
        return bookmarkRepo.addBookmark(bookmark)
    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark) =
        BookmarkView(
            id = bookmark.id,
            location = LatLng(bookmark.latitude, bookmark.longitude),
            name = bookmark.name,
            phone = bookmark.phone,
            categoryResourceId = bookmarkRepo.getCategoryResourceId(bookmark.category)
        )


    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks())
        { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
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
}