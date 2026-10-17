package io.github.eduardods3.norteia.ui

import io.github.eduardods3.norteia.core.astro.SunMoon
import io.github.eduardods3.norteia.core.geo.CoordinateFormat
import io.github.eduardods3.norteia.core.geo.LatLon
import io.github.eduardods3.norteia.data.location.GnssSummary
import io.github.eduardods3.norteia.data.sensors.CompassAccuracy
import io.github.eduardods3.norteia.data.sensors.LevelReading
import io.github.eduardods3.norteia.data.settings.NorthReference

/** How usable the current position fix is — drives the colour of the badge. */
enum class FixQuality { NONE, POOR, FAIR, GOOD }

/**
 * Everything the single screen needs, recomputed on every sensor/GPS tick.
 * Immutable so Compose can skip untouched subtrees.
 */
data class MainUiState(
    // --- heading -----------------------------------------------------------
    /** False until the first real sensor event has arrived. [headingDeg] is a
     *  meaningless placeholder until then — the UI must not present it as 0°. */
    val headingAvailable: Boolean = false,
    val headingDeg: Float = 0f,
    val magneticHeadingDeg: Float = 0f,
    val northReference: NorthReference = NorthReference.TRUE,
    val usingTrueNorth: Boolean = false,
    val declinationDeg: Float? = null,
    val compassAccuracy: CompassAccuracy = CompassAccuracy.LOW,
    val needsCalibration: Boolean = false,
    val phoneTilted: Boolean = false,

    // --- position --------------------------------------------------------
    val position: LatLon? = null,
    val horizontalAccuracyM: Float? = null,
    val altitudeM: Double? = null,
    val altitudeAccuracyM: Float? = null,
    val speedMps: Float? = null,
    val courseDeg: Float? = null,
    val isMockLocation: Boolean = false,
    val locationAgeMs: Long? = null,
    val fixQuality: FixQuality = FixQuality.NONE,

    // --- satellites -----------------------------------------------------
    val gnss: GnssSummary = GnssSummary.Unsupported,

    // --- coordinates card --------------------------------------------------
    val coordinateFormat: CoordinateFormat = CoordinateFormat.DECIMAL,
    val formattedCoordinates: String = EMPTY_VALUE,

    // --- extras ---------------------------------------------------------
    val level: LevelReading? = null,
    val showLevel: Boolean = false,
    val sun: SunMoon.SunInfo? = null,
    val moon: SunMoon.MoonInfo? = null,
    val showSunMoon: Boolean = true,
    val hapticOnCardinals: Boolean = true,

    // --- permission ----------------------------------------------------
    val locationPermissionGranted: Boolean = false,
    val locationServicesOff: Boolean = false,
) {
    val hasFix: Boolean get() = position != null

    companion object {
        const val EMPTY_VALUE = "—"
    }
}
