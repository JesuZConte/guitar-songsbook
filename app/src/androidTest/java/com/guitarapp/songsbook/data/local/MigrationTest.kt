package com.guitarapp.songsbook.data.local

import android.database.Cursor
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests: each test creates a raw SQLite database seeded with an old schema,
 * then opens it via Room (which runs the registered migrations automatically), and
 * verifies data integrity and table structure after migration.
 *
 * This approach avoids MigrationTestHelper, which has a binary incompatibility between
 * Room 2.8.4's pre-compiled serializer stubs and kotlinx-serialization 1.8.x at runtime.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    companion object {
        private const val DB_NAME = "migration-test"

        private const val V1_CREATE_SONGS = """
            CREATE TABLE IF NOT EXISTS songs (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                artist TEXT NOT NULL,
                genre TEXT NOT NULL,
                difficulty TEXT NOT NULL,
                `key` TEXT NOT NULL,
                capo INTEGER NOT NULL,
                chords TEXT NOT NULL,
                tags TEXT NOT NULL,
                notes TEXT NOT NULL,
                content TEXT NOT NULL
            )"""

        private const val V2_ADD_FAVORITE =
            "ALTER TABLE songs ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0"

        private const val V3_CREATE_PLAYLISTS = """
            CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )"""

        private const val V3_CREATE_PLAYLIST_SONGS = """
            CREATE TABLE IF NOT EXISTS playlist_songs (
                playlist_id INTEGER NOT NULL,
                song_id TEXT NOT NULL,
                PRIMARY KEY(playlist_id, song_id),
                FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
            )"""

        private const val V3_CREATE_INDEX_PLAYLIST_SONGS =
            "CREATE INDEX IF NOT EXISTS index_playlist_songs_song_id ON playlist_songs(song_id)"

        private const val V4_CREATE_SONG_VERSIONS = """
            CREATE TABLE IF NOT EXISTS song_versions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                song_id TEXT NOT NULL,
                name TEXT NOT NULL DEFAULT 'Default',
                `key` TEXT NOT NULL DEFAULT '',
                capo INTEGER NOT NULL DEFAULT 0,
                chords TEXT NOT NULL DEFAULT '[]',
                notes TEXT NOT NULL DEFAULT '',
                content TEXT NOT NULL DEFAULT '[]',
                FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
            )"""

        private const val V4_CREATE_INDEX_SONG_VERSIONS =
            "CREATE INDEX IF NOT EXISTS index_song_versions_song_id ON song_versions(song_id)"
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DB_NAME)
    }

    // ── Helpers ──

    private fun seedAndMigrate(
        startVersion: Int,
        seed: (android.database.sqlite.SQLiteDatabase) -> Unit
    ): androidx.sqlite.db.SupportSQLiteDatabase {
        context.deleteDatabase(DB_NAME)
        context.openOrCreateDatabase(DB_NAME, 0, null).apply {
            seed(this)
            version = startVersion
            close()
        }
        return Room.databaseBuilder(context, SongDatabase::class.java, DB_NAME)
            .addMigrations(*SongDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
            .openHelper
            .writableDatabase
    }

    private fun insertSong(
        db: android.database.sqlite.SQLiteDatabase,
        id: String,
        key: String = "G",
        withFavorite: Boolean = false
    ) {
        if (withFavorite) {
            db.execSQL("INSERT INTO songs (id, title, artist, genre, difficulty, `key`, capo, chords, tags, notes, content, is_favorite) VALUES ('$id', 'Song $id', 'Artist', 'Rock', 'beginner', '$key', 0, '[]', '[]', '', '[]', 0)")
        } else {
            db.execSQL("INSERT INTO songs (id, title, artist, genre, difficulty, `key`, capo, chords, tags, notes, content) VALUES ('$id', 'Song $id', 'Artist', 'Rock', 'beginner', '$key', 0, '[]', '[]', '', '[]')")
        }
    }

    private fun query(db: androidx.sqlite.db.SupportSQLiteDatabase, sql: String): Cursor =
        db.query(SimpleSQLiteQuery(sql))

    // ── 1→2: adds is_favorite column ──

    @Test
    fun migrate1To2_existingRowHasIsFavoriteDefaultZero() {
        val db = seedAndMigrate(startVersion = 1) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            insertSong(raw, "s1")
        }
        val cursor = query(db, "SELECT is_favorite FROM songs WHERE id='s1'")
        assertTrue("Row should exist after migration", cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun migrate1To2_existingDataPreserved() {
        val db = seedAndMigrate(startVersion = 1) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            insertSong(raw, "s1", key = "Am")
        }
        val cursor = query(db, "SELECT id, title FROM songs WHERE id='s1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("s1", cursor.getString(0))
        assertEquals("Song s1", cursor.getString(1))
        cursor.close()
    }

    // ── 2→3: creates playlists + playlist_songs tables ──

    @Test
    fun migrate2To3_playlistsTableExists() {
        val db = seedAndMigrate(startVersion = 2) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
        }
        val cursor = query(db, "SELECT name FROM sqlite_master WHERE type='table' AND name='playlists'")
        assertTrue("playlists table should exist", cursor.moveToFirst())
        cursor.close()
    }

    @Test
    fun migrate2To3_playlistSongsTableExists() {
        val db = seedAndMigrate(startVersion = 2) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
        }
        val cursor = query(db, "SELECT name FROM sqlite_master WHERE type='table' AND name='playlist_songs'")
        assertTrue("playlist_songs table should exist", cursor.moveToFirst())
        cursor.close()
    }

    @Test
    fun migrate2To3_existingSongsUnaffected() {
        val db = seedAndMigrate(startVersion = 2) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            insertSong(raw, "s1", withFavorite = true)
        }
        val cursor = query(db, "SELECT COUNT(*) FROM songs")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()
    }

    // ── 3→4: creates song_versions + seeds Default version ──

    @Test
    fun migrate3To4_songVersionsTableExists() {
        val db = seedAndMigrate(startVersion = 3) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
        }
        val cursor = query(db, "SELECT name FROM sqlite_master WHERE type='table' AND name='song_versions'")
        assertTrue("song_versions table should exist", cursor.moveToFirst())
        cursor.close()
    }

    @Test
    fun migrate3To4_seedsDefaultVersionForExistingSong() {
        val db = seedAndMigrate(startVersion = 3) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
            insertSong(raw, "s1", withFavorite = true)
        }
        val cursor = query(db, "SELECT name, song_id FROM song_versions WHERE song_id='s1'")
        assertTrue("Default version should be seeded for s1", cursor.moveToFirst())
        assertEquals("Default", cursor.getString(0))
        assertEquals("s1", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun migrate3To4_versionCountMatchesSongCount() {
        val db = seedAndMigrate(startVersion = 3) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
            insertSong(raw, "s1", withFavorite = true)
            insertSong(raw, "s2", withFavorite = true)
        }
        val cursor = query(db, "SELECT COUNT(*) FROM song_versions")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        cursor.close()
    }

    // ── 4→5: adds position column to playlist_songs ──

    @Test
    fun migrate4To5_positionColumnExists() {
        val db = seedAndMigrate(startVersion = 4) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
            raw.execSQL(V4_CREATE_SONG_VERSIONS)
            raw.execSQL(V4_CREATE_INDEX_SONG_VERSIONS)
        }
        val cursor = query(db, "PRAGMA table_info(playlist_songs)")
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) columns.add(cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        assertTrue("position column should exist after migration", "position" in columns)
    }

    @Test
    fun migrate4To5_backfillAssignsDistinctPositionsPerPlaylist() {
        val db = seedAndMigrate(startVersion = 4) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
            raw.execSQL(V4_CREATE_SONG_VERSIONS)
            raw.execSQL(V4_CREATE_INDEX_SONG_VERSIONS)
            insertSong(raw, "s1", withFavorite = true)
            insertSong(raw, "s2", withFavorite = true)
            insertSong(raw, "s3", withFavorite = true)
            // Playlist 1 with 3 songs
            raw.execSQL("INSERT INTO playlists (id, name) VALUES (1, 'Show')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (1, 's1')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (1, 's2')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (1, 's3')")
        }
        val cursor = query(db, "SELECT position FROM playlist_songs WHERE playlist_id=1 ORDER BY ROWID")
        val positions = mutableListOf<Int>()
        while (cursor.moveToNext()) positions.add(cursor.getInt(0))
        cursor.close()
        assertEquals(listOf(0, 1, 2), positions)
    }

    @Test
    fun migrate4To5_twoPlaylistsGetIndependentPositions() {
        val db = seedAndMigrate(startVersion = 4) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            raw.execSQL(V2_ADD_FAVORITE)
            raw.execSQL(V3_CREATE_PLAYLISTS)
            raw.execSQL(V3_CREATE_PLAYLIST_SONGS)
            raw.execSQL(V3_CREATE_INDEX_PLAYLIST_SONGS)
            raw.execSQL(V4_CREATE_SONG_VERSIONS)
            raw.execSQL(V4_CREATE_INDEX_SONG_VERSIONS)
            insertSong(raw, "s1", withFavorite = true)
            insertSong(raw, "s2", withFavorite = true)
            raw.execSQL("INSERT INTO playlists (id, name) VALUES (1, 'A')")
            raw.execSQL("INSERT INTO playlists (id, name) VALUES (2, 'B')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (1, 's1')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (2, 's1')")
            raw.execSQL("INSERT INTO playlist_songs (playlist_id, song_id) VALUES (2, 's2')")
        }
        val c1 = query(db, "SELECT position FROM playlist_songs WHERE playlist_id=1")
        assertTrue(c1.moveToFirst())
        assertEquals(0, c1.getInt(0))
        c1.close()

        val c2 = query(db, "SELECT position FROM playlist_songs WHERE playlist_id=2 ORDER BY ROWID")
        val p2 = mutableListOf<Int>()
        while (c2.moveToNext()) p2.add(c2.getInt(0))
        c2.close()
        assertEquals(listOf(0, 1), p2)
    }

    // ── Full chain: 1→4 ──

    @Test
    fun migrateFullChain_1To4_allTablesExist() {
        val db = seedAndMigrate(startVersion = 1) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            insertSong(raw, "s1")
        }
        val cursor = query(db, "SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) tables.add(cursor.getString(0))
        cursor.close()
        listOf("songs", "playlists", "playlist_songs", "song_versions").forEach { table ->
            assertTrue("Table '$table' should exist after full migration", table in tables)
        }
    }

    @Test
    fun migrateFullChain_1To4_defaultVersionSeeded() {
        val db = seedAndMigrate(startVersion = 1) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            insertSong(raw, "s1")
        }
        val cursor = query(db, "SELECT name FROM song_versions WHERE song_id='s1'")
        assertTrue("Default version should be seeded through full chain", cursor.moveToFirst())
        assertEquals("Default", cursor.getString(0))
        cursor.close()
    }

    @Test
    fun migrateFullChain_1To4_isFavoriteDefaultZero() {
        val db = seedAndMigrate(startVersion = 1) { raw ->
            raw.execSQL(V1_CREATE_SONGS)
            insertSong(raw, "s1")
        }
        val cursor = query(db, "SELECT is_favorite FROM songs WHERE id='s1'")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()
    }
}
