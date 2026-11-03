package com.example.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.MediaWithUserState
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaPurple
import com.example.ui.theme.CinemaRed
import com.example.viewmodel.FilterState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    mediaList: List<MediaWithUserState>,
    filterState: FilterState,
    onQueryChange: (String) -> Unit,
    onMediaTypeFilterChange: (MediaType?) -> Unit,
    onGenreFilterChange: (String?) -> Unit,
    onAgeRatingFilterChange: (String?) -> Unit,
    onToggleKidsMode: () -> Unit,
    onMediaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val genres = listOf("Ciencia ficción", "Drama", "Animación", "Aventura", "Comedia", "Misterio", "Historia")
    val ageRatings = listOf("ATP", "+13", "+16", "+18")

    // Filter application
    val filteredResults = mediaList.filter { item ->
        val query = filterState.query.trim().lowercase()
        val matchesQuery = query.isEmpty() ||
                item.media.title.lowercase().contains(query) ||
                item.media.originalTitle.lowercase().contains(query) ||
                item.media.directorOrCreator.lowercase().contains(query) ||
                item.media.cast.lowercase().contains(query) ||
                item.media.genres.lowercase().contains(query)

        val matchesType = filterState.mediaType == null || item.media.mediaType == filterState.mediaType
        val matchesGenre = filterState.selectedGenre == null || item.media.genres.contains(filterState.selectedGenre, ignoreCase = true)
        val matchesAge = filterState.selectedAgeRating == null || item.media.ageRating == filterState.selectedAgeRating
        val matchesKidsMode = !filterState.isKidsModeActive || item.media.isForKids

        matchesQuery && matchesType && matchesGenre && matchesAge && matchesKidsMode
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search Input Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar película, serie, actor, director...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CinemaGold)
                    },
                    trailingIcon = {
                        if (filterState.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = CinemaGold,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Kids Mode Parental Control Switch Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterState.isKidsModeActive) Color(0xFF10B981).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleKidsMode() }
                        .testTag("kids_mode_toggle_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = if (filterState.isKidsModeActive) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Perfil Infantil / Control Parental",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filterState.isKidsModeActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (filterState.isKidsModeActive) "Activo: Se oculta todo contenido +13, +16 y +18" else "Toca para filtrar solo contenido apto para niños",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (filterState.isKidsModeActive) Color(0xFF10B981) else Color.DarkGray
                        ) {
                            Text(
                                text = if (filterState.isKidsModeActive) "ACTIVADO" else "DESACTIVADO",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type Filters (Película vs Serie)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterState.mediaType == null,
                        onClick = { onMediaTypeFilterChange(null) },
                        label = { Text("Todos", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CinemaGold.copy(alpha = 0.25f), selectedLabelColor = CinemaGold)
                    )
                    FilterChip(
                        selected = filterState.mediaType == MediaType.MOVIE,
                        onClick = { onMediaTypeFilterChange(if (filterState.mediaType == MediaType.MOVIE) null else MediaType.MOVIE) },
                        label = { Text("Películas", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CinemaGold.copy(alpha = 0.25f), selectedLabelColor = CinemaGold)
                    )
                    FilterChip(
                        selected = filterState.mediaType == MediaType.SERIE,
                        onClick = { onMediaTypeFilterChange(if (filterState.mediaType == MediaType.SERIE) null else MediaType.SERIE) },
                        label = { Text("Series", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CinemaGold.copy(alpha = 0.25f), selectedLabelColor = CinemaGold)
                    )
                }

                // Genre Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    genres.forEach { genre ->
                        val isSelected = filterState.selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGenreFilterChange(if (isSelected) null else genre) },
                            label = { Text(genre, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaPurple.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Search Results Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredResults.size} títulos encontrados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Results Grid
        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No se encontraron títulos con esos filtros.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intenta buscar con otros términos o desactivar el perfil infantil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredResults) { item ->
                    MediaCard(
                        media = item.media,
                        userState = item.userState,
                        onClick = { onMediaClick(item.media.id) }
                    )
                }
            }
        }
    }
}
