package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongById(id: String): Song?
    suspend fun findSongsByTitle(title: String): List<Song>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun getGenres(): List<String>
    suspend fun getDifficulties(): List<String>
    suspend fun getFavorites(): List<Song>
    suspend fun toggleFavorite(songId: String)
    suspend fun insertSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSong(songId: String)
    suspend fun getVersionsForSong(songId: String): List<SongVersion>
    suspend fun getVersionById(versionId: Long): SongVersion?
    suspend fun insertVersion(version: SongVersion): Long
    suspend fun updateVersion(version: SongVersion)
    suspend fun deleteVersion(versionId: Long)
}