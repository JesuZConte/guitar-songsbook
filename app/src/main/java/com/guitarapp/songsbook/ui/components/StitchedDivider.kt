package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// StitchedDivider — the dashed cream stitch line characteristic of the
// Leather Journal direction. Used inside leather chrome to separate header
// rows from action rows, and to frame the inner edge of the leather surface.
// =============================================================================

@Composable
fun StitchedDivider(
    color: Color,
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    dashOn: Float = 4f,
    dashOff: Float = 4f,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        val effect = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff), 0f)
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end   = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = thickness.toPx(),
            pathEffect = effect,
        )
    }
}
