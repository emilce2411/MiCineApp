package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserMediaState
import com.example.data.model.WatchlistType
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMediaDao {
    @Query("SELECT * FROM user_media_states")
    fun getAllUserStates(): Flow<List<UserMediaState>>

    @Query("SELECT * FROM user_media_states WHERE mediaId = :mediaId")
    fun observeUserState(mediaId: String): Flow<UserMediaState?>

    @Query("SELECT * FROM user_media_states WHERE mediaId = :mediaId")
    suspend fun getUserState(mediaId: String): UserMediaState?

    @Query("SELECT * FROM user_media_states WHERE listType = :type ORDER BY lastUpdated DESC")
    fun getByListType(type: WatchlistType): Flow<List<UserMediaState>>

    @Query("SELECT * FROM user_media_states WHERE isFavorite = 1 ORDER BY lastUpdated DESC")
    fun getFavorites(): Flow<List<UserMediaState>>

    @Query("SELECT * FROM user_media_states WHERE customTags LIKE '%' || :tag || '%'")
    fun getByCustomTag(tag: String): Flow<List<UserMediaState>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: UserMediaState)

    @Query("DELETE FROM user_media_states WHERE mediaId = :mediaId")
    suspend fun deleteState(mediaId: String)
}
