package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.utils.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val song: Song? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val fontSize: Int = 14,
    val isFullscreen: Boolean = false,
    val deleteSuccess: Boolean = false,
    val transposeSteps: Int = 0
) {
    companion object {
        const val MIN_FONT_SIZE = 10
        const val MAX_FONT_SIZE = 24
        const val DEFAULT_FONT_SIZE = 14
    }
}

class ReaderViewModel(
    private val songRepository: SongRepository,
    private val songId: String,
    initialFontSize: Int = ReaderUiState.DEFAULT_FONT_SIZE
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState(fontSize = initialFontSize))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadSong()
    }

    fun refresh() {
        loadSong()
    }

    private fun loadSong() {
        viewModelScope.launch {
            try {
                FirebaseCrashlytics.getInstance().log("ReaderViewModel: loading song $songId")
                val song = songRepository.getSongById(songId)
                if (song != null) {
                    _uiState.update { it.copy(song = song, isLoading = false) }
                    AnalyticsHelper.logSongOpened(song.id, song.title)
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Song not found") }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    /** Called by VirtualPagedSong after every layout pass with the measured page count. */
    fun onMeasuredPageCount(count: Int) {
        _uiState.update { it.copy(totalPages = count) }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun increaseFontSize() {
        _uiState.update {
            if (it.fontSize < ReaderUiState.MAX_FONT_SIZE) it.copy(fontSize = it.fontSize + 2) else it
        }
    }

    fun decreaseFontSize() {
        _uiState.update {
            if (it.fontSize > ReaderUiState.MIN_FONT_SIZE) it.copy(fontSize = it.fontSize - 2) else it
        }
    }

    fun setFontSize(size: Int) {
        val clamped = size.coerceIn(ReaderUiState.MIN_FONT_SIZE, ReaderUiState.MAX_FONT_SIZE)
        _uiState.update { it.copy(fontSize = clamped) }
    }

    fun transposeUp() {
        _uiState.update { it.copy(transposeSteps = it.transposeSteps + 1) }
    }

    fun transposeDown() {
        _uiState.update { it.copy(transposeSteps = it.transposeSteps - 1) }
    }

    fun resetTranspose() {
        _uiState.update { it.copy(transposeSteps = 0) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun toggleFavorite() {
        val songId = _uiState.value.song?.id ?: return
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
            _uiState.update { state ->
                state.copy(song = state.song?.copy(isFavorite = !state.song.isFavorite))
            }
        }
    }

    fun deleteSong() {
        val songId = _uiState.value.song?.id ?: return
        viewModelScope.launch {
            songRepository.deleteSong(songId)
            AnalyticsHelper.logSongDeleted()
            _uiState.update { it.copy(deleteSuccess = true) }
        }
    }

    class Factory(
        private val songRepository: SongRepository,
        private val songId: String,
        private val initialFontSize: Int = ReaderUiState.DEFAULT_FONT_SIZE
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(songRepository, songId, initialFontSize) as T
        }
    }
}
