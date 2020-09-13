package hr.from.ivantoplak.placebook.repository

import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import hr.from.ivantoplak.placebook.model.Bookmark

interface BookmarkRepo {

    fun addBookmark(bookmark: Bookmark): Bookmark?
    fun allBookmarks(): LiveData<List<Bookmark>>
    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark>
    fun updateBookmark(bookmark: Bookmark)
    fun getBookmark(bookmarkId: Long): Bookmark?
    fun placeTypeToCategory(placeType: Place.Type): String
    fun getDefaultCategory(): String
    fun getCategoryResourceId(placeCategory: String): Int
    fun getCategories(): List<String>
    fun deleteBookmark(bookmark: Bookmark)
}