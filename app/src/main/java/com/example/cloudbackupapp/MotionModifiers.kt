package com.example.cloudbackupapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Press animation for anything clickable (tiles, buttons, list rows).
 *
 * Usage:
 *   val src = remember { MutableInteractionSource() }
 *   Modifier.pressScale(src)
 *   .clickable(interactionSource = src, indication = rememberRipple(), onClick = ...)
 */
@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val cfg = AppMotion.press
    val target = if (pressed) cfg.pressedScale else 1f
    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = cfg.durationMs, easing = cfg.easing),
        label = "press-scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
