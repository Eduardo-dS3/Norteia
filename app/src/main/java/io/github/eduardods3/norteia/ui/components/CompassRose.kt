package io.github.eduardods3.norteia.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.eduardods3.norteia.ui.theme.Ink
import io.github.eduardods3.norteia.ui.theme.NorteiaExtraColors
import io.github.eduardods3.norteia.ui.theme.NorteiaTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The hand-drawn compass rose. Everything is vector maths on a [Canvas] — no
 * bitmaps, no third-party art — so it stays razor sharp at any size and in any
 * density, and there is nothing to plagiarise.
 *
 * The dial (ticks, cardinal letters, north needle, sun/moon) rotates by
 * `-heading`; a fixed lubber pointer at 12 o'clock marks the way the top of the
 * phone points.
 */
@Composable
fun CompassRose(
    headingDeg: Float,
    sunAzimuthDeg: Double?,
    sunUp: Boolean,
    moonAzimuthDeg: Double?,
    moonUp: Boolean,
    moonIllumination: Double,
    courseDeg: Float?,
    modifier: Modifier = Modifier,
) {
    val extra = NorteiaTheme.extra
    val onSurface = MaterialTheme.colorScheme.onSurface
    val measurer = rememberTextMeasurer()

    val shimmer by rememberInfiniteTransition(label = "rim").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rimSweep",
    )

    val cardinalStyle = remember {
        TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center)
    }
    val ordinalStyle = remember {
        TextStyle(fontSize = 11.sp, textAlign = TextAlign.Center)
    }
    val degreeStyle = remember {
        TextStyle(fontSize = 10.sp, textAlign = TextAlign.Center)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f - 8.dp.toPx()

        drawAmbientGlow(center, radius, extra.auroraStart)
        drawLuminousRim(center, radius, shimmer, extra)

        // --- rotating dial ---------------------------------------------------
        drawTicks(center, radius, headingDeg, extra)
        drawDegreeLabels(center, radius, headingDeg, measurer, degreeStyle.copy(color = extra.ticksMinor))
        drawCardinalLabels(
            center, radius, headingDeg, measurer,
            cardinalStyle = cardinalStyle,
            ordinalStyle = ordinalStyle,
            northColor = extra.north,
            textColor = onSurface,
            mutedColor = extra.ticksMajor,
        )
        drawNorthNeedle(center, radius, headingDeg, extra.north)

        sunAzimuthDeg?.let {
            drawSunMarker(center, radius * 0.72f, (it - headingDeg).toFloat(), extra.sun, dim = !sunUp)
        }
        moonAzimuthDeg?.let {
            drawMoonMarker(
                center, radius * 0.72f, (it - headingDeg).toFloat(),
                extra.moon, dim = !moonUp, illumination = moonIllumination,
            )
        }
        courseDeg?.let {
            drawCourseMarker(center, radius, it - headingDeg, extra.auroraEnd)
        }

        // --- fixed overlay -------------------------------------------------
        drawLubberPointer(center, radius, extra.auroraStart)
        drawHub(center, extra)
    }
}

// --- individual pieces -------------------------------------------------------

private fun DrawScope.drawAmbientGlow(center: Offset, radius: Float, accent: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
            center = center,
            radius = radius * 1.15f,
        ),
        radius = radius * 1.15f,
        center = center,
    )
}

private fun DrawScope.drawLuminousRim(
    center: Offset,
    radius: Float,
    sweepStart: Float,
    extra: NorteiaExtraColors,
) {
    // Static hairline.
    drawCircle(
        color = extra.glassStroke,
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx()),
    )
    // Rotating aurora sweep, kept low-alpha so it reads as a sheen not a toy.
    rotate(sweepStart, pivot = center) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    extra.auroraStart.copy(alpha = 0.0f),
                    extra.auroraStart.copy(alpha = 0.55f),
                    extra.auroraMid.copy(alpha = 0.35f),
                    extra.auroraEnd.copy(alpha = 0.55f),
                    Color.Transparent,
                ),
                center = center,
            ),
            radius = radius - 2.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

