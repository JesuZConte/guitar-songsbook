// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.consent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** What the UI needs to know. Ads and analytics start blocked (ADR-027: fail closed). */
data class ConsentState(
    val adsAllowed: Boolean = false,
    val privacyOptionsRequired: Boolean = false
)

/** Thin seam over Google's UMP SDK so the decision logic has no Android types. */
interface ConsentGateway {
    /** UMP's persisted answer; true on later launches once consent was given or is not required. */
    val canRequestAds: Boolean
    val privacyOptionsRequired: Boolean

    /** Refreshes consent info and shows the form if needed. [onFinished] runs on success and on failure. */
    fun requestUpdateAndShowFormIfRequired(onFinished: () -> Unit)

    /** Reopens the form so the user can change or withdraw consent. [onFinished] runs when it closes. */
    fun showPrivacyOptionsForm(onFinished: () -> Unit)
}

/**
 * Decides when ads and analytics may run (ADR-027).
 *
 * [onAdsAllowed] fires once, the first time ads are allowed (the ads SDK cannot be un-initialized;
 * the banner itself is gated by [state]). [onAnalyticsAllowed] fires whenever the analytics decision changes.
 */
class ConsentController(
    private val gateway: ConsentGateway,
    private val onAdsAllowed: () -> Unit,
    private val onAnalyticsAllowed: (Boolean) -> Unit
) {
    private val _state = MutableStateFlow(ConsentState())
    val state: StateFlow<ConsentState> = _state.asStateFlow()

    private var started = false
    private var adsInitialized = false
    private var lastAnalytics: Boolean? = null

    fun start() {
        if (started) return
        started = true
        refresh() // use the cached answer right away, do not wait for the network
        gateway.requestUpdateAndShowFormIfRequired { refresh() }
    }

    fun showPrivacyOptions() {
        gateway.showPrivacyOptionsForm { refresh() }
    }

    private fun refresh() {
        val allowed = gateway.canRequestAds
        _state.value = ConsentState(
            adsAllowed = allowed,
            privacyOptionsRequired = gateway.privacyOptionsRequired
        )
        if (lastAnalytics != allowed) {
            lastAnalytics = allowed
            onAnalyticsAllowed(allowed)
        }
        if (allowed && !adsInitialized) {
            adsInitialized = true
            onAdsAllowed()
        }
    }
}
