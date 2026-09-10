package io.github.eduardods3.norteia.core.geo

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Decimal-degree <-> degrees/minutes/seconds formatting.
 *
 * Kept separate from Android so it is trivially unit-testable on the JVM.
 * All output uses [Locale.US] so the decimal separator is always a dot,
 * which is what mapping tools and search boxes expect when the string is
 * copied out of the app.
 */
object Dms {

    /** e.g. "-23.55052" (5 decimals ≈ 1.1 m at the equator). */
    fun decimal(value: Double, decimals: Int = 5): String =
        String.format(Locale.US, "%.${decimals}f", value)

    /** e.g. "23°33'01.9\"S". [isLatitude] picks the N/S vs E/W suffix. */
    fun sexagesimal(value: Double, isLatitude: Boolean): String {
        val hemisphere = when {
            isLatitude && value >= 0 -> "N"
            isLatitude -> "S"
            value >= 0 -> "E"
            else -> "W"
        }
        val abs = abs(value)
        var degrees = floor(abs).toInt()
        var minutesFull = (abs - degrees) * 60.0
        var minutes = floor(minutesFull).toInt()
        var seconds = (minutesFull - minutes) * 60.0

        // Carry rounding so we never print 60'' or 60'.
        if (seconds >= 59.995) {
            seconds = 0.0
            minutes += 1
        }
        if (minutes >= 60) {
            minutes = 0
            degrees += 1
        }
        return String.format(Locale.US, "%d°%02d'%05.2f\"%s", degrees, minutes, seconds, hemisphere)
    }

    /** Both components joined, e.g. "23°33'01.9\"S  46°38'02.0\"W". */
    fun sexagesimalPair(pos: LatLon): String =
        sexagesimal(pos.latitude, isLatitude = true) + "  " +
            sexagesimal(pos.longitude, isLatitude = false)

    /** Both components as plain decimals, e.g. "-23.55052, -46.63389". */
    fun decimalPair(pos: LatLon, decimals: Int = 5): String =
        decimal(pos.latitude, decimals) + ", " + decimal(pos.longitude, decimals)
}
