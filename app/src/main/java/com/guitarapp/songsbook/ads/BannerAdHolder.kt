// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.ads

/** The parts of a banner the holder drives; the real one wraps an AdMob `AdView`. */
interface BannerHandle {
    fun load()
    fun pause()
    fun resume()
    fun destroy()
}

/**
 * Keeps a single banner for the lifetime of an Activity.
 *
 * The library screen leaves composition every time the user opens a song, so creating the
 * `AdView` inside the composable meant a new WebView and a new ad request on every return,
 * and the old view was never destroyed. The holder creates and loads it once, on first use,
 * and destroys it with the Activity.
 *
 * The banner is active only while the Activity is resumed **and** the banner is on screen, so
 * it does not keep refreshing behind the reader (where ads are never shown).
 */
class BannerAdHolder<H : BannerHandle>(private val create: () -> H) {
    private var banner: H? = null
    private var activityResumed = false
    private var shown = false
    private var active = false

    val isCreated: Boolean get() = banner != null

    /** Returns the banner, creating and loading it the first time. A new AdView starts active. */
    fun obtain(): H = banner ?: create().also {
        banner = it
        active = true
        it.load()
    }

    fun onShown() { shown = true; sync() }
    fun onHidden() { shown = false; sync() }
    fun onActivityResumed() { activityResumed = true; sync() }
    fun onActivityPaused() { activityResumed = false; sync() }

    fun destroy() {
        banner?.destroy()
        banner = null
        active = false
    }

    private fun sync() {
        val current = banner ?: return
        val shouldBeActive = activityResumed && shown
        if (shouldBeActive == active) return
        active = shouldBeActive
        if (shouldBeActive) current.resume() else current.pause()
    }
}
