package com.guitarapp.songsbook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongSection

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val genre: String,
    val difficulty: String,
    val key: String,
    val capo: Int,
    val chords: List<String>,
    val tags: List<String>,
    val notes: String,
    val content: List<SongSection>
) {

    fun toDomain(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            genre = genre,
            difficulty = difficulty,
            key = key,
            capo = capo,
            chords = chords,
            tags = tags,
            notes = notes,
            content = content
        )
    }

    companion object {
        fun fromDomain(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                genre = song.genre,
                difficulty = song.difficulty,
                key = song.key,
                capo = song.capo,
                chords = song.chords,
                tags = song.tags,
                notes = song.notes,
                content = song.content
            )
        }
    }
}