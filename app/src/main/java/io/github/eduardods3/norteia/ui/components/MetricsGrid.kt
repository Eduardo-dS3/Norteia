package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.core.astro.SunMoon
import io.github.eduardods3.norteia.ui.MainUiState
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Secondary read-outs. Everything here is computed on-device and offline:
 * altitude/speed from the fix, declination from Android's built-in World
 * Magnetic Model, Sun/Moon from [SunMoon].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MetricsGrid(state: MainUiState, modifier: Modifier = Modifier) {
    val dash = MainUiState.EMPTY_VALUE

    val altitude = state.altitudeM?.let { "${it.roundToInt()} m" } ?: dash
    val speed = state.speedKmh?.let { String.format(Locale.US, "%.0f km/h", it) } ?: dash
    val declination = state.declinationDeg?.let {
        val dir = if (it >= 0) "L" else "O"
        String.format(Locale.US, "%.1f° %s", abs(it), dir)
    } ?: dash

    val sun = state.sun
    val sunValue = sun?.let {
        val arrow = if (it.position.isUp) "↑" else "↓"
        "${it.position.azimuthDeg.roundToInt()}° $arrow"
    } ?: dash
    val sunSub = sun?.sunsetAzimuthDeg?.let { "Poente ${it.roundToInt()}°" }
        ?: "Azimute do Sol"

    val moon = state.moon
    val moonValue = moon?.let { "${it.position.azimuthDeg.roundToInt()}°" } ?: dash
    val moonSub = moon?.phase?.label ?: "Azimute da Lua"

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetricTile("Altitude", altitude, "Acima do elipsoide", Modifier.tileWidth())
        MetricTile("Velocidade", speed, "Sobre o solo", Modifier.tileWidth())
        MetricTile("Declinação", declination, "Magnética vs. verdadeira", Modifier.tileWidth())
        if (state.showSunMoon) {
            MetricTile("Sol", sunValue, sunSub, Modifier.tileWidth())
            MetricTile("Lua", moonValue, moonSub, Modifier.tileWidth())
        }
    }
}

private fun Modifier.tileWidth(): Modifier = this.fillMaxWidth(0.47f)

@Composable
private fun MetricTile(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NorteiaTheme.extra.auroraStart,
            )
            Text(
                value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
