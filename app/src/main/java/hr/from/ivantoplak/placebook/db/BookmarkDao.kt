package hr.from.ivantoplak.placebook.db

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import hr.from.ivantoplak.placebook.model.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM Bookmark ORDER BY name")
    fun loadAll(): Flow<List<Bookmark>>

    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    suspend fun loadBookmark(bookmarkId: Long): Bookmark?

    @Query("SELECT * FROM Bookmark WHERE placeId = :placeId AND placeId <> ''")
    suspend fun loadBookmark(placeId: String): Bookmark?

    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadLiveBookmark(bookmarkId: Long): Flow<Bookmark>

    @Insert(onConflict = IGNORE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Update(onConflict = REPLACE)
    suspend fun updateBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
}