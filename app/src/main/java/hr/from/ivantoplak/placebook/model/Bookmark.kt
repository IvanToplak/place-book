package hr.from.ivantoplak.placebook.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "") val placeId: String = "",
    @ColumnInfo(defaultValue = "") val name: String = "",
    @ColumnInfo(defaultValue = "") val address: String = "",
    @ColumnInfo(defaultValue = "0") val latitude: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val longitude: Double = 0.0,
    @ColumnInfo(defaultValue = "") val phone: String = "",
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(defaultValue = "") val category: String = ""
)