package io.github.eduardods3.norteia.core.math

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small, dependency-free helpers for working with compass bearings.
 *
 * Bearings are messy to smooth because they wrap at 360°: a naïve average of
 * 359° and 1° gives 180°, pointing the needle exactly backwards. Every function
 * here treats an angle as a point on the unit circle so the wrap disappears.
 */
object AngleMath {

    /** Wraps any angle (degrees) into the [0, 360) range. */
    fun normalize360(deg: Float): Float {
        var d = deg % 360f
        if (d < 0f) d += 360f
        return d
    }

    /** Wraps any angle (degrees) into the (-180, 180] range. */
    fun normalize180(deg: Float): Float {
        var d = normalize360(deg)
        if (d > 180f) d -= 360f
        return d
    }

    /**
     * Shortest signed rotation (degrees) to get from [from] to [to].
     * Result is in (-180, 180]; positive means clockwise.
     */
    fun delta(from: Float, to: Float): Float = normalize180(to - from)

    /**
     * Exponential smoothing that respects the 360° wrap.
     *
     * @param previous last emitted heading, or null on the first sample
     * @param target   the raw new heading
     * @param alpha    0..1 — higher reacts faster, lower is smoother
     */
    fun lowPass(previous: Float?, target: Float, alpha: Float): Float {
        if (previous == null) return normalize360(target)
        val stepped = previous + alpha * delta(previous, target)
        return normalize360(stepped)
    }

    /**
     * Circular mean of a set of angles (degrees). Used to fuse redundant
     * orientation readings. Returns 0 for an empty input.
     */
    fun circularMean(anglesDeg: FloatArray): Float {
        if (anglesDeg.isEmpty()) return 0f
        var sumSin = 0.0
        var sumCos = 0.0
        for (a in anglesDeg) {
            val r = Math.toRadians(a.toDouble())
            sumSin += sin(r)
            sumCos += cos(r)
        }
        return normalize360(Math.toDegrees(atan2(sumSin, sumCos)).toFloat())
    }

    /**
     * True when [a] and [b] are within [toleranceDeg] of each other on the
     * circle. Handy for haptic "you are on a cardinal point" feedback.
     */
    fun isNear(a: Float, b: Float, toleranceDeg: Float): Boolean =
        abs(delta(a, b)) <= toleranceDeg
}
