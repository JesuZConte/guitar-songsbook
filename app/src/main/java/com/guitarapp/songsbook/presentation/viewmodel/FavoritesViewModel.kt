package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<Song> = emptyList()
)

class FavoritesViewModel(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                val favorites = songRepository.getFavorites()
                _uiState.update { it.copy(favorites = favorites) }
            } catch (e: Exception) {
                // Silent fail — empty list is shown
            }
        }
    }

    fun removeFavorite(songId: String) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
            _uiState.update { state ->
                state.copy(favorites = state.favorites.filter { it.id != songId })
            }
        }
    }

    class Factory(
        private val songRepository: SongRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoritesViewModel(songRepository) as T
        }
    }
}