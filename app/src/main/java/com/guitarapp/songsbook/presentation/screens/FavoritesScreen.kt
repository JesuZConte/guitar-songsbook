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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.statusBarsPadding
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.presentation.viewmodel.FavoritesViewModel
import com.guitarapp.songsbook.ui.components.LeatherHeader
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onSongClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            LeatherHeader(
                title = stringResource(R.string.favorites_title),
                modifier = Modifier.statusBarsPadding(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    uiState.error != null -> Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    uiState.favorites.isEmpty() -> EmptyFavoritesContent()
                    else -> FavoritesList(uiState.favorites, onSongClick, viewModel::removeFavorite)
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    val c = LocalLeatherColors.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = c.rule,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.favorites_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = c.ink,
            )
            Text(
                text = stringResource(R.string.favorites_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = c.inkSoft,
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Song>,
    onSongClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Box(modifier = Modifier.padding(top = 8.dp)) }
        items(favorites) { song ->
            FavoriteSongRow(song, onSongClick, onRemoveFavorite)
        }
        item { Box(modifier = Modifier.padding(bottom = 8.dp)) }
    }
}

@Composable
private fun FavoriteSongRow(
    song: Song,
    onSongClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    val c = LocalLeatherColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onSongClick(song.id) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = song.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.section,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.reader_key_label, song.key),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = c.inkSoft,
                )
            }
        }
        IconButton(onClick = { onRemoveFavorite(song.id) }) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = c.accent,
            )
        }
    }
}
