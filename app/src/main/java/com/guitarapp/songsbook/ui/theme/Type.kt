package com.guitarapp.songsbook.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.guitarapp.songsbook.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// =============================================================================
// LEATHER JOURNAL — type ramp
// Two families:
//   Fraunces        — all UI text. Small-caps for titles, italic for subtitles.
//   JetBrains Mono  — chord/lyric grids only (alignment matters).
// Sizes are conservative; titles trade size for character (small-caps + tracking).
// =============================================================================

val Fraunces = FontFamily(
    Font(R.font.fraunces, FontWeight.Normal),
    Font(R.font.fraunces, FontWeight.Medium),
    Font(R.font.fraunces, FontWeight.SemiBold),
    Font(R.font.fraunces, FontWeight.Bold),
    Font(R.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.fraunces_italic, FontWeight.Medium, FontStyle.Italic),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold),
)

// Small-caps via OpenType feature tag.
// Compose: pass `fontFeatureSettings = "smcp"` on TextStyle.
private const val SmallCaps = "smcp"

val CancioneroTypography = Typography(
    // App-level wordmark — "CANCIONERO" in the leather header.
    displaySmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
        fontFeatureSettings = SmallCaps,
    ),
    // Screen title in sub-screen headers (e.g., "Amazing Grace").
    headlineSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.3.sp,
        fontFeatureSettings = SmallCaps,
    ),
    // Section header inside a "page" (e.g., "Verse I", "Chorus").
    titleMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        fontFeatureSettings = SmallCaps,
    ),
    // Card titles, list-row primary text.
    titleSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    // Body — list secondary, descriptions.
    bodyLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    // Italic subtitle — "Traditional (John Newton, 1779)".
    bodySmall = TextStyle(
        fontFamily = Fraunces,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
    // Chip / pill label.
    labelLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    // Toolbar tiny caption ("SIZE", "TRANSPOSE").
    labelSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.6.sp,
        fontFeatureSettings = SmallCaps,
    ),
)

// Use these directly when you need the chord/lyric grid alignment.
val ChordLineStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    lineHeight = 17.sp,
)

val LyricLineStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 17.sp,
)
