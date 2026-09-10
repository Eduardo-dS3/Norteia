package io.github.eduardods3.norteia.data.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.eduardods3.norteia.core.geo.LatLon
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * [LocationSource] backed by Google Play services' fused provider.
 *
 * Why fused and not raw LocationManager: the fused provider blends GNSS, Wi-Fi,
 * cell and the device's motion sensors, which yields a better first fix, a
 * steadier position when signal dips, and lower battery draw for the same
 * update rate. It is free — no API key, no quota, no billing. (The paid Google
 * Maps SDK is a completely separate product and is not used anywhere here.)
 *
 * The caller guarantees the location permission is held before collecting.
 */
class FusedLocationSource(context: Context) : LocationSource {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val request: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setWaitForAccurateLocation(true)
            .setMinUpdateDistanceMeters(0f)
            .build()

    @get:RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override val fixes: Flow<GeoFix> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toGeoFix()) }
            }
        }
        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            close(e)
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun lastKnown(): GeoFix? = suspendCancellableCoroutine { cont ->
        try {
            client.lastLocation
                .addOnSuccessListener { location -> cont.resume(location?.toGeoFix()) }
                .addOnFailureListener { cont.resume(null) }
        } catch (e: SecurityException) {
            cont.resume(null)
        }
    }

    private fun Location.toGeoFix(): GeoFix = GeoFix(
        position = LatLon(latitude, longitude),
        horizontalAccuracyM = if (hasAccuracy()) accuracy else null,
        altitudeM = if (hasAltitude()) altitude else null,
        altitudeAccuracyM = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasVerticalAccuracy()) {
            verticalAccuracyMeters
        } else {
            null
        },
        speedMps = if (hasSpeed()) speed else null,
        speedAccuracyMps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasSpeedAccuracy()) {
            speedAccuracyMetersPerSecond
        } else {
            null
        },
        bearingDeg = if (hasBearing() && (!hasSpeed() || speed > 0.5f)) bearing else null,
        elapsedRealtimeMillis = elapsedRealtimeNanos / 1_000_000L,
        fixTimeMillis = time,
        isFromMockProvider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isMock
        } else {
            @Suppress("DEPRECATION") isFromMockProvider
        },
    )

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
        const val FASTEST_INTERVAL_MS = 500L
    }
}
