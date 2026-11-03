package com.example.data.remote

import com.example.data.local.CineTrackDatabase
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TmdbSyncManager(
    private val database: CineTrackDatabase,
    private val apiService: TmdbApiService = TmdbClient.api
) {
    private val mediaDao = database.mediaDao()
    private val episodeDao = database.episodeDao()

    // Supported countries to extract from watch/providers
    private val targetCountries = listOf("AR", "MX", "ES", "US", "CO", "CL")

    suspend fun fetchAndCachePopularFromTmdb(apiKey: String): Int = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext 0

        var count = 0
        try {
            // 1. Fetch Trending Movies
            val movieResponse = apiService.getTrendingMovies(apiKey = apiKey)
            for (movie in movieResponse.results.take(10)) {
                try {
                    val details = apiService.getMovieDetails(movieId = movie.id, apiKey = apiKey)
                    val mediaItem = mapMovieDetailsToMediaItem(details)
                    mediaDao.insert(mediaItem)
                    count++
                } catch (e: Exception) {
                    // Fallback to basic mapping if detail call fails
                    val basic = mapBasicMovieToMediaItem(movie)
                    mediaDao.insert(basic)
                    count++
                }
            }

            // 2. Fetch Trending TV Shows
            val tvResponse = apiService.getTrendingTv(apiKey = apiKey)
            for (tv in tvResponse.results.take(10)) {
                try {
                    val details = apiService.getTvDetails(seriesId = tv.id, apiKey = apiKey)
                    val mediaItem = mapTvDetailsToMediaItem(details)
                    mediaDao.insert(mediaItem)
                    count++

                    // Fetch Season 1 episodes for series
                    try {
                        val season1 = apiService.getTvSeason(seriesId = tv.id, seasonNumber = 1, apiKey = apiKey)
                        val episodes = season1.episodes.map { ep ->
                            Episode(
                                id = "tmdb_tv_${tv.id}_s1_e${ep.episodeNumber}",
                                seriesId = mediaItem.id,
                                seasonNumber = ep.seasonNumber,
                                episodeNumber = ep.episodeNumber,
                                title = ep.name.ifBlank { "Capítulo ${ep.episodeNumber}" },
                                overview = ep.overview.ifBlank { "Sinopsis oficial disponible en emisión." },
                                airDate = ep.airDate ?: "",
                                durationMinutes = ep.runtime ?: 45
                            )
                        }
                        if (episodes.isNotEmpty()) {
                            episodeDao.insertAll(episodes)
                        }
                    } catch (_: Exception) {}
                } catch (e: Exception) {
                    val basic = mapBasicTvToMediaItem(tv)
                    mediaDao.insert(basic)
                    count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        count
    }

    suspend fun searchOnlineTmdb(apiKey: String, query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || query.isBlank()) return@withContext emptyList()

        try {
            val response = apiService.searchMulti(apiKey = apiKey, query = query)
            val results = mutableListOf<MediaItem>()

            for (item in response.results.take(15)) {
                when (item.mediaType) {
                    "movie" -> {
                        val mediaItem = MediaItem(
                            id = "tmdb_movie_${item.id}",
                            title = item.title ?: item.name ?: "Película",
                            originalTitle = item.originalTitle ?: item.title ?: "",
                            mediaType = MediaType.MOVIE,
                            year = item.releaseDate?.take(4)?.toIntOrNull() ?: 2024,
                            posterUrl = TmdbClient.buildPosterUrl(item.posterPath),
                            backdropUrl = TmdbClient.buildBackdropUrl(item.backdropPath),
                            overview = item.overview ?: "",
                            durationText = "Película",
                            genres = "Cine",
                            ageRating = "+13",
                            voteAverage = item.voteAverage
                        )
                        mediaDao.insert(mediaItem)
                        results.add(mediaItem)
                    }
                    "tv" -> {
                        val mediaItem = MediaItem(
                            id = "tmdb_tv_${item.id}",
                            title = item.name ?: item.title ?: "Serie",
                            originalTitle = item.originalName ?: item.name ?: "",
                            mediaType = MediaType.SERIE,
                            year = item.firstAirDate?.take(4)?.toIntOrNull() ?: 2024,
                            posterUrl = TmdbClient.buildPosterUrl(item.posterPath),
                            backdropUrl = TmdbClient.buildBackdropUrl(item.backdropPath),
                            overview = item.overview ?: "",
                            durationText = "Serie",
                            genres = "Serie",
                            ageRating = "+16",
                            voteAverage = item.voteAverage
                        )
                        mediaDao.insert(mediaItem)
                        results.add(mediaItem)
                    }
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapMovieDetailsToMediaItem(details: TmdbMovieDetails): MediaItem {
        val providersRaw = extractProvidersRaw(details.watchProviders)
        val director = details.credits?.crew?.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.name ?: ""
        val castNames = details.credits?.cast?.take(4)?.joinToString(", ") { it.name } ?: ""
        val genreNames = details.genres.joinToString(", ") { it.name }
        val year = details.releaseDate?.take(4)?.toIntOrNull() ?: 2024
        val duration = if (details.runtime != null && details.runtime > 0) "${details.runtime} min" else "120 min"

        return MediaItem(
            id = "tmdb_movie_${details.id}",
            title = details.title,
            originalTitle = details.originalTitle.ifBlank { details.title },
            mediaType = MediaType.MOVIE,
            year = year,
            posterUrl = TmdbClient.buildPosterUrl(details.posterPath),
            backdropUrl = TmdbClient.buildBackdropUrl(details.backdropPath),
            overview = details.overview.ifBlank { "Sinopsis oficial en catálogo TMDB." },
            durationText = duration,
            genres = genreNames.ifBlank { "Drama, Aventura" },
            ageRating = if (details.voteAverage >= 8.0) "+16" else "+13",
            isForKids = genreNames.contains("Animación", ignoreCase = true) || genreNames.contains("Familia", ignoreCase = true),
            voteAverage = details.voteAverage,
            directorOrCreator = director,
            cast = castNames,
            streamProvidersRaw = providersRaw
        )
    }

    private fun mapTvDetailsToMediaItem(details: TmdbTvDetails): MediaItem {
        val providersRaw = extractProvidersRaw(details.watchProviders)
        val creator = details.credits?.crew?.firstOrNull { it.job?.contains("Creator", true) == true || it.department?.contains("Direct", true) == true }?.name ?: ""
        val castNames = details.credits?.cast?.take(4)?.joinToString(", ") { it.name } ?: ""
        val genreNames = details.genres.joinToString(", ") { it.name }
        val year = details.firstAirDate?.take(4)?.toIntOrNull() ?: 2024

        return MediaItem(
            id = "tmdb_tv_${details.id}",
            title = details.name,
            originalTitle = details.originalName.ifBlank { details.name },
            mediaType = MediaType.SERIE,
            year = year,
            posterUrl = TmdbClient.buildPosterUrl(details.posterPath),
            backdropUrl = TmdbClient.buildBackdropUrl(details.backdropPath),
            overview = details.overview.ifBlank { "Sinopsis oficial en catálogo TMDB." },
            durationText = "${details.numberOfSeasons} ${if (details.numberOfSeasons == 1) "Temporada" else "Temporadas"}",
            totalSeasons = details.numberOfSeasons.coerceAtLeast(1),
            totalEpisodes = details.numberOfEpisodes.coerceAtLeast(1),
            genres = genreNames.ifBlank { "Drama, Ciencia ficción" },
            ageRating = if (details.voteAverage >= 8.5) "+18" else "+16",
            isForKids = genreNames.contains("Animación", ignoreCase = true) || genreNames.contains("Familia", ignoreCase = true),
            voteAverage = details.voteAverage,
            directorOrCreator = creator,
            cast = castNames,
            streamProvidersRaw = providersRaw
        )
    }

    private fun mapBasicMovieToMediaItem(movie: TmdbMovieResult): MediaItem {
        return MediaItem(
            id = "tmdb_movie_${movie.id}",
            title = movie.title,
            originalTitle = movie.originalTitle.ifBlank { movie.title },
            mediaType = MediaType.MOVIE,
            year = movie.releaseDate?.take(4)?.toIntOrNull() ?: 2024,
            posterUrl = TmdbClient.buildPosterUrl(movie.posterPath),
            backdropUrl = TmdbClient.buildBackdropUrl(movie.backdropPath),
            overview = movie.overview,
            durationText = "120 min",
            genres = "Película",
            ageRating = "+13",
            voteAverage = movie.voteAverage
        )
    }

    private fun mapBasicTvToMediaItem(tv: TmdbTvResult): MediaItem {
        return MediaItem(
            id = "tmdb_tv_${tv.id}",
            title = tv.name,
            originalTitle = tv.originalName.ifBlank { tv.name },
            mediaType = MediaType.SERIE,
            year = tv.firstAirDate?.take(4)?.toIntOrNull() ?: 2024,
            posterUrl = TmdbClient.buildPosterUrl(tv.posterPath),
            backdropUrl = TmdbClient.buildBackdropUrl(tv.backdropPath),
            overview = tv.overview,
            durationText = "1 Temporada",
            genres = "Serie",
            ageRating = "+16",
            voteAverage = tv.voteAverage
        )
    }

    private fun extractProvidersRaw(watchProviders: TmdbWatchProvidersResponse?): String {
        if (watchProviders == null || watchProviders.results.isEmpty()) {
            return "AR:Netflix,Max|MX:Prime Video,Max|ES:Movistar+,Max|US:Netflix,Max|CO:Netflix|CL:Netflix"
        }

        val segments = mutableListOf<String>()
        for (country in targetCountries) {
            val countryData = watchProviders.results[country]
            if (countryData != null) {
                val providers = mutableListOf<String>()
                countryData.flatrate?.forEach { providers.add(it.providerName) }
                if (providers.isEmpty() && countryData.rent != null) {
                    countryData.rent.take(2).forEach { providers.add(it.providerName) }
                }
                if (providers.isNotEmpty()) {
                    segments.add("$country:${providers.distinct().joinToString(",")}")
                }
            }
        }

        if (segments.isEmpty()) {
            return "AR:Netflix,Max|MX:Prime Video,Max|ES:Movistar+,Max|US:Netflix,Max|CO:Netflix|CL:Netflix"
        }
        return segments.joinToString("|")
    }
}
