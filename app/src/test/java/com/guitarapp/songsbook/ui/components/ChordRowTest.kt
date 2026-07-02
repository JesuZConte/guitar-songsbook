package com.guitarapp.songsbook.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ChordRowTest {

    // Regression: reported as "chords super juntos" on Loca (Chico Trujillo) in Latin
    // notation. Latin chord names (Rem, Solm, Remmaj7...) are longer than the American
    // ones (Dm, Gm, Dmmaj7) the source positions were tuned for, so tight single-space
    // gaps collided and merged (e.g. "RemSolmLa7...").

    @Test
    fun `intro chord-only line does not merge in Latin notation`() {
        val chords = listOf(
            ChordPlacement("Rem", 0),
            ChordPlacement("Solm", 3),
            ChordPlacement("La7", 6),
            ChordPlacement("Rem", 9),
            ChordPlacement("Solm", 12),
            ChordPlacement("La7", 15)
        )
        val parts = buildChordRow(chords).trim().split(Regex("\\s+"))
        assertEquals(listOf("Rem", "Solm", "La7", "Rem", "Solm", "La7"), parts)
    }

    @Test
    fun `intro chord-only line stays as-is in American notation`() {
        val chords = listOf(
            ChordPlacement("Dm", 0),
            ChordPlacement("Gm", 3),
            ChordPlacement("A7", 6),
            ChordPlacement("Dm", 9),
            ChordPlacement("Gm", 12),
            ChordPlacement("A7", 15)
        )
        assertEquals("Dm Gm A7 Dm Gm A7", buildChordRow(chords))
    }

    @Test
    fun `first verse line does not merge in Latin notation`() {
        val chords = listOf(
            ChordPlacement("Rem", 0),
            ChordPlacement("Remmaj7", 5),
            ChordPlacement("Rem7", 12)
        )
        val parts = buildChordRow(chords).trim().split(Regex("\\s+"))
        assertEquals(listOf("Rem", "Remmaj7", "Rem7"), parts)
    }
}