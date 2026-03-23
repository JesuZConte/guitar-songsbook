package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongById(id: String): Song?
}