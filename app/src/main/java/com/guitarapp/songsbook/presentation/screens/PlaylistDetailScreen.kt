package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.guitarapp.songsbook.ui.components.BrassSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import com.guitarapp.songsbook.ui.components.LeatherHeader
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistsViewModel,
    onSongClick: (String) -> Unit,
    onStartSetlist: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val playlistId = detailState.playlist?.id ?: 0L

    Scaffold(
        floatingActionButton = {
            if (detailState.songs.isNotEmpty()) {
                BrassSurface(
                    shape = RoundedCornerShape(14.dp),
                    elevation = 6.dp,
                    modifier = Modifier.clickable { onStartSetlist(playlistId) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text(
                            text = stringResource(R.string.setlist_start_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            LeatherHeader(
                title = detailState.playlist?.name ?: stringResource(R.string.playlists_title),
                onBack = onBackClick,
                modifier = Modifier.statusBarsPadding(),
            )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            when {
                detailState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                detailState.error != null -> {
                    Text(
                        text = detailState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                detailState.songs.isEmpty() -> {
                    EmptyPlaylistDetailContent()
                }
                else -> {
                    ReorderableSongList(
                        songs = detailState.songs,
                        playlistId = playlistId,
                        onSongClick = onSongClick,
                        onRemoveSong = viewModel::removeSongFromPlaylist,
                        onReorder = { reordered -> viewModel.reorderSongs(playlistId, reordered) }
                    )
                }
            }
        }
        } // Column
    }
}

@Composable
private fun EmptyPlaylistDetailContent() {
    val c = LocalLeatherColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.playlist_detail_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = c.ink,
            )
            Text(
                text = stringResource(R.string.playlist_detail_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = c.inkSoft,
            )
        }
    }
}

@Composable
private fun ReorderableSongList(
    songs: List<Song>,
    playlistId: Long,
    onSongClick: (String) -> Unit,
    onRemoveSong: (Long, String) -> Unit,
    onReorder: (List<Song>) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val mutable = songs.toMutableList()
        mutable.add(to.index - 1, mutable.removeAt(from.index - 1)) // -1 for the top spacer item
        onReorder(mutable)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Box(modifier = Modifier.padding(top = 8.dp)) }
        items(songs, key = { it.id }) { song ->
            ReorderableItem(reorderState, key = song.id) { isDragging ->
                PlaylistSongCard(
                    song = song,
                    playlistId = playlistId,
                    isDragging = isDragging,
                    onSongClick = onSongClick,
                    onRemoveSong = onRemoveSong,
                    dragHandle = {
                        IconButton(
                            modifier = Modifier.draggableHandle(),
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.DragHandle, contentDescription = null,
                                tint = LocalLeatherColors.current.inkFaint)
                        }
                    }
                )
            }
        }
        item { Box(modifier = Modifier.padding(bottom = 80.dp)) } // space for FAB
    }
}

@Composable
private fun PlaylistSongCard(
    song: Song,
    playlistId: Long,
    isDragging: Boolean,
    onSongClick: (String) -> Unit,
    onRemoveSong: (Long, String) -> Unit,
    dragHandle: @Composable () -> Unit
) {
    val c = LocalLeatherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onSongClick(song.id) }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dragHandle()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.ink,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = c.inkSoft,
            )
        }
        IconButton(onClick = { onRemoveSong(playlistId, song.id) }) {
            Icon(
                imageVector = Icons.Filled.RemoveCircleOutline,
                contentDescription = null,
                tint = c.accent,
            )
        }
    }
}
