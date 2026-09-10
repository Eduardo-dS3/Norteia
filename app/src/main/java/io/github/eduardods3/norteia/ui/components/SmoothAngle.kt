package io.github.eduardods3.norteia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import io.github.eduardods3.norteia.core.math.AngleMath

/**
 * Turns a wrapping 0–360° heading into a value that grows/shrinks continuously
 * and springs towards it, so the dial always rotates the short way and never
 * snaps across the 0/360 seam.
 */
@Composable
fun rememberSmoothedAngle(targetDeg: Float, label: String = "heading"): State<Float> {
    val accumulated = remember { mutableFloatStateOf(targetDeg) }
    LaunchedEffect(targetDeg) {
        accumulated.floatValue += AngleMath.delta(
            AngleMath.normalize360(accumulated.floatValue),
            targetDeg,
        )
    }
    return animateFloatAsState(targetValue = accumulated.floatValue, label = label)
}
