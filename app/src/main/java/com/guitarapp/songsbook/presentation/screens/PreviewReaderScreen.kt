package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.ui.theme.ChordColorDark
import com.guitarapp.songsbook.ui.theme.ChordColorLight
import com.guitarapp.songsbook.ui.theme.Merriweather
import com.guitarapp.songsbook.utils.buildChordLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewReaderScreen(
    song: Song,
    pages: List<List<SongSection>>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${song.title} — ${song.artist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val pagerState = rememberPagerState(pageCount = { pages.size })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                PreviewPageContent(
                    sections = pages[pageIndex],
                    pageNumber = pageIndex + 1,
                    totalPages = pages.size,
                    song = if (pageIndex == 0) song else null
                )
            }
        }
    }
}

@Composable
private fun PreviewPageContent(
    sections: List<SongSection>,
    pageNumber: Int,
    totalPages: Int,
    song: Song?
) {
    val fontSize = 14

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (song != null) {
                PreviewSongHeader(song, fontSize)
            }

            sections.forEach { section ->
                PreviewSectionContent(section, fontSize)
            }
        }

        Text(
            text = "$pageNumber / $totalPages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun PreviewSongHeader(song: Song, fontSize: Int) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = song.title,
            fontFamily = Merriweather,
            fontSize = (fontSize + 4).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = song.artist,
            fontSize = (fontSize + 1).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (song.key.isNotBlank()) {
                Text(
                    text = "Key: ${song.key}",
                    fontSize = (fontSize - 2).sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (song.capo > 0) {
                Text(
                    text = "Capo: ${song.capo}",
                    fontSize = (fontSize - 2).sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun PreviewSectionContent(section: SongSection, fontSize: Int) {
    val sectionColor = when (section.type.lowercase()) {
        "chorus" -> MaterialTheme.colorScheme.primary
        "verse" -> MaterialTheme.colorScheme.tertiary
        "intro", "outro" -> MaterialTheme.colorScheme.secondary
        "bridge" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "${section.type.replaceFirstChar { it.uppercase() }} ${section.number}",
            fontSize = (fontSize - 2).sp,
            fontWeight = FontWeight.Bold,
            color = sectionColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        section.lines.forEach { line ->
            PreviewLineContent(line, fontSize)
        }
    }
}

@Composable
private fun PreviewLineContent(line: SongLine, fontSize: Int) {
    val chordColor = if (isSystemInDarkTheme()) ChordColorDark else ChordColorLight
    val notation = UserPreferences.getNotation(LocalContext.current)

    Column(modifier = Modifier.padding(bottom = 2.dp)) {
        if (line.chords.isNotEmpty()) {
            Text(
                text = buildChordLine(line, notation),
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = chordColor,
                lineHeight = (fontSize + 4).sp
            )
        }
        if (line.text.isNotBlank()) {
            Text(
                text = line.text,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                lineHeight = (fontSize + 6).sp
            )
        }
    }
}
