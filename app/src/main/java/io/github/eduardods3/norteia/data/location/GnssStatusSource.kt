package io.github.eduardods3.norteia.data.location

import android.Manifest
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Live GNSS satellite tally: how many are visible, how many are actually
 * contributing to the fix, and their signal strength (C/N0). This is the
 * number people trust when they say "wait for the GPS to lock".
 */
data class GnssSummary(
    val satellitesInView: Int,
    val satellitesUsedInFix: Int,
    /** Mean carrier-to-noise density of the satellites used, in dB-Hz. */
    val averageCn0DbHz: Float?,
    val maxCn0DbHz: Float?,
    val supported: Boolean,
) {
    companion object {
        val Unsupported = GnssSummary(0, 0, null, null, supported = false)
    }
}

class GnssStatusSource(context: Context) {

    private val locationManager =
        context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @get:RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val summaries: Flow<GnssSummary> = callbackFlow {
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val count = status.satelliteCount
                var usedCount = 0
                var cn0Sum = 0f
                var cn0Max = 0f
                for (i in 0 until count) {
                    val cn0 = status.getCn0DbHz(i)
                    if (cn0 > cn0Max) cn0Max = cn0
                    if (status.usedInFix(i)) {
                        usedCount++
                        cn0Sum += cn0
                    }
                }
                trySend(
                    GnssSummary(
                        satellitesInView = count,
                        satellitesUsedInFix = usedCount,
                        averageCn0DbHz = if (usedCount > 0) cn0Sum / usedCount else null,
                        maxCn0DbHz = if (cn0Max > 0f) cn0Max else null,
                        supported = true,
                    ),
                )
            }
        }

        val registered = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.registerGnssStatusCallback(
                    { it.run() }, // direct executor; the callback is cheap
                    callback,
                )
            } else {
                @Suppress("DEPRECATION")
                locationManager.registerGnssStatusCallback(
                    callback,
                    Handler(Looper.getMainLooper()),
                )
            }
        } catch (e: SecurityException) {
            close(e)
            false
        }

        if (!registered) {
            trySend(GnssSummary.Unsupported)
        }

        awaitClose { locationManager.unregisterGnssStatusCallback(callback) }
    }.conflate()
}
