package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playlists_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
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
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.playlists.isEmpty() -> {
                    EmptyPlaylistsContent()
                }
                else -> {
                    PlaylistList(
                        playlists = uiState.playlists,
                        onPlaylistClick = onPlaylistClick,
                        onDeletePlaylist = viewModel::deletePlaylist
                    )
                }
            }
        }

        if (showCreateDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    viewModel.createPlaylist(name)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun EmptyPlaylistsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.playlists_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.playlists_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Box(modifier = Modifier.padding(top = 8.dp)) }
        items(playlists, key = { it.id }) { playlist ->
            PlaylistCard(playlist, onPlaylistClick, onDeletePlaylist)
        }
        item { Box(modifier = Modifier.padding(bottom = 8.dp)) }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onPlaylistClick: (Long) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaylistClick(playlist.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.playlists_song_count,
                        playlist.songCount,
                        playlist.songCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.playlists_new_dialog_title)) },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text(stringResource(R.string.playlists_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(playlistName) },
                enabled = playlistName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
