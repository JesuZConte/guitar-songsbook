// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * One banner per Activity: created and loaded once, reused when the library screen comes back,
 * active only while the Activity is resumed and the banner is on screen, destroyed with the Activity.
 */
class BannerAdHolderTest {

    private class FakeBanner : BannerHandle {
        var loads = 0
        var pauses = 0
        var resumes = 0
        var destroyed = false
        override fun load() { loads++ }
        override fun pause() { pauses++ }
        override fun resume() { resumes++ }
        override fun destroy() { destroyed = true }
    }

    private val created = mutableListOf<FakeBanner>()
    private val holder = BannerAdHolder { FakeBanner().also { created += it } }

    @Before
    fun activityIsInForeground() {
        holder.onActivityResumed()
    }

    @Test
    fun `nothing is created until the banner is first needed`() {
        assertFalse(holder.isCreated)
        assertTrue(created.isEmpty())
    }

    @Test
    fun `obtain creates and loads the banner once and reuses it`() {
        val first = holder.obtain()
        val second = holder.obtain()

        assertSame(first, second)
        assertEquals(1, created.size)
        assertEquals(1, created[0].loads)
        assertTrue(holder.isCreated)
    }

    @Test
    fun `leaving the screen pauses the banner and coming back resumes it`() {
        holder.obtain()
        holder.onShown()
        holder.onHidden()
        holder.onShown()

        assertEquals(1, created[0].pauses)
        assertEquals(1, created[0].resumes)
    }

    @Test
    fun `activity pause and resume are forwarded while the banner is on screen`() {
        holder.obtain()
        holder.onShown()
        holder.onActivityPaused()
        holder.onActivityResumed()

        assertEquals(1, created[0].pauses)
        assertEquals(1, created[0].resumes)
    }

    @Test
    fun `activity resumed while the banner is off screen keeps it paused`() {
        holder.obtain()
        holder.onShown()
        holder.onHidden()          // user opened a song
        holder.onActivityPaused()  // app went to background from the reader
        holder.onActivityResumed() // and came back to the reader

        assertEquals(1, created[0].pauses)
        assertEquals(0, created[0].resumes)
    }

    @Test
    fun `lifecycle calls before the banner exists do nothing`() {
        holder.onActivityPaused()
        holder.onActivityResumed()
        holder.onHidden()

        assertTrue(created.isEmpty())
    }

    @Test
    fun `destroy releases the banner and a later obtain builds a fresh one`() {
        val first = holder.obtain()
        holder.destroy()

        assertTrue(created[0].destroyed)
        assertFalse(holder.isCreated)

        val second = holder.obtain()
        assertNotSame(first, second)
        assertEquals(2, created.size)
    }

    @Test
    fun `lifecycle calls after destroy are ignored`() {
        holder.obtain()
        holder.onShown()
        holder.destroy()
        holder.onHidden()
        holder.onActivityPaused()

        assertEquals(0, created[0].pauses)
        assertEquals(0, created[0].resumes)
    }
}
