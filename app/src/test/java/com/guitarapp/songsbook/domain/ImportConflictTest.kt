package com.guitarapp.songsbook.domain

import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.domain.model.suggestVersionName
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportConflictTest {

    private fun song(key: String, capo: Int, versions: List<SongVersion> = emptyList()) = Song(
        id = "existing-id", title = "Test Song", artist = "Artist",
        genre = "Rock", difficulty = "beginner",
        key = key, capo = capo,
        chords = emptyList(), tags = emptyList(), notes = "", content = emptyList(),
        versions = versions
    )

    private fun incoming(key: String, capo: Int) = Song(
        id = "new-id", title = "Test Song", artist = "Artist",
        genre = "Rock", difficulty = "beginner",
        key = key, capo = capo,
        chords = emptyList(), tags = emptyList(), notes = "", content = emptyList()
    )

    private fun version(name: String, key: String, capo: Int) =
        SongVersion(id = 1L, songId = "existing-id", name = name, key = key, capo = capo)

    @Test fun `different key with no capo uses key name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("A", suggestVersionName(existing, incoming("A", 0)))
    }

    @Test fun `different capo uses key slash capo name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("G / Capo 2", suggestVersionName(existing, incoming("G", 2)))
    }

    @Test fun `different key and capo uses key slash capo name`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("A / Capo 1", suggestVersionName(existing, incoming("A", 1)))
    }

    @Test fun `same key and capo with one version generates Version 2`() {
        val existing = song("G", 0, listOf(version("Default", "G", 0)))
        assertEquals("Versión 2", suggestVersionName(existing, incoming("G", 0)))
    }

    @Test fun `same key and capo with two versions generates Version 3`() {
        val existing = song("G", 0, listOf(
            version("Default", "G", 0),
            version("Versión 2", "G", 0)
        ))
        assertEquals("Versión 3", suggestVersionName(existing, incoming("G", 0)))
    }

    @Test fun `no versions falls back to Version 2`() {
        val existing = song("G", 0, emptyList())
        assertEquals("Versión 2", suggestVersionName(existing, incoming("G", 0)))
    }
}
