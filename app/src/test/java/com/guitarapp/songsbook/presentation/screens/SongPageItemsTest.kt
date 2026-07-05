package com.guitarapp.songsbook.presentation.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class SongPageItemsTest {

    // Three items laid out consecutively: [0,100), [100,250), [250,300)
    private val items = listOf(
        SongItemPlacement(si = -1, li = -1, y = 0, height = 100),
        SongItemPlacement(si = 0, li = -1, y = 100, height = 150),
        SongItemPlacement(si = 0, li = 0, y = 250, height = 50)
    )

    @Test
    fun `items fully inside the range are included`() {
        val result = itemsInRange(items, startPx = 0, endPx = 300)
        assertEquals(items, result)
    }

    @Test
    fun `item ending exactly at range start is excluded`() {
        val result = itemsInRange(items, startPx = 100, endPx = 300)
        assertEquals(listOf(items[1], items[2]), result)
    }

    @Test
    fun `item starting exactly at range end is excluded`() {
        val result = itemsInRange(items, startPx = 0, endPx = 250)
        assertEquals(listOf(items[0], items[1]), result)
    }

    @Test
    fun `item straddling the range start is included for clipping`() {
        // Pixel-exact fallback break mid-item (very long single item):
        // the item must appear on both pages, clipped by the page bounds.
        val result = itemsInRange(items, startPx = 150, endPx = 300)
        assertEquals(listOf(items[1], items[2]), result)
    }

    @Test
    fun `empty when range is past all content`() {
        val result = itemsInRange(items, startPx = 300, endPx = 400)
        assertEquals(emptyList<SongItemPlacement>(), result)
    }
}