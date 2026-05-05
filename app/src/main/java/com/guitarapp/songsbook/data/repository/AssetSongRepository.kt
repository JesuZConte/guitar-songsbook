package com.guitarapp.songsbook.data.repository

import android.content.res.AssetManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.guitarapp.songsbook.data.local.SongDao
import com.guitarapp.songsbook.data.local.SongEntity
import com.guitarapp.songsbook.data.local.SongVersionDao
import com.guitarapp.songsbook.data.local.SongVersionEntity
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AssetSongRepository(
    private val assetManager: AssetManager,
    private val songDao: SongDao,
    private val songVersionDao: SongVersionDao
) : SongRepository {

    override suspend fun getSongs(): List<Song> {
        ensureSeeded()
        return songDao.getAll().map { entity ->
            entity.toDomain().copy(
                versions = songVersionDao.getVersionsForSong(entity.id).map { it.toDomain() }
            )
        }
    }

    override suspend fun getSongById(id: String): Song? {
        ensureSeeded()
        val entity = songDao.getById(id) ?: return null
        val versions = songVersionDao.getVersionsForSong(id).map { it.toDomain() }
        return entity.toDomain().copy(versions = versions)
    }

    override suspend fun searchSongs(query: String): List<Song> {
        ensureSeeded()
        return songDao.search(query).map { entity ->
            entity.toDomain().copy(
                versions = songVersionDao.getVersionsForSong(entity.id).map { it.toDomain() }
            )
        }
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
        return songDao.getFavorites().map { entity ->
            entity.toDomain().copy(
                versions = songVersionDao.getVersionsForSong(entity.id).map { it.toDomain() }
            )
        }
    }

    override suspend fun toggleFavorite(songId: String) {
        songDao.toggleFavorite(songId)
    }

    override suspend fun insertSong(song: Song) {
        songDao.insert(SongEntity.fromDomain(song))
        val defaultVersion = SongVersionEntity(
            songId = song.id,
            name = "Default",
            key = song.key,
            capo = song.capo,
            chords = song.chords,
            notes = song.notes,
            content = song.content
        )
        songVersionDao.insert(defaultVersion)
    }

    override suspend fun updateSong(song: Song) {
        songDao.update(SongEntity.fromDomain(song))
        val existing = songVersionDao.getVersionsForSong(song.id)
        if (existing.isNotEmpty()) {
            val updated = existing[0].copy(
                key = song.key,
                capo = song.capo,
                chords = song.chords,
                notes = song.notes,
                content = song.content
            )
            songVersionDao.update(updated)
        } else {
            songVersionDao.insert(
                SongVersionEntity(
                    songId = song.id,
                    name = "Default",
                    key = song.key,
                    capo = song.capo,
                    chords = song.chords,
                    notes = song.notes,
                    content = song.content
                )
            )
        }
    }

    override suspend fun deleteSong(songId: String) {
        songDao.deleteById(songId)
    }

    override suspend fun getVersionsForSong(songId: String): List<SongVersion> =
        songVersionDao.getVersionsForSong(songId).map { it.toDomain() }

    override suspend fun getVersionById(versionId: Long): SongVersion? =
        songVersionDao.getById(versionId)?.toDomain()

    override suspend fun insertVersion(version: SongVersion): Long =
        songVersionDao.insert(SongVersionEntity.fromDomain(version))

    override suspend fun updateVersion(version: SongVersion) =
        songVersionDao.update(SongVersionEntity.fromDomain(version))

    override suspend fun deleteVersion(versionId: Long) =
        songVersionDao.deleteById(versionId)

    private val seedMutex = Mutex()
    @Volatile private var seeded = false

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (!seeded) {
                if (songDao.count() == 0) seedFromAssets()
                seeded = true
            }
        }
    }

    private suspend fun seedFromAssets() {
        try {
            FirebaseCrashlytics.getInstance().log("AssetSongRepository: seeding from assets")
            val jsonString = assetManager
                .open("songs.json")
                .bufferedReader()
                .use { it.readText() }

            val response = Gson().fromJson(jsonString, SongbookResponse::class.java)
            val entities = response.songbook.songs.map { SongEntity.fromDomain(it) }
            songDao.insertAll(entities)
            val versionEntities = entities.map { entity ->
                SongVersionEntity(
                    songId = entity.id,
                    name = "Default",
                    key = entity.key,
                    capo = entity.capo,
                    chords = entity.chords,
                    notes = entity.notes,
                    content = entity.content
                )
            }
            songVersionDao.insertAll(versionEntities)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        }
    }
}
