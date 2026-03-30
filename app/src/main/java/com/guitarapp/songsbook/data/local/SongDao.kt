package com.guitarapp.songsbook.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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

    @Query("SELECT * FROM songs WHERE is_favorite = 1")
    suspend fun getFavorites(): List<SongEntity>

    @Query("UPDATE songs SET is_favorite = NOT is_favorite WHERE id = :songId")
    suspend fun toggleFavorite(songId: String)

    @Query("SELECT DISTINCT genre FROM songs ORDER BY genre")
    suspend fun getAllGenres(): List<String>

    @Query("SELECT DISTINCT difficulty FROM songs ORDER BY difficulty")
    suspend fun getAllDifficulties(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteById(songId: String)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}