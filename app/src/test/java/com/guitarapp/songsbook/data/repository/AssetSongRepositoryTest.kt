package com.guitarapp.songsbook.data.repository

import com.google.gson.Gson
import com.guitarapp.songsbook.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongJsonParsingTest {

    private lateinit var songs: List<Song>

    private val testJson = """
        {
          "songbook": {
            "version": "1.0",
            "lastUpdated": "2026-03-22",
            "songs": [
              {
                "id": "1",
                "title": "Test Song",
                "artist": "Test Artist",
                "genre": "Rock",
                "difficulty": "beginner",
                "key": "G",
                "capo": 0,
                "chords": ["G", "C", "D"],
                "tags": ["test"],
                "notes": "Test notes",
                "content": [
                  {
                    "type": "verse",
                    "number": 1,
                    "lines": [
                      {
                        "text": "Test line one",
                        "chords": [
                          { "chord": "G", "position": 0 }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                "id": "2",
                "title": "Second Song",
                "artist": "Another Artist",
                "genre": "Pop",
                "difficulty": "intermediate",
                "key": "Am",
                "capo": 2,
                "chords": ["Am", "F"],
                "tags": ["pop"],
                "notes": "",
                "content": []
              }
            ]
          }
        }
    """.trimIndent()

    @Before
    fun setup() {
        val response = Gson().fromJson(testJson, SongbookResponse::class.java)
        songs = response.songbook.songs
    }

    @Test
    fun `parses correct number of songs`() {
        assertEquals(2, songs.size)
    }

    @Test
    fun `parses song fields correctly`() {
        val song = songs.first()
        assertEquals("1", song.id)
        assertEquals("Test Song", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Rock", song.genre)
        assertEquals("beginner", song.difficulty)
        assertEquals("G", song.key)
        assertEquals(0, song.capo)
    }

    @Test
    fun `parses chords list`() {
        val song = songs.first()
        assertEquals(listOf("G", "C", "D"), song.chords)
    }

    @Test
    fun `parses content structure`() {
        val song = songs.first()
        assertEquals(1, song.content.size)

        val section = song.content.first()
        assertEquals("verse", section.type)
        assertEquals(1, section.number)
        assertEquals(1, section.lines.size)

        val line = section.lines.first()
        assertEquals("Test line one", line.text)
        assertEquals("G", line.chords.first().chord)
        assertEquals(0, line.chords.first().position)
    }

    @Test
    fun `parses song with empty content`() {
        val song = songs[1]
        assertTrue(song.content.isEmpty())
        assertEquals(2, song.capo)
    }

    @Test
    fun `parses tags correctly`() {
        assertEquals(listOf("test"), songs[0].tags)
        assertEquals(listOf("pop"), songs[1].tags)
    }
}