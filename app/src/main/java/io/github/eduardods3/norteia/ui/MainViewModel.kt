package io.github.eduardods3.norteia.ui

import android.hardware.GeomagneticField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.eduardods3.norteia.NorteiaApp
import io.github.eduardods3.norteia.core.astro.SunMoon
import io.github.eduardods3.norteia.core.geo.CoordinateFormat
import io.github.eduardods3.norteia.core.geo.LatLon
import io.github.eduardods3.norteia.core.math.AngleMath
import io.github.eduardods3.norteia.data.location.GeoFix
import io.github.eduardods3.norteia.data.location.GnssStatusSource
import io.github.eduardods3.norteia.data.location.GnssSummary
import io.github.eduardods3.norteia.data.location.LocationSource
import io.github.eduardods3.norteia.data.sensors.CompassAccuracy
import io.github.eduardods3.norteia.data.sensors.CompassReading
import io.github.eduardods3.norteia.data.sensors.CompassSource
import io.github.eduardods3.norteia.data.sensors.LevelReading
import io.github.eduardods3.norteia.data.sensors.LevelSource
import io.github.eduardods3.norteia.data.settings.NorthReference
import io.github.eduardods3.norteia.data.settings.SettingsRepository
import io.github.eduardods3.norteia.data.settings.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Fuses the sensor, GNSS, location and settings streams into one [MainUiState].
 *
 * The upstream flows are all cold, and everything is `stateIn(WhileSubscribed)`,
 * so GPS and the magnetometer are only powered while the screen is actually on
 * screen — important for battery on a utility app people leave open.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val settings: SettingsRepository,
    private val locationSource: LocationSource,
    private val gnssSource: GnssStatusSource,
    compassSource: CompassSource,
    levelSource: LevelSource,
) : ViewModel() {

    private val permissionGranted = MutableStateFlow(false)
    private val servicesEnabled = MutableStateFlow(true)

    /** 30 s heartbeat so Sun/Moon and the "fix age" stay fresh when idle. */
    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }

    private val locationFlow: Flow<GeoFix?> = permissionGranted.flatMapLatest { granted ->
        if (granted) {
            locationSource.fixes.map<GeoFix, GeoFix?> { it }.catch { emit(null) }
        } else {
            flowOf(null)
        }
    }

    private val gnssFlow: Flow<GnssSummary> = permissionGranted.flatMapLatest { granted ->
        if (granted) gnssSource.summaries.catch { emit(GnssSummary.Unsupported) }
        else flowOf(GnssSummary.Unsupported)
    }

    private val compassFlow: Flow<CompassReading> = compassSource.readings
    private val levelFlow: Flow<LevelReading?> = levelSource.readings.catch { emit(null) }

    private data class Sensors(
        val compass: CompassReading,
        val fix: GeoFix?,
        val gnss: GnssSummary,
        val level: LevelReading?,
    )

    private val sensors: Flow<Sensors> =
        combine(compassFlow, locationFlow, gnssFlow, levelFlow, ::Sensors)

    val state: StateFlow<MainUiState> =
        combine(sensors, settings.settings, ticker, permissionGranted, servicesEnabled) {
                s, prefs, now, granted, enabled ->
            buildState(s, prefs, now, granted, enabled)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState(),
        )

    // -- inputs from the Activity -----------------------------------------

    fun onLocationPermissionResult(granted: Boolean) {
        permissionGranted.value = granted
    }

    fun onLocationServicesState(enabled: Boolean) {
        servicesEnabled.value = enabled
    }

    fun cycleCoordinateFormat() {
        viewModelScope.launch {
            settings.setCoordinateFormat(state.value.coordinateFormat.next())
        }
    }

    fun toggleNorthReference() {
        viewModelScope.launch {
            val next = if (state.value.northReference == NorthReference.TRUE) {
                NorthReference.MAGNETIC
            } else {
                NorthReference.TRUE
            }
            settings.setNorthReference(next)
        }
    }

    fun setShowLevel(value: Boolean) {
        viewModelScope.launch { settings.setShowLevel(value) }
    }

    fun setShowSunMoon(value: Boolean) {
        viewModelScope.launch { settings.setShowSunMoon(value) }
    }

    fun setHaptics(value: Boolean) {
        viewModelScope.launch { settings.setHapticOnCardinals(value) }
    }

    // -- state assembly -------------------------------------------------

    private fun buildState(
        s: Sensors,
        prefs: UserSettings,
        nowMillis: Long,
        granted: Boolean,
        servicesEnabled: Boolean,
    ): MainUiState {
        val fix = s.fix
        val pos: LatLon? = fix?.position

        val declination: Float? = pos?.let {
            GeomagneticField(
                it.latitude.toFloat(),
                it.longitude.toFloat(),
                (fix.altitudeM ?: 0.0).toFloat(),
                fix.fixTimeMillis.takeIf { t -> t > 0 } ?: nowMillis,
            ).declination
        }

        val magnetic = s.compass.magneticHeadingDeg
        val trueHeading = declination?.let { AngleMath.normalize360(magnetic + it) }
        val usingTrue = prefs.northReference == NorthReference.TRUE && trueHeading != null
        val heading = if (usingTrue) trueHeading!! else magnetic

        val accuracyM = fix?.horizontalAccuracyM
        val fixQuality = when {
            fix == null -> FixQuality.NONE
            accuracyM == null -> FixQuality.FAIR
            accuracyM <= 8f && s.gnss.satellitesUsedInFix >= 5 -> FixQuality.GOOD
            accuracyM <= 8f -> FixQuality.GOOD
            accuracyM <= 20f -> FixQuality.FAIR
            else -> FixQuality.POOR
        }

        val sun = pos?.let { SunMoon.sun(nowMillis, it.latitude, it.longitude) }
        val moon = pos?.let { SunMoon.moon(nowMillis, it.latitude, it.longitude) }

        val formatted = if (pos != null) {
            prefs.coordinateFormat.render(pos)
        } else {
            MainUiState.EMPTY_VALUE
        }

        val ageMs = fix?.let {
            (android.os.SystemClock.elapsedRealtime() - it.elapsedRealtimeMillis).coerceAtLeast(0)
        }

        return MainUiState(
            headingDeg = heading,
            magneticHeadingDeg = magnetic,
            northReference = prefs.northReference,
            usingTrueNorth = usingTrue,
            declinationDeg = declination,
            compassAccuracy = s.compass.accuracy,
            needsCalibration = s.compass.accuracy == CompassAccuracy.UNRELIABLE ||
                s.compass.accuracy == CompassAccuracy.LOW,
            phoneTilted = s.compass.tiltedTooFar,
            position = pos,
            horizontalAccuracyM = accuracyM,
            altitudeM = fix?.altitudeM,
            altitudeAccuracyM = fix?.altitudeAccuracyM,
            speedMps = fix?.speedMps,
            courseDeg = fix?.bearingDeg,
            isMockLocation = fix?.isFromMockProvider == true,
            locationAgeMs = ageMs,
            fixQuality = fixQuality,
            gnss = s.gnss,
            coordinateFormat = prefs.coordinateFormat,
            formattedCoordinates = formatted,
            level = s.level,
            showLevel = prefs.showLevel,
            sun = sun,
            moon = moon,
            showSunMoon = prefs.showSunMoon,
            hapticOnCardinals = prefs.hapticOnCardinals,
            locationPermissionGranted = granted,
            locationServicesOff = granted && !servicesEnabled,
        )
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NorteiaApp
                val c = app.container
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(
                    settings = c.settings,
                    locationSource = c.locationSource,
                    gnssSource = c.gnssSource,
                    compassSource = c.compassSource,
                    levelSource = c.levelSource,
                ) as T
            }
        }
    }
}
