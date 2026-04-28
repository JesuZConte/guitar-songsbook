package com.guitarapp.songsbook.domain.model

data class Song(
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
    val content: List<SongSection>,
    val isFavorite: Boolean = false,
    val versions: List<SongVersion> = emptyList()
)

data class SongVersion(
    val id: Long = 0,
    val songId: String,
    val name: String,
    val key: String = "",
    val capo: Int = 0,
    val chords: List<String> = emptyList(),
    val notes: String = "",
    val content: List<SongSection> = emptyList()
)

data class SongSection(
    val type: String,
    val number: Int,
    val lines: List<SongLine>
)

data class SongLine(
    val text: String,
    val chords: List<ChordPosition>
)

data class ChordPosition(
    val chord: String,
    val position: Int
)