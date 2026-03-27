package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.domain.model.Song

interface PlaylistRepository {
    suspend fun getPlaylists(): List<Playlist>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun getSongsForPlaylist(playlistId: Long): List<Song>
    suspend fun addSongToPlaylist(playlistId: Long, songId: String)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
    suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean
}
