package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbPagedResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "total_results") val totalResults: Int = 0
)

@JsonClass(generateAdapter = true)
data class TmdbMovieResult(
    val id: Int,
    val title: String = "",
    @Json(name = "original_title") val originalTitle: String = "",
    val overview: String = "",
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbTvResult(
    val id: Int,
    val name: String = "",
    @Json(name = "original_name") val originalName: String = "",
    val overview: String = "",
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbMultiItem(
    val id: Int,
    @Json(name = "media_type") val mediaType: String? = null,
    val title: String? = null,
    val name: String? = null,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class TmdbGenre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbProviderInfo(
    @Json(name = "provider_id") val providerId: Int = 0,
    @Json(name = "provider_name") val providerName: String = "",
    @Json(name = "logo_path") val logoPath: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCountryProviders(
    val link: String? = null,
    val flatrate: List<TmdbProviderInfo>? = null,
    val rent: List<TmdbProviderInfo>? = null,
    val buy: List<TmdbProviderInfo>? = null
)

@JsonClass(generateAdapter = true)
data class TmdbWatchProvidersResponse(
    val results: Map<String, TmdbCountryProviders> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TmdbCastMember(
    val name: String,
    val character: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCrewMember(
    val name: String,
    val job: String? = null,
    val department: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCredits(
    val cast: List<TmdbCastMember> = emptyList(),
    val crew: List<TmdbCrewMember> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonBrief(
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int = 0,
    val name: String = "",
    val overview: String = ""
)

@JsonClass(generateAdapter = true)
data class TmdbEpisodeDetail(
    val id: Int,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    val name: String = "",
    val overview: String = "",
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "still_path") val stillPath: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonDetails(
    @Json(name = "season_number") val seasonNumber: Int,
    val name: String = "",
    val episodes: List<TmdbEpisodeDetail> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetails(
    val id: Int,
    val title: String,
    @Json(name = "original_title") val originalTitle: String = "",
    val overview: String = "",
    val runtime: Int? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    val genres: List<TmdbGenre> = emptyList(),
    val credits: TmdbCredits? = null,
    @Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetails(
    val id: Int,
    val name: String,
    @Json(name = "original_name") val originalName: String = "",
    val overview: String = "",
    @Json(name = "number_of_seasons") val numberOfSeasons: Int = 1,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int = 1,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    val genres: List<TmdbGenre> = emptyList(),
    val seasons: List<TmdbSeasonBrief> = emptyList(),
    val credits: TmdbCredits? = null,
    @Json(name = "watch/providers") val watchProviders: TmdbWatchProvidersResponse? = null
)
