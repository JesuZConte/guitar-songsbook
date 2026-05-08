package com.guitarapp.songsbook.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.guitarapp.songsbook.ui.theme.LocalLeatherColors

// =============================================================================
// BrassToggle — on/off switch that matches the leather journal visual language.
// ON : brass radial track + brass border + cream knob at the right.
// OFF: c.rule track + cream knob at the left.
// =============================================================================

@Composable
fun BrassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalLeatherColors.current
    val trackShape = RoundedCornerShape(13.dp)
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 23.dp else 3.dp,
        animationSpec = tween(durationMillis = 180),
        label = "knobOffset",
    )

    Box(
        modifier = modifier
            .semantics {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                stateDescription = if (checked) "On" else "Off"
            }
            .width(46.dp)
            .height(26.dp)
            .clip(trackShape)
            .then(
                if (checked) {
                    Modifier
                        .background(brassBrush(c), trackShape)
                        .border(1.5.dp, c.brassDark, trackShape)
                } else {
                    Modifier.background(c.rule, trackShape)
                }
            )
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(20.dp)
                .shadow(2.dp, CircleShape)
                .background(c.cream, CircleShape)
        )
    }
}
