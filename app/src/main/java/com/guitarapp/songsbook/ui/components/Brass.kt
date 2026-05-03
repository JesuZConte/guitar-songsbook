package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LeatherColors
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

// =============================================================================
// Brass primitives
// Three reusable shapes — pill, round button, FAB — all painted with the same
// radial-gradient + inset highlight recipe so they read as one material family.
// =============================================================================

/** Radial brass gradient (top-left highlight → mid → dark edge). */
fun brassBrush(c: LeatherColors): Brush = Brush.radialGradient(
    colorStops = arrayOf(
        0.0f to c.brassLight,
        0.55f to c.brass,
        1.0f to c.brassDark,
    ),
    // off-center (top-left) so the highlight sits where light would catch.
    center = Offset.Unspecified,  // resolved at draw time; see Modifier below
    radius = Float.POSITIVE_INFINITY,
)

/** Brass surface with all three layers: drop shadow, gradient fill, dark border. */
@Composable
fun BrassSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    contentColor: Color = LocalLeatherColors.current.navyDeep,
    content: @Composable () -> Unit,
) {
    val c = LocalLeatherColors.current
    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .background(brassBrush(c), shape)
            .border(1.5.dp, c.brassDark, shape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

// ---------- Pill (active version chip, active bottom-nav item) --------------

@Composable
fun BrassPill(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 5.dp),
    content: @Composable () -> Unit,
) {
    BrassSurface(
        shape = RoundedCornerShape(18.dp),
        modifier = modifier,
        elevation = 2.dp,
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

// ---------- Round button (toolbar actions) ----------------------------------

@Composable
fun BrassButton(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BrassSurface(
        shape = CircleShape,
        modifier = modifier.size(size),
        elevation = 3.dp,
    ) { content() }
}

// ---------- FAB (primary "Add Song" action) ---------------------------------

@Composable
fun BrassFAB(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BrassSurface(
        shape = CircleShape,
        modifier = modifier.size(56.dp),
        elevation = 8.dp,
    ) { content() }
}
