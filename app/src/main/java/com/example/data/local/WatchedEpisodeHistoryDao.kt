package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WatchedEpisodeHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedEpisodeHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchLog(log: WatchedEpisodeHistory): Long

    @Query("SELECT * FROM watched_episode_history ORDER BY watchedTimestamp DESC")
    fun getAllWatchedHistory(): Flow<List<WatchedEpisodeHistory>>

    @Query("SELECT * FROM watched_episode_history WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getHistoryForSeries(seriesId: String): Flow<List<WatchedEpisodeHistory>>

    @Query("SELECT COUNT(*) FROM watched_episode_history")
    fun getTotalWatchedEpisodesCount(): Flow<Int>

    @Query("DELETE FROM watched_episode_history WHERE seriesId = :seriesId AND seasonNumber = :season AND episodeNumber = :episode")
    suspend fun deleteHistoryEntry(seriesId: String, season: Int, episode: Int)

    @Query("SELECT * FROM watched_episode_history WHERE seriesId = :seriesId AND seasonNumber = :season AND episodeNumber = :episode LIMIT 1")
    suspend fun getWatchLogForEpisode(seriesId: String, season: Int, episode: Int): WatchedEpisodeHistory?
}
