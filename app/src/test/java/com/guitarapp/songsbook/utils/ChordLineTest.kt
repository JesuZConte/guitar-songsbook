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
}