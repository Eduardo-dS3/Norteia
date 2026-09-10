package io.github.eduardods3.norteia.di

import android.content.Context
import io.github.eduardods3.norteia.data.location.FusedLocationSource
import io.github.eduardods3.norteia.data.location.GnssStatusSource
import io.github.eduardods3.norteia.data.location.LocationSource
import io.github.eduardods3.norteia.data.sensors.CompassSource
import io.github.eduardods3.norteia.data.sensors.LevelSource
import io.github.eduardods3.norteia.data.settings.SettingsRepository

/**
 * Manual dependency container. Everything the app needs is built once and shared.
 * Sources expose cold [kotlinx.coroutines.flow.Flow]s, so nothing actually runs
 * until the UI collects — that keeps sensors and GPS off when the screen is gone.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settings: SettingsRepository = SettingsRepository(appContext)

    val locationSource: LocationSource = FusedLocationSource(appContext)

    val gnssSource: GnssStatusSource = GnssStatusSource(appContext)

    val compassSource: CompassSource = CompassSource(appContext)

    val levelSource: LevelSource = LevelSource(appContext)
}
