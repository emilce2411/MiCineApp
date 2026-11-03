package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AvailableCountries
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.MediaWithUserState
import com.example.data.model.WatchlistType
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaPurple
import com.example.ui.theme.CinemaRed

@Composable
fun HomeScreen(
    mediaList: List<MediaWithUserState>,
    selectedCountryCode: String,
    isSpoilerSafeActive: Boolean,
    isKidsModeActive: Boolean,
    onCountryClick: () -> Unit,
    onToggleSpoilerSafe: () -> Unit,
    onMediaClick: (String) -> Unit,
    onQuickAdvance: (String, Int, Int) -> Unit,
    onToggleNotification: (String) -> Unit,
    onTmdbClick: () -> Unit = {},
    isTmdbSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val country = AvailableCountries.firstOrNull { it.code == selectedCountryCode } ?: AvailableCountries.first()
    
    // Filter out 18+ content if kids mode is active
    val filteredList = if (isKidsModeActive) {
        mediaList.filter { it.media.isForKids }
    } else {
        mediaList
    }

    val watchingNow = filteredList.filter { it.userState?.listType == WatchlistType.WATCHING }
    val upcomingReleases = filteredList.filter { it.media.nextReleaseDate.isNotBlank() }
    val favorites = filteredList.filter { it.userState?.isFavorite == true || it.media.voteAverage >= 8.8 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Hero Header with Cinema Atmosphere
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_cinema),
                    contentDescription = "CineTrack Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Top Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CINETRACK",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = CinemaGold
                        )
                        Text(
                            text = "Tu centro de control cinematográfico",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // TMDB Sync / Status Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF01B4E4).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { onTmdbClick() }
                                .testTag("home_tmdb_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTmdbSyncing) "⏳ TMDB" else "🎬 TMDB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF01B4E4)
                                )
                            }
                        }

                        // Country Selector
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { onCountryClick() }
                                .testTag("home_country_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flag, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = country.code,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaGold
                                )
                            }
                        }

                        // Spoiler-Safe Quick Toggle
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSpoilerSafeActive) CinemaPurple.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSpoilerSafeActive) CinemaPurple else Color.Gray.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable { onToggleSpoilerSafe() }
                                .testTag("home_spoiler_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSpoilerSafeActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Spoiler-Safe",
                                    tint = if (isSpoilerSafeActive) CinemaGold else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSpoilerSafeActive) "Spoiler-Safe" else "Spoilers ON",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSpoilerSafeActive) CinemaGold else Color.LightGray
                                )
                            }
                        }
                    }
                }

                // Active mode status bar (if Kids Mode active)
                if (isKidsModeActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Modo Infantil Activo (Solo ATP)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Viendo Ahora (Series en curso)
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Viendo Ahora",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "${watchingNow.size} en curso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (watchingNow.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No tienes series en curso todavía. ¡Explora el catálogo y pulsa 'Viendo ahora' para registrar tus episodios!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(watchingNow) { item ->
                            WatchingNowCard(
                                item = item,
                                onClick = { onMediaClick(item.media.id) },
                                onAdvance = {
                                    onQuickAdvance(item.media.id, 10, item.media.totalSeasons)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Próximos Estrenos y Avisos
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CinemaRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Próximos Estrenos y Avisos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(upcomingReleases) { item ->
                        UpcomingReleaseCard(
                            item = item,
                            onClick = { onMediaClick(item.media.id) },
                            onToggleNotify = { onToggleNotification(item.media.id) }
                        )
                    }
                }
            }
        }

        // Section 3: Destacados y Streaming en tu País
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Populares en Streaming (${country.name})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(filteredList) { item ->
                        Box(modifier = Modifier.width(140.dp)) {
                            MediaCard(
                                media = item.media,
                                userState = item.userState,
                                onClick = { onMediaClick(item.media.id) },
                                onQuickAdvance = {
                                    onQuickAdvance(item.media.id, 10, item.media.totalSeasons)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchingNowCard(
    item: MediaWithUserState,
    onClick: () -> Unit,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = item.userState
    val currentSeason = state?.currentSeason ?: 1
    val currentEpisode = state?.currentEpisode ?: 0

    Card(
        modifier = modifier
            .width(260.dp)
            .clickable { onClick() }
            .testTag("watching_now_${item.media.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.media.posterUrl,
                contentDescription = item.media.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 60.dp, height = 85.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.media.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CinemaPurple.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "Temp. $currentSeason • Cap. $currentEpisode",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD8B4FE),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CinemaGold,
                    modifier = Modifier
                        .clickable { onAdvance() }
                        .testTag("advance_btn_${item.media.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1 Capítulo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingReleaseCard(
    item: MediaWithUserState,
    onClick: () -> Unit,
    onToggleNotify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNotified = item.userState?.notifyUpcoming ?: true

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("upcoming_card_${item.media.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CinemaRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = item.media.nextReleaseDate,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CinemaRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onToggleNotify,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isNotified) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = if (isNotified) CinemaGold else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.media.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.media.nextReleaseTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )
        }
    }
}
