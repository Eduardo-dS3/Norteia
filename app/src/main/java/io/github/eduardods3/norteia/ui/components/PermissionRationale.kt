package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Plain-language explanation shown before the system location prompt, and
 * again (with a Settings shortcut) if the user has permanently denied it.
 *
 * The wording doubles as Play's required "prominent disclosure": it states
 * what is accessed, that it is foreground-only, and that nothing leaves the
 * device.
 */
@Composable
fun PermissionRationaleDialog(
    permanentlyDenied: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Localização para GPS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "O Norteia usa a localização precisa apenas enquanto o app está aberto, " +
                        "para mostrar coordenadas, altitude, velocidade e satélites. " +
                        "Nada é enviado para servidores nem armazenado — os dados ficam no aparelho. " +
                        "A bússola continua funcionando sem essa permissão.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (permanentlyDenied) {
                    Text(
                        "A permissão foi negada antes. Abra as configurações do app para ativá-la.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) { Text("Agora não") }
                    TextButton(onClick = onConfirm) {
                        Text(if (permanentlyDenied) "Abrir configurações" else "Permitir")
                    }
                }
            }
        }
    }
}
