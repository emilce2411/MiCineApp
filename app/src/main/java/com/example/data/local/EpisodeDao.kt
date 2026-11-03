package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Episode
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<Episode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<Episode>)

    @Query("UPDATE episodes SET isWatched = :watched WHERE id = :episodeId")
    suspend fun updateEpisodeWatched(episodeId: String, watched: Boolean)

    @Query("UPDATE episodes SET isWatched = :watched WHERE seriesId = :seriesId AND seasonNumber <= :maxSeason AND (seasonNumber < :maxSeason OR episodeNumber <= :maxEpisode)")
    suspend fun markEpisodesWatchedUpTo(seriesId: String, maxSeason: Int, maxEpisode: Int, watched: Boolean = true)
}
