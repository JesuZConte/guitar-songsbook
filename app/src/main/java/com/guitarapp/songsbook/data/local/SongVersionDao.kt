package com.guitarapp.songsbook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SongVersionDao {

    @Query("SELECT * FROM song_versions WHERE song_id = :songId ORDER BY id ASC")
    suspend fun getVersionsForSong(songId: String): List<SongVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(version: SongVersionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(versions: List<SongVersionEntity>)

    @Update
    suspend fun update(version: SongVersionEntity)

    @Query("DELETE FROM song_versions WHERE id = :versionId")
    suspend fun deleteById(versionId: Long)

    @Query("SELECT * FROM song_versions WHERE id = :versionId")
    suspend fun getById(versionId: Long): SongVersionEntity?

    @Query("DELETE FROM song_versions WHERE song_id = :songId")
    suspend fun deleteAllForSong(songId: String)
}
