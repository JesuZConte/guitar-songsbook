// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Debug builds must never request real ads: impressions on dev devices count as invalid traffic. */
class AdUnitsTest {

    @Test
    fun `debug builds use Google's test banner unit`() {
        assertEquals(AdUnits.GOOGLE_TEST_BANNER, AdUnits.banner(isDebug = true))
    }

    @Test
    fun `release builds use the production banner unit`() {
        assertEquals(AdUnits.PRODUCTION_BANNER, AdUnits.banner(isDebug = false))
    }

    @Test
    fun `test and production units are different`() {
        assertNotEquals(AdUnits.GOOGLE_TEST_BANNER, AdUnits.PRODUCTION_BANNER)
    }
}
