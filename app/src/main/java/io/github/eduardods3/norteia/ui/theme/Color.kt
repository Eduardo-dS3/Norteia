package io.github.eduardods3.norteia.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// -- Raw palette ----------------------------------------------------------------
// "Aurora on black": a true-black ground for OLED battery + sunlight legibility,
// lifted by a cyan→violet accent and a warm amber for the sun / warnings.

internal val Ink = Color(0xFF000000)
internal val Surface1 = Color(0xFF0B0C10)
internal val Surface2 = Color(0xFF13141B)
internal val Surface3 = Color(0xFF1C1E27)
internal val Hairline = Color(0x14FFFFFF)

internal val TextHigh = Color(0xFFF3F4F8)
internal val TextMed = Color(0xFFAEB1C2)
internal val TextLow = Color(0xFF6C6F82)

internal val AuroraCyan = Color(0xFF44E3C9)
internal val AuroraTeal = Color(0xFF2FB7D8)
internal val AuroraViolet = Color(0xFF8B7BFF)
internal val AuroraIndigo = Color(0xFF5B4BD6)

internal val NorthRed = Color(0xFFFF5B6B)
internal val SunAmber = Color(0xFFFFB874)
internal val MoonSilver = Color(0xFFD7DCF0)

internal val Good = Color(0xFF49E08B)
internal val Fair = Color(0xFFFFC24B)
internal val Poor = Color(0xFFFF6B6B)

internal val DarkColors = darkColorScheme(
    primary = AuroraCyan,
    onPrimary = Ink,
    primaryContainer = Surface3,
    onPrimaryContainer = AuroraCyan,
    secondary = AuroraViolet,
    onSecondary = Ink,
    tertiary = SunAmber,
    onTertiary = Ink,
    background = Ink,
    onBackground = TextHigh,
    surface = Surface1,
    onSurface = TextHigh,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMed,
    outline = Hairline,
    error = Poor,
    onError = Ink,
)

/** Semantic colours Material3 has no slot for. */
@Immutable
data class NorteiaExtraColors(
    val fixGood: Color = Good,
    val fixFair: Color = Fair,
    val fixPoor: Color = Poor,
    val north: Color = NorthRed,
    val sun: Color = SunAmber,
    val moon: Color = MoonSilver,
    val ticksMajor: Color = TextMed,
    val ticksMinor: Color = TextLow,
    val glassStroke: Color = Hairline,
    val auroraStart: Color = AuroraCyan,
    val auroraMid: Color = AuroraTeal,
    val auroraEnd: Color = AuroraViolet,
)

val LocalNorteiaColors = staticCompositionLocalOf { NorteiaExtraColors() }
