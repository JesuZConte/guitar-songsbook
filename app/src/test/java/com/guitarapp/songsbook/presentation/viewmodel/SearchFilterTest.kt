package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFilterTest {

    private val songs = listOf(
        makeSong("1", "Wish You Were Here", "Pink Floyd", "Rock", "intermediate"),
        makeSong("2", "Wonderwall", "Oasis", "Britpop", "beginner"),
        makeSong("3", "Hotel California", "Eagles", "Rock", "advanced"),
        makeSong("4", "La Bamba", "Ritchie Valens", "Latin Rock", "beginner"),
        makeSong("5", "Zombie", "The Cranberries", "Alternative Rock", "intermediate")
    )

    private fun makeSong(
        id: String, title: String, artist: String, genre: String, difficulty: String
    ): Song {
        return Song(
            id = id, title = title, artist = artist, genre = genre,
            difficulty = difficulty, key = "G", capo = 0,
            chords = emptyList(), tags = emptyList(), notes = "",
            content = emptyList()
        )
    }

    private fun filterSongs(
        songs: List<Song>,
        query: String = "",
        genre: String? = null,
        difficulty: String? = null
    ): List<Song> {
        return songs
            .filter { song ->
                query.isBlank() ||
                        song.title.contains(query, ignoreCase = true) ||
                        song.artist.contains(query, ignoreCase = true)
            }
            .filter { song -> genre == null || song.genre == genre }
            .filter { song -> difficulty == null || song.difficulty == difficulty }
    }

    @Test
    fun `empty query returns all songs`() {
        val results = filterSongs(songs, query = "")
        assertEquals(5, results.size)
    }

    @Test
    fun `search by title finds song`() {
        val results = filterSongs(songs, query = "zombie")
        assertEquals(1, results.size)
        assertEquals("Zombie", results[0].title)
    }

    @Test
    fun `search by artist finds songs`() {
        val results = filterSongs(songs, query = "pink")
        assertEquals(1, results.size)
        assertEquals("Pink Floyd", results[0].artist)
    }

    @Test
    fun `search is case insensitive`() {
        val results = filterSongs(songs, query = "HOTEL")
        assertEquals(1, results.size)
        assertEquals("Hotel California", results[0].title)
    }

    @Test
    fun `search with no match returns empty`() {
        val results = filterSongs(songs, query = "nonexistent")
        assertEquals(0, results.size)
    }

    @Test
    fun `filter by genre`() {
        val results = filterSongs(songs, genre = "Rock")
        assertEquals(2, results.size)
    }

    @Test
    fun `filter by difficulty`() {
        val results = filterSongs(songs, difficulty = "beginner")
        assertEquals(2, results.size)
    }

    @Test
    fun `combine search and genre filter`() {
        val results = filterSongs(songs, query = "pink", genre = "Rock")
        assertEquals(1, results.size)
        assertEquals("Wish You Were Here", results[0].title)
    }

    @Test
    fun `combine search and difficulty filter`() {
        val results = filterSongs(songs, query = "a", difficulty = "beginner")
        assertEquals(2, results.size)
    }

    @Test
    fun `combine genre and difficulty filter`() {
        val results = filterSongs(songs, genre = "Rock", difficulty = "advanced")
        assertEquals(1, results.size)
        assertEquals("Hotel California", results[0].title)
    }

    @Test
    fun `all filters combined`() {
        val results = filterSongs(songs, query = "hotel", genre = "Rock", difficulty = "advanced")
        assertEquals(1, results.size)
        assertEquals("Hotel California", results[0].title)
    }

    @Test
    fun `all filters combined with no match`() {
        val results = filterSongs(songs, query = "hotel", genre = "Rock", difficulty = "beginner")
        assertEquals(0, results.size)
    }
}