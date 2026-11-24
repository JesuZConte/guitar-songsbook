// SPDX-FileCopyrightText: 2026 Luis Jesus
// SPDX-License-Identifier: GPL-3.0-or-later

package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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

/** Diagonal brass gradient: top-left highlight sweeps to bottom-right shadow. */
fun brassBrush(c: LeatherColors): Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to c.brassLight,
        0.45f to c.brass,
        1.0f to c.brassDark,
    ),
    start = Offset.Zero,
    end = Offset.Infinite,
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

// ---------- Button (primary action in forms: Save) ---------------------------

/**
 * Brass-gradient replacement for Material's flat `Button`. Disabled state fades the whole
 * surface (gradient, border, shadow) instead of swapping to a grey fill.
 */
@Composable
fun BrassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    BrassSurface(
        shape = shape,
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        elevation = if (enabled) 4.dp else 0.dp,
    ) {
        ProvideTextStyle(MaterialTheme.typography.labelLarge) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .clickable(enabled = enabled, onClick = onClick)
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

// ---------- FAB (primary "Add Song" action) ---------------------------------

@Composable
fun BrassFAB(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BrassSurface(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.size(52.dp),
        elevation = 8.dp,
    ) { content() }
}
