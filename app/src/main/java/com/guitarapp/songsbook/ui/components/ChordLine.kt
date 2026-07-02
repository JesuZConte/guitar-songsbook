package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guitarapp.songsbook.ui.theme.ChordLineStyle
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import com.guitarapp.songsbook.ui.theme.LyricLineStyle
import com.guitarapp.songsbook.utils.layoutChordRow

// =============================================================================
// ChordLine — one chord row + one lyric row, monospaced so chord positions
// align over the syllables they fall on.
//
// Input model:
//   - lyric: the full lyric line, e.g. "Amazing grace how sweet the sound"
//   - chords: list of (chord, columnIndex) pairs.
//             columnIndex = 0-based monospace cell where the chord starts.
//
// We build the chord row by padding with spaces up to each chord's column.
// (This matches the prototype's `Re` / `Sol` rendering.)
// =============================================================================

data class ChordPlacement(val chord: String, val column: Int)

/** Row-building logic used by [ChordLine], pulled out so it's unit-testable without Compose. */
fun buildChordRow(chords: List<ChordPlacement>): String =
    layoutChordRow(chords.map { it.chord to it.column })

@Composable
fun ChordLine(
    lyric: String,
    chords: List<ChordPlacement>,
    modifier: Modifier = Modifier,
    fontSize: Int = 13,
) {
    val c = LocalLeatherColors.current
    val chordRow = buildChordRow(chords)
    Column(modifier) {
        Text(text = chordRow, style = ChordLineStyle.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp), color = c.section)
        Text(text = lyric,    style = LyricLineStyle.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp), color = c.ink)
        Spacer(Modifier.height(4.dp))
    }
}
