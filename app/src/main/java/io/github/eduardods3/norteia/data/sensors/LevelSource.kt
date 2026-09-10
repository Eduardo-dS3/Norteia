package io.github.eduardods3.norteia.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlin.math.sqrt

data class LevelReading(
    /** Front-back tilt in degrees. 0 = flat. */
    val pitchDeg: Float,
    /** Left-right tilt in degrees. 0 = flat. */
    val rollDeg: Float,
    val hasSensor: Boolean,
) {
    /** Distance of the bubble from dead-centre, in degrees. */
    val offsetDeg: Float get() = sqrt(pitchDeg * pitchDeg + rollDeg * rollDeg)
    val isLevel: Boolean get() = offsetDeg < 1.0f
}

/**
 * A bubble level built on the gravity vector.
 *
 * Uses [Sensor.TYPE_GRAVITY] when the device provides it (already low-pass
 * filtered by the OS) and falls back to a manually smoothed accelerometer.
 */
class LevelSource(context: Context) {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val alpha = 0.15f

    val readings: Flow<LevelReading> = callbackFlow {
        val gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val sensor = gravity ?: accelerometer

        if (sensor == null) {
            trySend(LevelReading(0f, 0f, hasSensor = false))
            awaitClose { }
            return@callbackFlow
        }

        val needsSmoothing = gravity == null
        val g = FloatArray(3)
        var seeded = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (needsSmoothing && seeded) {
                    g[0] += alpha * (event.values[0] - g[0])
                    g[1] += alpha * (event.values[1] - g[1])
                    g[2] += alpha * (event.values[2] - g[2])
                } else {
                    g[0] = event.values[0]
                    g[1] = event.values[1]
                    g[2] = event.values[2]
                    seeded = true
                }

                val norm = sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]).coerceAtLeast(1e-3f)
                // Tilt of the screen plane relative to horizontal.
                val pitch = Math.toDegrees(Math.asin((g[1] / norm).toDouble())).toFloat()
                val roll = Math.toDegrees(Math.asin((-g[0] / norm).toDouble())).toFloat()
                trySend(LevelReading(pitch, roll, hasSensor = true))
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.conflate()
}
