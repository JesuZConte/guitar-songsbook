package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.data.local.PlaylistDao
import com.guitarapp.songsbook.data.local.PlaylistEntity
import com.guitarapp.songsbook.data.local.PlaylistSongCrossRef
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.domain.model.Song

class RoomPlaylistRepository(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override suspend fun getPlaylists(): List<Playlist> {
        return playlistDao.getAll().map { entity ->
            val songCount = playlistDao.getSongCount(entity.id)
            entity.toDomain(songCount)
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistDao.getById(playlistId) ?: return null
        val songCount = playlistDao.getSongCount(playlistId)
        return entity.toDomain(songCount)
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insert(PlaylistEntity(name = name))
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.delete(playlistId)
    }

    override suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        return playlistDao.getSongsForPlaylist(playlistId).map { it.toDomain() }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        if (!playlistDao.isSongInPlaylist(playlistId, songId)) {
            playlistDao.addSong(PlaylistSongCrossRef(playlistId, songId))
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSong(playlistId, songId)
    }

    override suspend fun isSongInPlaylist(playlistId: Long, songId: String): Boolean {
        return playlistDao.isSongInPlaylist(playlistId, songId)
    }
}
