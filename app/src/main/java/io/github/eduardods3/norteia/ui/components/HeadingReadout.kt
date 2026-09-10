package io.github.eduardods3.norteia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.core.geo.Cardinals
import io.github.eduardods3.norteia.ui.theme.HeadingDisplay
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme
import kotlin.math.roundToInt

/**
 * The value stack in the middle of the rose: big degrees, the cardinal name,
 * and a tappable pill that switches true ⇄ magnetic north.
 */
@Composable
fun HeadingReadout(
    headingDeg: Float,
    usingTrueNorth: Boolean,
    trueNorthAvailable: Boolean,
    onToggleNorthReference: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val degrees = headingDeg.roundToInt().mod(360)
    val extra = NorteiaTheme.extra

    Column(
        modifier = modifier.semantics {
            contentDescription = "Rumo $degrees graus, ${Cardinals.name(headingDeg)}"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "$degrees°",
            style = HeadingDisplay,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = Cardinals.name(headingDeg).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = extra.auroraStart,
            textAlign = TextAlign.Center,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
            modifier = Modifier.clip(RoundedCornerShape(50)),
        ) {
            Text(
                text = when {
                    !trueNorthAvailable -> "MAGNÉTICO"
                    usingTrueNorth -> "NORTE VERDADEIRO"
                    else -> "NORTE MAGNÉTICO"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(enabled = trueNorthAvailable, onClick = onToggleNorthReference)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}
