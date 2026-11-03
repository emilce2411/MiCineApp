package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AvailableCountries
import com.example.data.model.CoupleRecommendation
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.MediaWithUserState
import com.example.data.model.UserMediaState
import com.example.data.model.WatchlistType
import com.example.data.repository.CineTrackRepository
import com.example.data.sample.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    SEARCH,
    COUPLE,
    PROFILE,
    DETAIL
}

data class FilterState(
    val query: String = "",
    val mediaType: MediaType? = null,
    val selectedGenre: String? = null,
    val selectedAgeRating: String? = null,
    val isKidsModeActive: Boolean = false
)

class CineTrackViewModel(
    private val repository: CineTrackRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _selectedMediaId = MutableStateFlow<String?>(null)
    val selectedMediaId: StateFlow<String?> = _selectedMediaId.asStateFlow()

    private val _selectedCountryCode = MutableStateFlow("AR")
    val selectedCountryCode: StateFlow<String> = _selectedCountryCode.asStateFlow()

    private val _isSpoilerSafeActive = MutableStateFlow(true)
    val isSpoilerSafeActive: StateFlow<Boolean> = _isSpoilerSafeActive.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _partnerWatchlist = MutableStateFlow<Set<String>>(SampleData.partnerWatchlistIds)
    val partnerWatchlist: StateFlow<Set<String>> = _partnerWatchlist.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    val mediaWithStates: StateFlow<List<MediaWithUserState>> = repository.mediaWithStates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCustomLists: StateFlow<List<com.example.data.model.CustomList>> = repository.allCustomLists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allWatchedEpisodeHistory: StateFlow<List<com.example.data.model.WatchedEpisodeHistory>> = repository.allWatchedEpisodeHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createCustomList(name: String, description: String = "", colorHex: Long = 0xFFFFB800) {
        viewModelScope.launch {
            repository.createCustomList(name, description, colorHex)
            _notificationMessage.value = "Lista '$name' creada con éxito"
        }
    }

    fun addMediaToCustomList(listId: String, mediaId: String) {
        viewModelScope.launch {
            repository.addMediaToCustomList(listId, mediaId)
            _notificationMessage.value = "Añadido a lista personalizada"
        }
    }

    private val _tmdbApiKey = MutableStateFlow(
        com.example.BuildConfig.TMDB_API_KEY.let {
            if (it == "TMDB_API_KEY_PLACEHOLDER" || it.isBlank()) "" else it
        }
    )
    val tmdbApiKey: StateFlow<String> = _tmdbApiKey.asStateFlow()

    private val _isSyncingTmdb = MutableStateFlow(false)
    val isSyncingTmdb: StateFlow<Boolean> = _isSyncingTmdb.asStateFlow()

    fun updateTmdbApiKey(key: String) {
        _tmdbApiKey.value = key.trim()
        _notificationMessage.value = "Clave de API TMDB actualizada"
    }

    fun syncWithTmdb() {
        val key = _tmdbApiKey.value
        if (key.isBlank()) {
            _notificationMessage.value = "Configura tu API Key de TMDB en el menú de Ajustes para sincronizar en vivo"
            return
        }
        viewModelScope.launch {
            _isSyncingTmdb.value = true
            try {
                val count = repository.syncWithTmdb(key)
                _notificationMessage.value = "¡Sincronización completada! $count títulos actualizados desde TMDB."
            } catch (e: Exception) {
                _notificationMessage.value = "Error al conectar con TMDB. Verifica tu clave de API."
            } finally {
                _isSyncingTmdb.value = false
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            // If API key is available, sync popular in background
            if (_tmdbApiKey.value.isNotBlank()) {
                repository.syncWithTmdb(_tmdbApiKey.value)
            }
        }
    }

    fun navigateTo(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun openDetail(mediaId: String) {
        _selectedMediaId.value = mediaId
        _currentTab.value = ScreenTab.DETAIL
    }

    fun setCountry(countryCode: String) {
        _selectedCountryCode.value = countryCode
    }

    fun toggleSpoilerSafe() {
        _isSpoilerSafeActive.value = !_isSpoilerSafeActive.value
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
        val key = _tmdbApiKey.value
        if (key.isNotBlank() && query.trim().length >= 3) {
            viewModelScope.launch {
                repository.searchTmdbOnline(key, query.trim())
            }
        }
    }

    fun setMediaTypeFilter(type: MediaType?) {
        _filterState.value = _filterState.value.copy(mediaType = type)
    }

    fun setGenreFilter(genre: String?) {
        _filterState.value = _filterState.value.copy(selectedGenre = genre)
    }

    fun setAgeRatingFilter(rating: String?) {
        _filterState.value = _filterState.value.copy(selectedAgeRating = rating)
    }

    fun toggleKidsMode(pin: String? = null) {
        val current = _filterState.value.isKidsModeActive
        _filterState.value = _filterState.value.copy(isKidsModeActive = !current)
        _notificationMessage.value = if (!current) "Perfil Infantil activado: Solo contenido familiar y ATP" else "Perfil Infantil desactivado"
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    // CUJ: Watchlist & Favorites
    fun setWatchlistType(mediaId: String, type: WatchlistType) {
        viewModelScope.launch {
            repository.setWatchlistType(mediaId, type)
            val msg = when (type) {
                WatchlistType.WATCHING -> "Añadido a 'Viendo ahora'"
                WatchlistType.WATCHED -> "Marcado como 'Visto'"
                WatchlistType.WATCHLIST -> "Añadido a 'Pendientes (Por ver)'"
                WatchlistType.FAVORITES -> "Añadido a 'Favoritas'"
                WatchlistType.NONE -> "Eliminado de tus listas"
            }
            _notificationMessage.value = msg
        }
    }

    fun toggleFavorite(mediaId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(mediaId)
        }
    }

    fun updateRating(mediaId: String, rating: Float) {
        viewModelScope.launch {
            repository.updateRating(mediaId, rating)
            _notificationMessage.value = "Calificación guardada: $rating ★"
        }
    }

    fun saveReview(mediaId: String, reviewText: String) {
        viewModelScope.launch {
            repository.saveReview(mediaId, reviewText)
            _notificationMessage.value = "Reseña guardada con éxito"
        }
    }

    fun logRewatch(mediaId: String) {
        viewModelScope.launch {
            repository.logRewatch(mediaId)
            _notificationMessage.value = "¡Rewatch registrado!"
        }
    }

    fun advanceEpisode(mediaId: String, totalEpisodesInSeason: Int, totalSeasons: Int) {
        viewModelScope.launch {
            repository.advanceOneEpisode(mediaId, totalEpisodesInSeason, totalSeasons)
        }
    }

    fun updateProgress(mediaId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            repository.updateProgress(mediaId, season, episode)
        }
    }

    fun updateCustomTags(mediaId: String, tags: String) {
        viewModelScope.launch {
            repository.updateCustomTags(mediaId, tags)
            _notificationMessage.value = "Etiquetas actualizadas"
        }
    }

    fun toggleUpcomingNotification(mediaId: String) {
        viewModelScope.launch {
            repository.toggleUpcomingNotification(mediaId)
            _notificationMessage.value = "Preferencia de avisos actualizada"
        }
    }

    fun getEpisodesForSeries(seriesId: String) = repository.observeEpisodes(seriesId)

    // Couple Mode Logic
    fun computeCoupleRecommendations(mediaList: List<MediaWithUserState>): List<CoupleRecommendation> {
        val partnerSet = _partnerWatchlist.value
        val recommendations = mutableListOf<CoupleRecommendation>()

        mediaList.forEach { item ->
            val userWants = item.userState?.listType == WatchlistType.WATCHLIST || 
                            item.userState?.listType == WatchlistType.WATCHING ||
                            item.userState?.isFavorite == true
            val partnerWants = partnerSet.contains(item.media.id)

            if (userWants && partnerWants) {
                recommendations.add(
                    CoupleRecommendation(
                        media = item.media,
                        user1WantsToWatch = true,
                        user2WantsToWatch = true,
                        matchType = "¡Coincidencia 100%! Ambos quieren verla",
                        matchScorePercent = 100
                    )
                )
            } else if (userWants || partnerWants) {
                // High rating cross suggestion
                recommendations.add(
                    CoupleRecommendation(
                        media = item.media,
                        user1WantsToWatch = userWants,
                        user2WantsToWatch = partnerWants,
                        matchType = if (userWants) "En tu lista (¡Sorprende a tu pareja!)" else "En la lista de tu pareja",
                        matchScorePercent = (item.media.voteAverage * 10).toInt()
                    )
                )
            }
        }
        return recommendations.sortedByDescending { it.matchScorePercent }
    }

    // CSV Import / Export
    fun importSampleLetterboxdCsv() {
        val sampleCsv = """
            Title,Year,Rating,Rewatch,Watched Date
            "Dune: Part Two",2024,5.0,yes,2024-03-10
            "Interstellar",2014,5.0,yes,2023-12-01
            "Oppenheimer",2023,4.5,no,2023-08-15
            "Argentina, 1985",2022,4.5,no,2022-10-05
            "Spirited Away",2001,5.0,yes,2024-01-20
        """.trimIndent()

        viewModelScope.launch {
            val count = repository.importCsvData(sampleCsv)
            _notificationMessage.value = "Se importaron $count títulos desde Letterboxd con éxito"
        }
    }

    fun importCustomCsv(csvContent: String) {
        viewModelScope.launch {
            val count = repository.importCsvData(csvContent)
            _notificationMessage.value = "Se importaron $count títulos desde tu CSV"
        }
    }

    suspend fun exportCsv(): String {
        return repository.generateCsvExport()
    }
}

class CineTrackViewModelFactory(
    private val repository: CineTrackRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CineTrackViewModel(repository) as T
    }
}
