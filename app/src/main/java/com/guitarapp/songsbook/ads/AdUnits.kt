// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.ads

import com.guitarapp.songsbook.BuildConfig

/**
 * Ad unit IDs. Debug builds always use Google's public test unit so that development devices
 * never generate real impressions or clicks (AdMob treats those as invalid traffic).
 */
object AdUnits {
    /** Google's documented test unit for fixed-size banners on Android. */
    const val GOOGLE_TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val PRODUCTION_BANNER = "ca-app-pub-8804586949821046/3185447057"

    fun banner(isDebug: Boolean = BuildConfig.DEBUG): String =
        if (isDebug) GOOGLE_TEST_BANNER else PRODUCTION_BANNER
}
