package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * altitude from the fix, declination from Android's built-in World Magnetic
 * Model, Sun/Moon from [SunMoon].
 *
 * Speed is deliberately not shown here: GPS speed is noisy and meaningless
 * while standing still (the usual way people check this screen), so a
 * jittery "0 km/h" / "3 km/h" reads as broken rather than useful.
 */
@Composable
fun MetricsGrid(state: MainUiState, modifier: Modifier = Modifier) {
    val dash = MainUiState.EMPTY_VALUE

    val altitude = state.altitudeM?.let { "${it.roundToInt()} m" } ?: dash
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TileRow {
            MetricTile("Altitude", altitude, "Acima do elipsoide", Modifier.weight(1f))
            MetricTile("Declinação", declination, "Magnética vs. verdadeira", Modifier.weight(1f))
        }
        if (state.showSunMoon) {
            TileRow {
                MetricTile("Sol", sunValue, sunSub, Modifier.weight(1f))
                MetricTile("Lua", moonValue, moonSub, Modifier.weight(1f))
            }
        }
    }
}

/**
 * A row of tiles that are always the same height as each other — the taller
 * one's content sets the height, and the other stretches to match, no matter
 * how many lines its caption wraps to.
 */
@Composable
private fun TileRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = { content() },
    )
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
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
