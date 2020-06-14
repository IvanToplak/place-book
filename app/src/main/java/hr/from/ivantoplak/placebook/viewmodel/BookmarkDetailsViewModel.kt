package hr.from.ivantoplak.placebook.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import hr.from.ivantoplak.placebook.model.Bookmark
import hr.from.ivantoplak.placebook.model.BookmarkDetailsView
import hr.from.ivantoplak.placebook.repository.BookmarkRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookmarkDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkView(bookmarkId)
        }
        return bookmarkDetailsView
    }

    fun updateBookmark(bookmarkView: BookmarkDetailsView) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookmark = bookmarkViewToBookmark(bookmarkView)
            bookmark?.let { bookmarkRepo.updateBookmark(it) }
        }
    }

    fun getCategoryResourceId(category: String): Int = bookmarkRepo.getCategoryResourceId(category)

    fun getCategories(): List<String> = bookmarkRepo.getCategories()

    fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookmark = bookmarkRepo.getBookmark(bookmarkDetailsView.id)
            bookmark?.let { bookmarkRepo.deleteBookmark(it) }
        }
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkDetailsView =
        BookmarkDetailsView(
            id = bookmark.id,
            name = bookmark.name,
            phone = bookmark.phone,
            address = bookmark.address,
            notes = bookmark.notes,
            category = bookmark.category,
            longitude = bookmark.longitude,
            latitude = bookmark.latitude,
            placeId = bookmark.placeId
        )

    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark) { repoBookmark ->
            repoBookmark?.let {
                bookmarkToBookmarkView(it)
            }
        }
    }

    private fun bookmarkViewToBookmark(bookmarkView: BookmarkDetailsView): Bookmark? {
        val bookmark = bookmarkRepo.getBookmark(bookmarkView.id)
        return bookmark?.copy(
            name = bookmarkView.name,
            phone = bookmarkView.phone,
            address = bookmarkView.address,
            notes = bookmarkView.notes,
            category = bookmarkView.category
        )
    }
}