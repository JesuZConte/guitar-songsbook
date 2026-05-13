package com.guitarapp.songsbook.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// LEATHER JOURNAL — color tokens
// Hand-translated from the design system in Songbook Vintage Directions.html.
// Two layers:
//   1. Material 3 ColorScheme  — feeds MaterialTheme.colorScheme.*
//   2. LeatherColors            — extended palette (cognac fade, brass, stitch)
//      exposed via LocalLeatherColors for composables that need the full set.
// =============================================================================

// ---------- 1. MATERIAL 3 COLORSCHEME ---------------------------------------

val LightColors = lightColorScheme(
    primary           = Color(0xFFC89A3F),  // brass mid — selected chips, FAB
    onPrimary         = Color(0xFF1B2A3E),  // navy text on brass
    primaryContainer  = Color(0xFFE6C26B),  // brass light — pill backgrounds
    onPrimaryContainer = Color(0xFF2A1A0E), // ink on brass

    secondary         = Color(0xFF8A4A22),  // cognac — leather chrome
    onSecondary       = Color(0xFFFAF3DE),  // cream
    secondaryContainer = Color(0xFFB56A38), // cognac top
    onSecondaryContainer = Color(0xFFFAF3DE),

    tertiary          = Color(0xFF1B2A3E),  // navy — bottom of leather fade, nav
    onTertiary        = Color(0xFFFAF3DE),

    background        = Color(0xFFFBF3E2),  // cream page
    onBackground      = Color(0xFF2A1A0E),  // ink

    surface           = Color(0xFFFBF3E2),  // cream
    onSurface         = Color(0xFF2A1A0E),
    surfaceVariant    = Color(0xFFF7E4BE),  // peach card
    onSurfaceVariant  = Color(0xFF5C4A32),  // ink soft

    outline           = Color(0xFFE0C898),  // hairline rule
    outlineVariant    = Color(0xFFEAD9B0),  // faint rule

    error             = Color(0xFFB53225),  // accent red (trash)
    onError           = Color(0xFFFAF3DE),
)

val DarkColors = darkColorScheme(
    primary           = Color(0xFFE6C26B),  // brass light reads better on navy
    onPrimary         = Color(0xFF121B29),
    primaryContainer  = Color(0xFFC89A3F),
    onPrimaryContainer = Color(0xFFECD9A6),

    secondary         = Color(0xFF6A3A1C),
    onSecondary       = Color(0xFFECD9A6),
    secondaryContainer = Color(0xFF3C1F0C),
    onSecondaryContainer = Color(0xFFECD9A6),

    tertiary          = Color(0xFF0C1420),
    onTertiary        = Color(0xFFECD9A6),

    background        = Color(0xFF121B29),  // navy page
    onBackground      = Color(0xFFECD9A6),  // cream-gold ink

    surface           = Color(0xFF121B29),
    onSurface         = Color(0xFFECD9A6),
    surfaceVariant    = Color(0xFF243042),
    onSurfaceVariant  = Color(0xFFB29B6A),

    outline           = Color(0xFF3A4458),
    outlineVariant    = Color(0xFF28323E),

    error             = Color(0xFFE08A6A),
    onError           = Color(0xFF121B29),
)

// ---------- 2. EXTENDED LEATHER PALETTE -------------------------------------
// These don't map cleanly to M3 roles — they describe the gradient stops,
// stitch translucency, and brass shading used by the leather chrome.

@Immutable
data class LeatherColors(
    // Leather header gradient stops — top → mid → deep
    val leatherTop: Color,
    val leatherMid: Color,
    val leatherDeep: Color,
    // Stitching (translucent cream)
    val stitch: Color,
    // Cream foreground on leather
    val cream: Color,
    val creamSoft: Color,
    // Body text on cream/peach
    val ink: Color,
    val inkSoft: Color,
    val inkFaint: Color,
    // Hairlines
    val rule: Color,
    val ruleFaint: Color,
    // Brass — three stops for the radial gradient
    val brassLight: Color,
    val brass: Color,
    val brassDark: Color,
    // Section/accent
    val section: Color,    // section labels (chord rows, "Verse I")
    val accent: Color,     // destructive (trash)
    // Navy for nav bottom
    val navy: Color,
    val navyDeep: Color,
)

val LightLeather = LeatherColors(
    leatherTop  = Color(0xFFB56A38),
    leatherMid  = Color(0xFF8A4A22),
    leatherDeep = Color(0xFF1B2A3E),
    stitch      = Color(0x73FAF3DE),  // 45% cream
    cream       = Color(0xFFFAF3DE),
    creamSoft   = Color(0xC7FAF3DE),  // 78% cream
    ink         = Color(0xFF2A1A0E),
    inkSoft     = Color(0xFF5C4A32),
    inkFaint    = Color(0xFF8A7656),
    rule        = Color(0xFFE0C898),
    ruleFaint   = Color(0xFFEAD9B0),
    brassLight  = Color(0xFFF2CF58),
    brass       = Color(0xFFC89A3F),
    brassDark   = Color(0xFF8F6A20),
    section     = Color(0xFFB07020),
    accent      = Color(0xFFB53225),
    navy        = Color(0xFF1B2A3E),
    navyDeep    = Color(0xFF121B29),
)

val DarkLeather = LeatherColors(
    leatherTop  = Color(0xFF6A3A1C),
    leatherMid  = Color(0xFF3C1F0C),
    leatherDeep = Color(0xFF0C1420),
    stitch      = Color(0x40ECD9A6),  // 25% cream-gold
    cream       = Color(0xFFECD9A6),
    creamSoft   = Color(0xB3ECD9A6),  // 70%
    ink         = Color(0xFFECD9A6),
    inkSoft     = Color(0xFFB29B6A),
    inkFaint    = Color(0xFF6A5A3A),
    rule        = Color(0xFF3A4458),
    ruleFaint   = Color(0xFF28323E),
    brassLight  = Color(0xFFF2CF58),
    brass       = Color(0xFFC89A3F),
    brassDark   = Color(0xFF8F6A20),
    section     = Color(0xFFD9A858),
    accent      = Color(0xFFE08A6A),
    navy        = Color(0xFF0C1420),
    navyDeep    = Color(0xFF060A12),
)

val LocalLeatherColors = compositionLocalOf<LeatherColors> {
    error("LeatherColors not provided — wrap your tree in CancioneroTheme")
}

// Nocturno reader mode: warm deep-black night theme built on top of DarkColors so all
// unspecified M3 tokens (surfaceContainer, inverseSurface, scrim, etc.) stay consistent.
val NocturnoColorScheme = DarkColors.copy(
    primary            = Color(0xFFD9A858),  // amber-gold instead of brass-light
    onPrimary          = Color(0xFF0A0A0A),
    primaryContainer   = Color(0xFF3A2800),
    onPrimaryContainer = Color(0xFFFFDFA0),
    secondary          = Color(0xFF3A2800),
    onSecondary        = Color(0xFFD9A858),
    secondaryContainer = Color(0xFF1E1600),
    onSecondaryContainer = Color(0xFFD9A858),
    background         = Color(0xFF0A0800),  // near-black warm vs navy
    surface            = Color(0xFF0F0B00),
    surfaceVariant     = Color(0xFF1E1600),
    onSurfaceVariant   = Color(0xFFB29B6A),
    outline            = Color(0xFF3A2800),
    outlineVariant     = Color(0xFF1E1600),
)
