package io.github.eduardods3.norteia.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Ticks a light vibration each time the heading settles onto one of the eight
 * principal points — a quiet "you're aligned" cue people use without looking.
 * Keyed on the aligned index (changes ~8× per turn), not on the raw heading,
 * so it doesn't relaunch dozens of times a second.
 *
 * Drives [Vibrator] directly rather than Compose's `performHapticFeedback`:
 * the latter maps to weak constants (e.g. TEXT_HANDLE_MOVE) that several
 * OEMs — Samsung included — silently mute unless "touch feedback" is on in
 * system settings, independent of the general vibration toggle. A real
 * [VibrationEffect] only depends on vibration being allowed at all.
 */
@Composable
fun CardinalHaptics(headingDeg: Float, enabled: Boolean) {
    val vibrator = rememberVibrator()

    val nearest = (headingDeg / 45f).roundToInt().mod(8)
    val offset = abs(angleDelta(headingDeg, nearest * 45f))
    val alignedIndex = if (offset < 2f) nearest else -1

    LaunchedEffect(alignedIndex, enabled) {
        if (enabled && alignedIndex >= 0) {
            vibrator?.tick()
        }
    }
}

@Composable
private fun rememberVibrator(): Vibrator? {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

private fun Vibrator.tick() {
    if (!hasVibrator()) return
    val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
    } else {
        VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE)
    }
    vibrate(effect)
}

private fun angleDelta(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d > 180f) d -= 360f
    if (d < -180f) d += 360f
    return d
}
