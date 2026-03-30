package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.presentation.viewmodel.HomeUiState
import com.guitarapp.songsbook.presentation.viewmodel.HomeViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import com.guitarapp.songsbook.ui.theme.DifficultyAdvancedDark
import com.guitarapp.songsbook.ui.theme.DifficultyAdvancedLight
import com.guitarapp.songsbook.ui.theme.DifficultyBeginnerDark
import com.guitarapp.songsbook.ui.theme.DifficultyBeginnerLight
import com.guitarapp.songsbook.ui.theme.DifficultyIntermediateDark
import com.guitarapp.songsbook.ui.theme.DifficultyIntermediateLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSongClick: (String) -> Unit,
    onAddSongClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guitar Songbook") },
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
            FloatingActionButton(onClick = onAddSongClick) {
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
                else -> SearchableSongList(uiState, viewModel, onSongClick)
            }
        }
    }
}

@Composable
private fun SearchableSongList(
    uiState: HomeUiState,
    viewModel: HomeViewModel,
    onSongClick: (String) -> Unit
) {
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
            EmptyContent()
        } else {
            SongListContent(uiState.songs, onSongClick, viewModel::toggleFavorite)
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
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No songs found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SongListContent(
    songs: List<Song>,
    onSongClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Box(modifier = Modifier.padding(top = 4.dp)) }
        items(songs) { song ->
            SongCard(song, onSongClick, onFavoriteClick)
        }
        item { Box(modifier = Modifier.padding(bottom = 8.dp)) }
    }
}

@Composable
private fun SongCard(
    song: Song,
    onSongClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song.id) },
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
                Text(
                    text = "Key: ${song.key}",
                    style = MaterialTheme.typography.bodySmall
                )
                DifficultyIndicator(song.difficulty)
            }
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