package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.presentation.viewmodel.SongDetailViewModel
import com.guitarapp.songsbook.utils.buildChordLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    viewModel: SongDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.song?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
                    SongContent(uiState.song!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongContent(song: Song) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = song.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailLabel("Key: ${song.key}")
            if (song.capo > 0) DetailLabel("Capo: ${song.capo}")
            DetailLabel(song.difficulty)
        }

        // Chords
        FlowRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            song.chords.forEach { chord ->
                AssistChip(
                    onClick = { },
                    label = { Text(chord) }
                )
            }
        }

        // Notes
        if (song.notes.isNotBlank()) {
            Text(
                text = song.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // Sections
        song.content.forEach { section ->
            SongSectionContent(section)
        }
    }
}

@Composable
private fun SongSectionContent(section: SongSection) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = "${section.type.replaceFirstChar { it.uppercase() }} ${section.number}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        section.lines.forEach { line ->
            SongLineContent(line)
        }
    }
}

@Composable
private fun SongLineContent(line: SongLine) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        if (line.chords.isNotEmpty()) {
            Text(
                text = buildChordLine(line),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 18.sp
            )
        }
        Text(
            text = line.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.secondary
    )
}
/**
La función más interesante aquí es `buildChordLine`.
Lo que hace es posicionar los acordes **sobre** el texto de la canción usando
las posiciones que definimos en el JSON.
Crea un array de caracteres del tamaño de la línea, lo llena con espacios,
y luego "escribe" cada acorde en su posición. Así cuando ves:

Em              G
So, so you think you can tell **/