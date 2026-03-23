package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SongDetailUiState(
    val song: Song? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class SongDetailViewModel(
    private val songRepository: SongRepository,
    private val songId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState: StateFlow<SongDetailUiState> = _uiState.asStateFlow()

    init {
        loadSong()
    }

    private fun loadSong() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val song = songRepository.getSongById(songId)
                _uiState.value = SongDetailUiState(
                    song = song,
                    isLoading = false,
                    error = if (song == null) "Song not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = SongDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    class Factory(
        private val songRepository: SongRepository,
        private val songId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SongDetailViewModel(songRepository, songId) as T
        }
    }
}