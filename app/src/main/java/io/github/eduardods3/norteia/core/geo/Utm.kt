package io.github.eduardods3.norteia.core.geo

import java.util.Locale
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * WGS-84 -> UTM (Universal Transverse Mercator) and MGRS
 * (Military Grid Reference System) conversion.
 *
 * This is a fresh implementation of the standard, publicly documented
 * transverse-Mercator series (Snyder, "Map Projections — A Working Manual",
 * USGS Professional Paper 1395) and the NGA MGRS grid-lettering rules.
 * The formulae themselves are public-domain mathematics.
 *
 * Accuracy: sub-metre for all non-polar latitudes, which is well below any
 * consumer GNSS error. Polar regions (|lat| > 84° or < -80°) fall outside the
 * UTM/MGRS domain and are clamped to the nearest valid band.
 */
object Utm {

    // --- WGS-84 ellipsoid + UTM constants ------------------------------------
    private const val A = 6_378_137.0                 // semi-major axis (m)
    private const val F = 1.0 / 298.257223563         // flattening
    private const val K0 = 0.9996                     // scale on the central meridian
    private val E2 = F * (2 - F)                       // first eccentricity²
    private val EP2 = E2 / (1 - E2)                    // second eccentricity²

    /** Latitude band letters from -80° to +84°, 8° each (X is 12°). No I/O. */
    private const val LAT_BANDS = "CDEFGHJKLMNPQRSTUVWX"

    /** 100 km column letters, three sets of eight selected by zone mod 3. */
    private const val COL_SET_1 = "ABCDEFGH" // zone % 3 == 1
    private const val COL_SET_2 = "JKLMNPQR" // zone % 3 == 2
    private const val COL_SET_0 = "STUVWXYZ" // zone % 3 == 0

    /** 100 km row letters, 20 of them, repeating every 2 000 000 m. No I/O. */
    private const val ROW_LETTERS = "ABCDEFGHJKLMNPQRSTUV"

    data class Ref(
        val zone: Int,
        val hemisphere: Char, // 'N' or 'S'
        val latBand: Char,
        val easting: Double,
        val northing: Double,
    )

    /** UTM zone number, including the Norway and Svalbard exceptions. */
    fun zoneNumber(lat: Double, lon: Double): Int {
        var zone = floor((lon + 180.0) / 6.0).toInt() + 1
        if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0) zone = 32
        if (lat >= 72.0 && lat < 84.0) {
            zone = when {
                lon >= 0.0 && lon < 9.0 -> 31
                lon >= 9.0 && lon < 21.0 -> 33
                lon >= 21.0 && lon < 33.0 -> 35
                lon >= 33.0 && lon < 42.0 -> 37
                else -> zone
            }
        }
        return zone.coerceIn(1, 60)
    }

    fun latBand(lat: Double): Char {
        val clamped = lat.coerceIn(-80.0, 83.999999)
        val idx = floor((clamped + 80.0) / 8.0).toInt().coerceIn(0, LAT_BANDS.lastIndex)
        return LAT_BANDS[idx]
    }

    /** Projects a geographic position onto its UTM zone. */
    fun fromLatLon(pos: LatLon): Ref {
        val lat = pos.latitude
        val lon = pos.longitude
        val zone = zoneNumber(lat, lon)
        val lonOrigin = (zone - 1) * 6 - 180 + 3

        val latRad = Math.toRadians(lat)
        val dLonRad = Math.toRadians(lon - lonOrigin)

        val sinLat = sin(latRad)
        val cosLat = cos(latRad)
        val tanLat = tan(latRad)

        val n = A / sqrt(1 - E2 * sinLat * sinLat)
        val t = tanLat * tanLat
        val c = EP2 * cosLat * cosLat
        val a1 = cosLat * dLonRad

        val m = A * (
            (1 - E2 / 4 - 3 * E2 * E2 / 64 - 5 * E2 * E2 * E2 / 256) * latRad -
                (3 * E2 / 8 + 3 * E2 * E2 / 32 + 45 * E2 * E2 * E2 / 1024) * sin(2 * latRad) +
                (15 * E2 * E2 / 256 + 45 * E2 * E2 * E2 / 1024) * sin(4 * latRad) -
                (35 * E2 * E2 * E2 / 3072) * sin(6 * latRad)
            )

        val easting = K0 * n * (
            a1 +
                (1 - t + c) * a1.pow(3) / 6 +
                (5 - 18 * t + t * t + 72 * c - 58 * EP2) * a1.pow(5) / 120
            ) + 500_000.0

        var northing = K0 * (
            m + n * tanLat * (
                a1 * a1 / 2 +
                    (5 - t + 9 * c + 4 * c * c) * a1.pow(4) / 24 +
                    (61 - 58 * t + t * t + 600 * c - 330 * EP2) * a1.pow(6) / 720
                )
            )

        val hemisphere = if (lat >= 0) 'N' else 'S'
        if (hemisphere == 'S') northing += 10_000_000.0

        return Ref(zone, hemisphere, latBand(lat), easting, northing)
    }

    /** e.g. "23K 351430 mE 7394554 mN". */
    fun format(pos: LatLon): String {
        val r = fromLatLon(pos)
        return String.format(
            Locale.US,
            "%d%c %d mE %d mN",
            r.zone, r.latBand, r.easting.roundToLong(), r.northing.roundToLong(),
        )
    }

    /**
     * MGRS string, e.g. "23K PS 5143 9455" at [digits] = 4 (10 m) or
     * "23KPS5143094554" style at [digits] = 5 (1 m). Values are truncated,
     * per the MGRS convention, not rounded.
     */
    fun toMgrs(pos: LatLon, digits: Int = 5): String {
        require(digits in 1..5) { "digits must be 1..5" }
        val r = fromLatLon(pos)
        val square = hundredKmSquare(r.zone, r.easting, r.northing)

        val divisor = 10.0.pow(5 - digits).toLong()
        val e = ((r.easting % 100_000).toLong() / divisor).toString().padStart(digits, '0')
        val n = ((r.northing % 100_000).toLong() / divisor).toString().padStart(digits, '0')

        return "${r.zone}${r.latBand} $square $e $n"
    }

    private fun hundredKmSquare(zone: Int, easting: Double, northing: Double): Char2 {
        val colLetters = when (zone % 3) {
            1 -> COL_SET_1
            2 -> COL_SET_2
            else -> COL_SET_0
        }
        val colIdx = (floor(easting / 100_000).toInt() - 1).coerceIn(0, colLetters.lastIndex)

        var rowIdx = floor(northing / 100_000).toInt() % 20
        if (zone % 2 == 0) rowIdx = (rowIdx + 5) % 20 // even zones are offset by five rows

        return Char2(colLetters[colIdx], ROW_LETTERS[rowIdx])
    }

    /** Tiny value type so the two-letter square reads clearly at call sites. */
    private data class Char2(val a: Char, val b: Char) {
        override fun toString() = "$a$b"
    }
}
