package com.guitarapp.songsbook.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guitarapp.songsbook.ui.components.ReaderToolbar
import com.guitarapp.songsbook.ui.theme.CancioneroTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderToolbarTest {

    @get:Rule
    val rule = createComposeRule()

    private fun launch(
        fontSizeSp: Int = 14,
        transposeSemitones: Int = 0,
        page: Int = 1,
        totalPages: Int = 3,
        onSizeDelta: (Int) -> Unit = {},
        onTransposeDelta: (Int) -> Unit = {},
    ) = rule.setContent {
        CancioneroTheme {
            ReaderToolbar(
                fontSizeSp = fontSizeSp,
                transposeSemitones = transposeSemitones,
                page = page,
                totalPages = totalPages,
                onSizeDelta = onSizeDelta,
                onTransposeDelta = onTransposeDelta,
            )
        }
    }

    // ── Rendering ──

    @Test
    fun rendersFontSizeValue() {
        launch(fontSizeSp = 14)
        rule.onNodeWithText("14sp").assertIsDisplayed()
    }

    @Test
    fun rendersTransposeZeroAsPlaceholder() {
        launch(transposeSemitones = 0)
        rule.onNodeWithText("±0").assertIsDisplayed()
    }

    @Test
    fun rendersPositiveTransposeWithPrefix() {
        launch(transposeSemitones = 3)
        rule.onNodeWithText("+3").assertIsDisplayed()
    }

    @Test
    fun rendersNegativeTranspose() {
        launch(transposeSemitones = -2)
        rule.onNodeWithText("-2").assertIsDisplayed()
    }

    @Test
    fun rendersPageIndicator() {
        launch(page = 2, totalPages = 5)
        rule.onNodeWithText("2 / 5").assertIsDisplayed()
    }

    @Test
    fun sizeAndTransposeGroupsPresent() {
        launch()
        rule.onNodeWithTag("toolbar_size_minus").assertIsDisplayed()
        rule.onNodeWithTag("toolbar_size_plus").assertIsDisplayed()
        rule.onNodeWithTag("toolbar_transpose_minus").assertIsDisplayed()
        rule.onNodeWithTag("toolbar_transpose_plus").assertIsDisplayed()
    }

    // ── Interactions ──

    @Test
    fun sizePlusButtonInvokesCallbackWithPositiveOne() {
        var received = Int.MIN_VALUE
        launch(onSizeDelta = { received = it })
        rule.onNodeWithTag("toolbar_size_plus").performClick()
        assertEquals(+1, received)
    }

    @Test
    fun sizeMinusButtonInvokesCallbackWithNegativeOne() {
        var received = Int.MIN_VALUE
        launch(onSizeDelta = { received = it })
        rule.onNodeWithTag("toolbar_size_minus").performClick()
        assertEquals(-1, received)
    }

    @Test
    fun transposePlusButtonInvokesCallbackWithPositiveOne() {
        var received = Int.MIN_VALUE
        launch(onTransposeDelta = { received = it })
        rule.onNodeWithTag("toolbar_transpose_plus").performClick()
        assertEquals(+1, received)
    }

    @Test
    fun transposeMinusButtonInvokesCallbackWithNegativeOne() {
        var received = Int.MIN_VALUE
        launch(onTransposeDelta = { received = it })
        rule.onNodeWithTag("toolbar_transpose_minus").performClick()
        assertEquals(-1, received)
    }

    @Test
    fun sizePlusAndTransposePlusAreIndependent() {
        var sizeClicks = 0
        var transposeClicks = 0
        launch(onSizeDelta = { sizeClicks++ }, onTransposeDelta = { transposeClicks++ })
        rule.onNodeWithTag("toolbar_size_plus").performClick()
        rule.onNodeWithTag("toolbar_size_plus").performClick()
        rule.onNodeWithTag("toolbar_transpose_plus").performClick()
        assertEquals(2, sizeClicks)
        assertEquals(1, transposeClicks)
    }
}
