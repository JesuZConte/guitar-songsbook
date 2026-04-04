package com.guitarapp.songsbook.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.guitarapp.songsbook.data.repository.SongRepository
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.utils.AnalyticsHelper
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
    val isFullscreen: Boolean = false,
    val deleteSuccess: Boolean = false
) {
    companion object {
        const val MIN_FONT_SIZE = 10
        const val MAX_FONT_SIZE = 24
    }
}

class ReaderViewModel(
    private val songRepository: SongRepository,
    private val songId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var availablePageHeight: Float = DEFAULT_PAGE_HEIGHT

    init {
        loadSong()
    }

    fun refresh() {
        loadSong()
    }

    fun setAvailableHeight(heightDp: Float) {
        if (availablePageHeight != heightDp) {
            availablePageHeight = heightDp
            repaginate()
        }
    }

    private fun loadSong() {
        viewModelScope.launch {
            try {
                FirebaseCrashlytics.getInstance().log("ReaderViewModel: loading song $songId")
                val song = songRepository.getSongById(songId)
                if (song != null) {
                    val fontSize = _uiState.value.fontSize
                    val headerH = estimateSongHeaderHeight(song, fontSize)
                    val pages = paginateContent(song.content, fontSize, availablePageHeight, headerH)
                    _uiState.update {
                        it.copy(
                            song = song,
                            pages = pages,
                            totalPages = pages.size,
                            isLoading = false
                        )
                    }
                    AnalyticsHelper.logSongOpened(song.id, song.title)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Song not found")
                    }
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun repaginate() {
        val song = _uiState.value.song ?: return
        val fontSize = _uiState.value.fontSize
        val headerH = estimateSongHeaderHeight(song, fontSize)
        val pages = paginateContent(song.content, fontSize, availablePageHeight, headerH)
        val currentPage = _uiState.value.currentPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
        _uiState.update {
            it.copy(pages = pages, totalPages = pages.size, currentPage = currentPage)
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
        repaginate()
    }

    fun decreaseFontSize() {
        _uiState.update {
            if (it.fontSize > ReaderUiState.MIN_FONT_SIZE) {
                it.copy(fontSize = it.fontSize - 2)
            } else it
        }
        repaginate()
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun toggleFavorite() {
        val songId = _uiState.value.song?.id ?: return
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
            _uiState.update { state ->
                state.copy(
                    song = state.song?.copy(isFavorite = !state.song.isFavorite)
                )
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

    companion object {
        private const val DEFAULT_PAGE_HEIGHT = 550f

        // Reserve space at bottom of every page for the page indicator ("1 / 2")
        private const val PAGE_INDICATOR_HEIGHT = 32f

        // Compose Text actual height ≈ fontSize × 1.75 (font metrics 1.17× + includeFontPadding).
        // Fixed "+N" offsets don't scale — this multiplier stays accurate from 10sp to 24sp.
        private const val TEXT_HEIGHT_FACTOR = 1.8f

        private fun estimateSongHeaderHeight(song: Song, fontSize: Int): Float {
            var h = 0f
            h += (fontSize + 4f) * TEXT_HEIGHT_FACTOR   // title (Merriweather, fontSize+4)
            h += (fontSize + 1f) * TEXT_HEIGHT_FACTOR + 2f  // artist + top padding
            if (song.key.isNotBlank() || song.capo > 0) {
                h += (fontSize - 2f) * TEXT_HEIGHT_FACTOR + 6f  // key/capo + row padding
            }
            if (song.notes.isNotBlank()) {
                h += (fontSize - 2f) * TEXT_HEIGHT_FACTOR + 4f  // notes + padding
            }
            h += 12f + 16f                              // divider padding + bottom padding
            return h
        }

        fun paginateContent(
            sections: List<SongSection>,
            fontSize: Int,
            pageHeightDp: Float,
            firstPageReduction: Float = 0f
        ): List<List<SongSection>> {
            if (sections.isEmpty()) return listOf(emptyList())

            // Compose Text rendered height = fontSize × TEXT_HEIGHT_FACTOR.
            // LineContent wraps in Column(padding(bottom = 2.dp)).
            fun lineHeight(line: SongLine): Float {
                val chordH = if (line.chords.isNotEmpty()) (fontSize * TEXT_HEIGHT_FACTOR) else 0f
                val textH = if (line.text.isNotBlank()) (fontSize * TEXT_HEIGHT_FACTOR) else 0f
                return chordH + textH + 2f
            }

            // SectionContent: header text (fontSize-2) × factor + 6dp bottom padding
            // + 16dp bottom padding on the section Column
            val sectionHeaderHeight = ((fontSize - 2f) * TEXT_HEIGHT_FACTOR) + 6f + 16f

            val pages = mutableListOf<MutableList<SongSection>>()
            var currentPage = mutableListOf<SongSection>()
            var pageMaxHeight = pageHeightDp - firstPageReduction - PAGE_INDICATOR_HEIGHT
            var currentHeight = 0f

            for (section in sections) {
                var linesForPage = mutableListOf<SongLine>()
                var sectionHeight = sectionHeaderHeight

                for (line in section.lines) {
                    val lh = lineHeight(line)

                    if (currentHeight + sectionHeight + lh > pageMaxHeight &&
                        (currentPage.isNotEmpty() || linesForPage.isNotEmpty())
                    ) {
                        // Save accumulated lines as a (possibly partial) section
                        if (linesForPage.isNotEmpty()) {
                            currentPage.add(SongSection(section.type, section.number, linesForPage.toList()))
                        }
                        if (currentPage.isNotEmpty()) {
                            pages.add(currentPage)
                        }

                        // Start a new page
                        currentPage = mutableListOf()
                        currentHeight = 0f
                        pageMaxHeight = pageHeightDp - PAGE_INDICATOR_HEIGHT
                        linesForPage = mutableListOf()
                        sectionHeight = sectionHeaderHeight // continuation header
                    }

                    linesForPage.add(line)
                    sectionHeight += lh
                }

                // Add remaining lines of this section to the current page
                if (linesForPage.isNotEmpty()) {
                    currentPage.add(SongSection(section.type, section.number, linesForPage.toList()))
                    currentHeight += sectionHeight
                }
            }

            if (currentPage.isNotEmpty()) {
                pages.add(currentPage)
            }

            if (pages.isEmpty()) {
                pages.add(mutableListOf())
            }

            return pages
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
