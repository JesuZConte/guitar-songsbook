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
        if (songDao.count() == 0) {
            seedFromAssets()
        }
        return songDao.getAll().map { it.toDomain() }
    }

    override suspend fun getSongById(id: String): Song? {
        return songDao.getById(id)?.toDomain()
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