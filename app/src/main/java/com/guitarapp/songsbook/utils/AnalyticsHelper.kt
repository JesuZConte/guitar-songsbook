package com.guitarapp.songsbook.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Central analytics helper. All event names are defined as constants to avoid typos.
 * Replace placeholder google-services.json with real one from Firebase Console before release.
 */
object AnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    // ---- Screen views ----
    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    // ---- Song events ----
    fun logSongOpened(songId: String, songTitle: String) {
        analytics.logEvent("song_opened", Bundle().apply {
            putString("song_id", songId)
            putString("song_title", songTitle)
        })
    }

    fun logSongAdded() {
        analytics.logEvent("song_added", null)
    }

    fun logSongEdited() {
        analytics.logEvent("song_edited", null)
    }

    fun logSongDeleted() {
        analytics.logEvent("song_deleted", null)
    }

    // ---- Settings events ----
    fun logNotationChanged(notation: String) {
        analytics.logEvent("notation_changed", Bundle().apply {
            putString("notation", notation)
        })
    }

    // ---- Playlist events ----
    fun logPlaylistCreated() {
        analytics.logEvent("playlist_created", null)
    }
}
