package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY voteAverage DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaById(id: String): MediaItem?

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun observeMediaById(id: String): Flow<MediaItem?>

    @Query("SELECT * FROM media_items WHERE isForKids = 1 ORDER BY voteAverage DESC")
    fun getKidsSafeMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY voteAverage DESC")
    fun getMediaByType(type: MediaType): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun count(): Int
}
