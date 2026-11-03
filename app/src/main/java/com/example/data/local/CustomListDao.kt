package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomList
import com.example.data.model.CustomListItemCrossRef
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {
    @Query("SELECT * FROM custom_lists ORDER BY updatedAt DESC")
    fun getAllLists(): Flow<List<CustomList>>

    @Query("SELECT * FROM custom_lists WHERE id = :id")
    suspend fun getListById(id: String): CustomList?

    @Query("SELECT * FROM custom_lists WHERE id = :id")
    fun observeListById(id: String): Flow<CustomList?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CustomList)

    @Update
    suspend fun updateList(list: CustomList)

    @Delete
    suspend fun deleteList(list: CustomList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToList(crossRef: CustomListItemCrossRef)

    @Query("DELETE FROM custom_list_items WHERE listId = :listId AND mediaId = :mediaId")
    suspend fun removeItemFromList(listId: String, mediaId: String)

    @Query("""
        SELECT m.* FROM media_items m
        INNER JOIN custom_list_items c ON m.id = c.mediaId
        WHERE c.listId = :listId
        ORDER BY c.addedAt DESC
    """)
    fun getMediaForList(listId: String): Flow<List<MediaItem>>

    @Query("SELECT COUNT(*) FROM custom_list_items WHERE listId = :listId")
    fun getItemCountForList(listId: String): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_list_items WHERE listId = :listId AND mediaId = :mediaId)")
    suspend fun isMediaInList(listId: String, mediaId: String): Boolean
}
