package hr.from.ivantoplak.placebook.model

data class BookmarkDetailsView(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val category: String = "",
    val longitude: Double = 0.0,
    val latitude: Double = 0.0,
    val placeId: String = ""
)