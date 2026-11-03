package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CineTrackDatabase
import com.example.data.repository.CineTrackRepository
import com.example.ui.components.CountrySelectorDialog
import com.example.ui.screens.couple.CoupleModeScreen
import com.example.ui.screens.detail.DetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DeepDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CineTrackViewModel
import com.example.viewmodel.CineTrackViewModelFactory
import com.example.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val database by lazy { CineTrackDatabase.getDatabase(this) }
    private val repository by lazy { CineTrackRepository(database) }
    private val viewModel: CineTrackViewModel by viewModels {
        CineTrackViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CineTrackApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CineTrackApp(viewModel: CineTrackViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedMediaId by viewModel.selectedMediaId.collectAsState()
    val mediaWithStates by viewModel.mediaWithStates.collectAsState()
    val selectedCountryCode by viewModel.selectedCountryCode.collectAsState()
    val isSpoilerSafeActive by viewModel.isSpoilerSafeActive.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val tmdbApiKey by viewModel.tmdbApiKey.collectAsState()
    val isSyncingTmdb by viewModel.isSyncingTmdb.collectAsState()

    var showCountryDialog by remember { mutableStateOf(false) }
    var showTmdbDialog by remember { mutableStateOf(false) }
    var previousTab by remember { mutableStateOf(ScreenTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    // Country Selector Dialog
    if (showCountryDialog) {
        CountrySelectorDialog(
            selectedCountryCode = selectedCountryCode,
            onSelectCountry = { viewModel.setCountry(it) },
            onDismiss = { showCountryDialog = false }
        )
    }

    // TMDB Settings and Live Sync Dialog
    if (showTmdbDialog) {
        com.example.ui.components.TmdbSettingsDialog(
            currentApiKey = tmdbApiKey,
            isSyncing = isSyncingTmdb,
            onSaveApiKey = { viewModel.updateTmdbApiKey(it) },
            onSyncNow = { viewModel.syncWithTmdb() },
            onDismiss = { showTmdbDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentTab != ScreenTab.DETAIL) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == ScreenTab.HOME,
                        onClick = { viewModel.navigateTo(ScreenTab.HOME) },
                        icon = {
                            Icon(
                                if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Inicio"
                            )
                        },
                        label = { Text("Inicio", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CinemaGold,
                            selectedTextColor = CinemaGold,
                            indicatorColor = CinemaGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.SEARCH,
                        onClick = { viewModel.navigateTo(ScreenTab.SEARCH) },
                        icon = {
                            Icon(
                                if (currentTab == ScreenTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        label = { Text("Buscar", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CinemaGold,
                            selectedTextColor = CinemaGold,
                            indicatorColor = CinemaGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_search")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.COUPLE,
                        onClick = { viewModel.navigateTo(ScreenTab.COUPLE) },
                        icon = {
                            Icon(
                                if (currentTab == ScreenTab.COUPLE) Icons.Filled.Group else Icons.Outlined.Group,
                                contentDescription = "En Pareja"
                            )
                        },
                        label = { Text("En Pareja", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CinemaGold,
                            selectedTextColor = CinemaGold,
                            indicatorColor = CinemaGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_couple")
                    )

                    NavigationBarItem(
                        selected = currentTab == ScreenTab.PROFILE,
                        onClick = { viewModel.navigateTo(ScreenTab.PROFILE) },
                        icon = {
                            Icon(
                                if (currentTab == ScreenTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Perfil"
                            )
                        },
                        label = { Text("Perfil", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CinemaGold,
                            selectedTextColor = CinemaGold,
                            indicatorColor = CinemaGold.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        mediaList = mediaWithStates,
                        selectedCountryCode = selectedCountryCode,
                        isSpoilerSafeActive = isSpoilerSafeActive,
                        isKidsModeActive = filterState.isKidsModeActive,
                        onCountryClick = { showCountryDialog = true },
                        onToggleSpoilerSafe = { viewModel.toggleSpoilerSafe() },
                        onMediaClick = { id ->
                            previousTab = ScreenTab.HOME
                            viewModel.openDetail(id)
                        },
                        onQuickAdvance = { id, maxEpisodes, maxSeasons ->
                            viewModel.advanceEpisode(id, maxEpisodes, maxSeasons)
                        },
                        onToggleNotification = { id ->
                            viewModel.toggleUpcomingNotification(id)
                        },
                        onTmdbClick = { showTmdbDialog = true },
                        isTmdbSyncing = isSyncingTmdb
                    )
                }

                ScreenTab.SEARCH -> {
                    SearchScreen(
                        mediaList = mediaWithStates,
                        filterState = filterState,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onMediaTypeFilterChange = { viewModel.setMediaTypeFilter(it) },
                        onGenreFilterChange = { viewModel.setGenreFilter(it) },
                        onAgeRatingFilterChange = { viewModel.setAgeRatingFilter(it) },
                        onToggleKidsMode = { viewModel.toggleKidsMode() },
                        onMediaClick = { id ->
                            previousTab = ScreenTab.SEARCH
                            viewModel.openDetail(id)
                        }
                    )
                }

                ScreenTab.COUPLE -> {
                    val recommendations = remember(mediaWithStates) {
                        viewModel.computeCoupleRecommendations(mediaWithStates)
                    }
                    CoupleModeScreen(
                        recommendations = recommendations,
                        allMedia = mediaWithStates,
                        onMediaClick = { id ->
                            previousTab = ScreenTab.COUPLE
                            viewModel.openDetail(id)
                        }
                    )
                }

                ScreenTab.PROFILE -> {
                    ProfileScreen(
                        mediaList = mediaWithStates,
                        selectedCountryCode = selectedCountryCode,
                        isSpoilerSafeActive = isSpoilerSafeActive,
                        isKidsModeActive = filterState.isKidsModeActive,
                        onCountryClick = { showCountryDialog = true },
                        onToggleSpoilerSafe = { viewModel.toggleSpoilerSafe() },
                        onToggleKidsMode = { viewModel.toggleKidsMode() },
                        onImportSampleLetterboxd = { viewModel.importSampleLetterboxdCsv() },
                        onImportCustomCsv = { viewModel.importCustomCsv(it) },
                        onExportCsv = { viewModel.exportCsv() },
                        onMediaClick = { id ->
                            previousTab = ScreenTab.PROFILE
                            viewModel.openDetail(id)
                        },
                        onTmdbClick = { showTmdbDialog = true }
                    )
                }

                ScreenTab.DETAIL -> {
                    val currentMediaWithState = mediaWithStates.firstOrNull { it.media.id == selectedMediaId }
                    if (currentMediaWithState != null) {
                        val episodesFlow = remember(selectedMediaId) {
                            viewModel.getEpisodesForSeries(selectedMediaId ?: "")
                        }
                        DetailScreen(
                            media = currentMediaWithState.media,
                            allMedia = mediaWithStates,
                            userState = currentMediaWithState.userState,
                            episodesFlow = episodesFlow,
                            selectedCountryCode = selectedCountryCode,
                            isSpoilerSafeActive = isSpoilerSafeActive,
                            onBack = { viewModel.navigateTo(previousTab) },
                            onCountryClick = { showCountryDialog = true },
                            onSetWatchlistType = { type ->
                                selectedMediaId?.let { viewModel.setWatchlistType(it, type) }
                            },
                            onToggleFavorite = {
                                selectedMediaId?.let { viewModel.toggleFavorite(it) }
                            },
                            onUpdateRating = { rating ->
                                selectedMediaId?.let { viewModel.updateRating(it, rating) }
                            },
                            onSaveReview = { review ->
                                selectedMediaId?.let { viewModel.saveReview(it, review) }
                            },
                            onLogRewatch = {
                                selectedMediaId?.let { viewModel.logRewatch(it) }
                            },
                            onUpdateProgress = { season, episode ->
                                selectedMediaId?.let { viewModel.updateProgress(it, season, episode) }
                            },
                            onAdvanceEpisode = { maxEpisodes, maxSeasons ->
                                selectedMediaId?.let { viewModel.advanceEpisode(it, maxEpisodes, maxSeasons) }
                            },
                            onUpdateTags = { tags ->
                                selectedMediaId?.let { viewModel.updateCustomTags(it, tags) }
                            },
                            onSelectSimilar = { similarId ->
                                viewModel.openDetail(similarId)
                            }
                        )
                    } else {
                        // Fallback if not found
                        BackHandler { viewModel.navigateTo(previousTab) }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("Título no encontrado", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
