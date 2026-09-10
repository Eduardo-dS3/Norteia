package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.core.geo.CoordinateFormat
import io.github.eduardods3.norteia.ui.MainUiState

/**
 * The coordinates panel. Tapping the value cycles notation (decimal → GMS →
 * UTM → MGRS); the row of buttons copies, shares, or opens the point in the
 * user's map app.
 */
@Composable
fun CoordinatesCard(
    value: String,
    format: CoordinateFormat,
    hasFix: Boolean,
    permissionGranted: Boolean,
    onCycleFormat: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpenInMaps: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (hasFix) format.label else "Coordenadas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when {
                !permissionGranted -> {
                    Text(
                        text = "Ative a localização para ver coordenadas, altitude e satélites. A bússola funciona sem ela.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onRequestPermission) { Text("Permitir localização") }
                }

                !hasFix -> {
                    Text(
                        text = "Buscando sinal…",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onCycleFormat)
                            .padding(vertical = 2.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Toque no valor para trocar o formato",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onCopy) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar")
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Outlined.Share, contentDescription = "Compartilhar")
                        }
                        IconButton(onClick = onOpenInMaps) {
                            Icon(Icons.Outlined.Map, contentDescription = "Abrir no mapa")
                        }
                    }
                }
            }
        }
    }
}

/** Convenience overload driven straight off [MainUiState]. */
@Composable
fun CoordinatesCard(
    state: MainUiState,
    onCycleFormat: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpenInMaps: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) = CoordinatesCard(
    value = state.formattedCoordinates,
    format = state.coordinateFormat,
    hasFix = state.hasFix,
    permissionGranted = state.locationPermissionGranted,
    onCycleFormat = onCycleFormat,
    onCopy = onCopy,
    onShare = onShare,
    onOpenInMaps = onOpenInMaps,
    onRequestPermission = onRequestPermission,
    modifier = modifier,
)
