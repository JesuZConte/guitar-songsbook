package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

// =============================================================================
// LeatherHeader — the cognac → navy fade with stitched inner frame, flame-
// guitar emblem, and Merriweather title.
//
// Anatomy (top → bottom):
//   1. Outer Box: vertical gradient + inset shadows (M3 elevation OFF; we paint).
//   2. Stitched inner frame: dashed border 5dp inside the gradient surface.
//   3. Title row: optional back arrow, emblem, small-caps title + italic subtitle,
//      optional trailing icon.
//   4. Optional action row separated by a stitched divider.
//
// Do NOT use this as a TopAppBar slot in Scaffold — the gradient must extend
// edge-to-edge under the status bar. Place it above your screen content
// directly, with WindowInsets handled by the outer Surface.
// =============================================================================

@Composable
fun LeatherHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    leadingEmblem: Boolean = true,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    actionRow: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = LocalLeatherColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to c.leatherTop,
                        0.55f to c.leatherMid,
                        1.0f to c.leatherDeep,
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Stitched inner frame
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 6.dp, vertical = 5.dp)
                .stitchBorder(c.stitch)
        )

        Column(Modifier.fillMaxWidth()) {
            // Title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 38.dp),
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                            Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = c.cream,
                        )
                    }
                }
                if (leadingEmblem) {
                    FlameGuitar(size = 32.dp, color = c.cream, flame = c.brassLight)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.cream,
                        maxLines = 1,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.creamSoft,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                        )
                    }
                }
                if (trailingIcon != null) {
                    IconButton(onClick = onTrailingClick ?: {}) {
                        Icon(trailingIcon, contentDescription = null, tint = c.cream)
                    }
                }
            }

            // Optional second row (e.g. Share / Edit / Trash / Playlist / Theme / Heart)
            if (actionRow != null) {
                Spacer(Modifier.height(10.dp))
                StitchedDivider(color = c.stitch)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actionRow,
                )
            }
        }
    }
}

private fun Modifier.stitchBorder(color: androidx.compose.ui.graphics.Color): Modifier =
    this.drawWithContent {
        drawContent()
        val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        drawRect(color = color, style = Stroke(width = 1f, pathEffect = dash))
    }
