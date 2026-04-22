package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TransposeTest {

    // ── Zero offset ──

    @Test
    fun `zero semitones returns original chord`() {
        assertEquals("Am", ChordNotation.transpose("Am", 0))
        assertEquals("C#maj7", ChordNotation.transpose("C#maj7", 0))
    }

    // ── Simple roots ──

    @Test
    fun `transpose up by 2`() {
        assertEquals("D", ChordNotation.transpose("C", 2))
        assertEquals("E", ChordNotation.transpose("D", 2))
        assertEquals("G", ChordNotation.transpose("F", 2))
    }

    @Test
    fun `transpose down by 2`() {
        assertEquals("Bb", ChordNotation.transpose("C", -2))
        assertEquals("G", ChordNotation.transpose("A", -2))
        assertEquals("Eb", ChordNotation.transpose("F", -2))
    }

    // ── Octave wrap ──

    @Test
    fun `B up 1 wraps to C`() {
        assertEquals("C", ChordNotation.transpose("B", 1))
    }

    @Test
    fun `C down 1 wraps to B`() {
        assertEquals("B", ChordNotation.transpose("C", -1))
    }

    @Test
    fun `transpose by full octave returns same note`() {
        assertEquals("Am", ChordNotation.transpose("Am", 12))
        assertEquals("Am", ChordNotation.transpose("Am", -12))
    }

    // ── Quality preservation ──

    @Test
    fun `quality is preserved`() {
        assertEquals("Cm", ChordNotation.transpose("Am", 3))
        assertEquals("Fmaj7", ChordNotation.transpose("Cmaj7", 5))
        assertEquals("Esus4", ChordNotation.transpose("Dsus4", 2))
        assertEquals("Ddim", ChordNotation.transpose("Bdim", 3))
    }

    // ── E and B edge cases (no E# / B# in output) ──

    @Test
    fun `E up 1 gives F not E sharp`() {
        assertEquals("F", ChordNotation.transpose("E", 1))
    }

    @Test
    fun `B up 1 gives C not B sharp`() {
        assertEquals("C", ChordNotation.transpose("B", 1))
    }

    // ── Sharps vs flats ──

    @Test
    fun `upward transposition prefers sharps`() {
        assertEquals("C#", ChordNotation.transpose("C", 1))
        assertEquals("D#", ChordNotation.transpose("D", 1))
        assertEquals("F#", ChordNotation.transpose("F", 1))
    }

    @Test
    fun `downward transposition prefers flats`() {
        assertEquals("Db", ChordNotation.transpose("D", -1))
        assertEquals("Eb", ChordNotation.transpose("E", -1))
        assertEquals("Ab", ChordNotation.transpose("A", -1))
    }

    // ── Slash chords ──

    @Test
    fun `slash chord transposes both root and bass`() {
        assertEquals("A/C#", ChordNotation.transpose("G/B", 2))
        assertEquals("C/E", ChordNotation.transpose("A/C#", 3))
    }

    // ── Input with flats / sharps ──

    @Test
    fun `flat input transposes correctly`() {
        assertEquals("C", ChordNotation.transpose("Bb", 2))   // Bb(10) + 2 = 12 % 12 = C
        assertEquals("A", ChordNotation.transpose("Bb", -1))  // Bb(10) - 1 = 9 = A
    }

    @Test
    fun `sharp input transposes correctly`() {
        assertEquals("D#", ChordNotation.transpose("C#", 2))  // C#(1) + 2 = 3 = D#
        assertEquals("F#m", ChordNotation.transpose("Em", 2)) // E(4) + 2 = 6 = F#
    }
}
