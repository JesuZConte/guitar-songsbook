// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.consent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ADR-027: ads and analytics stay off until the consent flow says they are allowed. */
class ConsentControllerTest {

    private class FakeGateway(
        override var canRequestAds: Boolean = false,
        override var privacyOptionsRequired: Boolean = false
    ) : ConsentGateway {
        var pendingUpdate: (() -> Unit)? = null
        var pendingOptionsForm: (() -> Unit)? = null
        var updateRequests = 0

        override fun requestUpdateAndShowFormIfRequired(onFinished: () -> Unit) {
            updateRequests++
            pendingUpdate = onFinished
        }

        override fun showPrivacyOptionsForm(onFinished: () -> Unit) {
            pendingOptionsForm = onFinished
        }

        /** The user finishes the form (or the update fails): UMP always calls back. */
        fun finishUpdate(canRequestAds: Boolean, privacyOptionsRequired: Boolean = this.privacyOptionsRequired) {
            this.canRequestAds = canRequestAds
            this.privacyOptionsRequired = privacyOptionsRequired
            pendingUpdate?.invoke()
        }

        fun finishOptionsForm(canRequestAds: Boolean) {
            this.canRequestAds = canRequestAds
            pendingOptionsForm?.invoke()
        }
    }

    private class Harness(gateway: FakeGateway) {
        var adsAllowedCalls = 0
        val analyticsCalls = mutableListOf<Boolean>()
        val controller = ConsentController(
            gateway = gateway,
            onAdsAllowed = { adsAllowedCalls++ },
            onAnalyticsAllowed = { analyticsCalls += it }
        )
    }

    @Test
    fun `fresh install with no cached consent blocks ads and analytics until the update finishes`() {
        val gateway = FakeGateway(canRequestAds = false)
        val h = Harness(gateway)

        h.controller.start()

        assertFalse(h.controller.state.value.adsAllowed)
        assertEquals(0, h.adsAllowedCalls)
        assertEquals(listOf(false), h.analyticsCalls)
    }

    @Test
    fun `cached consent from a previous launch allows ads immediately without waiting for the network`() {
        val gateway = FakeGateway(canRequestAds = true)
        val h = Harness(gateway)

        h.controller.start()

        assertTrue(h.controller.state.value.adsAllowed)
        assertEquals(1, h.adsAllowedCalls)
        assertEquals(listOf(true), h.analyticsCalls)
        assertEquals(1, gateway.updateRequests)
    }

    @Test
    fun `accepting the consent form enables ads and analytics`() {
        val gateway = FakeGateway(canRequestAds = false)
        val h = Harness(gateway)
        h.controller.start()

        gateway.finishUpdate(canRequestAds = true)

        assertTrue(h.controller.state.value.adsAllowed)
        assertEquals(1, h.adsAllowedCalls)
        assertEquals(listOf(false, true), h.analyticsCalls)
    }

    @Test
    fun `declining or a failed update keeps everything blocked`() {
        val gateway = FakeGateway(canRequestAds = false)
        val h = Harness(gateway)
        h.controller.start()

        gateway.finishUpdate(canRequestAds = false)

        assertFalse(h.controller.state.value.adsAllowed)
        assertEquals(0, h.adsAllowedCalls)
        assertEquals(listOf(false), h.analyticsCalls)
    }

    @Test
    fun `region where consent is not required allows ads right after the update`() {
        val gateway = FakeGateway(canRequestAds = false, privacyOptionsRequired = false)
        val h = Harness(gateway)
        h.controller.start()

        gateway.finishUpdate(canRequestAds = true, privacyOptionsRequired = false)

        assertTrue(h.controller.state.value.adsAllowed)
        assertFalse(h.controller.state.value.privacyOptionsRequired)
    }

    @Test
    fun `privacy options entry is exposed only when the SDK requires it`() {
        val gateway = FakeGateway(canRequestAds = false)
        val h = Harness(gateway)
        h.controller.start()
        assertFalse(h.controller.state.value.privacyOptionsRequired)

        gateway.finishUpdate(canRequestAds = true, privacyOptionsRequired = true)

        assertTrue(h.controller.state.value.privacyOptionsRequired)
    }

    @Test
    fun `ads SDK initialization is requested only once even after several refreshes`() {
        val gateway = FakeGateway(canRequestAds = true)
        val h = Harness(gateway)
        h.controller.start()
        gateway.finishUpdate(canRequestAds = true)

        h.controller.showPrivacyOptions()
        gateway.finishOptionsForm(canRequestAds = true)

        assertEquals(1, h.adsAllowedCalls)
    }

    @Test
    fun `withdrawing consent from the privacy options form blocks ads and turns analytics off`() {
        val gateway = FakeGateway(canRequestAds = true, privacyOptionsRequired = true)
        val h = Harness(gateway)
        h.controller.start()
        gateway.finishUpdate(canRequestAds = true)

        h.controller.showPrivacyOptions()
        gateway.finishOptionsForm(canRequestAds = false)

        assertFalse(h.controller.state.value.adsAllowed)
        assertEquals(listOf(true, false), h.analyticsCalls)
    }

    @Test
    fun `analytics callback fires only when the decision changes`() {
        val gateway = FakeGateway(canRequestAds = true)
        val h = Harness(gateway)
        h.controller.start()

        gateway.finishUpdate(canRequestAds = true)
        gateway.finishUpdate(canRequestAds = true)

        assertEquals(listOf(true), h.analyticsCalls)
    }

    @Test
    fun `calling start twice (activity recreated) does not request a second update`() {
        val gateway = FakeGateway(canRequestAds = false)
        val h = Harness(gateway)

        h.controller.start()
        h.controller.start()

        assertEquals(1, gateway.updateRequests)
    }
}
