package io.github.eduardods3.norteia.core.astro

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Offline Sun and Moon position maths — no network, no dependencies, no cost.
 *
 * The Sun uses the standard low-order VSOP-derived series (as popularised by
 * the NOAA solar calculator); the Moon uses the abbreviated ELP-2000 series
 * from Meeus, "Astronomical Algorithms", chapter 47. Both are public-domain
 * mathematics. Accuracy is a few arc-minutes for the Sun and roughly 0.2° for
 * the Moon — far finer than a phone magnetometer, which is all this feature
 * needs (draw the Sun/Moon markers on the compass rose).
 */
object SunMoon {

    private const val DEG = Math.PI / 180.0
    private const val RAD = 180.0 / Math.PI

    /** Azimuth measured clockwise from true north; altitude above the horizon. */
    data class SkyPosition(val azimuthDeg: Double, val altitudeDeg: Double) {
        val isUp: Boolean get() = altitudeDeg > 0.0
    }

    data class SunInfo(
        val position: SkyPosition,
        /** Compass bearing of the sunrise point, or null on a polar day/night. */
        val sunriseAzimuthDeg: Double?,
        val sunsetAzimuthDeg: Double?,
    )

    data class MoonInfo(
        val position: SkyPosition,
        /** 0.0 = new, 1.0 = full. */
        val illuminatedFraction: Double,
        val phase: MoonPhase,
    )

    enum class MoonPhase(val label: String) {
        NEW("Lua nova"),
        WAXING_CRESCENT("Crescente côncava"),
        FIRST_QUARTER("Quarto crescente"),
        WAXING_GIBBOUS("Crescente convexa"),
        FULL("Lua cheia"),
        WANING_GIBBOUS("Minguante convexa"),
        LAST_QUARTER("Quarto minguante"),
        WANING_CRESCENT("Minguante côncava"),
    }

    // -- public API -----------------------------------------------------------

    fun sun(epochMillis: Long, latDeg: Double, lonDeg: Double): SunInfo {
        val jd = julianDay(epochMillis)
        val t = julianCentury(jd)

        val (raDeg, decDeg, _) = solarEquatorial(t)
        val lst = localSiderealTimeDeg(jd, lonDeg)
        val h = norm360(lst - raDeg) // hour angle, degrees

        val pos = horizontal(h, decDeg, latDeg)

        // Rise/set geometry at the standard -0.833° (refraction + solar radius).
        val h0 = 0.833
        val cosH0 = (sin(-h0 * DEG) - sin(latDeg * DEG) * sin(decDeg * DEG)) /
            (cos(latDeg * DEG) * cos(decDeg * DEG))
        val riseAz: Double?
        val setAz: Double?
        if (cosH0 in -1.0..1.0) {
            val hAngle = Math.acos(cosH0) * RAD
            riseAz = horizontal(-hAngle, decDeg, latDeg).azimuthDeg
            setAz = horizontal(hAngle, decDeg, latDeg).azimuthDeg
        } else {
            riseAz = null
            setAz = null
        }
        return SunInfo(pos, riseAz, setAz)
    }

