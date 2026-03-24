package com.guitarapp.songsbook.data.repository

import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.presentation.viewmodel.ReaderViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class PaginationTest {

    private fun makeSection(type: String, number: Int): SongSection {
        return SongSection(type = type, number = number, lines = emptyList())
    }

    @Test
    fun `empty sections produce single empty page`() {
        val pages = ReaderViewModel.paginateSections(emptyList())
        assertEquals(1, pages.size)
        assertEquals(0, pages[0].size)
    }

    @Test
    fun `sections fewer than page size stay on one page`() {
        val sections = listOf(
            makeSection("verse", 1),
            makeSection("chorus", 1)
        )
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals(1, pages.size)
        assertEquals(2, pages[0].size)
    }

    @Test
    fun `sections equal to page size stay on one page`() {
        val sections = listOf(
            makeSection("verse", 1),
            makeSection("chorus", 1),
            makeSection("verse", 2),
            makeSection("chorus", 2)
        )
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals(1, pages.size)
        assertEquals(4, pages[0].size)
    }

    @Test
    fun `sections exceeding page size split into multiple pages`() {
        val sections = listOf(
            makeSection("intro", 1),
            makeSection("verse", 1),
            makeSection("chorus", 1),
            makeSection("verse", 2),
            makeSection("chorus", 2),
            makeSection("outro", 1)
        )
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals(2, pages.size)
        assertEquals(4, pages[0].size)
        assertEquals(2, pages[1].size)
    }

    @Test
    fun `first page contains correct sections`() {
        val sections = listOf(
            makeSection("intro", 1),
            makeSection("verse", 1),
            makeSection("chorus", 1),
            makeSection("verse", 2),
            makeSection("chorus", 2)
        )
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals("intro", pages[0][0].type)
        assertEquals("verse", pages[0][1].type)
        assertEquals("chorus", pages[0][2].type)
        assertEquals("verse", pages[0][3].type)
    }

    @Test
    fun `last page contains remaining sections`() {
        val sections = listOf(
            makeSection("intro", 1),
            makeSection("verse", 1),
            makeSection("chorus", 1),
            makeSection("verse", 2),
            makeSection("chorus", 2)
        )
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals(1, pages[1].size)
        assertEquals("chorus", pages[1][0].type)
        assertEquals(2, pages[1][0].number)
    }

    @Test
    fun `single section produces single page`() {
        val sections = listOf(makeSection("chorus", 1))
        val pages = ReaderViewModel.paginateSections(sections, sectionsPerPage = 4)
        assertEquals(1, pages.size)
        assertEquals(1, pages[0].size)
    }
}