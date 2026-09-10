package io.github.eduardods3.norteia.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.ui.MainUiState
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme

private data class Advice(val icon: ImageVector, val title: String, val body: String, val tint: Color)

/**
 * A single, non-modal hint bar. It shows the most urgent issue only — nagging
 * with several banners is the fastest way to a one-star review.
 */
@Composable
fun AdviceBanner(state: MainUiState, modifier: Modifier = Modifier) {
    val extra = NorteiaTheme.extra
    val advice: Advice? = when {
        state.locationServicesOff -> Advice(
            Icons.Outlined.LocationOff,
            "Localização desligada",
            "Ative o GPS nas configurações rápidas para ver coordenadas e satélites.",
            extra.fixFair,
        )
        state.isMockLocation -> Advice(
            Icons.Outlined.Warning,
            "Localização simulada ativa",
            "Um app está fornecendo uma posição falsa. Desative-o nas Opções do desenvolvedor.",
            extra.fixPoor,
        )
        state.needsCalibration -> Advice(
            Icons.Outlined.Explore,
            "Calibre a bússola",
            "Desenhe um 8 no ar com o aparelho algumas vezes, longe de metais e ímãs.",
            extra.fixFair,
        )
        state.phoneTilted -> Advice(
            Icons.Outlined.ScreenRotation,
            "Deixe o aparelho na horizontal",
            "A leitura fica mais precisa com a tela nivelada, como uma bússola de verdade.",
            extra.auroraStart,
        )
        else -> null
    }

    AnimatedVisibility(
        visible = advice != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        advice?.let { a ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(a.tint.copy(alpha = 0.12f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(a.icon, contentDescription = null, tint = a.tint)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        a.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        a.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
