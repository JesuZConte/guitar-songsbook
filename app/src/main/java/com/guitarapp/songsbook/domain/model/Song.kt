package com.guitarapp.songsbook.domain.model

/**
 * Domain model representing a song in the songbook.
 * Pure Kotlin — no Android or framework dependencies.
 */
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
    val content: List<SongSection>
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