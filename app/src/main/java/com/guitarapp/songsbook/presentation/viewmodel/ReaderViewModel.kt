package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val song: Song? = null,
    val pages: List<List<SongSection>> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val fontSize: Int = 14,
    val isFullscreen: Boolean = false
) {
    companion object {
        const val MIN_FONT_SIZE = 10
        const val MAX_FONT_SIZE = 24
        const val SECTIONS_PER_PAGE = 2
    }
}

class ReaderViewModel(
    private val songRepository: SongRepository,
    private val songId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadSong()
    }

    private fun loadSong() {
        viewModelScope.launch {
            try {
                val song = songRepository.getSongById(songId)
                if (song != null) {
                    val pages = paginateSections(song.content)
                    _uiState.update {
                        it.copy(
                            song = song,
                            pages = pages,
                            totalPages = pages.size,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Song not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun increaseFontSize() {
        _uiState.update {
            if (it.fontSize < ReaderUiState.MAX_FONT_SIZE) {
                it.copy(fontSize = it.fontSize + 2)
            } else it
        }
    }

    fun decreaseFontSize() {
        _uiState.update {
            if (it.fontSize > ReaderUiState.MIN_FONT_SIZE) {
                it.copy(fontSize = it.fontSize - 2)
            } else it
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    companion object {
        fun paginateSections(
            sections: List<SongSection>,
            sectionsPerPage: Int = ReaderUiState.SECTIONS_PER_PAGE
        ): List<List<SongSection>> {
            if (sections.isEmpty()) return listOf(emptyList())
            return sections.chunked(sectionsPerPage)
        }
    }

    class Factory(
        private val songRepository: SongRepository,
        private val songId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(songRepository, songId) as T
        }
    }
}