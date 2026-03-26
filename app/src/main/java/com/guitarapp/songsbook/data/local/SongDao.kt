package com.guitarapp.songsbook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SongDao {

    @Query("SELECT * FROM songs")
    suspend fun getAll(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getById(songId: String): SongEntity?

    @Query(
        """
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
        OR artist LIKE '%' || :query || '%'
        """
    )
    suspend fun search(query: String): List<SongEntity>

    @Query("SELECT DISTINCT genre FROM songs ORDER BY genre")
    suspend fun getAllGenres(): List<String>

    @Query("SELECT DISTINCT difficulty FROM songs ORDER BY difficulty")
    suspend fun getAllDifficulties(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}