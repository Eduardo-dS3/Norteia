package io.github.eduardods3.norteia.data.location

import io.github.eduardods3.norteia.core.geo.LatLon
import kotlinx.coroutines.flow.Flow

/** A single position fix, with every quality figure the UI might surface. */
data class GeoFix(
    val position: LatLon,
    /** Horizontal 1-sigma error in metres (≈68 % confidence), or null. */
    val horizontalAccuracyM: Float?,
    /** Altitude in metres. Interpretation depends on the provider (usually
     *  above the WGS-84 ellipsoid on modern devices). */
    val altitudeM: Double?,
    val altitudeAccuracyM: Float?,
    /** Ground speed in m/s. */
    val speedMps: Float?,
    val speedAccuracyMps: Float?,
    /** Course over ground in degrees, or null when stationary. */
    val bearingDeg: Float?,
    val elapsedRealtimeMillis: Long,
    val fixTimeMillis: Long,
    val isFromMockProvider: Boolean,
) {
    val hasGoodAccuracy: Boolean get() = (horizontalAccuracyM ?: Float.MAX_VALUE) <= 10f
}

interface LocationSource {
    /** Hot while collected: high-accuracy fixes roughly once a second. */
    val fixes: Flow<GeoFix>

    /** Best cached fix for an instant first paint, or null. */
    suspend fun lastKnown(): GeoFix?
}
