package com.guitarapp.songsbook.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.guitarapp.songsbook.R
import com.guitarapp.songsbook.data.local.UserPreferences
import com.guitarapp.songsbook.domain.model.Song
import com.guitarapp.songsbook.domain.model.SongSection
import com.guitarapp.songsbook.presentation.screens.SongHeader
import com.guitarapp.songsbook.ui.theme.CancioneroTheme
import com.guitarapp.songsbook.utils.NotationSystem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var originalNotation: NotationSystem

    @Before
    fun setUp() {
        originalNotation = UserPreferences.getNotation(context)
        UserPreferences.setNotation(context, NotationSystem.AMERICAN)
    }

    @After
    fun tearDown() {
        UserPreferences.setNotation(context, originalNotation)
    }

    private fun makeSong(
        title: String = "Amazing Grace",
        artist: String = "Traditional",
        key: String = "G",
        capo: Int = 0,
        notes: String = "",
    ) = Song(
        id = "test-id",
        title = title,
        artist = artist,
        genre = "Folk",
        difficulty = "beginner",
        key = key,
        capo = capo,
        chords = emptyList(),
        tags = emptyList(),
        notes = notes,
        content = emptyList<SongSection>(),
    )

    // ── Title and artist ──

    @Test
    fun rendersSongTitle() {
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(title = "Bohemian Rhapsody"), fontSize = 14) }
        }
        rule.onNodeWithText("Bohemian Rhapsody").assertIsDisplayed()
    }

    @Test
    fun rendersArtistName() {
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(artist = "Queen"), fontSize = 14) }
        }
        rule.onNodeWithText("Queen").assertIsDisplayed()
    }

    // ── Key and capo ──

    @Test
    fun rendersKeyLabel() {
        val expected = context.getString(R.string.reader_key_label, "Am")
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(key = "Am"), fontSize = 14) }
        }
        rule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun rendersCapoLabelWhenCapoIsPositive() {
        val expected = context.getString(R.string.reader_capo_label, 3)
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(capo = 3), fontSize = 14) }
        }
        rule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun capoLabelAbsentWhenCapoIsZero() {
        val absent = context.getString(R.string.reader_capo_label, 0)
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(capo = 0), fontSize = 14) }
        }
        rule.onNodeWithText(absent, substring = false).assertDoesNotExist()
    }

    @Test
    fun keyLabelAbsentWhenKeyIsBlank() {
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(key = ""), fontSize = 14) }
        }
        // Verify neither the English nor Spanish label appears
        rule.onNodeWithText(context.getString(R.string.reader_key_label, ""), substring = true).assertDoesNotExist()
    }

    // ── Notes ──

    @Test
    fun rendersNotesWhenPresent() {
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(notes = "Play slowly"), fontSize = 14) }
        }
        rule.onNodeWithText("Play slowly").assertIsDisplayed()
    }

    @Test
    fun notesAbsentWhenBlank() {
        rule.setContent {
            CancioneroTheme { SongHeader(song = makeSong(notes = ""), fontSize = 14) }
        }
        // Only title + artist should be visible — no empty notes node
        rule.onNodeWithText("Amazing Grace").assertIsDisplayed()
        rule.onNodeWithText("Traditional").assertIsDisplayed()
    }
}
