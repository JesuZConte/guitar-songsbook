package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.ChordPosition
import com.guitarapp.songsbook.domain.model.SongLine
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaginationTest {

    private fun makeLine(text: String, hasChord: Boolean = false): SongLine {
        val chords = if (hasChord) listOf(ChordPosition("Am", 0)) else emptyList()
        return SongLine(text = text, chords = chords)
    }

    private fun makeSection(type: String, number: Int, lineCount: Int = 0, hasChords: Boolean = false): SongSection {
        val lines = (1..lineCount).map { makeLine("Line $it", hasChords) }
        return SongSection(type = type, number = number, lines = lines)
    }

    @Test
    fun `empty sections produce single empty page`() {
        val pages = ReaderViewModel.paginateContent(emptyList(), fontSize = 14, pageHeightDp = 550f)
        assertEquals(1, pages.size)
        assertEquals(0, pages[0].size)
    }

    @Test
    fun `short sections fit on one page`() {
        val sections = listOf(
            makeSection("verse", 1, lineCount = 4),
            makeSection("chorus", 1, lineCount = 4)
        )
        // 2 sections with 4 short lines each should fit in 550dp
        val pages = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 550f)
        assertEquals(1, pages.size)
        assertEquals(2, pages[0].size)
    }

    @Test
    fun `large section splits across pages`() {
        // 30 lines with chords at font 14: each line ~24dp, section header ~34dp
        // Page height 300dp → ~(300-34)/24 ≈ 11 lines per page
        val sections = listOf(
            makeSection("verse", 1, lineCount = 30, hasChords = true)
        )
        val pages = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 300f)
        assertTrue("Should split into multiple pages", pages.size > 1)
        // All lines should be preserved across pages
        val totalLines = pages.sumOf { page -> page.sumOf { it.lines.size } }
        assertEquals(30, totalLines)
    }

    @Test
    fun `larger font produces more pages`() {
        val sections = listOf(
            makeSection("verse", 1, lineCount = 20, hasChords = true)
        )
        val smallFontPages = ReaderViewModel.paginateContent(sections, fontSize = 12, pageHeightDp = 400f)
        val largeFontPages = ReaderViewModel.paginateContent(sections, fontSize = 22, pageHeightDp = 400f)
        assertTrue(
            "Larger font should produce more or equal pages",
            largeFontPages.size >= smallFontPages.size
        )
    }

    @Test
    fun `first page reduction leaves less room`() {
        val sections = listOf(
            makeSection("verse", 1, lineCount = 20, hasChords = true)
        )
        val noReduction = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 400f)
        val withReduction = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 400f, firstPageReduction = 100f)
        assertTrue(
            "First page reduction should produce more or equal pages",
            withReduction.size >= noReduction.size
        )
    }

    @Test
    fun `multiple sections paginate correctly`() {
        val sections = listOf(
            makeSection("intro", 1, lineCount = 3),
            makeSection("verse", 1, lineCount = 10, hasChords = true),
            makeSection("chorus", 1, lineCount = 8, hasChords = true),
            makeSection("verse", 2, lineCount = 10, hasChords = true),
            makeSection("outro", 1, lineCount = 3)
        )
        val pages = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 400f)
        assertTrue("Should produce multiple pages", pages.size > 1)
        // Verify all lines are preserved
        val totalLines = pages.sumOf { page -> page.sumOf { it.lines.size } }
        assertEquals(34, totalLines)
    }

    @Test
    fun `single section produces single page when small enough`() {
        val sections = listOf(makeSection("chorus", 1, lineCount = 2))
        val pages = ReaderViewModel.paginateContent(sections, fontSize = 14, pageHeightDp = 550f)
        assertEquals(1, pages.size)
        assertEquals(1, pages[0].size)
    }
}
