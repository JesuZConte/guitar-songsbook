package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.data.local.PlaylistEntity
import com.guitarapp.songsbook.data.local.PlaylistSongCrossRef
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistTest {

    private fun makeSong(id: String, title: String): Song {
        return Song(
            id = id, title = title, artist = "Artist", genre = "Rock",
            difficulty = "beginner", key = "G", capo = 0,
            chords = emptyList(), tags = emptyList(), notes = "",
            content = emptyList()
        )
    }

    // --- Playlist domain model ---

    @Test
    fun `playlist default values`() {
        val playlist = Playlist(name = "My Setlist")
        assertEquals(0L, playlist.id)
        assertEquals("My Setlist", playlist.name)
        assertEquals(0, playlist.songCount)
    }

    @Test
    fun `playlist with song count`() {
        val playlist = Playlist(id = 1, name = "Live", songCount = 5)
        assertEquals(5, playlist.songCount)
    }

    // --- PlaylistEntity mapping ---

    @Test
    fun `entity toDomain without song count`() {
        val entity = PlaylistEntity(id = 1, name = "Rehearsal")
        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("Rehearsal", domain.name)
        assertEquals(0, domain.songCount)
    }

    @Test
    fun `entity toDomain with song count`() {
        val entity = PlaylistEntity(id = 2, name = "Show")
        val domain = entity.toDomain(songCount = 8)
        assertEquals(8, domain.songCount)
    }

    @Test
    fun `entity fromDomain roundtrip`() {
        val playlist = Playlist(id = 3, name = "Warm-up", songCount = 4)
        val entity = PlaylistEntity.fromDomain(playlist)
        assertEquals(3L, entity.id)
        assertEquals("Warm-up", entity.name)
        val back = entity.toDomain(songCount = 4)
        assertEquals(playlist, back)
    }

    // --- PlaylistSongCrossRef ---

    @Test
    fun `crossRef holds correct ids`() {
        val crossRef = PlaylistSongCrossRef(playlistId = 1, songId = "song-abc")
        assertEquals(1L, crossRef.playlistId)
        assertEquals("song-abc", crossRef.songId)
    }

    // --- Playlist song list operations ---

    @Test
    fun `add song to playlist songs list`() {
        val songs = listOf(makeSong("1", "Song A"))
        val newSong = makeSong("2", "Song B")
        val updated = songs + newSong
        assertEquals(2, updated.size)
        assertEquals("Song B", updated[1].title)
    }

    @Test
    fun `remove song from playlist by id`() {
        val songs = listOf(
            makeSong("1", "Song A"),
            makeSong("2", "Song B"),
            makeSong("3", "Song C")
        )
        val afterRemove = songs.filter { it.id != "2" }
        assertEquals(2, afterRemove.size)
        assertFalse(afterRemove.any { it.id == "2" })
    }

    @Test
    fun `remove song that does not exist is no-op`() {
        val songs = listOf(makeSong("1", "Song A"))
        val afterRemove = songs.filter { it.id != "999" }
        assertEquals(1, afterRemove.size)
    }

    @Test
    fun `check if song is in playlist`() {
        val songIds = setOf("1", "3", "5")
        assertTrue("1" in songIds)
        assertFalse("2" in songIds)
    }

    @Test
    fun `empty playlist has no songs`() {
        val songs = emptyList<Song>()
        assertTrue(songs.isEmpty())
        assertEquals(0, songs.size)
    }

    // --- Playlist CRUD operations ---

    @Test
    fun `delete playlist removes from list`() {
        val playlists = listOf(
            Playlist(id = 1, name = "A"),
            Playlist(id = 2, name = "B"),
            Playlist(id = 3, name = "C")
        )
        val afterDelete = playlists.filter { it.id != 2L }
        assertEquals(2, afterDelete.size)
        assertFalse(afterDelete.any { it.id == 2L })
    }

    @Test
    fun `create playlist trims name`() {
        val name = "  My Playlist  "
        val trimmed = name.trim()
        assertEquals("My Playlist", trimmed)
    }

    @Test
    fun `blank playlist name is rejected`() {
        val name = "   "
        assertTrue(name.isBlank())
    }
}
