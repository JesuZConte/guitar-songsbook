package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.ui.theme.ChordColorDark
import com.guitarapp.songsbook.ui.theme.ChordColorLight
import com.guitarapp.songsbook.ui.theme.Merriweather
import com.guitarapp.songsbook.utils.ChordNotation
import com.guitarapp.songsbook.utils.buildChordLine
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Pixels reserved at the bottom of every page for the "1 / 2" indicator.
 * Must be large enough to contain bodySmall text (≈12sp) + line height + 8dp bottom padding.
 */
private const val PAGE_INDICATOR_DP = 48f

/** The HorizontalPager slot in the SubcomposeLayout. */
private enum class SongContentSlot { Pager }

// ─────────────────────────────────────────────────────────────────────────────
// Public entry point
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Kindle-style reader: measures the actual rendered height of each content item
 * to compute page break positions that snap to line boundaries.
 *
 * No estimation — font size changes auto-reflow the pages with pixel-perfect breaks.
 *
 * @param onPageCountMeasured called after every layout pass with the new total.
 * @param onTap called on a short tap (used to toggle fullscreen).
 */
@Composable
internal fun VirtualPagedSong(
    song: Song,
    fontSize: Int,
    transposeSteps: Int = 0,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    onPageCountMeasured: (Int) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val indicatorPx = (PAGE_INDICATOR_DP * density).roundToInt()
        val viewportH = constraints.maxHeight
        val effectiveViewportH = (viewportH - indicatorPx).coerceAtLeast(1)

        // FullSongColumn has padding(horizontal = 16.dp, vertical = 12.dp).
        // Use matching constraints for per-item measurements so text wraps identically.
        val hPaddingPx = (32 * density).roundToInt()   // 16dp × 2
        val topPaddingPx = (12 * density).roundToInt() // same value used for bottom padding
        val sectionBottomPx = (16 * density).roundToInt()

        val itemConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxWidth = (constraints.maxWidth - hPaddingPx).coerceAtLeast(1),
            maxHeight = Int.MAX_VALUE
        )

        // ── Pass 1: measure every item to find valid line-boundary break points ──

        val headerH = subcompose("h") {
            SongHeader(song = song, fontSize = fontSize, transposeSteps = transposeSteps)
        }.first().measure(itemConstraints).height

        var cumH = topPaddingPx + headerH

        // lineBreaks: content-pixel offsets where a page break won't cut a line
        val lineBreaks = mutableListOf<Int>()

        song.content.forEachIndexed { si, section ->
            // A break before a section header is acceptable (header + 1st line stay together)
            lineBreaks.add(cumH)

            val secHeaderH = subcompose("sh$si") {
                SectionHeaderText(section = section, fontSize = fontSize)
            }.first().measure(itemConstraints).height

            cumH += secHeaderH

            section.lines.forEachIndexed { li, line ->
                // Allow break between lines, but NOT between the header and its 1st line
                if (li > 0) lineBreaks.add(cumH)

                val lineH = subcompose("l${si}_$li") {
                    LineContent(line = line, fontSize = fontSize, transposeSteps = transposeSteps)
                }.first().measure(itemConstraints).height

                cumH += lineH
            }

            cumH += sectionBottomPx
            lineBreaks.add(cumH) // break is clean right after a complete section
        }

        val totalHeight = cumH + topPaddingPx // account for bottom padding

        // ── Compute page starts: snap each target break to the nearest valid boundary ──
        val pageStarts = mutableListOf(0)
        var targetBreak = effectiveViewportH

        while (targetBreak < totalHeight) {
            // Latest valid break that is strictly after the previous page start
            // and at or before the target pixel boundary.
            val snapped = lineBreaks
                .filter { it in (pageStarts.last() + 1)..targetBreak }
                .maxOrNull()
                ?: targetBreak  // fallback: pixel-exact break (rare, very long single item)

            pageStarts.add(snapped)
            targetBreak = snapped + effectiveViewportH
        }

        // Drop the last page if its only content is the bottom padding (invisible empty page).
        if (pageStarts.size > 1 && totalHeight - pageStarts.last() <= topPaddingPx) {
            pageStarts.removeAt(pageStarts.lastIndex)
        }

        val pageCount = pageStarts.size

        // ── Pass 2: render pager using the computed page starts ───────────────
        val pagerPlaceable = subcompose(SongContentSlot.Pager) {
            LaunchedEffect(pageCount) { onPageCountMeasured(pageCount) }

            val scope = rememberCoroutineScope()
            var pagerWidthPx by remember { mutableIntStateOf(0) }

            val pagerState = rememberPagerState(
                initialPage = currentPage.coerceIn(0, pageCount - 1),
                pageCount = { pageCount }
            )

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect(onPageChanged)
            }

            // Keep pager in sync when a font-size change reduces the page count
            LaunchedEffect(currentPage, pageCount) {
                if (pagerState.currentPage != currentPage && currentPage < pageCount) {
                    pagerState.scrollToPage(currentPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { pagerWidthPx = it.width }
                    .pointerInput(pageCount) {
                        detectTapGestures { offset ->
                            when {
                                offset.x < pagerWidthPx / 3f -> {
                                    // Left third → previous page
                                    if (pagerState.currentPage > 0) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }
                                offset.x > pagerWidthPx * 2f / 3f -> {
                                    // Right third → next page
                                    if (pagerState.currentPage < pageCount - 1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    }
                                }
                                else -> onTap() // Centre third → toggle fullscreen
                            }
                        }
                    }
            ) { pageIndex ->
                // Clip exactly to the distance between this page's start and the next,
                // so no line is ever cut at the bottom. Any remaining space (last page or
                // a page whose content is shorter than effectiveViewportH) stays blank.
                val contentHeightPx = if (pageIndex + 1 < pageStarts.size)
                    pageStarts[pageIndex + 1] - pageStarts[pageIndex]
                else
                    (totalHeight - pageStarts[pageIndex]).coerceAtLeast(1)

                Box(modifier = Modifier.fillMaxSize()) {
                    PageSlice(
                        contentOffsetPx = pageStarts[pageIndex],
                        contentHeightPx = contentHeightPx
                    ) {
                        FullSongColumn(song = song, fontSize = fontSize, transposeSteps = transposeSteps)
                    }

                    if (pageCount > 1) {
                        Text(
                            text = "${pageIndex + 1} / $pageCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
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

// ─────────────────────────────────────────────────────────────────────────────
// Layout primitives
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Clips the full song content to exactly [contentHeightPx] pixels starting at
 * [contentOffsetPx]. Because [contentHeightPx] equals the distance to the next
 * page's start (a line boundary), no line is ever split at the bottom of a page.
 */
@Composable
private fun PageSlice(
    contentOffsetPx: Int,
    contentHeightPx: Int,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            constraints.copy(maxHeight = Int.MAX_VALUE)
        )
        layout(constraints.maxWidth, contentHeightPx) {
            placeable.placeRelative(x = 0, y = -contentOffsetPx)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Song content composables (shared between Reader and Preview)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The entire song as one continuous column — measured for pagination
 * and rendered (clipped) inside each PageSlice.
 */
@Composable
internal fun FullSongColumn(song: Song, fontSize: Int, transposeSteps: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SongHeader(song = song, fontSize = fontSize, transposeSteps = transposeSteps)
        song.content.forEach { section ->
            SectionContent(section = section, fontSize = fontSize, transposeSteps = transposeSteps)
        }
    }
}

@Composable
internal fun SongHeader(song: Song, fontSize: Int, transposeSteps: Int = 0) {
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
                val displayKey = ChordNotation.convert(
                    ChordNotation.transpose(song.key, transposeSteps),
                    UserPreferences.getNotation(LocalContext.current)
                )
                Text(
                    text = "Key: $displayKey",
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
internal fun SectionContent(section: SongSection, fontSize: Int, transposeSteps: Int = 0) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        SectionHeaderText(section = section, fontSize = fontSize)
        section.lines.forEach { line ->
            LineContent(line = line, fontSize = fontSize, transposeSteps = transposeSteps)
        }
    }
}

/** Section label only — measured individually for line-aware page break computation. */
@Composable
private fun SectionHeaderText(section: SongSection, fontSize: Int) {
    Text(
        text = "${section.type.replaceFirstChar { it.uppercase() }} ${section.number}",
        fontSize = (fontSize - 2).sp,
        fontWeight = FontWeight.Bold,
        color = sectionColor(section.type),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
internal fun LineContent(line: SongLine, fontSize: Int, transposeSteps: Int = 0) {
    val chordColor = if (isSystemInDarkTheme()) ChordColorDark else ChordColorLight
    val notation = UserPreferences.getNotation(LocalContext.current)

    val displayLine = if (transposeSteps != 0) {
        line.copy(chords = line.chords.map {
            it.copy(chord = ChordNotation.transpose(it.chord, transposeSteps))
        })
    } else line

    Column(modifier = Modifier.padding(bottom = 2.dp)) {
        if (line.chords.isNotEmpty()) {
            Text(
                text = buildChordLine(displayLine, notation),
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

@Composable
internal fun sectionColor(type: String): Color = when (type.lowercase()) {
    "chorus" -> MaterialTheme.colorScheme.primary
    "verse" -> MaterialTheme.colorScheme.tertiary
    "intro", "outro" -> MaterialTheme.colorScheme.secondary
    "bridge" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
