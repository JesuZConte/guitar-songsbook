package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.SongVersion
import com.guitarapp.songsbook.utils.BracketParser
import com.guitarapp.songsbook.utils.BracketSerializer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VersionEditorUiState(
    val name: String = "",
    val key: String = "",
    val capo: String = "0",
    val rawText: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean get() = name.isNotBlank() && rawText.isNotBlank()
}

class VersionEditorViewModel(
    private val songRepository: SongRepository,
    private val songId: String,
    private val editVersionId: Long?,
    private val sourceVersionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(VersionEditorUiState(isLoading = true))
    val uiState: StateFlow<VersionEditorUiState> = _uiState.asStateFlow()

    val isEditMode: Boolean get() = editVersionId != null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                if (editVersionId != null) {
                    val version = songRepository.getVersionById(editVersionId)
                    if (version != null) {
                        _uiState.value = VersionEditorUiState(
                            name = version.name,
                            key = version.key,
                            capo = version.capo.toString(),
                            rawText = BracketSerializer.serialize(version.content),
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Version not found")
                    }
                } else {
                    val (source, existingVersions) = coroutineScope {
                        val sourceDeferred = async { songRepository.getVersionById(sourceVersionId) }
                        val versionsDeferred = async { songRepository.getVersionsForSong(songId) }
                        sourceDeferred.await() to versionsDeferred.await()
                    }
                    val newName = "Version ${existingVersions.size + 1}"
                    _uiState.value = VersionEditorUiState(
                        name = newName,
                        key = source?.key ?: "",
                        capo = (source?.capo ?: 0).toString(),
                        rawText = if (source != null) BracketSerializer.serialize(source.content) else "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(key = value)
    }

    fun onCapoChanged(value: String) {
        _uiState.value = _uiState.value.copy(capo = value)
    }

    fun onRawTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(rawText = value)
    }

    fun saveVersion() {
        val state = _uiState.value
        if (!state.isValid) return

        val sections = BracketParser.parse(state.rawText)
        val chords = BracketParser.extractChordNames(sections)
        val capo = state.capo.toIntOrNull() ?: 0

        _uiState.value = state.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                FirebaseCrashlytics.getInstance().log("VersionEditorViewModel: saving version, editMode=$isEditMode")
                if (editVersionId != null) {
                    val existing = songRepository.getVersionById(editVersionId) ?: return@launch
                    songRepository.updateVersion(
                        existing.copy(
                            name = state.name.trim(),
                            key = state.key,
                            capo = capo,
                            chords = chords,
                            content = sections
                        )
                    )
                } else {
                    songRepository.insertVersion(
                        SongVersion(
                            songId = songId,
                            name = state.name.trim(),
                            key = state.key,
                            capo = capo,
                            chords = chords,
                            content = sections
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "Failed to save version")
            }
        }
    }

    class Factory(
        private val songRepository: SongRepository,
        private val songId: String,
        private val editVersionId: Long?,
        private val sourceVersionId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VersionEditorViewModel(songRepository, songId, editVersionId, sourceVersionId) as T
        }
    }
}
