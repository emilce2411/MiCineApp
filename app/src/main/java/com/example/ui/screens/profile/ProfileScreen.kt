package com.example.ui.screens.profile

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailableCountries
import com.example.data.model.MediaWithUserState
import com.example.data.model.WatchlistType
import com.example.ui.components.MediaCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaGoldDark
import com.example.ui.theme.CinemaPurple
import com.example.ui.theme.CinemaRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    mediaList: List<MediaWithUserState>,
    selectedCountryCode: String,
    isSpoilerSafeActive: Boolean,
    isKidsModeActive: Boolean,
    onCountryClick: () -> Unit,
    onToggleSpoilerSafe: () -> Unit,
    onToggleKidsMode: () -> Unit,
    onImportSampleLetterboxd: () -> Unit,
    onImportCustomCsv: (String) -> Unit,
    onExportCsv: suspend () -> String,
    onMediaClick: (String) -> Unit,
    onTmdbClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var customCsvDraft by remember { mutableStateOf("") }
    var exportedCsvContent by remember { mutableStateOf("") }

    val country = AvailableCountries.firstOrNull { it.code == selectedCountryCode } ?: AvailableCountries.first()

    // Library groups
    val watchingList = mediaList.filter { it.userState?.listType == WatchlistType.WATCHING }
    val watchedList = mediaList.filter { it.userState?.listType == WatchlistType.WATCHED }
    val favoritesList = mediaList.filter { it.userState?.isFavorite == true }
    val watchlist = mediaList.filter { it.userState?.listType == WatchlistType.WATCHLIST }

    // Aggregate tags
    val allTags = mediaList.flatMap { it.userState?.getTagsList() ?: emptyList() }.distinct()

    val totalRewatches = mediaList.sumOf { it.userState?.rewatchCount ?: 0 }
    val totalWatchedCount = watchedList.size

    val tabs = listOf("Viendo", "Vistas", "Favoritas", "Pendientes", "Etiquetas")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // User Profile & Stats Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "CT",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mi Perfil de Cine & Series",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Región de Streaming: ${country.flag} ${country.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CinemaGold
                            )
                        }

                        IconButton(onClick = onCountryClick) {
                            Icon(Icons.Default.Language, contentDescription = "Cambiar País", tint = CinemaGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Vistas",
                            value = "$totalWatchedCount",
                            icon = Icons.Default.Movie,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "En curso",
                            value = "${watchingList.size}",
                            icon = Icons.Default.Tv,
                            color = CinemaPurple,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Rewatches",
                            value = "$totalRewatches",
                            icon = Icons.Default.Replay,
                            color = CinemaGold,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Por ver",
                            value = "${watchlist.size}",
                            icon = Icons.Default.Bookmark,
                            color = CinemaGoldDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Import / Export History Migration Row (Letterboxd / Trakt / CSV)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Migración de Historial (Letterboxd / Trakt / CSV)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showImportDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaPurple.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("import_history_button")
                                ) {
                                    Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Importar", fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            exportedCsvContent = onExportCsv()
                                            showExportDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold.copy(alpha = 0.25f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("export_history_button")
                                ) {
                                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(16.dp), tint = CinemaGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exportar CSV", fontSize = 11.sp, color = CinemaGold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // TMDB Integration Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTmdbClick() }
                            .testTag("profile_tmdb_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF01B4E4).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "TMDB",
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF01B4E4),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Conexión en Vivo con TMDB",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sincroniza pósteres, sinopsis y plataformas en streaming",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes TMDB",
                                tint = Color(0xFF01B4E4)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Settings Toggles (Spoiler-Safe & Kids Mode)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Modo Spoiler-Safe", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("Oculta automáticamente sinopsis de episodios no vistos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isSpoilerSafeActive,
                                    onCheckedChange = { onToggleSpoilerSafe() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CinemaGold, checkedTrackColor = CinemaGold.copy(alpha = 0.5f))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Perfil Infantil / Control Parental", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text("Filtra y bloquea títulos no aptos para niños (ATP)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isKidsModeActive,
                                    onCheckedChange = { onToggleKidsMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs: Viendo, Vistas, Favoritas, Pendientes, Etiquetas
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CinemaGold,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CinemaGold
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) CinemaGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Display contents based on selected tab
        when (selectedTabIndex) {
            0 -> { // Viendo ahora
                if (watchingList.isEmpty()) {
                    item { EmptyStateMessage("No tienes títulos en curso.") }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(watchingList) { item ->
                                Box(modifier = Modifier.width(140.dp)) {
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
            }
            1 -> { // Vistas
                if (watchedList.isEmpty()) {
                    item { EmptyStateMessage("Aún no has marcado títulos como vistos.") }
                } else {
                    items(watchedList) { item ->
                        WatchedListItemRow(item = item, onClick = { onMediaClick(item.media.id) })
                    }
                }
            }
            2 -> { // Favoritas
                if (favoritesList.isEmpty()) {
                    item { EmptyStateMessage("No tienes películas o series favoritas marcadas.") }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(favoritesList) { item ->
                                Box(modifier = Modifier.width(140.dp)) {
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
            }
            3 -> { // Pendientes (Por ver)
                if (watchlist.isEmpty()) {
                    item { EmptyStateMessage("Tu lista de 'Por ver' está vacía.") }
                } else {
                    items(watchlist) { item ->
                        WatchedListItemRow(item = item, onClick = { onMediaClick(item.media.id) })
                    }
                }
            }
            4 -> { // Etiquetas Personalizadas
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tus Listas y Etiquetas Creadas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedTagFilter == null,
                                onClick = { selectedTagFilter = null },
                                label = { Text("Todas") }
                            )
                            allTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTagFilter == tag,
                                    onClick = { selectedTagFilter = if (selectedTagFilter == tag) null else tag },
                                    label = { Text(tag) },
                                    leadingIcon = { Icon(Icons.Default.Label, null, Modifier.size(12.dp)) }
                                )
                            }
                        }
                    }
                }

                val taggedItems = mediaList.filter { item ->
                    val tags = item.userState?.getTagsList() ?: emptyList()
                    if (selectedTagFilter == null) tags.isNotEmpty() else tags.contains(selectedTagFilter)
                }

                if (taggedItems.isEmpty()) {
                    item { EmptyStateMessage("No hay títulos asignados a esta etiqueta.") }
                } else {
                    items(taggedItems) { item ->
                        WatchedListItemRow(item = item, onClick = { onMediaClick(item.media.id) })
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar desde Letterboxd / CSV") },
            text = {
                Column {
                    Text(
                        text = "Puedes probar con 1 toque la importación de ejemplo con películas de Letterboxd, o pegar tu propio CSV:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onImportSampleLetterboxd()
                            showImportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        modifier = Modifier.fillMaxWidth().testTag("import_sample_button")
                    ) {
                        Text("Cargar Ejemplo Letterboxd (1 Toque)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customCsvDraft,
                        onValueChange = { customCsvDraft = it },
                        placeholder = { Text("Title,Year,Rating,Rewatch\n\"Dune 2\",2024,5,yes") },
                        label = { Text("O pega contenido CSV") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                if (customCsvDraft.isNotBlank()) {
                    Button(
                        onClick = {
                            onImportCustomCsv(customCsvDraft)
                            showImportDialog = false
                        }
                    ) {
                        Text("Importar Texto")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Biblioteca (CSV)") },
            text = {
                Column {
                    Text(
                        text = "Tu historial completo formateado en CSV listo para respaldar o sincronizar:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    ) {
                        Text(
                            text = exportedCsvContent,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold)
                ) {
                    Text("Listo", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun WatchedListItemRow(
    item: MediaWithUserState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("watched_row_${item.media.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.media.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.media.year} • ${item.media.durationText} • ${item.media.ageRating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.userState?.rewatchCount ?: 0 > 0) {
                    Text(
                        text = "Rewatches: ${item.userState?.rewatchCount} veces",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaGold,
                        fontSize = 11.sp
                    )
                }
            }

            if (item.userState?.rating ?: 0f > 0f) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CinemaGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "★ ${item.userState?.rating}",
                        color = CinemaGold,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
