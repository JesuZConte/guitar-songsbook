package com.guitarapp.songsbook.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors
import com.guitarapp.songsbook.ui.theme.PillShape

// =============================================================================
// LeatherChip — branded selection chip used for filters and settings selectors.
//
// Active: brass radial gradient fill + dark brass border (BrassPill)
// Inactive: transparent background + c.rule border
// =============================================================================

@Composable
fun LeatherChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalLeatherColors.current
    if (selected) {
        BrassPill(
            modifier = modifier.clickable(onClick = onClick),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = label,
                color = c.navyDeep,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Box(
            modifier = modifier
                .clip(PillShape)
                .border(1.5.dp, c.rule, PillShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = c.ink,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
