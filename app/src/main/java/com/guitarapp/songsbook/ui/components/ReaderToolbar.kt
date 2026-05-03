package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

// =============================================================================
// ReaderToolbar — bottom strip on the song reader.
// Three groups in a leather→navy fade:
//   • Size      — minus / 14sp / plus
//   • Transpose — minus / ±0    / plus
//   • Page      — italic "1 / 2"
// Each group sits in a stitched, translucent cream container.
// =============================================================================

@Composable
fun ReaderToolbar(
    fontSizeSp: Int,
    transposeSemitones: Int,
    page: Int,
    totalPages: Int,
    onSizeDelta: (Int) -> Unit,
    onTransposeDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalLeatherColors.current
    Box(
        modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(c.leatherDeep, c.navyDeep)
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarGroup {
                IconButton(onClick = { onSizeDelta(-1) }, modifier = Modifier.size(30.dp).testTag("toolbar_size_minus")) {
                    Icon(Icons.Filled.Remove, null, tint = c.cream)
                }
                LabeledValue(label = "SIZE", value = "${fontSizeSp}sp")
                IconButton(onClick = { onSizeDelta(+1) }, modifier = Modifier.size(30.dp).testTag("toolbar_size_plus")) {
                    Icon(Icons.Filled.Add, null, tint = c.cream)
                }
            }
            ToolbarGroup {
                IconButton(onClick = { onTransposeDelta(-1) }, modifier = Modifier.size(30.dp).testTag("toolbar_transpose_minus")) {
                    Icon(Icons.Filled.Remove, null, tint = c.cream)
                }
                LabeledValue(
                    label = "TRANSPOSE",
                    value = when {
                        transposeSemitones == 0 -> "±0"
                        transposeSemitones > 0  -> "+$transposeSemitones"
                        else -> "$transposeSemitones"
                    },
                )
                IconButton(onClick = { onTransposeDelta(+1) }, modifier = Modifier.size(30.dp).testTag("toolbar_transpose_plus")) {
                    Icon(Icons.Filled.Add, null, tint = c.cream)
                }
            }
            Text(
                text = "$page / $totalPages",
                style = MaterialTheme.typography.bodySmall,
                color = c.creamSoft,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun ToolbarGroup(content: @Composable RowScope.() -> Unit) {
    val c = LocalLeatherColors.current
    Row(
        Modifier
            .background(c.cream.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
            .border(1.dp, c.stitch, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
private fun LabeledValue(label: String, value: String) {
    val c = LocalLeatherColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 38.dp)
            .padding(horizontal = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.creamSoft)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = c.cream)
    }
}
