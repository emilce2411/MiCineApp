package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    MOVIE,
    SERIE
}

enum class WatchlistType {
    NONE,
    WATCHING,   // Viendo ahora
    WATCHED,    // Vistas
    WATCHLIST,  // Pendientes / Por ver
    FAVORITES   // Favoritas
}

data class StreamingProvider(
    val name: String,
    val type: String = "Suscripción", // Suscripción, Alquiler, Compra
    val colorHex: Long = 0xFFE50914
)

data class CountryOption(
    val code: String,
    val name: String,
    val flag: String
)

val AvailableCountries = listOf(
    CountryOption("AR", "Argentina", "🇦🇷"),
    CountryOption("MX", "México", "🇲🇽"),
    CountryOption("ES", "España", "🇪🇸"),
    CountryOption("US", "Estados Unidos", "🇺🇸"),
    CountryOption("CO", "Colombia", "🇨🇴"),
    CountryOption("CL", "Chile", "🇨🇱")
)

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey val id: String,
    val title: String,
    val originalTitle: String,
    val mediaType: MediaType,
    val year: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val overview: String,
    val durationText: String, // e.g. "148 min" or "4 Temporadas"
    val totalSeasons: Int = 1,
    val totalEpisodes: Int = 1,
    val genres: String, // Comma separated: "Ciencia ficción, Drama"
    val ageRating: String, // "ATP" (Apta para todo público), "+13", "+16", "+18"
    val isForKids: Boolean = false,
    val voteAverage: Double = 8.0,
    val directorOrCreator: String = "",
    val cast: String = "",
    val similarIds: String = "", // Comma-separated media IDs
    val streamProvidersRaw: String = "", // JSON-like simple serialized map "AR:Netflix,Max|MX:Prime,Netflix"
    val nextReleaseDate: String = "", // e.g. "2026-11-20"
    val nextReleaseTitle: String = "" // e.g. "Temporada 2 - Ep. 1" or "Estreno en streaming"
) {
    fun getGenresList(): List<String> = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    fun getProvidersForCountry(countryCode: String): List<StreamingProvider> {
        // Parse simple formatted string: "AR:Netflix,Max,Disney+|MX:Prime,Netflix|ES:Movistar+,Max"
        if (streamProvidersRaw.isBlank()) return emptyList()
        val countrySegment = streamProvidersRaw.split("|")
            .firstOrNull { it.startsWith("$countryCode:") }
            ?: streamProvidersRaw.split("|").firstOrNull() // fallback
            ?: return emptyList()

        val providerNames = countrySegment.substringAfter(":").split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return providerNames.map { name ->
            val color = when (name.lowercase()) {
                "netflix" -> 0xFFE50914
                "max", "hbo max" -> 0xFF002BE7
                "disney+", "disney plus" -> 0xFF113CCF
                "prime video", "amazon prime" -> 0xFF00A8E1
                "apple tv+", "apple tv" -> 0xFF4A4A4A
                "paramount+" -> 0xFF0064FF
                "crunchyroll" -> 0xFFFF6000
                "mubi" -> 0xFF001489
                else -> 0xFF3F51B5
            }
            StreamingProvider(name = name, type = "Suscripción", colorHex = color)
        }
    }
}

@Entity(tableName = "user_media_states")
data class UserMediaState(
    @PrimaryKey val mediaId: String,
    val listType: WatchlistType = WatchlistType.NONE,
    val isFavorite: Boolean = false,
    val rating: Float = 0f, // 0.0 to 5.0 in 0.5 increments
    val userReview: String = "",
    val reviewDate: Long = 0L,
    val rewatchCount: Int = 0,
    val lastRewatchDate: Long = 0L,
    val rewatchDatesRaw: String = "", // Comma-separated timestamps
    val currentSeason: Int = 1,
    val currentEpisode: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val customTags: String = "", // Comma-separated
    val notifyUpcoming: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getTagsList(): List<String> = customTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    fun getRewatchHistory(): List<Long> {
        if (rewatchDatesRaw.isBlank()) return emptyList()
        return rewatchDatesRaw.split(",").mapNotNull { it.trim().toLongOrNull() }
    }
}

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val overview: String,
    val airDate: String = "",
    val durationMinutes: Int = 50,
    val isWatched: Boolean = false
)

data class MediaWithUserState(
    val media: MediaItem,
    val userState: UserMediaState?
)

data class CoupleRecommendation(
    val media: MediaItem,
    val user1WantsToWatch: Boolean,
    val user2WantsToWatch: Boolean,
    val matchType: String, // "Ambos quieren verla", "Recomendación cruzada"
    val matchScorePercent: Int
)

@Entity(tableName = "custom_lists")
data class CustomList(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val iconName: String = "favorite",
    val colorHex: Long = 0xFFFFB800,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "custom_list_items",
    primaryKeys = ["listId", "mediaId"]
)
data class CustomListItemCrossRef(
    val listId: String,
    val mediaId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watched_episode_history")
data class WatchedEpisodeHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String = "",
    val watchedTimestamp: Long = System.currentTimeMillis(),
    val rating: Float = 0f, // Puntuación de 0.0 a 5.0 con medias estrellas
    val notes: String = ""
)

