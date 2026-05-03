package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import com.guitarapp.songsbook.ui.theme.PillShape

@Composable
fun VersionChips(
    versions: List<String>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalLeatherColors.current
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(versions) { i, label ->
            if (i == activeIndex) {
                BrassPill {
                    Text(
                        text = label,
                        color = c.leatherDeep,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Box(
                    Modifier
                        .border(1.5.dp, c.rule, PillShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) { Text(label, color = c.ink) }
            }
        }
        item {
            Box(
                Modifier
                    .dashedPillBorder(c.rule)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("+ Version", color = c.inkFaint) }
        }
    }
}

private fun Modifier.dashedPillBorder(color: Color): Modifier =
    this.drawBehind {
        val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
        val r = size.height / 2f
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = dash),
        )
    }
