// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.utils.TextSongDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSongImportDraftTest {

    private val draft = TextSongDraft(
        title = "Mi canción",
        artist = "Un artista",
        key = "Am",
        capo = "2",
        body = "[Estrofa 1]\nAm   G\nhola mundo"
    )

    @Test
    fun `withImportedDraft - fills the metadata fields`() {
        val state = AddSongUiState().withImportedDraft(draft)
        assertEquals("Mi canción", state.title)
        assertEquals("Un artista", state.artist)
        assertEquals("Am", state.key)
        assertEquals("2", state.capo)
    }

    @Test
    fun `withImportedDraft - converts chords-over-lyrics body to bracket format`() {
        val state = AddSongUiState().withImportedDraft(draft)
        assertEquals("[Estrofa 1]\n[Am]hola [G]mundo", state.rawText)
    }

    @Test
    fun `withImportedDraft - body that is not over-under is kept as is`() {
        val bracket = "[Verso]\n[Am]hola [G]mundo"
        val state = AddSongUiState().withImportedDraft(draft.copy(body = bracket))
        assertEquals(bracket, state.rawText)
    }

    @Test
    fun `withImportedDraft - blank key and capo keep the defaults`() {
        val state = AddSongUiState().withImportedDraft(draft.copy(key = "", capo = ""))
        assertEquals("", state.key)
        assertEquals("0", state.capo)
    }

    @Test
    fun `withImportedDraft - marks the state as opened from an import`() {
        assertFalse(AddSongUiState().openedFromImport)
        assertTrue(AddSongUiState().withImportedDraft(draft).openedFromImport)
    }

    @Test
    fun `holder - a draft is delivered exactly once`() {
        val holder = TextDraftHolder()
        assertNull(holder.take())
        holder.set(draft)
        assertSame(draft, holder.take())
        assertNull(holder.take())
    }
}
