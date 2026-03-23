package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.Song

data class SongbookResponse(
    val songbook: SongbookData
)

data class SongbookData(
    val version: String,
    val lastUpdated: String,
    val songs: List<Song>
)