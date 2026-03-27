package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.PlaylistRepository
import com.guitarapp.songsbook.domain.model.Playlist
import com.guitarapp.songsbook.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PlaylistsViewModel(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(PlaylistDetailUiState())
    val detailState: StateFlow<PlaylistDetailUiState> = _detailState.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val playlists = playlistRepository.getPlaylists()
                _uiState.update { it.copy(playlists = playlists, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load playlists")
                }
            }
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                playlistRepository.createPlaylist(name.trim())
                loadPlaylists()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to create playlist") }
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlistId)
                _uiState.update { state ->
                    state.copy(playlists = state.playlists.filter { it.id != playlistId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete playlist") }
            }
        }
    }

    fun loadPlaylistDetail(playlistId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, error = null) }
            try {
                val playlist = playlistRepository.getPlaylistById(playlistId)
                val songs = playlistRepository.getSongsForPlaylist(playlistId)
                _detailState.update {
                    it.copy(playlist = playlist, songs = songs, isLoading = false)
                }
            } catch (e: Exception) {
                _detailState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load playlist")
                }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
            _detailState.update { state ->
                val updatedSongs = state.songs.filter { it.id != songId }
                state.copy(
                    songs = updatedSongs,
                    playlist = state.playlist?.copy(songCount = updatedSongs.size)
                )
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            try {
                playlistRepository.addSongToPlaylist(playlistId, songId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add song") }
            }
        }
    }

    class Factory(
        private val playlistRepository: PlaylistRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistsViewModel(playlistRepository) as T
        }
    }
}
