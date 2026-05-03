package com.guitarapp.songsbook.ui.theme

import androidx.compose.ui.unit.dp

// =============================================================================
// LEATHER JOURNAL — elevation & shadow recipes
//
// Material's plain elevation doesn't match the brass "metallic" feel — that
// look needs an *inner* highlight + shadow plus an outer drop shadow. We
// expose the recipe as constants here; the actual painting is in
// composables/Brass.kt where we draw three layered Box backgrounds.
// =============================================================================

object CancioneroElevation {
    // Cards on cream — almost flat, a 1dp lift is plenty given the warm bg.
    val Card           = 1.dp
    // Brass FAB — sits on cream; needs visible drop.
    val FabOuterDrop   = 6.dp
    // Bottom nav — subtle separation from page.
    val BottomNav      = 4.dp
    // Leather header — relies on its own inset shadow; no Material elevation.
    val LeatherHeader  = 0.dp
}
