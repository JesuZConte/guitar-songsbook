package com.guitarapp.songsbook.data.repository

import android.content.res.AssetManager
import com.google.gson.Gson
import com.guitarapp.songsbook.data.local.SongDao
import com.guitarapp.songsbook.data.local.SongEntity
import com.guitarapp.songsbook.domain.model.Song

class AssetSongRepository(
    private val assetManager: AssetManager,
    private val songDao: SongDao
) : SongRepository {

    override suspend fun getSongs(): List<Song> {
        ensureSeeded()
        return songDao.getAll().map { it.toDomain() }
    }

    override suspend fun getSongById(id: String): Song? {
        ensureSeeded()
        return songDao.getById(id)?.toDomain()
    }

    override suspend fun searchSongs(query: String): List<Song> {
        ensureSeeded()
        return songDao.search(query).map { it.toDomain() }
    }

    override suspend fun getGenres(): List<String> {
        ensureSeeded()
        return songDao.getAllGenres()
    }

    override suspend fun getDifficulties(): List<String> {
        ensureSeeded()
        return songDao.getAllDifficulties()
    }

    override suspend fun getFavorites(): List<Song> {
        ensureSeeded()
        return songDao.getFavorites().map { it.toDomain() }
    }

    override suspend fun toggleFavorite(songId: String) {
        songDao.toggleFavorite(songId)
    }

    override suspend fun insertSong(song: Song) {
        songDao.insert(SongEntity.fromDomain(song))
    }

    override suspend fun updateSong(song: Song) {
        songDao.update(SongEntity.fromDomain(song))
    }

    override suspend fun deleteSong(songId: String) {
        songDao.deleteById(songId)
    }

    private suspend fun ensureSeeded() {
        if (songDao.count() == 0) {
            seedFromAssets()
        }
    }

    private suspend fun seedFromAssets() {
        val jsonString = assetManager
            .open("songs.json")
            .bufferedReader()
            .use { it.readText() }

        val response = Gson().fromJson(jsonString, SongbookResponse::class.java)
        val entities = response.songbook.songs.map { SongEntity.fromDomain(it) }
        songDao.insertAll(entities)
    }
}