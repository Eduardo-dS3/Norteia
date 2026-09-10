package io.github.eduardods3.norteia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.eduardods3.norteia.data.sensors.LevelReading
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme
import java.util.Locale
import kotlin.math.min

/**
 * A bubble level driven by the gravity vector. The bubble drifts opposite the
 * tilt, like a real spirit level, and snaps green within ±1°.
 */
@Composable
fun BubbleLevel(reading: LevelReading?, modifier: Modifier = Modifier) {
    val extra = NorteiaTheme.extra
    val pitch = reading?.pitchDeg ?: 0f
    val roll = reading?.rollDeg ?: 0f
    val level = reading?.isLevel == true

    val animPitch by animateFloatAsState(pitch, label = "pitch")
    val animRoll by animateFloatAsState(roll, label = "roll")

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(84.dp)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = min(size.width, size.height) / 2f - 2.dp.toPx()
                    drawCircle(extra.glassStroke, r, c, style = Stroke(1.dp.toPx()))
                    drawCircle(extra.glassStroke, r * 0.32f, c, style = Stroke(1.dp.toPx()))
                    drawLine(
                        extra.glassStroke,
                        Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1.dp.toPx(),
                    )
                    drawLine(
                        extra.glassStroke,
                        Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1.dp.toPx(),
                    )
                    // Bubble: 1° ≈ 6 % of the radius, clamped inside the ring.
                    val maxOffset = r * 0.8f
                    val bx = (animRoll / 12f * maxOffset).coerceIn(-maxOffset, maxOffset)
                    val by = (animPitch / 12f * maxOffset).coerceIn(-maxOffset, maxOffset)
                    val bubble = if (level) extra.fixGood else extra.auroraStart
                    drawCircle(bubble.copy(alpha = 0.35f), r * 0.26f, Offset(c.x + bx, c.y + by))
                    drawCircle(bubble, r * 0.20f, Offset(c.x + bx, c.y + by))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "NÍVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = extra.auroraStart,
                )
                Text(
                    if (reading?.hasSensor == false) {
                        "Sem sensor"
                    } else {
                        String.format(Locale.US, "%+.1f°  ·  %+.1f°", pitch, roll)
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    if (level) "Nivelado" else "Frente/trás  ·  esquerda/direita",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (level) extra.fixGood else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
