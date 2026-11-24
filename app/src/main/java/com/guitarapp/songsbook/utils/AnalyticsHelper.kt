// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.utils

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Central analytics helper. All event names are defined as constants to avoid typos.
 * Each method also logs a Crashlytics breadcrumb so crash reports include user action context.
 */
object AnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private val crashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    // ---- Screen views ----
    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    // ---- Song events ----
    /**
     * Only the internal id is sent, never the title: titles are typed by the user and the app
     * promises that song content stays on the device (see docs/PRIVACY_POLICY.md).
     */
    fun logSongOpened(songId: String) {
        crashlytics.log("song_opened (id=$songId)")
        analytics.logEvent("song_opened", Bundle().apply {
            putString("song_id", songId)
        })
    }

    fun logSongAdded() {
        crashlytics.log("song_added")
        analytics.logEvent("song_added", null)
    }

    fun logSongEdited() {
        crashlytics.log("song_edited")
        analytics.logEvent("song_edited", null)
    }

    fun logSongDeleted() {
        crashlytics.log("song_deleted")
        analytics.logEvent("song_deleted", null)
    }

    // ---- Settings events ----
    fun logNotationChanged(notation: String) {
        crashlytics.log("notation_changed: $notation")
        analytics.logEvent("notation_changed", Bundle().apply {
            putString("notation", notation)
        })
    }

    // ---- Playlist events ----
    fun logPlaylistCreated() {
        crashlytics.log("playlist_created")
        analytics.logEvent("playlist_created", null)
    }
}
