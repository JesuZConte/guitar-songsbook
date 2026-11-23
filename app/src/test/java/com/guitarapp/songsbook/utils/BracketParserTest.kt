package com.guitarapp.songsbook.utils

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.SongLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BracketParserTest {

    // ── parseLine ──

    @Test
    fun `parseLine - plain text with no chords`() {
        val result = BracketParser.parseLine("Hello darkness my old friend")
        assertEquals("Hello darkness my old friend", result.text)
        assertTrue(result.chords.isEmpty())
    }

    @Test
    fun `parseLine - single chord at start`() {
        val result = BracketParser.parseLine("[Am]Hello darkness")
        assertEquals("Hello darkness", result.text)
        assertEquals(1, result.chords.size)
        assertEquals(ChordPosition("Am", 0), result.chords[0])
    }

    @Test
    fun `parseLine - multiple inline chords`() {
        val result = BracketParser.parseLine("[Am]Hello [F]darkness my [C]old [G]friend")
        assertEquals("Hello darkness my old friend", result.text)
        assertEquals(4, result.chords.size)
        assertEquals(ChordPosition("Am", 0), result.chords[0])
        assertEquals(ChordPosition("F", 6), result.chords[1])
        assertEquals(ChordPosition("C", 18), result.chords[2])
        assertEquals(ChordPosition("G", 22), result.chords[3])
    }

    @Test
    fun `parseLine - chord-only line with spaces`() {
        val result = BracketParser.parseLine("[Am] [F] [C] [G]")
        assertEquals(4, result.chords.size)
        assertEquals("Am", result.chords[0].chord)
        assertEquals("G", result.chords[3].chord)
    }

    @Test
    fun `parseLine - complex chord names`() {
        val result = BracketParser.parseLine("[F#m7]Hello [Bb]world [Gsus4]today")
        assertEquals("Hello world today", result.text)
        assertEquals("F#m7", result.chords[0].chord)
        assertEquals("Bb", result.chords[1].chord)
        assertEquals("Gsus4", result.chords[2].chord)
    }

    @Test
    fun `parseLine - chord at end of line`() {
        val result = BracketParser.parseLine("Hello world [Am]")
        assertEquals("Hello world ", result.text)
        assertEquals(1, result.chords.size)
        assertEquals(ChordPosition("Am", 12), result.chords[0])
    }

    // ── parseSectionHeader ──

    @Test
    fun `parseSectionHeader - verse with number`() {
        val result = BracketParser.parseSectionHeader("[Verse 1]")
        assertEquals("verse", result?.first)
        assertEquals(1, result?.second)
    }

    @Test
    fun `parseSectionHeader - chorus no number`() {
        val result = BracketParser.parseSectionHeader("[Chorus]")
        assertEquals("chorus", result?.first)
        assertEquals(0, result?.second)
    }

    @Test
    fun `parseSectionHeader - bridge`() {
        val result = BracketParser.parseSectionHeader("[Bridge]")
        assertEquals("bridge", result?.first)
    }

    @Test
    fun `parseSectionHeader - intro`() {
        val result = BracketParser.parseSectionHeader("[Intro]")
        assertEquals("intro", result?.first)
    }

    @Test
    fun `parseSectionHeader - outro`() {
        val result = BracketParser.parseSectionHeader("[Outro]")
        assertEquals("outro", result?.first)
    }

    @Test
    fun `parseSectionHeader - pre-chorus`() {
        val result = BracketParser.parseSectionHeader("[Pre-Chorus]")
        assertEquals("pre-chorus", result?.first)
    }

    @Test
    fun `parseSectionHeader - solo`() {
        val result = BracketParser.parseSectionHeader("[Solo]")
        assertEquals("solo", result?.first)
    }

    @Test
    fun `parseSectionHeader - not a section returns null`() {
        assertNull(BracketParser.parseSectionHeader("[Am]"))
        assertNull(BracketParser.parseSectionHeader("[G7]"))
        assertNull(BracketParser.parseSectionHeader("[F#m]"))
    }

    @Test
    fun `parseSectionHeader - Spanish labels map to canonical keys`() {
        assertEquals(Pair("verse", 1), BracketParser.parseSectionHeader("[Estrofa 1]"))
        assertEquals(Pair("verse", 0), BracketParser.parseSectionHeader("[Verso]"))
        assertEquals(Pair("chorus", 0), BracketParser.parseSectionHeader("[Estribillo]"))
        assertEquals(Pair("chorus", 2), BracketParser.parseSectionHeader("[Coro 2]"))
        assertEquals(Pair("bridge", 0), BracketParser.parseSectionHeader("[Puente]"))
        assertEquals(Pair("intro", 0), BracketParser.parseSectionHeader("[Introducción]"))
        assertEquals(Pair("outro", 0), BracketParser.parseSectionHeader("[Final]"))
        assertEquals(Pair("pre-chorus", 0), BracketParser.parseSectionHeader("[Pre-coro]"))
        assertEquals(Pair("pre-chorus", 0), BracketParser.parseSectionHeader("[Pre-estribillo]"))
    }

    @Test
    fun `parseSectionHeader - English behavior is unchanged`() {
        // Pre-existing: first word decides the type, anything after a non-number is ignored.
        assertEquals(Pair("chorus", 0), BracketParser.parseSectionHeader("[Chorus x2]"))
        assertEquals(Pair("instrumental", 0), BracketParser.parseSectionHeader("[Instrumental]"))
        assertEquals(Pair("interlude", 0), BracketParser.parseSectionHeader("[Interlude]"))
    }

    @Test
    fun `parseSectionHeader - Spanish matching ignores case and accents`() {
        assertEquals(Pair("intro", 0), BracketParser.parseSectionHeader("[INTRODUCCION]"))
        assertEquals(Pair("verse", 3), BracketParser.parseSectionHeader("[ESTROFA 3]"))
    }

    @Test
    fun `parseSectionHeader - ordinal parts map to numbered verses`() {
        assertEquals(Pair("verse", 1), BracketParser.parseSectionHeader("[Primera Parte]"))
        assertEquals(Pair("verse", 2), BracketParser.parseSectionHeader("[Segunda Parte]"))
        assertEquals(Pair("verse", 3), BracketParser.parseSectionHeader("[Tercera Parte]"))
    }

    @Test
    fun `parseSectionHeader - unknown multi-word label is kept verbatim`() {
        assertEquals(Pair("Puente instrumental", 0), BracketParser.parseSectionHeader("[Puente instrumental]"))
    }

    @Test
    fun `parseSectionHeader - unknown single word label is kept verbatim`() {
        assertEquals(Pair("Interludio", 0), BracketParser.parseSectionHeader("[Interludio]"))
    }

    @Test
    fun `parseSectionHeader - Latin chord names are chords, not sections`() {
        assertNull(BracketParser.parseSectionHeader("[Do]"))
        assertNull(BracketParser.parseSectionHeader("[Sol]"))
        assertNull(BracketParser.parseSectionHeader("[Lam]"))
        assertNull(BracketParser.parseSectionHeader("[Rem]"))
    }

    @Test
    fun `parseSectionHeader - bracket chord rows are not sections`() {
        assertNull(BracketParser.parseSectionHeader("[C]   [D]   [Em]"))
        assertNull(BracketParser.parseSectionHeader("[Am]Hello [F]"))
    }

    @Test
    fun `parse - Spanish headers create typed sections and are not chords`() {
        val sections = BracketParser.parse("[Estrofa 1]\n[C]hola\n\n[Coro]\n[G]adios")
        assertEquals(listOf("verse", "chorus"), sections.map { it.type })
        assertEquals(listOf("C", "G"), BracketParser.extractChordNames(sections))
    }

    @Test
    fun `parseSectionHeader - not bracketed returns null`() {
        assertNull(BracketParser.parseSectionHeader("Verse 1"))
        assertNull(BracketParser.parseSectionHeader("[Verse 1"))
    }

    // ── parse (full text) ──

    @Test
    fun `parse - complete song with sections`() {
        val text = """
            [Intro]
            [Am] [F] [C] [G]

            [Verse 1]
            [Am]Hello [F]darkness my [C]old [G]friend
            [Am]I've come to [F]talk with you [C]again

            [Chorus]
            [C]Because a [D]vision softly [Em]creeping
        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(3, sections.size)

        assertEquals("intro", sections[0].type)
        assertEquals(1, sections[0].number)
        assertEquals(1, sections[0].lines.size)

        assertEquals("verse", sections[1].type)
        assertEquals(1, sections[1].number)
        assertEquals(2, sections[1].lines.size)

        assertEquals("chorus", sections[2].type)
        assertEquals(1, sections[2].number)
        assertEquals(1, sections[2].lines.size)
    }

    @Test
    fun `parse - auto-numbers repeated section types`() {
        val text = """
            [Verse]
            First verse line

            [Verse]
            Second verse line
        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(2, sections.size)
        assertEquals(1, sections[0].number)
        assertEquals(2, sections[1].number)
    }

    @Test
    fun `parse - explicit number overrides auto-number`() {
        val text = """
            [Verse 3]
            Third verse line
        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(1, sections.size)
        assertEquals(3, sections[0].number)
    }

    @Test
    fun `parse - text without sections defaults to verse`() {
        val text = """
            [Am]Hello [G]world
            [C]Second line
        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(1, sections.size)
        assertEquals("verse", sections[0].type)
        assertEquals(1, sections[0].number)
        assertEquals(2, sections[0].lines.size)
    }

    @Test
    fun `parse - empty text returns empty list`() {
        val sections = BracketParser.parse("")
        assertTrue(sections.isEmpty())
    }

    @Test
    fun `parse - blank lines between sections are handled`() {
        val text = """
            [Verse 1]
            Line one
            Line two


            [Chorus]
            Chorus line
        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(2, sections.size)
        assertEquals(2, sections[0].lines.size)
        assertEquals(1, sections[1].lines.size)
    }

    @Test
    fun `parse - plain text only returns single verse`() {
        val text = "Just a plain text line"
        val sections = BracketParser.parse(text)
        assertEquals(1, sections.size)
        assertEquals("verse", sections[0].type)
        assertEquals("Just a plain text line", sections[0].lines[0].text)
    }

    @Test
    fun `parse - trailing blank lines are dropped from sections`() {
        val text = """
            [Verse 1]
            Line one

        """.trimIndent()

        val sections = BracketParser.parse(text)
        assertEquals(1, sections.size)
        assertEquals(1, sections[0].lines.size)
        assertEquals("Line one", sections[0].lines[0].text)
    }

    // ── extractChordNames ──

    @Test
    fun `extractChordNames - returns distinct chords`() {
        val text = """
            [Verse 1]
            [Am]Hello [F]world
            [Am]Again [C]here [F]now
        """.trimIndent()

        val sections = BracketParser.parse(text)
        val chords = BracketParser.extractChordNames(sections)
        assertEquals(listOf("Am", "F", "C"), chords)
    }

    @Test
    fun `extractChordNames - empty sections returns empty list`() {
        val chords = BracketParser.extractChordNames(emptyList())
        assertTrue(chords.isEmpty())
    }
}
