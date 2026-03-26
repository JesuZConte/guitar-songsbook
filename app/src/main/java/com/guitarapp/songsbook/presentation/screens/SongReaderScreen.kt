package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.utils.buildChordLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                uiState.pages.isNotEmpty() -> {
                    SongPager(
                        pages = uiState.pages,
                        fontSize = uiState.fontSize,
                        currentPage = uiState.currentPage,
                        isFullscreen = uiState.isFullscreen,
                        onPageChanged = viewModel::onPageChanged,
                        onToggleFullscreen = viewModel::toggleFullscreen,
                        song = uiState.song
                    )
                }
            }
        }
    }
}

@Composable
private fun SongPager(
    pages: List<List<SongSection>>,
    fontSize: Int,
    currentPage: Int,
    isFullscreen: Boolean,
    onPageChanged: (Int) -> Unit,
    onToggleFullscreen: () -> Unit,
    song: Song?
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pages.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        PageContent(
            sections = pages[pageIndex],
            fontSize = fontSize,
            pageNumber = pageIndex + 1,
            totalPages = pages.size,
            isFullscreen = isFullscreen,
            onTap = onToggleFullscreen,
            song = song
        )
    }
}

@Composable
private fun PageContent(
    sections: List<SongSection>,
    fontSize: Int,
    pageNumber: Int,
    totalPages: Int,
    isFullscreen: Boolean,
    onTap: () -> Unit,
    song: Song?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onTap() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (pageNumber == 1 && song != null) {
                SongHeader(song, fontSize)
            }

            sections.forEach { section ->
                SectionContent(section, fontSize)
            }
        }

        if (isFullscreen) {
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
}

@Composable
private fun SongHeader(song: Song, fontSize: Int) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = song.title,
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
            Text(
                text = "Key: ${song.key}",
                fontSize = (fontSize - 2).sp,
                color = MaterialTheme.colorScheme.secondary
            )
            if (song.capo > 0) {
                Text(
                    text = "Capo: ${song.capo}",
                    fontSize = (fontSize - 2).sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        if (song.notes.isNotBlank()) {
            Text(
                text = song.notes,
                fontSize = (fontSize - 2).sp,
                color = MaterialTheme.colorScheme.outline,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun SectionContent(section: SongSection, fontSize: Int) {
    val sectionColor = getSectionColor(section.type)

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = "${section.type.replaceFirstChar { it.uppercase() }} ${section.number}",
            fontSize = (fontSize - 2).sp,
            fontWeight = FontWeight.Bold,
            color = sectionColor,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        section.lines.forEach { line ->
            LineContent(line, fontSize)
        }
    }
}

@Composable
private fun getSectionColor(type: String): Color {
    return when (type.lowercase()) {
        "chorus" -> MaterialTheme.colorScheme.primary
        "verse" -> MaterialTheme.colorScheme.tertiary
        "intro", "outro" -> MaterialTheme.colorScheme.secondary
        "bridge" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun LineContent(line: SongLine, fontSize: Int) {
    Column(modifier = Modifier.padding(bottom = 2.dp)) {
        if (line.chords.isNotEmpty()) {
            Text(
                text = buildChordLine(line),
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                lineHeight = (fontSize + 4).sp
            )
        }
        Text(
            text = line.text,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 6).sp
        )
    }
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
            // Font size controls
            IconButton(onClick = onDecreaseFontSize) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease font")
            }
            Text(
                text = "${fontSize}sp",
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onIncreaseFontSize) {
                Icon(Icons.Filled.Add, contentDescription = "Increase font")
            }

            // Page indicator
            Text(
                text = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Fullscreen toggle
            IconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (false) Icons.Filled.FullscreenExit
                    else Icons.Filled.Fullscreen,
                    contentDescription = "Toggle fullscreen"
                )
            }
        }
    }
}