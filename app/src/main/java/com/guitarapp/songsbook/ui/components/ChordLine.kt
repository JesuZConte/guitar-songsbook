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

@Composable
fun ChordLine(
    lyric: String,
    chords: List<ChordPlacement>,
    modifier: Modifier = Modifier,
    fontSize: Int = 13,
) {
    val c = LocalLeatherColors.current
    val chordRow = buildString {
        var cursor = 0
        for ((chord, pos) in chords) {
            while (cursor < pos) { append(' '); cursor++ }
            append(chord); cursor += chord.length
        }
    }
    Column(modifier) {
        Text(text = chordRow, style = ChordLineStyle.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp), color = c.section)
        Text(text = lyric,    style = LyricLineStyle.copy(fontSize = fontSize.sp, lineHeight = (fontSize + 4).sp), color = c.ink)
        Spacer(Modifier.height(4.dp))
    }
}
