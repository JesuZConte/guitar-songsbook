package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.data.repository.PlaylistRepository
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelReorderTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakePlaylistRepository
    private lateinit var vm: PlaylistsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakePlaylistRepository()
        vm = PlaylistsViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeSong(id: String, title: String) = Song(
        id = id, title = title, artist = "Artist", genre = "Rock",
        difficulty = "beginner", key = "G", capo = 0,
        chords = emptyList(), tags = emptyList(), notes = "", content = emptyList()
    )

    // ── reorderSongs ──

    @Test
    fun `reorderSongs updates detailState immediately with new order`() = runTest {
        val songs = listOf(makeSong("1", "A"), makeSong("2", "B"), makeSong("3", "C"))
        vm._detailState.value = PlaylistDetailUiState(
            playlist = Playlist(id = 1, name = "Show"),
            songs = songs,
            isLoading = false
        )

        val reordered = listOf(songs[2], songs[0], songs[1]) // C, A, B
        vm.reorderSongs(playlistId = 1, songs = reordered)

        assertEquals(listOf("3", "1", "2"), vm.detailState.value.songs.map { it.id })
    }

    @Test
    fun `reorderSongs persists new order to repository`() = runTest {
        val songs = listOf(makeSong("1", "A"), makeSong("2", "B"), makeSong("3", "C"))
        vm._detailState.value = PlaylistDetailUiState(
            playlist = Playlist(id = 1, name = "Show"),
            songs = songs,
            isLoading = false
        )

        val reordered = listOf(songs[1], songs[2], songs[0]) // B, C, A
        vm.reorderSongs(playlistId = 1, songs = reordered)
        advanceUntilIdle()

        assertEquals(listOf("2", "3", "1"), fakeRepo.lastReorderedIds)
    }

    @Test
    fun `reorderSongs with single song is a no-op on order`() = runTest {
        val songs = listOf(makeSong("1", "Solo"))
        vm._detailState.value = PlaylistDetailUiState(
            playlist = Playlist(id = 1, name = "Show"),
            songs = songs,
            isLoading = false
        )

        vm.reorderSongs(playlistId = 1, songs = songs)
        advanceUntilIdle()

        assertEquals(listOf("1"), vm.detailState.value.songs.map { it.id })
        assertEquals(listOf("1"), fakeRepo.lastReorderedIds)
    }

    // ── Fake ──

    private class FakePlaylistRepository : PlaylistRepository {
        var lastReorderedIds: List<String> = emptyList()

        override suspend fun getPlaylists() = emptyList<Playlist>()
        override suspend fun getPlaylistById(playlistId: Long) = null
        override suspend fun createPlaylist(name: String) = 0L
        override suspend fun deletePlaylist(playlistId: Long) {}
        override suspend fun getSongsForPlaylist(playlistId: Long) = emptyList<Song>()
        override suspend fun addSongToPlaylist(playlistId: Long, songId: String) {}
        override suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {}
        override suspend fun isSongInPlaylist(playlistId: Long, songId: String) = false
        override suspend fun reorderSongs(playlistId: Long, songIds: List<String>) {
            lastReorderedIds = songIds
        }
        override suspend fun ensureDefaultCollections() {}
    }
}
