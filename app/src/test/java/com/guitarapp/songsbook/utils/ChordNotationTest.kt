package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ChordNotationTest {

    // ── American to Latin ──

    @Test
    fun `simple major chords`() {
        assertEquals("La", ChordNotation.toLatinNotation("A"))
        assertEquals("Si", ChordNotation.toLatinNotation("B"))
        assertEquals("Do", ChordNotation.toLatinNotation("C"))
        assertEquals("Re", ChordNotation.toLatinNotation("D"))
        assertEquals("Mi", ChordNotation.toLatinNotation("E"))
        assertEquals("Fa", ChordNotation.toLatinNotation("F"))
        assertEquals("Sol", ChordNotation.toLatinNotation("G"))
    }

    @Test
    fun `minor chords`() {
        assertEquals("Lam", ChordNotation.toLatinNotation("Am"))
        assertEquals("Mim", ChordNotation.toLatinNotation("Em"))
        assertEquals("Rem", ChordNotation.toLatinNotation("Dm"))
    }

    @Test
    fun `seventh chords`() {
        assertEquals("La7", ChordNotation.toLatinNotation("A7"))
        assertEquals("Sim7", ChordNotation.toLatinNotation("Bm7"))
        assertEquals("Domaj7", ChordNotation.toLatinNotation("Cmaj7"))
    }

    @Test
    fun `sharp chords`() {
        assertEquals("Fa#m", ChordNotation.toLatinNotation("F#m"))
        assertEquals("Do#", ChordNotation.toLatinNotation("C#"))
        assertEquals("Sol#m7", ChordNotation.toLatinNotation("G#m7"))
    }

    @Test
    fun `flat chords`() {
        assertEquals("Sib", ChordNotation.toLatinNotation("Bb"))
        assertEquals("Mib", ChordNotation.toLatinNotation("Eb"))
        assertEquals("Lab", ChordNotation.toLatinNotation("Ab"))
    }

    @Test
    fun `sus and add chords`() {
        assertEquals("Resus4", ChordNotation.toLatinNotation("Dsus4"))
        assertEquals("Lasus2", ChordNotation.toLatinNotation("Asus2"))
        assertEquals("Doadd9", ChordNotation.toLatinNotation("Cadd9"))
    }

    @Test
    fun `dim and aug chords`() {
        assertEquals("Sidim", ChordNotation.toLatinNotation("Bdim"))
        assertEquals("Faaug", ChordNotation.toLatinNotation("Faug"))
    }

    // ── Latin to American ──

    @Test
    fun `latin to american - simple`() {
        assertEquals("A", ChordNotation.toAmericanNotation("La"))
        assertEquals("B", ChordNotation.toAmericanNotation("Si"))
        assertEquals("C", ChordNotation.toAmericanNotation("Do"))
        assertEquals("G", ChordNotation.toAmericanNotation("Sol"))
    }

    @Test
    fun `latin to american - with modifiers`() {
        assertEquals("Am7", ChordNotation.toAmericanNotation("Lam7"))
        assertEquals("F#m", ChordNotation.toAmericanNotation("Fa#m"))
        assertEquals("Bb", ChordNotation.toAmericanNotation("Sib"))
        assertEquals("G#m7", ChordNotation.toAmericanNotation("Sol#m7"))
    }

    // ── Roundtrip ──

    @Test
    fun `roundtrip american to latin and back`() {
        val chords = listOf("Am", "F#m7", "Bb", "Cmaj7", "Dsus4", "G#dim", "Eaug")
        for (chord in chords) {
            val latin = ChordNotation.toLatinNotation(chord)
            val back = ChordNotation.toAmericanNotation(latin)
            assertEquals("Roundtrip failed for $chord (latin: $latin)", chord, back)
        }
    }

    // ── convert() ──

    @Test
    fun `convert uses correct system`() {
        assertEquals("Lam", ChordNotation.convert("Am", NotationSystem.LATIN))
        assertEquals("Am", ChordNotation.convert("Lam", NotationSystem.AMERICAN))
    }

    // ── Edge cases ──

    @Test
    fun `unknown chord returned as-is`() {
        assertEquals("Xyz", ChordNotation.toLatinNotation("Xyz"))
        assertEquals("Xyz", ChordNotation.toAmericanNotation("Xyz"))
    }
}
