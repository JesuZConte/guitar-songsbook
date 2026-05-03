package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// FlameGuitar — brand emblem.
// Round guitar body + flame on top; reads from 24dp up to ~40dp.
// Drawn as a Canvas so it scales without raster blur.
// =============================================================================

@Composable
fun FlameGuitar(
    size: Dp = 30.dp,
    color: Color,
    flame: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width
        val unit = s / 40f  // original SVG was 40×40

        // Flame
        val flamePath = Path().apply {
            moveTo(20 * unit, 3 * unit)
            cubicTo(17 * unit, 7 * unit, 16.5f * unit, 10.5f * unit, 18.5f * unit, 13 * unit)
            cubicTo(16.5f * unit, 11.5f * unit, 16 * unit, 9.5f * unit, 16.5f * unit, 7.5f * unit)
            cubicTo(14 * unit, 11 * unit, 13.5f * unit, 14 * unit, 16 * unit, 16.5f * unit)
            cubicTo(18.5f * unit, 19 * unit, 22.5f * unit, 18 * unit, 24 * unit, 14.5f * unit)
            cubicTo(25.5f * unit, 11 * unit, 24 * unit, 8 * unit, 22.5f * unit, 6.5f * unit)
            cubicTo(22.5f * unit, 9 * unit, 22 * unit, 10.5f * unit, 21 * unit, 11 * unit)
            cubicTo(22 * unit, 9 * unit, 22.5f * unit, 5.5f * unit, 20 * unit, 3 * unit)
            close()
        }
        drawPath(flamePath, flame)
        drawPath(flamePath, color, style = Stroke(width = 1.4f * unit))

        // Guitar body
        drawCircle(
            color = color,
            radius = 10 * unit,
            center = Offset(20 * unit, 26 * unit),
            style = Stroke(width = 1.6f * unit),
        )
        // Sound hole
        drawCircle(
            color = color,
            radius = 3.2f * unit,
            center = Offset(20 * unit, 26 * unit),
        )
        // Strings
        val stringColor = color.copy(alpha = 0.6f)
        drawLine(stringColor, Offset(16 * unit, 19 * unit), Offset(16 * unit, 34 * unit), 0.7f * unit)
        drawLine(stringColor, Offset(20 * unit, 18 * unit), Offset(20 * unit, 22.5f * unit), 0.7f * unit)
        drawLine(stringColor, Offset(20 * unit, 29.5f * unit), Offset(20 * unit, 35 * unit), 0.7f * unit)
        drawLine(stringColor, Offset(24 * unit, 19 * unit), Offset(24 * unit, 34 * unit), 0.7f * unit)
    }
}
