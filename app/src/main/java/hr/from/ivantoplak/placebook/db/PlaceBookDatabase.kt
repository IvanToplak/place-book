package hr.from.ivantoplak.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hr.from.ivantoplak.placebook.model.Bookmark
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Database(entities = [Bookmark::class], version = 1)
abstract class PlaceBookDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        private var instance: PlaceBookDatabase? = null

        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
                val lock = ReentrantLock(true)
                lock.withLock {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            PlaceBookDatabase::class.java,
                            "PlaceBook"
                        ).build()
                    }
                }
            }
            return instance as PlaceBookDatabase
        }
    }
}