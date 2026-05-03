package com.guitarapp.songsbook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// =============================================================================
// CancioneroTheme — single entry point. Wrap your top-level `setContent { ... }`
// in this and Material 3 + the extended LeatherColors will be available
// everywhere.
//
// Usage:
//   setContent {
//     CancioneroTheme {
//       CancioneroNavGraph()
//     }
//   }
//
// Inside any composable:
//   val c = LocalLeatherColors.current   // for cognac/brass/stitch tokens
//   MaterialTheme.colorScheme.primary    // for stock M3 roles
// =============================================================================

@Composable
fun CancioneroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors  = if (darkTheme) DarkColors  else LightColors
    val leather = if (darkTheme) DarkLeather else LightLeather

    CompositionLocalProvider(LocalLeatherColors provides leather) {
        MaterialTheme(
            colorScheme = colors,
            typography  = CancioneroTypography,
            shapes      = CancioneroShapes,
            content     = content,
        )
    }
}
