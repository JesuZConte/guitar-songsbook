// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.consent

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Real [ConsentGateway] backed by Google's User Messaging Platform (ADR-027).
 *
 * [debugTestDeviceId] (debug builds only) makes UMP behave as if the device were in the EEA so the
 * form can be tested; the id is printed in logcat by UMP ("Use new ConsentDebugSettings.Builder()
 * .addTestDeviceHashedId(...)").
 */
class UmpConsentGateway(
    private val activity: Activity,
    private val debugTestDeviceId: String? = null
) : ConsentGateway {

    private val info: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)

    override val canRequestAds: Boolean get() = info.canRequestAds()

    override val privacyOptionsRequired: Boolean
        get() = info.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override fun requestUpdateAndShowFormIfRequired(onFinished: () -> Unit) {
        val params = ConsentRequestParameters.Builder().apply {
            if (debugTestDeviceId != null) {
                setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .addTestDeviceHashedId(debugTestDeviceId)
                        .build()
                )
            }
        }.build()
        info.requestConsentInfoUpdate(
            activity,
            params,
            { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { onFinished() } },
            { onFinished() } // failed: keep whatever UMP has cached (fail closed on a first launch)
        )
    }

    override fun showPrivacyOptionsForm(onFinished: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { onFinished() }
    }
}
