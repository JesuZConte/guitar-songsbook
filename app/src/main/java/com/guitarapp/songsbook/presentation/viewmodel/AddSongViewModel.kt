package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.utils.BracketParser
import com.guitarapp.songsbook.utils.BracketSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AddSongUiState(
    val title: String = "",
    val artist: String = "",
    val key: String = "",
    val capo: String = "0",
    val genre: String = "",
    val difficulty: String = "beginner",
    val rawText: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && artist.isNotBlank() && rawText.isNotBlank()
}

class AddSongViewModel(
    private val songRepository: SongRepository,
    private val editSongId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSongUiState())
    val uiState: StateFlow<AddSongUiState> = _uiState.asStateFlow()

    val isEditMode: Boolean get() = editSongId != null

    init {
        if (editSongId != null) loadSongForEdit(editSongId)
    }

    private fun loadSongForEdit(songId: String) {
        viewModelScope.launch {
            val song = songRepository.getSongById(songId) ?: return@launch
            _uiState.value = AddSongUiState(
                title = song.title,
                artist = song.artist,
                key = song.key,
                capo = song.capo.toString(),
                genre = song.genre,
                difficulty = song.difficulty,
                rawText = BracketSerializer.serialize(song.content)
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onArtistChanged(value: String) {
        _uiState.value = _uiState.value.copy(artist = value)
    }

    fun onKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(key = value)
    }

    fun onCapoChanged(value: String) {
        _uiState.value = _uiState.value.copy(capo = value)
    }

    fun onGenreChanged(value: String) {
        _uiState.value = _uiState.value.copy(genre = value)
    }

    fun onDifficultyChanged(value: String) {
        _uiState.value = _uiState.value.copy(difficulty = value)
    }

    fun onRawTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(rawText = value)
    }

    fun buildPreviewSong(): Song? {
        val state = _uiState.value
        if (!state.isValid) return null

        val sections = BracketParser.parse(state.rawText)
        val chordNames = BracketParser.extractChordNames(sections)
        val detectedKey = if (state.key.isBlank() && chordNames.isNotEmpty()) {
            chordNames.first()
        } else {
            state.key
        }

        return Song(
            id = "",
            title = state.title.trim(),
            artist = state.artist.trim(),
            genre = state.genre.trim(),
            difficulty = state.difficulty,
            key = detectedKey,
            capo = state.capo.toIntOrNull() ?: 0,
            chords = chordNames,
            tags = emptyList(),
            notes = "",
            content = sections
        )
    }

    fun saveSong() {
        val song = buildPreviewSong() ?: return
        val songWithId = if (editSongId != null) {
            song.copy(id = editSongId)
        } else {
            song.copy(id = UUID.randomUUID().toString())
        }

        _uiState.value = _uiState.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                if (editSongId != null) {
                    songRepository.updateSong(songWithId)
                } else {
                    songRepository.insertSong(songWithId)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save song"
                )
            }
        }
    }

    companion object {
        var pendingPreview: Song? = null
    }

    class Factory(
        private val songRepository: SongRepository,
        private val editSongId: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddSongViewModel(songRepository, editSongId) as T
        }
    }
}
