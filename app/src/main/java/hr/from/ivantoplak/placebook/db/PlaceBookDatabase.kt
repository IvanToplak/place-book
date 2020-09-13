package hr.from.ivantoplak.placebook.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hr.from.ivantoplak.placebook.db.PlaceBookDatabase.Companion.VERSION
import hr.from.ivantoplak.placebook.model.Bookmark

@Database(entities = [Bookmark::class], version = VERSION)
abstract class PlaceBookDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        const val NAME = "PlaceBook"
        const val VERSION = 1
    }
}