    fun moon(epochMillis: Long, latDeg: Double, lonDeg: Double): MoonInfo {
        val jd = julianDay(epochMillis)
        val t = julianCentury(jd)

        // Fundamental arguments (Meeus 47.1–47.6), degrees.
        val lp = norm360(218.3164477 + 481267.88123421 * t - 0.0015786 * t * t +
            t * t * t / 538841.0 - t * t * t * t / 65194000.0)
        val d = norm360(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t +
            t * t * t / 545868.0 - t * t * t * t / 113065000.0)
        val m = norm360(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t +
            t * t * t / 24490000.0)
        val mp = norm360(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t +
            t * t * t / 69699.0 - t * t * t * t / 14712000.0)
        val f = norm360(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t -
            t * t * t / 3526000.0 + t * t * t * t / 863310000.0)
        val e = 1.0 - 0.002516 * t - 0.0000074 * t * t

        // Longitude terms Σl, coefficients in 1e-6 degrees (largest ~15 terms).
        var sumL = 0.0
        sumL += 6288774 * s(mp)
        sumL += 1274027 * s(2 * d - mp)
        sumL += 658314 * s(2 * d)
        sumL += 213618 * s(2 * mp)
        sumL += -185116 * e * s(m)
        sumL += -114332 * s(2 * f)
        sumL += 58793 * s(2 * d - 2 * mp)
        sumL += 57066 * e * s(2 * d - m - mp)
        sumL += 53322 * s(2 * d + mp)
        sumL += 45758 * e * s(2 * d - m)
        sumL += -40923 * e * s(m - mp)
        sumL += -34720 * s(d)
        sumL += -30383 * e * s(m + mp)
        sumL += 15327 * s(2 * d - 2 * f)
        sumL += -12528 * s(mp + 2 * f)
        sumL += 10980 * s(mp - 2 * f)

        // Latitude terms Σb, coefficients in 1e-6 degrees.
        var sumB = 0.0
        sumB += 5128122 * s(f)
        sumB += 280602 * s(mp + f)
        sumB += 277693 * s(mp - f)
        sumB += 173237 * s(2 * d - f)
        sumB += 55413 * s(2 * d - mp + f)
        sumB += 46271 * s(2 * d - mp - f)
        sumB += 32573 * s(2 * d + f)
        sumB += 17198 * s(2 * mp + f)
        sumB += 9266 * s(2 * d + mp - f)
        sumB += 8822 * s(2 * mp - f)
        sumB += 8216 * e * s(2 * d - m - f)
        sumB += 4324 * s(2 * d - 2 * mp - f)

        val moonLon = norm360(lp + sumL / 1_000_000.0)
        val moonLat = sumB / 1_000_000.0

        val eps = 23.439291 - 0.0130042 * t
        val ra = norm360(
            atan2(
                sin(moonLon * DEG) * cos(eps * DEG) - tan(moonLat * DEG) * sin(eps * DEG),
                cos(moonLon * DEG),
            ) * RAD,
        )
        val dec = asin(
            sin(moonLat * DEG) * cos(eps * DEG) +
                cos(moonLat * DEG) * sin(eps * DEG) * sin(moonLon * DEG),
        ) * RAD

        val lst = localSiderealTimeDeg(jd, lonDeg)
        val pos = horizontal(norm360(lst - ra), dec, latDeg)

        // Phase from a simple Sun longitude and the Moon–Sun elongation.
        val sunLon = norm360(280.4665 + 36000.7698 * t + 1.9147 * s(norm360(357.529 + 35999.05 * t)))
        val elongation = norm360(moonLon - sunLon)
        val illum = (1.0 - cos(elongation * DEG)) / 2.0
        val phase = when {
            elongation < 22.5 || elongation >= 337.5 -> MoonPhase.NEW
            elongation < 67.5 -> MoonPhase.WAXING_CRESCENT
            elongation < 112.5 -> MoonPhase.FIRST_QUARTER
            elongation < 157.5 -> MoonPhase.WAXING_GIBBOUS
            elongation < 202.5 -> MoonPhase.FULL
            elongation < 247.5 -> MoonPhase.WANING_GIBBOUS
            elongation < 292.5 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
        return MoonInfo(pos, illum.coerceIn(0.0, 1.0), phase)
    }

    // -- shared maths -------------------------------------------------------

    private data class Equatorial(val raDeg: Double, val decDeg: Double, val eclLonDeg: Double)

    /** Apparent RA / Dec of the Sun for Julian century [t]. */
    private fun solarEquatorial(t: Double): Equatorial {
        val l0 = norm360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * s(m) +
            (0.019993 - 0.000101 * t) * s(2 * m) +
            0.000289 * s(3 * m)
        val trueLon = l0 + c
        val omega = 125.04 - 1934.136 * t
        val appLon = trueLon - 0.00569 - 0.00478 * s(omega)
        val eps = 23.439291 - 0.0130042 * t + 0.00256 * cos(omega * DEG)
        val ra = norm360(atan2(cos(eps * DEG) * s(appLon), cos(appLon * DEG)) * RAD)
        val dec = asin(sin(eps * DEG) * s(appLon)) * RAD
        return Equatorial(ra, dec, appLon)
    }

    /** Local apparent sidereal time in degrees (nutation ignored, ~arc-sec). */
    private fun localSiderealTimeDeg(jd: Double, lonDeg: Double): Double {
        val d = jd - 2451545.0
        val gmst = 280.46061837 + 360.98564736629 * d
        return norm360(gmst + lonDeg)
    }

    /**
     * Converts an hour angle [hDeg] (positive west of the meridian) and
     * declination to a compass azimuth + altitude for the observer at [latDeg].
     * Follows Meeus 13.5/13.6, then rotates azimuth to be from true north.
     */
    private fun horizontal(hDeg: Double, decDeg: Double, latDeg: Double): SkyPosition {
        val h = hDeg * DEG
        val dec = decDeg * DEG
        val lat = latDeg * DEG
        val azFromSouth = atan2(
            sin(h),
            cos(h) * sin(lat) - tan(dec) * cos(lat),
        )
        val alt = asin(sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(h))
        val azFromNorth = norm360(azFromSouth * RAD + 180.0)
        return SkyPosition(azFromNorth, alt * RAD)
    }

    private fun julianDay(epochMillis: Long): Double =
        epochMillis / 86_400_000.0 + 2440587.5

    private fun julianCentury(jd: Double): Double = (jd - 2451545.0) / 36525.0

    private fun s(deg: Double): Double = sin(deg * DEG)

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /** True when two bearings are within [tol]° — used by the UI for markers. */
    fun bearingsClose(a: Double, b: Double, tol: Double = 2.0): Boolean {
        val diff = abs(norm360(a - b))
        return diff <= tol || diff >= 360.0 - tol
    }
}
