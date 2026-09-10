package io.github.eduardods3.norteia.core.geo

import kotlin.math.roundToInt

/** Portuguese compass-point names. */
object Cardinals {

    private val eightWind = listOf(
        "Norte", "Nordeste", "Leste", "Sudeste",
        "Sul", "Sudoeste", "Oeste", "Noroeste",
    )

    private val sixteenWindShort = listOf(
        "N", "NNE", "NE", "ENE", "L", "ESE", "SE", "SSE",
        "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO",
    )

    /** Full name of the nearest of the 8 principal points, e.g. "Nordeste". */
    fun name(bearingDeg: Float): String {
        val idx = (bearingDeg / 45f).roundToInt().mod(8)
        return eightWind[idx]
    }

    /** Two/three-letter code of the nearest of the 16 points, e.g. "NNE". */
    fun code(bearingDeg: Float): String {
        val idx = (bearingDeg / 22.5f).roundToInt().mod(16)
        return sixteenWindShort[idx]
    }
}
