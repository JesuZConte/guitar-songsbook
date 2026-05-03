package com.guitarapp.songsbook.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =============================================================================
// LEATHER JOURNAL — shape tokens
// =============================================================================

val CancioneroShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // icon-button background
    small      = RoundedCornerShape(8.dp),   // toolbar groups, action squares
    medium     = RoundedCornerShape(12.dp),  // small cards
    large      = RoundedCornerShape(16.dp),  // collection cards, sheets
    extraLarge = RoundedCornerShape(26.dp),  // FAB
)

// Pill (used for version chips and bottom-nav active state)
val PillShape = RoundedCornerShape(18.dp)
