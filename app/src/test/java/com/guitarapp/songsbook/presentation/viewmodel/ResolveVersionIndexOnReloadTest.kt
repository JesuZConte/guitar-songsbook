package com.guitarapp.songsbook.presentation.viewmodel

import com.guitarapp.songsbook.domain.model.SongVersion
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveVersionIndexOnReloadTest {

    private fun version(id: Long) = SongVersion(id = id, songId = "s1", name = "v$id")

    @Test
    fun `selects the newly added version`() {
        val previous = listOf(version(1))
        val updated = listOf(version(1), version(2))

        val result = resolveVersionIndexOnReload(previous, updated, currentIndex = 0)

        assertEquals(1, result)
    }

    @Test
    fun `keeps current selection when no version was added`() {
        val previous = listOf(version(1), version(2))
        val updated = listOf(version(1), version(2))

        val result = resolveVersionIndexOnReload(previous, updated, currentIndex = 1)

        assertEquals(1, result)
    }

    @Test
    fun `keeps current selection on first load`() {
        val updated = listOf(version(1), version(2))

        val result = resolveVersionIndexOnReload(previousVersions = null, updated, currentIndex = 0)

        assertEquals(0, result)
    }

    @Test
    fun `clamps current selection when a version was removed`() {
        val previous = listOf(version(1), version(2))
        val updated = listOf(version(1))

        val result = resolveVersionIndexOnReload(previous, updated, currentIndex = 1)

        assertEquals(0, result)
    }
}