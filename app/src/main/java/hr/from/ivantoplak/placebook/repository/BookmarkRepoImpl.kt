package hr.from.ivantoplak.placebook.repository

import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import hr.from.ivantoplak.placebook.R
import hr.from.ivantoplak.placebook.db.BookmarkDao
import hr.from.ivantoplak.placebook.model.Bookmark

private const val DEFAULT_CATEGORY = "Other"

class BookmarkRepoImpl(private val bookmarkDao: BookmarkDao) : BookmarkRepo {

    private val categoryMap: Map<Place.Type, String> = buildCategoryMap()
    private val allCategories: Map<String, Int> = buildCategories()

    override fun addBookmark(bookmark: Bookmark): Bookmark? {
        if (bookmark.placeId.isNotEmpty()) {
            val savedBookmark = getBookmark(bookmark.placeId)
            savedBookmark?.let { return it }
        }
        val newId = bookmarkDao.insertBookmark(bookmark)
        return bookmarkDao.loadBookmark(newId)
    }

    override fun allBookmarks(): LiveData<List<Bookmark>> = bookmarkDao.loadAll()

    override fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)

    override fun updateBookmark(bookmark: Bookmark) = bookmarkDao.updateBookmark(bookmark)

    override fun getBookmark(bookmarkId: Long): Bookmark? = bookmarkDao.loadBookmark(bookmarkId)

    private fun getBookmark(placeId: String): Bookmark? = bookmarkDao.loadBookmark(placeId)

    override fun placeTypeToCategory(placeType: Place.Type): String =
        if (categoryMap.containsKey(placeType)) categoryMap[placeType].toString() else DEFAULT_CATEGORY

    override fun getDefaultCategory(): String = DEFAULT_CATEGORY

    override fun getCategoryResourceId(placeCategory: String): Int =
        allCategories[placeCategory] ?: allCategories[getDefaultCategory()] as Int

    override fun getCategories(): List<String> = ArrayList(allCategories.keys)

    override fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    private fun buildCategoryMap(): Map<Place.Type, String> = hashMapOf(
        Place.Type.BAKERY to "Restaurant",
        Place.Type.BAR to "Restaurant",
        Place.Type.CAFE to "Restaurant",
        Place.Type.FOOD to "Restaurant",
        Place.Type.RESTAURANT to "Restaurant",
        Place.Type.MEAL_DELIVERY to "Restaurant",
        Place.Type.MEAL_TAKEAWAY to "Restaurant",
        Place.Type.GAS_STATION to "Gas",
        Place.Type.CLOTHING_STORE to "Shopping",
        Place.Type.DEPARTMENT_STORE to "Shopping",
        Place.Type.FURNITURE_STORE to "Shopping",
        Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
        Place.Type.HARDWARE_STORE to "Shopping",
        Place.Type.HOME_GOODS_STORE to "Shopping",
        Place.Type.JEWELRY_STORE to "Shopping",
        Place.Type.SHOE_STORE to "Shopping",
        Place.Type.SHOPPING_MALL to "Shopping",
        Place.Type.STORE to "Shopping",
        Place.Type.LODGING to "Lodging",
        Place.Type.ROOM to "Lodging"
    )

    private fun buildCategories(): Map<String, Int> = hashMapOf(
        "Gas" to R.drawable.ic_gas,
        "Lodging" to R.drawable.ic_lodging,
        DEFAULT_CATEGORY to R.drawable.ic_other,
        "Restaurant" to R.drawable.ic_restaurant,
        "Shopping" to R.drawable.ic_shopping
    )
}