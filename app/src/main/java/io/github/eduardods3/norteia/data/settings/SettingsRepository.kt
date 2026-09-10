package io.github.eduardods3.norteia.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.eduardods3.norteia.core.geo.CoordinateFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** True north vs. magnetic north for the heading readout. */
enum class NorthReference { TRUE, MAGNETIC }

data class UserSettings(
    val coordinateFormat: CoordinateFormat = CoordinateFormat.DECIMAL,
    val northReference: NorthReference = NorthReference.TRUE,
    val showLevel: Boolean = false,
    val showSunMoon: Boolean = true,
    val hapticOnCardinals: Boolean = true,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "norteia_settings")

/**
 * Tiny persisted-preferences layer. Everything here is a user UI choice — no
 * personal data, nothing that leaves the device.
 */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val settings: Flow<UserSettings> = store.data.map { p ->
        UserSettings(
            coordinateFormat = p[KEY_FORMAT]?.let { runCatching { CoordinateFormat.valueOf(it) }.getOrNull() }
                ?: CoordinateFormat.DECIMAL,
            northReference = p[KEY_NORTH]?.let { runCatching { NorthReference.valueOf(it) }.getOrNull() }
                ?: NorthReference.TRUE,
            showLevel = p[KEY_LEVEL] ?: false,
            showSunMoon = p[KEY_SUNMOON] ?: true,
            hapticOnCardinals = p[KEY_HAPTIC] ?: true,
        )
    }

    suspend fun setCoordinateFormat(value: CoordinateFormat) =
        store.edit { it[KEY_FORMAT] = value.name }

    suspend fun setNorthReference(value: NorthReference) =
        store.edit { it[KEY_NORTH] = value.name }

    suspend fun setShowLevel(value: Boolean) = store.edit { it[KEY_LEVEL] = value }

    suspend fun setShowSunMoon(value: Boolean) = store.edit { it[KEY_SUNMOON] = value }

    suspend fun setHapticOnCardinals(value: Boolean) = store.edit { it[KEY_HAPTIC] = value }

    private companion object {
        val KEY_FORMAT = stringPreferencesKey("coordinate_format")
        val KEY_NORTH = stringPreferencesKey("north_reference")
        val KEY_LEVEL = booleanPreferencesKey("show_level")
        val KEY_SUNMOON = booleanPreferencesKey("show_sun_moon")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_cardinals")
    }
}
