package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongReaderScreen(
    viewModel: ReaderViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlistsState by playlistsViewModel.uiState.collectAsState()
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) onDeleteSuccess()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.song?.title ?: "Loading...",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (uiState.song != null) {
                                Text(
                                    text = uiState.song!!.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (uiState.song != null) {
                            IconButton(onClick = onEditClick) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit song")
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete song")
                            }
                            IconButton(onClick = {
                                playlistsViewModel.loadPlaylists()
                                showPlaylistPicker = true
                            }) {
                                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to playlist")
                            }
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (uiState.song!!.isFavorite) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Toggle favorite",
                                    tint = if (uiState.song!!.isFavorite) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ReaderBottomBar(
                    currentPage = uiState.currentPage,
                    totalPages = uiState.totalPages,
                    fontSize = uiState.fontSize,
                    onIncreaseFontSize = viewModel::increaseFontSize,
                    onDecreaseFontSize = viewModel::decreaseFontSize,
                    onToggleFullscreen = viewModel::toggleFullscreen
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.song != null -> {
                    VirtualPagedSong(
                        song = uiState.song!!,
                        fontSize = uiState.fontSize,
                        currentPage = uiState.currentPage,
                        onPageChanged = viewModel::onPageChanged,
                        onPageCountMeasured = viewModel::onMeasuredPageCount,
                        onTap = viewModel::toggleFullscreen,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showDeleteConfirm && uiState.song != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete song") },
                text = { Text("Delete \"${uiState.song!!.title}\"? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteSong()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }

        if (showPlaylistPicker && uiState.song != null) {
            PlaylistPickerDialog(
                playlists = playlistsState.playlists,
                onPlaylistSelected = { playlistId ->
                    playlistsViewModel.addSongToPlaylist(playlistId, uiState.song!!.id)
                    showPlaylistPicker = false
                },
                onDismiss = { showPlaylistPicker = false }
            )
        }
    }
}

@Composable
private fun PlaylistPickerDialog(
    playlists: List<Playlist>,
    onPlaylistSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = "No playlists yet. Create one from the Playlists tab.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(playlists, key = { it.id }) { playlist ->
                        Text(
                            text = "${playlist.name} (${playlist.songCount})",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistSelected(playlist.id) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    fontSize: Int,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecreaseFontSize) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease font")
            }
            Text(text = "${fontSize}sp", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = onIncreaseFontSize) {
                Icon(Icons.Filled.Add, contentDescription = "Increase font")
            }
            Text(
                text = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onToggleFullscreen) {
                Icon(Icons.Filled.Fullscreen, contentDescription = "Toggle fullscreen")
            }
        }
    }
}
