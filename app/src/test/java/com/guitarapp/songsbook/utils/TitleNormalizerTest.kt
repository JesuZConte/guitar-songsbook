package com.guitarapp.songsbook.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TitleNormalizerTest {

    @Test fun `lowercase is normalized`() {
        assertEquals(normalizeTitle("CANCION"), normalizeTitle("cancion"))
    }

    @Test fun `leading and trailing spaces are stripped`() {
        assertEquals(normalizeTitle("  Hola  "), normalizeTitle("hola"))
    }

    @Test fun `acute accents are stripped`() {
        assertEquals(normalizeTitle("Corazón"), normalizeTitle("Corazon"))
        assertEquals(normalizeTitle("Séptimo"), normalizeTitle("Septimo"))
        assertEquals(normalizeTitle("Última"), normalizeTitle("Ultima"))
        assertEquals(normalizeTitle("Búscame"), normalizeTitle("Buscame"))
    }

    @Test fun `combined trim accent and case`() {
        assertEquals(normalizeTitle("  La Canción  "), normalizeTitle("la cancion"))
    }

    @Test fun `different titles do not match`() {
        assertNotEquals(normalizeTitle("La Bamba"), normalizeTitle("La Rumba"))
    }

    @Test fun `empty string returns empty`() {
        assertEquals("", normalizeTitle(""))
    }

    @Test fun `whitespace-only string returns empty`() {
        assertEquals("", normalizeTitle("   "))
    }
}
