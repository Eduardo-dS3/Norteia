package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.data.location.GnssSummary
import io.github.eduardods3.norteia.ui.FixQuality
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme
import kotlin.math.roundToInt

/**
 * The one-line trust strip under the rose: fix quality, horizontal error,
 * satellites used, and — only when it matters — a mock-location warning.
 * Competitor reviews say this is what people check first.
 */
@Composable
fun StatusStrip(
    fixQuality: FixQuality,
    horizontalAccuracyM: Float?,
    gnss: GnssSummary,
    isMock: Boolean,
    modifier: Modifier = Modifier,
) {
    val extra = NorteiaTheme.extra
    val qualityColor = when (fixQuality) {
        FixQuality.GOOD -> extra.fixGood
        FixQuality.FAIR -> extra.fixFair
        FixQuality.POOR -> extra.fixPoor
        FixQuality.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val qualityLabel = when (fixQuality) {
        FixQuality.GOOD -> "Sinal ótimo"
        FixQuality.FAIR -> "Sinal razoável"
        FixQuality.POOR -> "Sinal fraco"
        FixQuality.NONE -> "Sem posição"
    }
    val accuracyText = horizontalAccuracyM?.let { " · ±${it.roundToInt()} m" }.orEmpty()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Pill {
            Box(Modifier.size(7.dp).clip(CircleShape).background(qualityColor))
            PillText(qualityLabel + accuracyText)
        }
        if (gnss.supported) {
            val cn0 = gnss.averageCn0DbHz?.let { " · ${it.roundToInt()} dB" }.orEmpty()
            Pill { PillText("${gnss.satellitesUsedInFix}/${gnss.satellitesInView} sat$cn0") }
        }
        if (isMock) {
            Pill(bg = extra.fixPoor.copy(alpha = 0.16f)) {
                PillText("Localização simulada", color = extra.fixPoor)
            }
        }
    }
}

@Composable
private fun Pill(
    bg: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = { content() },
    )
}

@Composable
private fun PillText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color ?: MaterialTheme.colorScheme.onSurface,
    )
}
