package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val songs: List<Song> = emptyList(),
    val genres: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val query: String = "",
    val selectedGenre: String? = null,
    val selectedDifficulty: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadInitialData()
        observeSearch()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val songs = songRepository.getSongs()
                val genres = songRepository.getGenres()
                val difficulties = songRepository.getDifficulties()
                _uiState.update {
                    it.copy(
                        songs = songs,
                        genres = genres,
                        difficulties = difficulties,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    performSearch(query, _uiState.value.selectedGenre, _uiState.value.selectedDifficulty)
                }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        _searchQuery.value = query
    }

    fun onGenreSelected(genre: String?) {
        val newGenre = if (genre == _uiState.value.selectedGenre) null else genre
        _uiState.update { it.copy(selectedGenre = newGenre) }
        viewModelScope.launch {
            performSearch(_uiState.value.query, newGenre, _uiState.value.selectedDifficulty)
        }
    }

    fun onDifficultySelected(difficulty: String?) {
        val newDifficulty = if (difficulty == _uiState.value.selectedDifficulty) null else difficulty
        _uiState.update { it.copy(selectedDifficulty = newDifficulty) }
        viewModelScope.launch {
            performSearch(_uiState.value.query, _uiState.value.selectedGenre, newDifficulty)
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(query = "", selectedGenre = null, selectedDifficulty = null) }
        _searchQuery.value = ""
        viewModelScope.launch {
            performSearch("", null, null)
        }
    }

    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
            _uiState.update { state ->
                state.copy(
                    songs = state.songs.map { song ->
                        if (song.id == songId) song.copy(isFavorite = !song.isFavorite)
                        else song
                    }
                )
            }
        }
    }

    fun refreshSongs() {
        viewModelScope.launch {
            try {
                performSearch(
                    _uiState.value.query,
                    _uiState.value.selectedGenre,
                    _uiState.value.selectedDifficulty
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Refresh failed") }
            }
        }
    }

    private suspend fun performSearch(query: String, genre: String?, difficulty: String?) {
        try {
            val results = if (query.isBlank()) {
                songRepository.getSongs()
            } else {
                songRepository.searchSongs(query)
            }

            val filtered = results
                .filter { song -> genre == null || song.genre == genre }
                .filter { song -> difficulty == null || song.difficulty == difficulty }

            _uiState.update { it.copy(songs = filtered) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Search failed") }
        }
    }

    class Factory(
        private val songRepository: SongRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(songRepository) as T
        }
    }
}