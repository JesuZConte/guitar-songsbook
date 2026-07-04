package com.guitarapp.songsbook.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.ui.components.ChordLine
import com.guitarapp.songsbook.ui.components.ChordPlacement
import com.guitarapp.songsbook.ui.theme.LeatherColors
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import com.guitarapp.songsbook.ui.theme.Merriweather
import com.guitarapp.songsbook.utils.ChordNotation
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Pixels reserved at the bottom of every page for the "1 / 2" indicator.
 * Must be large enough to contain bodySmall text (≈12sp) + line height + 8dp bottom padding.
 */
private const val PAGE_INDICATOR_DP = 48f

internal val LocalNocturnoMode = compositionLocalOf { false }

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

            val c = LocalLeatherColors.current

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { pagerWidthPx = it.width }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(pageCount) {
                            detectTapGestures { offset ->
                                when {
                                    offset.x < pagerWidthPx / 3f -> {
                                        if (pagerState.currentPage > 0) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    }
                                    offset.x > pagerWidthPx * 2f / 3f -> {
                                        if (pagerState.currentPage < pageCount - 1) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    }
                                    else -> onTap()
                                }
                            }
                        }
                ) { pageIndex ->
                    val contentHeightPx = if (pageIndex + 1 < pageStarts.size)
                        pageStarts[pageIndex + 1] - pageStarts[pageIndex]
                    else
                        (totalHeight - pageStarts[pageIndex]).coerceAtLeast(1)

                    PageSlice(
                        contentOffsetPx = pageStarts[pageIndex],
                        contentHeightPx = contentHeightPx
                    ) {
                        FullSongColumn(song = song, fontSize = fontSize, transposeSteps = transposeSteps)
                    }
                }

                PageSeamOverlay(pagerState = pagerState, pagerWidthPx = pagerWidthPx, c = c)

                if (pageCount > 1) {
                    PageIndicator(
                        pagerState = pagerState,
                        pageCount = pageCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }.first().measure(constraints)

        layout(constraints.maxWidth, viewportH) {
            pagerPlaceable.placeRelative(0, 0)
        }
    }
}

// pagerState.currentPageOffsetFraction changes on every frame of a swipe, so it
// must only be read inside these leaf composables: reading it in VirtualPagedSong's
// scope would recompose the whole pager (all PageSlices) per frame.

/** Leather seam strip + shadow at the physical boundary between pages during swipe. */
@Composable
private fun PageSeamOverlay(pagerState: PagerState, pagerWidthPx: Int, c: LeatherColors) {
    val offsetFraction = pagerState.currentPageOffsetFraction
    if (abs(offsetFraction) <= 0.005f || pagerWidthPx <= 0) return

    val seamX = if (offsetFraction >= 0f)
        ((1f - offsetFraction) * pagerWidthPx).roundToInt()
    else
        ((-offsetFraction) * pagerWidthPx).roundToInt()

    // Soft shadow on the incoming page's left edge
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp)
            .offset { IntOffset(x = seamX - 24.dp.roundToPx(), y = 0) }
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        c.leatherDeep.copy(alpha = 0.20f)
                    )
                )
            )
    )

    // Seam strip (leather spine colour)
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(3.dp)
            .offset { IntOffset(x = seamX, y = 0) }
            .background(
                Brush.horizontalGradient(
                    listOf(
                        c.leatherDeep.copy(alpha = 0.53f),
                        c.leatherMid.copy(alpha = 0.40f),
                        c.leatherDeep.copy(alpha = 0.53f)
                    )
                )
            )
    )
}

@Composable
private fun PageIndicator(pagerState: PagerState, pageCount: Int, modifier: Modifier = Modifier) {
    val offsetFraction = pagerState.currentPageOffsetFraction
    val indicatorText = if (abs(offsetFraction) > 0.005f) {
        val left = if (offsetFraction >= 0f) pagerState.currentPage + 1 else pagerState.currentPage
        val right = left + 1
        "$left ↔ $right"
    } else {
        "${pagerState.currentPage + 1} / $pageCount"
    }
    Text(
        text = indicatorText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = song.artist,
            fontSize = 15.sp,
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
                    text = stringResource(R.string.reader_key_label, displayKey),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (song.capo > 0) {
                Text(
                    text = stringResource(R.string.reader_capo_label, song.capo),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        if (song.notes.isNotBlank()) {
            Text(
                text = song.notes,
                fontSize = 12.sp,
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

@Composable
internal fun SectionHeaderText(section: SongSection, fontSize: Int) {
    Text(
        text = "${sectionTypeLabel(section.type)} ${section.number}",
        fontSize = (fontSize - 2).sp,
        fontWeight = FontWeight.Bold,
        color = sectionColor(section.type),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
internal fun LineContent(line: SongLine, fontSize: Int, transposeSteps: Int = 0) {
    val notation = UserPreferences.getNotation(LocalContext.current)
    val chordPlacements = line.chords.map { cp ->
        ChordPlacement(
            chord = ChordNotation.convert(ChordNotation.transpose(cp.chord, transposeSteps), notation),
            column = cp.position
        )
    }
    ChordLine(lyric = line.text, chords = chordPlacements, fontSize = fontSize)
}

@Composable
internal fun sectionTypeLabel(type: String): String = when (type.lowercase()) {
    "verse" -> stringResource(R.string.section_verse)
    "chorus" -> stringResource(R.string.section_chorus)
    "bridge" -> stringResource(R.string.section_bridge)
    "intro" -> stringResource(R.string.section_intro)
    "outro" -> stringResource(R.string.section_outro)
    "pre-chorus" -> stringResource(R.string.section_pre_chorus)
    "solo" -> stringResource(R.string.section_solo)
    else -> type.replaceFirstChar { it.uppercase() }
}

@Composable
internal fun sectionColor(type: String): Color {
    val c = LocalLeatherColors.current
    return when (type.lowercase()) {
        "chorus" -> MaterialTheme.colorScheme.primary
        "verse" -> c.ink
        "intro", "outro" -> MaterialTheme.colorScheme.secondary
        "bridge" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
