package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.presentation.viewmodel.HomeUiState
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import com.guitarapp.songsbook.ui.theme.DifficultyAdvancedDark
import com.guitarapp.songsbook.ui.theme.DifficultyAdvancedLight
import com.guitarapp.songsbook.ui.theme.DifficultyBeginnerDark
import com.guitarapp.songsbook.ui.theme.DifficultyBeginnerLight
import com.guitarapp.songsbook.ui.theme.DifficultyIntermediateDark
import com.guitarapp.songsbook.ui.theme.DifficultyIntermediateLight
import com.guitarapp.songsbook.utils.ChordNotation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSongClick: (String) -> Unit,
    onEditClick: (String) -> Unit = {},
    onAddSongClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cancionero",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "My personal songbook",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddSongClick,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add song")
            }
        },
        bottomBar = { BannerAd() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.error != null -> ErrorContent(uiState.error!!)
                else -> SearchableSongList(uiState, viewModel, onSongClick, onEditClick, onAddSongClick)
            }
        }
    }
}

@Composable
private fun SearchableSongList(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onSongClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onAddSongClick: () -> Unit
) {
    val hasActiveFilter = uiState.query.isNotBlank() ||
            uiState.selectedGenre != null ||
            uiState.selectedDifficulty != null

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = uiState.query,
            onQueryChanged = viewModel::onQueryChanged,
            onClear = viewModel::clearFilters
        )

        FilterSection(
            genres = uiState.genres,
            difficulties = uiState.difficulties,
            selectedGenre = uiState.selectedGenre,
            selectedDifficulty = uiState.selectedDifficulty,
            onGenreSelected = viewModel::onGenreSelected,
            onDifficultySelected = viewModel::onDifficultySelected
        )

        if (uiState.songs.isEmpty()) {
            if (hasActiveFilter) {
                NoResultsContent()
            } else {
                EmptyLibraryContent(onAddSongClick)
            }
        } else {
            SongListContent(
                songs = uiState.songs,
                onSongClick = onSongClick,
                onFavoriteClick = viewModel::toggleFavorite,
                onEditClick = onEditClick,
                onDeleteClick = viewModel::deleteSong
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search by title or artist...") },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    genres: List<String>,
    difficulties: List<String>,
    selectedGenre: String?,
    selectedDifficulty: String?,
    onGenreSelected: (String?) -> Unit,
    onDifficultySelected: (String?) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (difficulties.isNotEmpty()) {
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                difficulties.forEach { difficulty ->
                    FilterChip(
                        selected = difficulty == selectedDifficulty,
                        onClick = { onDifficultySelected(difficulty) },
                        label = { Text(difficulty.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }

        if (genres.isNotEmpty()) {
            Text(
                text = "Genre",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                genres.forEach { genre ->
                    FilterChip(
                        selected = genre == selectedGenre,
                        onClick = { onGenreSelected(genre) },
                        label = { Text(genre) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyLibraryContent(onAddSongClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your songbook is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add your own songs and arrangements.\nEverything is stored on your device,\nno account or internet needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAddSongClick) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Add your first song")
            }
        }
    }
}

@Composable
private fun NoResultsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No songs match your search",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SongListContent(
    songs: List<Song>,
    onSongClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Box(modifier = Modifier.padding(top = 4.dp)) }
        items(songs) { song ->
            SongCard(song, onSongClick, onFavoriteClick, onEditClick, onDeleteClick)
        }
        item { Box(modifier = Modifier.padding(bottom = 72.dp)) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongCard(
    song: Song,
    onSongClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete song") },
            text = { Text("\"${song.title}\" will be permanently deleted. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(song.id)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onSongClick(song.id) },
                    onLongClick = { showMenu = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val bounceScale = remember { Animatable(1f) }
                val scope = rememberCoroutineScope()
                IconButton(onClick = {
                    onFavoriteClick(song.id)
                    scope.launch {
                        bounceScale.animateTo(
                            targetValue = 1.3f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        bounceScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (song.isFavorite) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.scale(bounceScale.value)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = song.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (song.key.isNotBlank()) {
                    val notation = UserPreferences.getNotation(LocalContext.current)
                    Text(
                        text = "Key: ${ChordNotation.convert(song.key, notation)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                DifficultyIndicator(song.difficulty)
            }
        }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onEditClick(song.id)
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }
}

@Composable
private fun DifficultyIndicator(difficulty: String) {
    val isDark = isSystemInDarkTheme()
    val level = when (difficulty.lowercase()) {
        "beginner" -> 1
        "intermediate" -> 2
        "advanced" -> 3
        else -> 0
    }
    val color = when (level) {
        1 -> if (isDark) DifficultyBeginnerDark else DifficultyBeginnerLight
        2 -> if (isDark) DifficultyIntermediateDark else DifficultyIntermediateLight
        3 -> if (isDark) DifficultyAdvancedDark else DifficultyAdvancedLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dots = (1..3).joinToString("") { if (it <= level) "\u25CF" else "\u25CB" }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dots,
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = difficulty.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}