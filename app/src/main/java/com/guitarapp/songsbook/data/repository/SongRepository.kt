package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongById(id: String): Song?
    suspend fun searchSongs(query: String): List<Song>
    suspend fun getGenres(): List<String>
    suspend fun getDifficulties(): List<String>
    suspend fun getFavorites(): List<Song>
    suspend fun toggleFavorite(songId: String)
    suspend fun insertSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSong(songId: String)
}