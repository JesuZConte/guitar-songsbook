package com.guitarapp.songsbook.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.presentation.viewmodel.PlaylistsViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetlistScreen(
    playlistId: Long,
    viewModel: PlaylistsViewModel,
    onExit: () -> Unit
) {
    val detailState by viewModel.detailState.collectAsState()
    val songs = detailState.songs

    var currentSongIndex by remember { mutableIntStateOf(0) }
    val currentSongTitle = songs.getOrNull(currentSongIndex)?.title ?: ""
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistDetail(playlistId)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentSongTitle,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    actions = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.setlist_exit))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (songs.isNotEmpty()) {
                VirtualSetlist(
                    songs = songs,
                    fontSize = 14,
                    onCurrentSongChanged = { currentSongIndex = it },
                    onTap = { isFullscreen = !isFullscreen },
                    onExit = onExit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VirtualSetlist — multi-song continuous pager
// ─────────────────────────────────────────────────────────────────────────────

private const val SETLIST_PAGE_INDICATOR_DP = 48f

private enum class SetlistSlot { Pager }

/**
 * Renders all songs as a continuous paged document.
 * Each song is measured independently; its pages are concatenated into a flat
 * page index. The last entry is a dedicated end-of-setlist page.
 *
 * Page breaks never split a line (same algorithm as VirtualPagedSong).
 * No nested pagers — one HorizontalPager for the entire setlist.
 */
@Composable
private fun VirtualSetlist(
    songs: List<Song>,
    fontSize: Int,
    onCurrentSongChanged: (Int) -> Unit,
    onTap: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    SubcomposeLayout(modifier = modifier) { constraints ->
        val density = this.density
        val indicatorPx = (SETLIST_PAGE_INDICATOR_DP * density).roundToInt()
        val viewportH = constraints.maxHeight
        val effectiveViewportH = (viewportH - indicatorPx).coerceAtLeast(1)

        val hPaddingPx = (32 * density).roundToInt()
        val topPaddingPx = (12 * density).roundToInt()
        val sectionBottomPx = (16 * density).roundToInt()

        val itemConstraints = constraints.copy(
            minWidth = 0, minHeight = 0,
            maxWidth = (constraints.maxWidth - hPaddingPx).coerceAtLeast(1),
            maxHeight = Int.MAX_VALUE
        )

        // ── Measure each song and compute its page starts ──
        data class SongPages(val songIndex: Int, val pageStarts: List<Int>, val totalHeight: Int)

        val songPages = songs.mapIndexed { si, song ->
            val headerH = subcompose("h$si") {
                SongHeader(song = song, fontSize = fontSize)
            }.first().measure(itemConstraints).height

            var cumH = topPaddingPx + headerH
            val lineBreaks = mutableListOf<Int>()

            song.content.forEachIndexed { secIdx, section ->
                lineBreaks.add(cumH)
                val secHeaderH = subcompose("sh${si}_$secIdx") {
                    SectionHeaderText(section = section, fontSize = fontSize)
                }.first().measure(itemConstraints).height
                cumH += secHeaderH

                section.lines.forEachIndexed { li, line ->
                    if (li > 0) lineBreaks.add(cumH)
                    val lineH = subcompose("l${si}_${secIdx}_$li") {
                        LineContent(line = line, transposeSteps = 0)
                    }.first().measure(itemConstraints).height
                    cumH += lineH
                }
                cumH += sectionBottomPx
                lineBreaks.add(cumH)
            }

            val totalH = cumH + topPaddingPx

            val pageStarts = mutableListOf(0)
            var target = effectiveViewportH
            while (target < totalH) {
                val snapped = lineBreaks
                    .filter { it in (pageStarts.last() + 1)..target }
                    .maxOrNull() ?: target
                pageStarts.add(snapped)
                target = snapped + effectiveViewportH
            }
            if (pageStarts.size > 1 && totalH - pageStarts.last() <= topPaddingPx) {
                pageStarts.removeAt(pageStarts.lastIndex)
            }

            SongPages(si, pageStarts, totalH)
        }

        // ── Build flat page table: (songIndex, pageStartOffset) ──
        data class PageEntry(val songIndex: Int, val startOffset: Int, val songTotalHeight: Int, val pageStarts: List<Int>)

        val pages = mutableListOf<PageEntry>()
        songPages.forEach { sp ->
            sp.pageStarts.forEach { start ->
                pages.add(PageEntry(sp.songIndex, start, sp.totalHeight, sp.pageStarts))
            }
        }
        val totalPages = pages.size + 1 // +1 for end screen

        // ── Pass 2: render the pager ──
        val pagerPlaceable = subcompose(SetlistSlot.Pager) {
            val pagerState = rememberPagerState(pageCount = { totalPages })

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    val songIndex = pages.getOrNull(page)?.songIndex ?: (songs.size - 1)
                    onCurrentSongChanged(songIndex)
                }
            }

            var pagerWidthPx by remember { mutableIntStateOf(0) }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { pagerWidthPx = it.width }
                    .pointerInput(totalPages) {
                        detectTapGestures { offset ->
                            when {
                                offset.x < pagerWidthPx / 3f -> {
                                    if (pagerState.currentPage > 0)
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                }
                                offset.x > pagerWidthPx * 2f / 3f -> {
                                    if (pagerState.currentPage < totalPages - 1)
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                                else -> onTap()
                            }
                        }
                    }
            ) { pageIndex ->
                if (pageIndex >= pages.size) {
                    // End-of-setlist page
                    SetlistEndPage(
                        onRestart = { scope.launch { pagerState.scrollToPage(0) } },
                        onExit = onExit
                    )
                } else {
                    val entry = pages[pageIndex]
                    val contentHeightPx = if (pageIndex + 1 < pages.size &&
                        pages[pageIndex + 1].songIndex == entry.songIndex) {
                        pages[pageIndex + 1].startOffset - entry.startOffset
                    } else {
                        (entry.songTotalHeight - entry.startOffset).coerceAtLeast(1)
                    }

                    val song = songs[entry.songIndex]
                    val isLastPageOfSong = pageIndex + 1 >= pages.size ||
                            pages[pageIndex + 1].songIndex != entry.songIndex
                    val songPageIndex = entry.pageStarts.indexOf(entry.startOffset)
                    val songPageCount = entry.pageStarts.size

                    Box(modifier = Modifier.fillMaxSize()) {
                        Layout(
                            content = {
                                FullSongColumn(song = song, fontSize = fontSize)
                            },
                            modifier = Modifier.fillMaxSize().clipToBounds()
                        ) { measurables, layoutConstraints ->
                            val placeable = measurables.first().measure(
                                layoutConstraints.copy(minHeight = 0, maxHeight = Int.MAX_VALUE)
                            )
                            layout(layoutConstraints.maxWidth, contentHeightPx) {
                                placeable.placeRelative(0, -entry.startOffset)
                            }
                        }

                        // Page indicator: shows position within song + song progress
                        val nextSong = if (isLastPageOfSong)
                            songs.getOrNull(entry.songIndex + 1)?.title else null
                        SetlistPageIndicator(
                            songPage = songPageIndex + 1,
                            songPageCount = songPageCount,
                            nextSongTitle = nextSong,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }.first().measure(constraints)

        layout(constraints.maxWidth, viewportH) {
            pagerPlaceable.placeRelative(0, 0)
        }
    }
}

@Composable
private fun SetlistPageIndicator(
    songPage: Int,
    songPageCount: Int,
    nextSongTitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (nextSongTitle != null) {
            Text(
                text = stringResource(R.string.setlist_next_song, nextSongTitle),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
        if (songPageCount > 1) {
            Text(
                text = "$songPage / $songPageCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SetlistEndPage(
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setlist_end_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.setlist_end_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart) {
            Icon(Icons.Filled.Replay, contentDescription = null)
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text(stringResource(R.string.setlist_restart_button))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onExit) {
            Text(stringResource(R.string.setlist_exit_to_home))
        }
    }
}
