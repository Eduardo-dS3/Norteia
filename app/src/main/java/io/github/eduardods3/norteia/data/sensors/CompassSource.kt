package io.github.eduardods3.norteia.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import io.github.eduardods3.norteia.core.math.AngleMath
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/** How much the OS trusts the current magnetometer calibration. */
enum class CompassAccuracy { NO_SENSOR, UNRELIABLE, LOW, MEDIUM, HIGH }

data class CompassReading(
    /** Smoothed heading in [0, 360) relative to **magnetic** north. */
    val magneticHeadingDeg: Float,
    val accuracy: CompassAccuracy,
    /** True while the phone is tilted too far for a trustworthy reading. */
    val tiltedTooFar: Boolean,
    /** False only for the synthetic placeholder sent before the first real
     *  sensor callback arrives — see [CompassSource.readings]. */
    val hasReading: Boolean = true,
)

/**
 * Streams a smoothed magnetic heading from the device's fused orientation
 * sensor.
 *
 * Precision choices:
 *  - Prefers [Sensor.TYPE_ROTATION_VECTOR] (gyro + accelerometer + magnetometer
 *    fused by the OS) and only falls back to raw accelerometer + magnetometer
 *    when no rotation vector exists. The fused sensor is dramatically steadier.
 *  - Smooths on the unit circle (see [AngleMath.lowPass]) so the needle never
 *    flips through 180° when the heading crosses 0/360.
 *  - Remaps the coordinate system for the current display rotation, so a
 *    future landscape mode or a foldable stays correct.
 *  - Reports the OS calibration quality so the UI can ask for a figure-8.
 *
 * Declination (magnetic → true north) is applied later, in the ViewModel,
 * because it needs the current location.
 */
class CompassSource(context: Context) {

    private val appContext = context.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val displayManager =
        appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // Higher alpha = snappier, lower = smoother. 0.18 is a good phone-compass feel.
    private val smoothingAlpha = 0.18f

    // Beyond ~55° of tilt the horizontal heading gets unreliable.
    private val maxTiltDegrees = 55.0

    val readings: Flow<CompassReading> = callbackFlow {
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationVector == null && (accelerometer == null || magnetometer == null)) {
            trySend(CompassReading(0f, CompassAccuracy.NO_SENSOR, tiltedTooFar = false))
            awaitClose { }
            return@callbackFlow
        }

        // A real sensor callback can take a second or more to arrive on a cold
        // app start (fresh process, sensor HAL warming up). Seed a placeholder
        // right away so downstream combine()s aren't starved waiting on this
        // one flow — everything else (permission, settings) would otherwise
        // sit frozen on its own stale default for that whole window too.
        trySend(CompassReading(0f, CompassAccuracy.LOW, tiltedTooFar = false, hasReading = false))

        val rotationMatrix = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        val lastAccel = FloatArray(3)
        val lastMagnet = FloatArray(3)
        var haveAccel = false
        var haveMagnet = false
        var smoothed: Float? = null
        var accuracy = CompassAccuracy.LOW

        fun currentDisplayRotation(): Int =
            (displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0)

        fun emitFrom(matrix: FloatArray) {
            val rotation = currentDisplayRotation()
            val (axisX, axisY) = when (rotation) {
                Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
            }
            SensorManager.remapCoordinateSystem(matrix, axisX, axisY, remapped)
            SensorManager.getOrientation(remapped, orientation)

            val rawHeading = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val pitchDeg = Math.toDegrees(orientation[1].toDouble())
            val rollDeg = Math.toDegrees(orientation[2].toDouble())
            val tilted = kotlin.math.abs(pitchDeg) > maxTiltDegrees ||
                kotlin.math.abs(rollDeg) > maxTiltDegrees

            smoothed = AngleMath.lowPass(smoothed, AngleMath.normalize360(rawHeading), smoothingAlpha)
            trySend(CompassReading(smoothed!!, accuracy, tilted))
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        emitFrom(rotationMatrix)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, lastAccel, 0, 3)
                        haveAccel = true
                        if (haveMagnet && SensorManager.getRotationMatrix(
                                rotationMatrix, null, lastAccel, lastMagnet,
                            )
                        ) {
                            emitFrom(rotationMatrix)
                        }
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, lastMagnet, 0, 3)
                        haveMagnet = true
                        if (haveAccel && SensorManager.getRotationMatrix(
                                rotationMatrix, null, lastAccel, lastMagnet,
                            )
                        ) {
                            emitFrom(rotationMatrix)
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, value: Int) {
                if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD ||
                    sensor.type == Sensor.TYPE_ROTATION_VECTOR
                ) {
                    accuracy = when (value) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                        else -> CompassAccuracy.UNRELIABLE
                    }
                }
            }
        }

        val rate = SensorManager.SENSOR_DELAY_GAME
        if (rotationVector != null) {
            sensorManager.registerListener(listener, rotationVector, rate)
        } else {
            sensorManager.registerListener(listener, accelerometer, rate)
            sensorManager.registerListener(listener, magnetometer, rate)
        }

        awaitClose { sensorManager.unregisterListener(listener) }
    }
        .conflate()
        .distinctUntilChanged()
}
