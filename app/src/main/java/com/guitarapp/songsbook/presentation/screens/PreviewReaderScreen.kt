package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.ui.components.LeatherHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewReaderScreen(
    song: Song,
    onBackClick: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(Modifier.fillMaxSize()) {
            LeatherHeader(
                title = song.title,
                subtitle = song.artist,
                onBack = onBackClick,
                leadingEmblem = false,
                modifier = Modifier.statusBarsPadding()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                FullSongColumn(song = song, fontSize = 14)
            }
        }
    }
}
