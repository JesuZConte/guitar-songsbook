package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelStateTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Prevents viewModelScope from crashing on Dispatchers.Main — coroutines are queued
        // but never run (we don't advance the dispatcher), so Firebase is never touched.
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(initialFontSize: Int = ReaderUiState.DEFAULT_FONT_SIZE) =
        ReaderViewModel(FakeSongRepository(), "test-id", initialFontSize)

    // ── Font size ──

    @Test
    fun `increaseFontSize increments by 2`() {
        val vm = makeViewModel(initialFontSize = 14)
        vm.increaseFontSize()
        assertEquals(16, vm.uiState.value.fontSize)
    }

    @Test
    fun `increaseFontSize does not exceed MAX_FONT_SIZE`() {
        val vm = makeViewModel(initialFontSize = ReaderUiState.MAX_FONT_SIZE)
        vm.increaseFontSize()
        assertEquals(ReaderUiState.MAX_FONT_SIZE, vm.uiState.value.fontSize)
    }

    @Test
    fun `decreaseFontSize decrements by 2`() {
        val vm = makeViewModel(initialFontSize = 14)
        vm.decreaseFontSize()
        assertEquals(12, vm.uiState.value.fontSize)
    }

    @Test
    fun `decreaseFontSize does not go below MIN_FONT_SIZE`() {
        val vm = makeViewModel(initialFontSize = ReaderUiState.MIN_FONT_SIZE)
        vm.decreaseFontSize()
        assertEquals(ReaderUiState.MIN_FONT_SIZE, vm.uiState.value.fontSize)
    }

    @Test
    fun `setFontSize clamps low values to MIN_FONT_SIZE`() {
        val vm = makeViewModel()
        vm.setFontSize(0)
        assertEquals(ReaderUiState.MIN_FONT_SIZE, vm.uiState.value.fontSize)
    }

    @Test
    fun `setFontSize clamps high values to MAX_FONT_SIZE`() {
        val vm = makeViewModel()
        vm.setFontSize(999)
        assertEquals(ReaderUiState.MAX_FONT_SIZE, vm.uiState.value.fontSize)
    }

    @Test
    fun `setFontSize accepts value within range`() {
        val vm = makeViewModel()
        vm.setFontSize(18)
        assertEquals(18, vm.uiState.value.fontSize)
    }

    // ── Transpose ──

    @Test
    fun `transposeUp increments transposeSteps by 1`() {
        val vm = makeViewModel()
        vm.transposeUp()
        assertEquals(1, vm.uiState.value.transposeSteps)
    }

    @Test
    fun `transposeDown decrements transposeSteps by 1`() {
        val vm = makeViewModel()
        vm.transposeDown()
        assertEquals(-1, vm.uiState.value.transposeSteps)
    }

    @Test
    fun `multiple transposeUp calls accumulate`() {
        val vm = makeViewModel()
        repeat(5) { vm.transposeUp() }
        assertEquals(5, vm.uiState.value.transposeSteps)
    }

    @Test
    fun `resetTranspose returns steps to zero`() {
        val vm = makeViewModel()
        repeat(3) { vm.transposeUp() }
        vm.resetTranspose()
        assertEquals(0, vm.uiState.value.transposeSteps)
    }

    // ── Version selection ──

    @Test
    fun `selectVersion updates selectedVersionIndex`() {
        val vm = makeViewModel()
        vm.selectVersion(2)
        assertEquals(2, vm.uiState.value.selectedVersionIndex)
    }

    @Test
    fun `selectVersion resets currentPage to 0`() {
        val vm = makeViewModel()
        vm.onPageChanged(3)
        vm.selectVersion(1)
        assertEquals(0, vm.uiState.value.currentPage)
    }

    // ── Page tracking ──

    @Test
    fun `onMeasuredPageCount updates totalPages`() {
        val vm = makeViewModel()
        vm.onMeasuredPageCount(5)
        assertEquals(5, vm.uiState.value.totalPages)
    }

    @Test
    fun `onPageChanged updates currentPage`() {
        val vm = makeViewModel()
        vm.onMeasuredPageCount(4)
        vm.onPageChanged(2)
        assertEquals(2, vm.uiState.value.currentPage)
    }

    // ── Toggle states ──

    @Test
    fun `toggleFullscreen flips from false to true`() {
        val vm = makeViewModel()
        assertFalse(vm.uiState.value.isFullscreen)
        vm.toggleFullscreen()
        assertTrue(vm.uiState.value.isFullscreen)
    }

    @Test
    fun `toggleFullscreen flips back to false`() {
        val vm = makeViewModel()
        vm.toggleFullscreen()
        vm.toggleFullscreen()
        assertFalse(vm.uiState.value.isFullscreen)
    }

    @Test
    fun `toggleNocturno flips from false to true`() {
        val vm = makeViewModel()
        assertFalse(vm.uiState.value.isNocturno)
        vm.toggleNocturno()
        assertTrue(vm.uiState.value.isNocturno)
    }

    @Test
    fun `toggleNocturno flips back to false`() {
        val vm = makeViewModel()
        vm.toggleNocturno()
        vm.toggleNocturno()
        assertFalse(vm.uiState.value.isNocturno)
    }

    // ── Fake repository — all methods are no-ops ──

    private class FakeSongRepository : SongRepository {
        override suspend fun getSongs() = emptyList<Song>()
        override suspend fun getSongById(id: String) = null
        override suspend fun findSongsByTitle(title: String) = emptyList<Song>()
        override suspend fun searchSongs(query: String) = emptyList<Song>()
        override suspend fun getGenres() = emptyList<String>()
        override suspend fun getDifficulties() = emptyList<String>()
        override suspend fun getFavorites() = emptyList<Song>()
        override suspend fun toggleFavorite(songId: String) {}
        override suspend fun insertSong(song: Song) {}
        override suspend fun updateSong(song: Song) {}
        override suspend fun deleteSong(songId: String) {}
        override suspend fun getVersionsForSong(songId: String) = emptyList<SongVersion>()
        override suspend fun getVersionById(versionId: Long) = null
        override suspend fun insertVersion(version: SongVersion) = 0L
        override suspend fun updateVersion(version: SongVersion) {}
        override suspend fun deleteVersion(versionId: Long) {}
    }
}
