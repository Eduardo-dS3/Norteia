package io.github.eduardods3.norteia.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Ticks a light haptic each time the heading settles onto one of the eight
 * principal points — a quiet "you're aligned" cue people use without looking.
 * Keyed on the aligned index (changes ~8× per turn), not on the raw heading,
 * so it doesn't relaunch dozens of times a second.
 */
@Composable
fun CardinalHaptics(headingDeg: Float, enabled: Boolean) {
    val haptics = LocalHapticFeedback.current

    val nearest = (headingDeg / 45f).roundToInt().mod(8)
    val offset = abs(AngleDelta(headingDeg, nearest * 45f))
    val alignedIndex = if (offset < 2f) nearest else -1

    LaunchedEffect(alignedIndex, enabled) {
        if (enabled && alignedIndex >= 0) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

private fun AngleDelta(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d > 180f) d -= 360f
    if (d < -180f) d += 360f
    return d
}
