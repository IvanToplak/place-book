package hr.from.ivantoplak.placebook.mappings

import com.google.android.gms.maps.model.LatLng
import hr.from.ivantoplak.placebook.model.Bookmark
import hr.from.ivantoplak.placebook.model.BookmarkDetailsViewData
import hr.from.ivantoplak.placebook.model.BookmarkViewData

fun Bookmark.toBookmarkDetailsViewData(): BookmarkDetailsViewData = BookmarkDetailsViewData(
    id = id,
    name = name,
    phone = phone,
    address = address,
    notes = notes,
    category = category,
    longitude = longitude,
    latitude = latitude,
    placeId = placeId
)

fun BookmarkDetailsViewData.toBookmark(bookmark: Bookmark): Bookmark = bookmark.copy(
    name = name,
    phone = phone,
    address = address,
    notes = notes,
    category = category
)

fun Bookmark.toBookmarkViewData(categoryResourceId: Int): BookmarkViewData = BookmarkViewData(
    id = id,
    location = LatLng(latitude, longitude),
    name = name,
    phone = phone,
    categoryResourceId = categoryResourceId
)