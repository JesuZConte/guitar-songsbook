package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.utils.buildChordLine
import org.junit.Assert.assertEquals
import org.junit.Test

class ChordLineTest {

    @Test
    fun `single chord at start`() {
        val line = SongLine(
            text = "Hello world",
            chords = listOf(ChordPosition("G", 0))
        )
        assertEquals("G", buildChordLine(line))
    }

    @Test
    fun `two chords spaced apart`() {
        val line = SongLine(
            text = "So, so you think you can tell",
            chords = listOf(
                ChordPosition("Em", 0),
                ChordPosition("G", 18)
            )
        )
        val result = buildChordLine(line)
        assertEquals("Em", result.substring(0, 2))
        assertEquals("G", result.substring(18, 19))
    }

    @Test
    fun `empty chords returns empty string`() {
        val line = SongLine(text = "No chords here", chords = emptyList())
        assertEquals("", buildChordLine(line))
    }

    @Test
    fun `chord positions do not overlap`() {
        val line = SongLine(
            text = "Short text with chords",
            chords = listOf(
                ChordPosition("Am", 0),
                ChordPosition("F", 5)
            )
        )
        val result = buildChordLine(line)
        assertEquals('A', result[0])
        assertEquals('m', result[1])
        assertEquals('F', result[5])
    }

    @Test
    fun `chord extends beyond text length`() {
        val line = SongLine(
            text = "Hi",
            chords = listOf(ChordPosition("G", 5))
        )
        val result = buildChordLine(line)
        assertEquals("G", result.trim())
    }

    @Test
    fun `chord-only line with overlapping positions renders without collision`() {
        // [Dm7] [G] [Dm7] [G] parses to positions 0, 1, 2, 3 (one space each between brackets)
        // Dm7 is 3 chars wide so positions 1 and 2 are inside it — they must be pushed right
        val line = SongLine(
            text = "   ",
            chords = listOf(
                ChordPosition("Dm7", 0),
                ChordPosition("G",   1),
                ChordPosition("Dm7", 2),
                ChordPosition("G",   3)
            )
        )
        val result = buildChordLine(line)
        // All four chord names must appear exactly once, none merged
        val parts = result.trim().split(Regex("\\s+"))
        assertEquals(listOf("Dm7", "G", "Dm7", "G"), parts)
    }

    @Test
    fun `adjacent chords with different lengths do not merge`() {
        // Cmaj7 (5 chars) followed immediately by Am (2 chars) at position 1
        val line = SongLine(
            text = " ",
            chords = listOf(
                ChordPosition("Cmaj7", 0),
                ChordPosition("Am",    1)
            )
        )
        val result = buildChordLine(line)
        val parts = result.trim().split(Regex("\\s+"))
        assertEquals(listOf("Cmaj7", "Am"), parts)
    }
}