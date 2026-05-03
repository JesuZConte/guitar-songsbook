package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.clip
import com.guitarapp.songsbook.ui.components.BrassFAB
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import com.guitarapp.songsbook.ui.components.LeatherHeader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsLandingScreen(
    playlistsViewModel: PlaylistsViewModel,
    onAllSongsClick: () -> Unit,
    onCollectionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by playlistsViewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            val c = LocalLeatherColors.current
            BrassFAB(modifier = Modifier.clickable { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = c.navyDeep)
            }
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            LeatherHeader(
                title = stringResource(R.string.home_title),
                subtitle = stringResource(R.string.home_subtitle),
                trailingIcon = Icons.Filled.Settings,
                onTrailingClick = onSettingsClick,
                modifier = Modifier.statusBarsPadding(),
            )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                AllSongsCard(onClick = onAllSongsClick)
            }

            if (uiState.playlists.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.collections_my_collections),
                        style = MaterialTheme.typography.titleSmall,
                        color = LocalLeatherColors.current.inkSoft,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(uiState.playlists, key = { it.id }) { playlist ->
                    CollectionCard(
                        playlist = playlist,
                        onClick = { onCollectionClick(playlist.id) },
                        onDelete = { playlistsViewModel.deletePlaylist(playlist.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        if (showCreateDialog) {
            CreateCollectionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    playlistsViewModel.createPlaylist(name)
                    showCreateDialog = false
                }
            )
        }
        } // Column
    }
}

@Composable
private fun AllSongsCard(onClick: () -> Unit) {
    val c = LocalLeatherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LibraryMusic,
            contentDescription = null,
            tint = c.section,
            modifier = Modifier.size(32.dp)
        )
        Column {
            Text(
                text = stringResource(R.string.collections_all_songs),
                style = MaterialTheme.typography.titleSmall,
                color = c.ink
            )
            Text(
                text = stringResource(R.string.collections_all_songs_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = c.inkSoft
            )
        }
    }
}

@Composable
private fun CollectionCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val c = LocalLeatherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                tint = c.section,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = c.ink
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.playlists_song_count,
                        playlist.songCount,
                        playlist.songCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.inkSoft
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = c.accent
            )
        }
    }
}

@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.playlists_new_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.playlists_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank()
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