private fun DrawScope.drawTicks(
    center: Offset,
    radius: Float,
    heading: Float,
    extra: NorteiaExtraColors,
) {
    val outer = radius - 4.dp.toPx()
    for (bearing in 0 until 360 step 5) {
        val major = bearing % 45 == 0
        val medium = bearing % 15 == 0
        val len = when {
            major -> 16.dp.toPx()
            medium -> 11.dp.toPx()
            else -> 6.dp.toPx()
        }
        val stroke = if (major) 2.5.dp.toPx() else 1.dp.toPx()
        val color = if (major || medium) extra.ticksMajor else extra.ticksMinor
        val a = angleRad(bearing - heading)
        val p1 = Offset(center.x + outer * cos(a), center.y + outer * sin(a))
        val p2 = Offset(center.x + (outer - len) * cos(a), center.y + (outer - len) * sin(a))
        drawLine(color, p1, p2, strokeWidth = stroke)
    }
}

private fun DrawScope.drawDegreeLabels(
    center: Offset,
    radius: Float,
    heading: Float,
    measurer: TextMeasurer,
    style: TextStyle,
) {
    val r = radius - 30.dp.toPx()
    for (bearing in 0 until 360 step 30) {
        if (bearing % 90 == 0) continue // 0/90/180/270 handled by letters
        val a = angleRad(bearing - heading)
        val pos = Offset(center.x + r * cos(a), center.y + r * sin(a))
        val layout = measurer.measure(bearing.toString(), style)
        drawText(
            layout,
            topLeft = Offset(
                pos.x - layout.size.width / 2f,
                pos.y - layout.size.height / 2f,
            ),
        )
    }
}

private fun DrawScope.drawCardinalLabels(
    center: Offset,
    radius: Float,
    heading: Float,
    measurer: TextMeasurer,
    cardinalStyle: TextStyle,
    ordinalStyle: TextStyle,
    northColor: Color,
    textColor: Color,
    mutedColor: Color,
) {
    val rCardinal = radius - 30.dp.toPx()
    val rOrdinal = radius - 28.dp.toPx()

    // bearing, text, isPrimary
    val labels = listOf(
        Triple(0, "N", true), Triple(90, "L", true), Triple(180, "S", true), Triple(270, "O", true),
        Triple(45, "NE", false), Triple(135, "SE", false),
        Triple(225, "SO", false), Triple(315, "NO", false),
    )
    for ((bearing, text, primary) in labels) {
        val a = angleRad(bearing - heading)
        val r = if (primary) rCardinal else rOrdinal
        val pos = Offset(center.x + r * cos(a), center.y + r * sin(a))
        val style = if (primary) {
            cardinalStyle.copy(color = if (bearing == 0) northColor else textColor)
        } else {
            ordinalStyle.copy(color = mutedColor)
        }
        val layout = measurer.measure(text, style)
        drawText(
            layout,
            topLeft = Offset(pos.x - layout.size.width / 2f, pos.y - layout.size.height / 2f),
        )
    }
}

private fun DrawScope.drawNorthNeedle(
    center: Offset,
    radius: Float,
    heading: Float,
    northColor: Color,
) {
    val tip = radius - 46.dp.toPx()
    val baseW = 9.dp.toPx()
    val aN = angleRad(0f - heading)
    val aBaseL = angleRad(0f - heading - 90f)
    val aBaseR = angleRad(0f - heading + 90f)

    val north = Offset(center.x + tip * cos(aN), center.y + tip * sin(aN))
    val bl = Offset(center.x + baseW * cos(aBaseL), center.y + baseW * sin(aBaseL))
    val br = Offset(center.x + baseW * cos(aBaseR), center.y + baseW * sin(aBaseR))
    drawPath(
        Path().apply {
            moveTo(north.x, north.y); lineTo(bl.x, bl.y); lineTo(br.x, br.y); close()
        },
        color = northColor,
    )

    val aS = angleRad(180f - heading)
    val south = Offset(center.x + (tip * 0.82f) * cos(aS), center.y + (tip * 0.82f) * sin(aS))
    drawPath(
        Path().apply {
            moveTo(south.x, south.y); lineTo(bl.x, bl.y); lineTo(br.x, br.y); close()
        },
        color = Color(0x33FFFFFF),
    )
}

