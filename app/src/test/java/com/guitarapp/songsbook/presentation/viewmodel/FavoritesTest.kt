package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesTest {

    private fun makeSong(id: String, title: String, isFavorite: Boolean): Song {
        return Song(
            id = id, title = title, artist = "Artist", genre = "Rock",
            difficulty = "beginner", key = "G", capo = 0,
            chords = emptyList(), tags = emptyList(), notes = "",
            content = emptyList(), isFavorite = isFavorite
        )
    }

    @Test
    fun `filter favorites from song list`() {
        val songs = listOf(
            makeSong("1", "Song A", isFavorite = true),
            makeSong("2", "Song B", isFavorite = false),
            makeSong("3", "Song C", isFavorite = true)
        )
        val favorites = songs.filter { it.isFavorite }
        assertEquals(2, favorites.size)
        assertEquals("Song A", favorites[0].title)
        assertEquals("Song C", favorites[1].title)
    }

    @Test
    fun `no favorites returns empty list`() {
        val songs = listOf(
            makeSong("1", "Song A", isFavorite = false),
            makeSong("2", "Song B", isFavorite = false)
        )
        val favorites = songs.filter { it.isFavorite }
        assertTrue(favorites.isEmpty())
    }

    @Test
    fun `toggle favorite flips boolean`() {
        val song = makeSong("1", "Song A", isFavorite = false)
        val toggled = song.copy(isFavorite = !song.isFavorite)
        assertTrue(toggled.isFavorite)

        val toggledBack = toggled.copy(isFavorite = !toggled.isFavorite)
        assertFalse(toggledBack.isFavorite)
    }

    @Test
    fun `remove favorite from list`() {
        val favorites = listOf(
            makeSong("1", "Song A", isFavorite = true),
            makeSong("2", "Song B", isFavorite = true),
            makeSong("3", "Song C", isFavorite = true)
        )
        val afterRemove = favorites.filter { it.id != "2" }
        assertEquals(2, afterRemove.size)
        assertEquals("Song A", afterRemove[0].title)
        assertEquals("Song C", afterRemove[1].title)
    }

    @Test
    fun `toggle updates correct song in list`() {
        val songs = listOf(
            makeSong("1", "Song A", isFavorite = false),
            makeSong("2", "Song B", isFavorite = true),
            makeSong("3", "Song C", isFavorite = false)
        )
        val updated = songs.map { song ->
            if (song.id == "1") song.copy(isFavorite = !song.isFavorite)
            else song
        }
        assertTrue(updated[0].isFavorite)
        assertTrue(updated[1].isFavorite)
        assertFalse(updated[2].isFavorite)
    }

    @Test
    fun `toggle does not affect other songs`() {
        val songs = listOf(
            makeSong("1", "Song A", isFavorite = false),
            makeSong("2", "Song B", isFavorite = true)
        )
        val updated = songs.map { song ->
            if (song.id == "1") song.copy(isFavorite = !song.isFavorite)
            else song
        }
        assertTrue(updated[1].isFavorite)
    }

    @Test
    fun `isFavorite default is false`() {
        val song = Song(
            id = "1", title = "Test", artist = "Artist", genre = "Rock",
            difficulty = "beginner", key = "G", capo = 0,
            chords = emptyList(), tags = emptyList(), notes = "",
            content = emptyList()
        )
        assertFalse(song.isFavorite)
    }
}