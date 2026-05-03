package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongExporterTest {

    private fun makeSection(type: String, number: Int, vararg chordSets: List<String>): SongSection {
        val lines = chordSets.map { chords ->
            SongLine(
                text = "Some lyrics that must NOT appear in the export",
                chords = chords.mapIndexed { i, chord -> ChordPosition(chord, i * 4) }
            )
        }
        return SongSection(type = type, number = number, lines = lines)
    }

    private fun makeSong(
        title: String = "Amazing Grace",
        artist: String = "Traditional",
        key: String = "G",
        capo: Int = 0,
        chords: List<String> = listOf("G", "C", "D"),
        sections: List<SongSection> = listOf(
            makeSection("verse", 1, listOf("G", "C"), listOf("G", "D"))
        )
    ) = Song(
        id = "1", title = title, artist = artist, genre = "Folk",
        difficulty = "beginner", key = key, capo = capo,
        chords = chords, tags = emptyList(), notes = "",
        content = sections
    )

    @Test
    fun `output starts with title and artist`() {
        val result = SongExporter.buildChordShareText(makeSong())
        assertTrue(result.startsWith("Amazing Grace — Traditional"))
    }

    @Test
    fun `key line included when key is present`() {
        val result = SongExporter.buildChordShareText(makeSong(key = "Am"))
        assertTrue(result.contains("Key: Am"))
    }

    @Test
    fun `capo appended to key line when capo is set`() {
        val result = SongExporter.buildChordShareText(makeSong(key = "G", capo = 2))
        assertTrue(result.contains("Key: G | Capo: 2"))
    }

    @Test
    fun `capo not shown when capo is zero`() {
        val result = SongExporter.buildChordShareText(makeSong(key = "G", capo = 0))
        assertFalse(result.contains("Capo"))
    }

    @Test
    fun `key line omitted when key is blank`() {
        val result = SongExporter.buildChordShareText(makeSong(key = ""))
        assertFalse(result.contains("Key:"))
    }

    @Test
    fun `chord list included in header`() {
        val result = SongExporter.buildChordShareText(makeSong(chords = listOf("Am", "F", "C", "G")))
        assertTrue(result.contains("Chords: Am, F, C, G"))
    }

    @Test
    fun `chord list omitted when chords are empty`() {
        val result = SongExporter.buildChordShareText(makeSong(chords = emptyList()))
        assertFalse(result.contains("Chords:"))
    }

    @Test
    fun `section headers present in output`() {
        val song = makeSong(sections = listOf(
            makeSection("verse", 1, listOf("G")),
            makeSection("chorus", 1, listOf("C"))
        ))
        val result = SongExporter.buildChordShareText(song)
        assertTrue(result.contains("[Verse 1]"))
        assertTrue(result.contains("[Chorus 1]"))
    }

    @Test
    fun `chord names appear for each section line`() {
        val song = makeSong(sections = listOf(
            makeSection("verse", 1, listOf("Am", "F"), listOf("C", "G"))
        ))
        val result = SongExporter.buildChordShareText(song)
        assertTrue(result.contains("Am"))
        assertTrue(result.contains("F"))
        assertTrue(result.contains("C"))
        assertTrue(result.contains("G"))
    }

    @Test
    fun `lyrics text does not appear in output`() {
        val song = makeSong(sections = listOf(
            makeSection("verse", 1, listOf("G"))
        ))
        val result = SongExporter.buildChordShareText(song)
        assertFalse(result.contains("Some lyrics that must NOT appear in the export"))
    }

    @Test
    fun `lines with no chords produce no output for that line`() {
        val section = SongSection(
            type = "verse", number = 1,
            lines = listOf(SongLine(text = "Lyrics only line", chords = emptyList()))
        )
        val song = makeSong(sections = listOf(section))
        val result = SongExporter.buildChordShareText(song)
        assertFalse(result.contains("Lyrics only line"))
    }
}
