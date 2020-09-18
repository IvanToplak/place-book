package hr.from.ivantoplak.placebook.repository

import com.google.android.libraries.places.api.model.Place
import hr.from.ivantoplak.placebook.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepo {

    suspend fun addBookmark(bookmark: Bookmark): Bookmark?
    fun allBookmarks(): Flow<List<Bookmark>>
    fun getLiveBookmark(bookmarkId: Long): Flow<Bookmark>
    suspend fun updateBookmark(bookmark: Bookmark)
    suspend fun getBookmark(bookmarkId: Long): Bookmark?
    fun placeTypeToCategory(placeType: Place.Type): String
    fun getDefaultCategory(): String
    fun getCategoryResourceId(placeCategory: String): Int
    fun getCategories(): List<String>
    suspend fun deleteBookmark(bookmark: Bookmark)
}