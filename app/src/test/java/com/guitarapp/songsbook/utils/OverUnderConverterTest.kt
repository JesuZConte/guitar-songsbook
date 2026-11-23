package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the "Detectar Formato" pipeline (OverUnderConverter → BracketParser) on the
 * shapes found in real tester .txt files (ADR-025, Phase 1a). Lyrics here are original
 * placeholders, not taken from the tester's songs.
 */
class OverUnderConverterTest {

    private fun pipeline(text: String) =
        BracketParser.parse(requireNotNull(OverUnderConverter.convert(text)) { "format not detected" })

    // ── Regression: classic over/under ──

    @Test
    fun `convert - classic over-under line merges chords into lyric`() {
        assertEquals(
            "[Am]Hello [F]darkness",
            OverUnderConverter.convert("Am    F\nHello darkness")
        )
    }

    @Test
    fun `convert - returns null when no chord line is present`() {
        assertEquals(null, OverUnderConverter.convert("just some words\nand more words"))
    }

    // ── Problem C: chords past the end of the lyric ──

    @Test
    fun `convert - chords past end of lyric keep their columns and never nest`() {
        val sections = pipeline("      D5  C5  F5  G5\nHello there")

        val line = sections.single().lines.single()
        assertEquals(listOf("D5", "C5", "F5", "G5"), line.chords.map { it.chord })
        assertEquals(listOf(6, 10, 14, 18), line.chords.map { it.position })
        assertEquals("Hello there", line.text.trimEnd())
        assertTrue(line.chords.none { '[' in it.chord || ']' in it.chord })
        assertFalse("brackets leaked into lyric text", line.text.contains(']') || line.text.contains('['))
    }

    @Test
    fun `convert - chord exactly at end of lyric is not padded away`() {
        // Guard: F sits at column 5 == lyric length. It must stay at 5, with no extra padding.
        val line = pipeline("Am   F\nHello").single().lines.single()
        assertEquals(listOf(0, 5), line.chords.map { it.position })
        assertEquals("Hello", line.text)
    }

    // ── Problem B: section label followed by chords on the same line ──

    @Test
    fun `convert - header with inline chords is split into header plus chord line`() {
        val converted = OverUnderConverter.convert("[Intro] F  Bb  Dm  C\n        F  Bb  Dm  C")
        assertEquals("[Intro]\n[F]   [Bb]   [Dm]   [C]\n[F]   [Bb]   [Dm]   [C]", converted)
    }

    @Test
    fun `convert - header with inline chords below 80 percent still splits`() {
        // "[Solo] Dm F C" has 3 chord tokens out of 4 (75%), so it is not a chord line on its own.
        val sections = pipeline("[Solo] Dm  F  C\n       Dm  C  G")

        val solo = sections.single()
        assertEquals("solo", solo.type)
        assertEquals(2, solo.lines.size)
        assertEquals(listOf("Dm", "F", "C"), solo.lines[0].chords.map { it.chord })
    }

    @Test
    fun `convert - header with inline chords alone is detected as a convertible format`() {
        assertNotNull(OverUnderConverter.convert("[Intro] Am  G"))
    }

    // ── Problem A: Spanish section labels, whole file ──

    @Test
    fun `pipeline - Spanish section labels become typed sections, not chords`() {
        val sections = pipeline(
            """
            [Intro] Am  G  F  E

            [Estrofa 1]
            Am          G
            Camino por la calle vacia
            F           E
            bajo una luna de papel

            [Estribillo]
            C         G
            Canto para el viento
            """.trimIndent()
        )

        assertEquals(listOf("intro", "verse", "chorus"), sections.map { it.type })
        assertEquals(listOf(1, 1, 1), sections.map { it.number })
        assertEquals(
            listOf("Am", "G", "F", "E", "C"),
            BracketParser.extractChordNames(sections)
        )
        val verse = sections[1]
        assertEquals("Camino por la calle vacia", verse.lines[0].text)
        assertEquals(listOf("Am", "G"), verse.lines[0].chords.map { it.chord })
    }

    @Test
    fun `pipeline - Primera and Segunda Parte number the verses`() {
        val sections = pipeline(
            "[Primera Parte]\nAm\nuno dos\n\n[Segunda Parte]\nG\ntres cuatro"
        )
        assertEquals(listOf("verse", "verse"), sections.map { it.type })
        assertEquals(listOf(1, 2), sections.map { it.number })
    }

    @Test
    fun `pipeline - unknown label is kept as its own section`() {
        val sections = pipeline("[Puente instrumental]\nAm  G\n\n[Coro]\nC\nla la la")
        assertEquals(listOf("Puente instrumental", "chorus"), sections.map { it.type })
    }
}
