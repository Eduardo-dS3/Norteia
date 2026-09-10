package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Dependency(val name: String, val license: String)

private val dependencies = listOf(
    Dependency("Jetpack Compose, Core, Lifecycle, Activity, DataStore (AndroidX)", "Apache License 2.0"),
    Dependency("Material Components / Material Icons", "Apache License 2.0"),
    Dependency("Kotlin & kotlinx.coroutines (JetBrains)", "Apache License 2.0"),
    Dependency("Google Play services – Location", "Android Software Development Kit License"),
)

/**
 * Static attribution screen. Kept hand-written instead of pulling in the OSS
 * licenses Gradle plugin, to keep the build lean; update this list whenever a
 * dependency is added.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Licenças de código aberto",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Text(
                "Todo o código do app, incluindo a rosa dos ventos, os cálculos de " +
                    "UTM/MGRS e a posição de Sol e Lua, é original. As bibliotecas abaixo " +
                    "são usadas sob suas respectivas licenças.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            dependencies.forEach { dep ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        dep.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        dep.license,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
