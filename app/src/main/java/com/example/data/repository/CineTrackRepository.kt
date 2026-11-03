package com.example.data.repository

import com.example.data.local.CineTrackDatabase
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.MediaWithUserState
import com.example.data.model.UserMediaState
import com.example.data.model.WatchlistType
import com.example.data.sample.SampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class CineTrackRepository(private val database: CineTrackDatabase) {
    private val mediaDao = database.mediaDao()
    private val userMediaDao = database.userMediaDao()
    private val episodeDao = database.episodeDao()
    private val customListDao = database.customListDao()
    private val watchedEpisodeHistoryDao = database.watchedEpisodeHistoryDao()
    private val tmdbSyncManager = com.example.data.remote.TmdbSyncManager(database)

    suspend fun syncWithTmdb(apiKey: String): Int = tmdbSyncManager.fetchAndCachePopularFromTmdb(apiKey)

    suspend fun searchTmdbOnline(apiKey: String, query: String): List<MediaItem> = tmdbSyncManager.searchOnlineTmdb(apiKey, query)

    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val allUserStates: Flow<List<UserMediaState>> = userMediaDao.getAllUserStates()
    val allCustomLists: Flow<List<com.example.data.model.CustomList>> = customListDao.getAllLists()
    val allWatchedEpisodeHistory: Flow<List<com.example.data.model.WatchedEpisodeHistory>> = watchedEpisodeHistoryDao.getAllWatchedHistory()

    val mediaWithStates: Flow<List<MediaWithUserState>> = combine(
        allMedia,
        allUserStates
    ) { mediaList, states ->
        val stateMap = states.associateBy { it.mediaId }
        mediaList.map { media ->
            MediaWithUserState(
                media = media,
                userState = stateMap[media.id]
            )
        }
    }

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        if (mediaDao.count() == 0) {
            mediaDao.insertAll(SampleData.initialMediaList)
            episodeDao.insertAll(SampleData.sampleEpisodes)
            SampleData.sampleInitialUserStates.forEach { state ->
                userMediaDao.insertOrUpdate(state)
            }

            // Seed Custom Lists
            val list1 = com.example.data.model.CustomList(
                id = "list_friends",
                name = "Para ver con amigos",
                description = "Maratones de finde semana",
                colorHex = 0xFFFFB800
            )
            val list2 = com.example.data.model.CustomList(
                id = "list_rain",
                name = "Domingos de lluvia",
                description = "Cine reconfortante y acogedor",
                colorHex = 0xFF8A2BE2
            )
            customListDao.insertList(list1)
            customListDao.insertList(list2)

            customListDao.addItemToList(com.example.data.model.CustomListItemCrossRef("list_friends", "stranger_things"))
            customListDao.addItemToList(com.example.data.model.CustomListItemCrossRef("list_friends", "the_last_of_us"))
            customListDao.addItemToList(com.example.data.model.CustomListItemCrossRef("list_rain", "spirited_away"))
            customListDao.addItemToList(com.example.data.model.CustomListItemCrossRef("list_rain", "interstellar"))

            // Seed Watched Episode History
            watchedEpisodeHistoryDao.insertWatchLog(
                com.example.data.model.WatchedEpisodeHistory(
                    seriesId = "stranger_things",
                    seasonNumber = 1,
                    episodeNumber = 1,
                    episodeTitle = "La desaparición de Will Byers",
                    watchedTimestamp = System.currentTimeMillis() - 86400000L * 14,
                    rating = 5.0f,
                    notes = "¡Increíble inicio de temporada!"
                )
            )
            watchedEpisodeHistoryDao.insertWatchLog(
                com.example.data.model.WatchedEpisodeHistory(
                    seriesId = "the_bear",
                    seasonNumber = 1,
                    episodeNumber = 7,
                    episodeTitle = "Revisión",
                    watchedTimestamp = System.currentTimeMillis() - 86400000L * 3,
                    rating = 5.0f,
                    notes = "El plano secuencia de 20 minutos es una genialidad de la televisión."
                )
            )
        }
    }

    fun observeMedia(id: String): Flow<MediaItem?> = mediaDao.observeMediaById(id)

    fun observeUserState(mediaId: String): Flow<UserMediaState?> = userMediaDao.observeUserState(mediaId)

    fun observeEpisodes(seriesId: String): Flow<List<Episode>> = episodeDao.getEpisodesForSeries(seriesId)

    suspend fun setWatchlistType(mediaId: String, type: WatchlistType) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            listType = type,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun toggleFavorite(mediaId: String) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            isFavorite = !current.isFavorite,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun updateRating(mediaId: String, rating: Float) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            rating = rating,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun saveReview(mediaId: String, reviewText: String) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            userReview = reviewText,
            reviewDate = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun logRewatch(mediaId: String) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val now = System.currentTimeMillis()
        val existingDates = if (current.rewatchDatesRaw.isBlank()) {
            listOf(now)
        } else {
            current.rewatchDatesRaw.split(",") + now.toString()
        }
        val updated = current.copy(
            rewatchCount = current.rewatchCount + 1,
            lastRewatchDate = now,
            rewatchDatesRaw = existingDates.joinToString(","),
            listType = if (current.listType == WatchlistType.NONE) WatchlistType.WATCHED else current.listType,
            lastUpdated = now
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun updateProgress(mediaId: String, season: Int, episode: Int) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            currentSeason = season,
            currentEpisode = episode,
            listType = if (current.listType != WatchlistType.WATCHED) WatchlistType.WATCHING else current.listType,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
        episodeDao.markEpisodesWatchedUpTo(mediaId, season, episode, true)
        watchedEpisodeHistoryDao.insertWatchLog(
            com.example.data.model.WatchedEpisodeHistory(
                seriesId = mediaId,
                seasonNumber = season,
                episodeNumber = episode,
                episodeTitle = "Capítulo $episode",
                watchedTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun getMediaForCustomList(listId: String): Flow<List<MediaItem>> = customListDao.getMediaForList(listId)

    suspend fun createCustomList(name: String, description: String, colorHex: Long) = withContext(Dispatchers.IO) {
        val list = com.example.data.model.CustomList(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            description = description,
            colorHex = colorHex
        )
        customListDao.insertList(list)
    }

    suspend fun addMediaToCustomList(listId: String, mediaId: String) = withContext(Dispatchers.IO) {
        customListDao.addItemToList(com.example.data.model.CustomListItemCrossRef(listId, mediaId))
    }

    suspend fun removeMediaFromCustomList(listId: String, mediaId: String) = withContext(Dispatchers.IO) {
        customListDao.removeItemFromList(listId, mediaId)
    }

    suspend fun advanceOneEpisode(mediaId: String, totalEpisodesInSeason: Int = 10, totalSeasons: Int = 4) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        var s = current.currentSeason
        var e = current.currentEpisode + 1
        if (e > totalEpisodesInSeason && s < totalSeasons) {
            s += 1
            e = 1
        }
        updateProgress(mediaId, s, e)
    }

    suspend fun updateCustomTags(mediaId: String, tags: String) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            customTags = tags,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun toggleUpcomingNotification(mediaId: String) = withContext(Dispatchers.IO) {
        val current = userMediaDao.getUserState(mediaId) ?: UserMediaState(mediaId = mediaId)
        val updated = current.copy(
            notifyUpcoming = !current.notifyUpcoming,
            lastUpdated = System.currentTimeMillis()
        )
        userMediaDao.insertOrUpdate(updated)
    }

    suspend fun importCsvData(csvText: String): Int = withContext(Dispatchers.IO) {
        // Support Letterboxd & standard CSV formats:
        // Format 1: Title,Year,Rating,Rewatch,Watched Date,Review
        // Format 2: Name,Year,Rating10,Tags
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var importedCount = 0
        val allCurrentMedia = database.mediaDao().getMediaByType(MediaType.MOVIE) // Just sample check
        
        for (i in 1 until lines.size) { // Skip header
            val line = lines[i]
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.isNotEmpty()) {
                val title = parts.getOrNull(0) ?: continue
                val year = parts.getOrNull(1)?.toIntOrNull() ?: 2020
                val ratingFloat = parts.getOrNull(2)?.toFloatOrNull() ?: 0f
                val isRewatch = parts.getOrNull(3)?.equals("yes", ignoreCase = true) == true || parts.getOrNull(3) == "1"
                
                // Match against existing media or create new entry
                val matchingId = SampleData.initialMediaList.firstOrNull { 
                    it.title.equals(title, ignoreCase = true) || it.originalTitle.equals(title, ignoreCase = true) 
                }?.id ?: "custom_${title.lowercase().replace(" ", "_")}"

                if (SampleData.initialMediaList.none { it.id == matchingId }) {
                    val newMedia = MediaItem(
                        id = matchingId,
                        title = title,
                        originalTitle = title,
                        mediaType = MediaType.MOVIE,
                        year = year,
                        posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80",
                        backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&q=80",
                        overview = "Importado desde Letterboxd / CSV.",
                        durationText = "120 min",
                        genres = "Cine",
                        ageRating = "+13",
                        voteAverage = ratingFloat.toDouble() * 2.0
                    )
                    mediaDao.insert(newMedia)
                }

                val current = userMediaDao.getUserState(matchingId) ?: UserMediaState(mediaId = matchingId)
                userMediaDao.insertOrUpdate(
                    current.copy(
                        listType = WatchlistType.WATCHED,
                        rating = ratingFloat,
                        rewatchCount = if (isRewatch) (current.rewatchCount + 1) else current.rewatchCount,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                importedCount++
            }
        }
        importedCount
    }

    suspend fun generateCsvExport(): String = withContext(Dispatchers.IO) {
        val states = database.userMediaDao().getByListType(WatchlistType.WATCHED)
        val sb = StringBuilder()
        sb.append("Title,Year,Rating,ListType,RewatchCount,CustomTags,Review\n")
        
        SampleData.initialMediaList.forEach { media ->
            val state = database.userMediaDao().getUserState(media.id)
            if (state != null && state.listType != WatchlistType.NONE) {
                sb.append("\"${media.title}\",")
                sb.append("${media.year},")
                sb.append("${state.rating},")
                sb.append("${state.listType.name},")
                sb.append("${state.rewatchCount},")
                sb.append("\"${state.customTags}\",")
                sb.append("\"${state.userReview.replace("\"", "\"\"")}\"\n")
            }
        }
        sb.toString()
    }
}
