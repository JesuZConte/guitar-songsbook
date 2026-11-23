package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.utils.TextSongDraft

/**
 * Hands a draft from the Home screen to the next Add Song screen. Song text can be large, so it
 * travels through this holder instead of a navigation argument (ADR-025). Delivered once.
 */
class TextDraftHolder {
    private var draft: TextSongDraft? = null

    fun set(value: TextSongDraft) { draft = value }

    fun take(): TextSongDraft? = draft.also { draft = null }
}