private fun DrawScope.drawSunMarker(
    center: Offset,
    orbit: Float,
    displayBearingDeg: Float,
    color: Color,
    dim: Boolean,
) {
    val a = angleRad(displayBearingDeg)
    val pos = Offset(center.x + orbit * cos(a), center.y + orbit * sin(a))
    val alpha = if (dim) 0.28f else 1f
    val r = 7.dp.toPx()

    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.35f * alpha), Color.Transparent),
            center = pos, radius = r * 3f,
        ),
        radius = r * 3f, center = pos,
    )
    drawCircle(color.copy(alpha = alpha), r, pos)
    for (i in 0 until 8) {
        val ra = angleRad(i * 45f)
        drawLine(
            color.copy(alpha = alpha),
            Offset(pos.x + (r + 2.dp.toPx()) * cos(ra), pos.y + (r + 2.dp.toPx()) * sin(ra)),
            Offset(pos.x + (r + 6.dp.toPx()) * cos(ra), pos.y + (r + 6.dp.toPx()) * sin(ra)),
            strokeWidth = 1.5.dp.toPx(),
        )
    }
}

private fun DrawScope.drawMoonMarker(
    center: Offset,
    orbit: Float,
    displayBearingDeg: Float,
    color: Color,
    dim: Boolean,
    illumination: Double,
) {
    val a = angleRad(displayBearingDeg)
    val pos = Offset(center.x + orbit * cos(a), center.y + orbit * sin(a))
    val alpha = if (dim) 0.28f else 1f
    val r = 7.dp.toPx()

    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.30f * alpha), Color.Transparent),
            center = pos, radius = r * 3f,
        ),
        radius = r * 3f, center = pos,
    )
    drawCircle(color.copy(alpha = alpha), r, pos)
    // Simple terminator hint: slide a dark disc across as the moon wanes.
    val shift = (r * 1.7f * (1f - illumination.toFloat())).toFloat().coerceIn(0f, r * 1.7f)
    if (shift > 0.5f) {
        drawCircle(Ink.copy(alpha = 0.92f), r, Offset(pos.x - shift, pos.y))
    }
}

private fun DrawScope.drawCourseMarker(
    center: Offset,
    radius: Float,
    displayBearingDeg: Float,
    color: Color,
) {
    val a = angleRad(displayBearingDeg)
    val tip = Offset(center.x + (radius - 4.dp.toPx()) * cos(a), center.y + (radius - 4.dp.toPx()) * sin(a))
    val bl = Offset(
        center.x + (radius - 16.dp.toPx()) * cos(a - 0.06f),
        center.y + (radius - 16.dp.toPx()) * sin(a - 0.06f),
    )
    val br = Offset(
        center.x + (radius - 16.dp.toPx()) * cos(a + 0.06f),
        center.y + (radius - 16.dp.toPx()) * sin(a + 0.06f),
    )
    drawPath(
        Path().apply { moveTo(tip.x, tip.y); lineTo(bl.x, bl.y); lineTo(br.x, br.y); close() },
        color = color,
    )
}

private fun DrawScope.drawLubberPointer(center: Offset, radius: Float, color: Color) {
    val topY = center.y - radius + 2.dp.toPx()
    val w = 9.dp.toPx()
    val h = 14.dp.toPx()
    drawPath(
        Path().apply {
            moveTo(center.x, topY + h)
            lineTo(center.x - w, topY)
            lineTo(center.x + w, topY)
            close()
        },
        color = color,
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(center.x, topY + h / 2f),
            radius = h * 1.6f,
        ),
        radius = h * 1.6f,
        center = Offset(center.x, topY + h / 2f),
    )
}

private fun DrawScope.drawHub(
    center: Offset,
    extra: NorteiaExtraColors,
) {
    drawCircle(extra.glassStroke, 5.dp.toPx(), center, style = Stroke(1.dp.toPx()))
    drawCircle(extra.auroraStart.copy(alpha = 0.9f), 3.dp.toPx(), center)
}

/** Compass bearing (0° = up, clockwise) → screen-space radians. */
private fun angleRad(bearingDeg: Float): Float =
    Math.toRadians((bearingDeg - 90f).toDouble()).toFloat()
