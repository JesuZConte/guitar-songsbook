package com.guitarapp.songsbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongCrossRef::class, SongVersionEntity::class],
    version = 4
)
@TypeConverters(Converters::class)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songVersionDao(): SongVersionDao

    companion object {
        @Volatile
        private var INSTANCE: SongDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE songs ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS playlists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS playlist_songs (
                        playlist_id INTEGER NOT NULL,
                        song_id TEXT NOT NULL,
                        PRIMARY KEY(playlist_id, song_id),
                        FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                        FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_playlist_songs_song_id ON playlist_songs(song_id)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS song_versions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        song_id TEXT NOT NULL,
                        name TEXT NOT NULL DEFAULT 'Default',
                        key TEXT NOT NULL DEFAULT '',
                        capo INTEGER NOT NULL DEFAULT 0,
                        chords TEXT NOT NULL DEFAULT '[]',
                        notes TEXT NOT NULL DEFAULT '',
                        content TEXT NOT NULL DEFAULT '[]',
                        FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_song_versions_song_id ON song_versions(song_id)"
                )
                db.execSQL(
                    """
                    INSERT INTO song_versions (song_id, name, key, capo, chords, notes, content)
                    SELECT id, 'Default', key, capo, chords, notes, content FROM songs
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): SongDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java,
                    "songbook.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
