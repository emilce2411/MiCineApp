package com.example.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Episode
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.MediaWithUserState
import com.example.data.model.WatchlistType
import com.example.ui.components.HalfStarRatingBar
import com.example.ui.components.MediaCard
import com.example.ui.components.SpoilerSafeCard
import com.example.ui.components.StreamingBadgeRow
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDark
import com.example.ui.theme.CinemaPurple
import com.example.ui.theme.CinemaRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    media: MediaItem,
    allMedia: List<MediaWithUserState>,
    userState: com.example.data.model.UserMediaState?,
    episodesFlow: Flow<List<Episode>>,
    selectedCountryCode: String,
    isSpoilerSafeActive: Boolean,
    onBack: () -> Unit,
    onCountryClick: () -> Unit,
    onSetWatchlistType: (WatchlistType) -> Unit,
    onToggleFavorite: () -> Unit,
    onUpdateRating: (Float) -> Unit,
    onSaveReview: (String) -> Unit,
    onLogRewatch: () -> Unit,
    onUpdateProgress: (Int, Int) -> Unit,
    onAdvanceEpisode: (Int, Int) -> Unit,
    onUpdateTags: (String) -> Unit,
    onSelectSimilar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val episodes by episodesFlow.collectAsState(initial = emptyList())
    var selectedSeason by remember { mutableIntStateOf(1) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var reviewTextDraft by remember(userState?.userReview) { mutableStateOf(userState?.userReview ?: "") }
    var tagsTextDraft by remember(userState?.customTags) { mutableStateOf(userState?.customTags ?: "") }

    val providers = media.getProvidersForCountry(selectedCountryCode)
    val similarList = allMedia.filter { item ->
        media.similarIds.split(",").contains(item.media.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("detail_screen_${media.id}"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Backdrop & Poster Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = media.backdropUrl.ifBlank { media.posterUrl },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("detail_fav_button")
                    ) {
                        Icon(
                            imageVector = if (userState?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorita",
                            tint = if (userState?.isFavorite == true) CinemaRed else Color.White
                        )
                    }
                }

                // Poster & Metadata Header Info
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AsyncImage(
                        model = media.posterUrl,
                        contentDescription = media.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 90.dp, height = 130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (media.originalTitle != media.title) {
                            Text(
                                text = media.originalTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (media.ageRating) {
                                    "ATP" -> Color(0xFF10B981)
                                    "+13" -> Color(0xFF3B82F6)
                                    "+16" -> Color(0xFFF59E0B)
                                    else -> CinemaRed
                                }
                            ) {
                                Text(
                                    text = media.ageRating,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "${media.year} • ${media.durationText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", media.voteAverage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Dónde Verla (Streaming oficial según país)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                StreamingBadgeRow(
                    providers = providers,
                    selectedCountryCode = selectedCountryCode,
                    onCountryClick = onCountryClick
                )
            }
        }

        // Section: Watchlist Action Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Tu Estado y Listas",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentList = userState?.listType ?: WatchlistType.NONE

                        FilterChip(
                            selected = currentList == WatchlistType.WATCHING,
                            onClick = {
                                onSetWatchlistType(if (currentList == WatchlistType.WATCHING) WatchlistType.NONE else WatchlistType.WATCHING)
                            },
                            label = { Text("Viendo", fontSize = 11.sp) },
                            leadingIcon = {
                                if (currentList == WatchlistType.WATCHING) Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaPurple.copy(alpha = 0.35f),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_watching")
                        )

                        FilterChip(
                            selected = currentList == WatchlistType.WATCHED,
                            onClick = {
                                onSetWatchlistType(if (currentList == WatchlistType.WATCHED) WatchlistType.NONE else WatchlistType.WATCHED)
                            },
                            label = { Text("Vista", fontSize = 11.sp) },
                            leadingIcon = {
                                if (currentList == WatchlistType.WATCHED) Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.35f),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_watched")
                        )

                        FilterChip(
                            selected = currentList == WatchlistType.WATCHLIST,
                            onClick = {
                                onSetWatchlistType(if (currentList == WatchlistType.WATCHLIST) WatchlistType.NONE else WatchlistType.WATCHLIST)
                            },
                            label = { Text("Por ver", fontSize = 11.sp) },
                            leadingIcon = {
                                if (currentList == WatchlistType.WATCHLIST) Icon(Icons.Default.Bookmark, null, Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGoldDark.copy(alpha = 0.35f),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_watchlist")
                        )
                    }
                }
            }
        }

        // Section: Calificación con Medias Estrellas & Rewatches
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Tu Calificación Personal (con medias estrellas)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HalfStarRatingBar(
                        rating = userState?.rating ?: 0f,
                        onRatingChanged = onUpdateRating,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rewatch Counter & History (¡Nuevo feature!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Rewatches: ${userState?.rewatchCount ?: 0} veces",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val history = userState?.getRewatchHistory() ?: emptyList()
                                if (history.isNotEmpty()) {
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(history.last()))
                                    Text(
                                        text = "Último rewatch: $dateStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onLogRewatch,
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("log_rewatch_button")
                        ) {
                            Icon(Icons.Default.Add, null, tint = CinemaGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1 Rewatch", color = CinemaGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Review and Personal Notes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tus Notas y Reseña",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { showReviewDialog = true },
                            modifier = Modifier.testTag("edit_review_button")
                        ) {
                            Text(if (userState?.userReview.isNullOrBlank()) "+ Escribir Reseña" else "Editar", color = CinemaGold)
                        }
                    }

                    if (!userState?.userReview.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“${userState?.userReview}”",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Tags Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Etiquetas Personalizadas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { showTagsDialog = true },
                            modifier = Modifier.testTag("edit_tags_button")
                        ) {
                            Text("+ Añadir Etiquetas", color = CinemaGold)
                        }
                    }

                    val tagsList = userState?.getTagsList() ?: emptyList()
                    if (tagsList.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tagsList.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CinemaPurple.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaPurple.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Label, null, tint = CinemaPurple, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = tag, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Sinopsis General
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Sinopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = media.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dirección / Creador: ${media.directorOrCreator}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Reparto: ${media.cast}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Episodios con Spoiler-Safe (si es serie)
        if (media.mediaType == MediaType.SERIE) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodios (Modo Spoiler-Safe)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CinemaPurple,
                            modifier = Modifier
                                .clickable {
                                    onAdvanceEpisode(10, media.totalSeasons)
                                }
                                .testTag("detail_advance_ep_btn")
                        ) {
                            Text(
                                text = "+1 Cap Visto",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val userCurrentSeason = userState?.currentSeason ?: 1
                    val userCurrentEpisode = userState?.currentEpisode ?: 0

                    val seasonEpisodes = episodes.filter { it.seasonNumber == selectedSeason }

                    if (seasonEpisodes.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "Episodios sincronizados con TMDB para temporada $selectedSeason. Puedes registrar tu avance con el botón rápido.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            seasonEpisodes.forEach { ep ->
                                val isPastProgress = (ep.seasonNumber > userCurrentSeason) || 
                                    (ep.seasonNumber == userCurrentSeason && ep.episodeNumber > userCurrentEpisode)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!isPastProgress) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (!isPastProgress) Color(0xFF10B981) else Color.DarkGray,
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        if (!isPastProgress) {
                                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                        } else {
                                                            Text(
                                                                text = "${ep.episodeNumber}",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = "${ep.episodeNumber}. ${ep.title}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Text(
                                                text = "${ep.durationMinutes}m",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Spoiler-Safe protected synopsis!
                                        SpoilerSafeCard(
                                            synopsisText = ep.overview,
                                            isSpoilerSafeActive = isSpoilerSafeActive,
                                            isUnwatchedEpisode = isPastProgress
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Recomendaciones TMDB (Similares y Recomendadas)
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Text(
                    text = "Similares (Recomendaciones de TMDB)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (similarList.isEmpty()) {
                    Text(
                        text = "No se encontraron títulos directamente similares.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(similarList) { item ->
                            Box(modifier = Modifier.width(130.dp)) {
                                MediaCard(
                                    media = item.media,
                                    userState = item.userState,
                                    onClick = { onSelectSimilar(item.media.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Editing Review
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Reseña y Notas Personales") },
            text = {
                OutlinedTextField(
                    value = reviewTextDraft,
                    onValueChange = { reviewTextDraft = it },
                    label = { Text("Escribe qué te pareció...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveReview(reviewTextDraft)
                        showReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                ) {
                    Text("Guardar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog for Editing Custom Tags
    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Etiquetas Personalizadas") },
            text = {
                Column {
                    Text(
                        text = "Separa con comas (ej. Para ver con amigos, Domingos de lluvia, Cine de autor):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagsTextDraft,
                        onValueChange = { tagsTextDraft = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateTags(tagsTextDraft)
                        showTagsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                ) {
                    Text("Guardar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
