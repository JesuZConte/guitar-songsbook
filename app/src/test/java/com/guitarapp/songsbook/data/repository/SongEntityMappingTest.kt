package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.data.local.SongEntity
import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import org.junit.Assert.assertEquals
import org.junit.Test

class SongEntityMappingTest {

    private val testSong = Song(
        id = "1",
        title = "Test Song",
        artist = "Test Artist",
        genre = "Rock",
        difficulty = "beginner",
        key = "G",
        capo = 2,
        chords = listOf("G", "C", "D"),
        tags = listOf("test", "rock"),
        notes = "Some notes",
        content = listOf(
            SongSection(
                type = "verse",
                number = 1,
                lines = listOf(
                    SongLine(
                        text = "Test line",
                        chords = listOf(
                            ChordPosition("G", 0),
                            ChordPosition("C", 5)
                        )
                    )
                )
            )
        )
    )

    @Test
    fun `fromDomain preserves all fields`() {
        val entity = SongEntity.fromDomain(testSong)

        assertEquals(testSong.id, entity.id)
        assertEquals(testSong.title, entity.title)
        assertEquals(testSong.artist, entity.artist)
        assertEquals(testSong.genre, entity.genre)
        assertEquals(testSong.difficulty, entity.difficulty)
        assertEquals(testSong.key, entity.key)
        assertEquals(testSong.capo, entity.capo)
        assertEquals(testSong.chords, entity.chords)
        assertEquals(testSong.tags, entity.tags)
        assertEquals(testSong.notes, entity.notes)
        assertEquals(testSong.content, entity.content)
    }

    @Test
    fun `toDomain preserves all fields`() {
        val entity = SongEntity.fromDomain(testSong)
        val domain = entity.toDomain()

        assertEquals(testSong, domain)
    }

    @Test
    fun `roundtrip domain to entity to domain is lossless`() {
        val result = SongEntity.fromDomain(testSong).toDomain()
        assertEquals(testSong.id, result.id)
        assertEquals(testSong.content.first().lines.first().chords.size, 2)
        assertEquals("G", result.content.first().lines.first().chords[0].chord)
        assertEquals("C", result.content.first().lines.first().chords[1].chord)
    }

    @Test
    fun `mapping handles empty content`() {
        val emptySong = testSong.copy(content = emptyList(), chords = emptyList(), tags = emptyList())
        val result = SongEntity.fromDomain(emptySong).toDomain()

        assertEquals(emptyList<SongSection>(), result.content)
        assertEquals(emptyList<String>(), result.chords)
        assertEquals(emptyList<String>(), result.tags)
    }
}