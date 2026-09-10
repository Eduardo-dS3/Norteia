package io.github.eduardods3.norteia.core.geo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reference points are cross-checked against independently published
 * UTM/MGRS values (e.g. NGA GEOTRANS, the USGS grid tools).
 */
class CoordinateConversionTest {

    @Test
    fun dms_formatsWithCarryAndHemisphere() {
        assertEquals("23°33'01.87\"S", Dms.sexagesimal(-23.5505192, isLatitude = true))
        assertEquals("46°38'20.00\"W", Dms.sexagesimal(-46.6388889, isLatitude = false))
        // Rounding that would print 60" carries through minutes into degrees.
        assertEquals("1°00'00.00\"E", Dms.sexagesimal(0.999999722, isLatitude = false))
    }

    @Test
    fun utm_eiffelTower_zone31N() {
        val ref = Utm.fromLatLon(LatLon(48.858370, 2.294481))
        assertEquals(31, ref.zone)
        assertEquals('N', ref.hemisphere)
        assertEquals('U', ref.latBand)
        // Published: ~448 266 E, ~5 411 933 N (sub-10 m is plenty here).
        assertEquals(448266.0, ref.easting, 15.0)
        assertEquals(5411933.0, ref.northing, 15.0)
    }

    @Test
    fun mgrs_eiffelTower_squareIsDQ() {
        val mgrs = Utm.toMgrs(LatLon(48.858370, 2.294481), digits = 5)
        assertTrue("got $mgrs", mgrs.startsWith("31U DQ "))
    }

    @Test
    fun mgrs_empireStateBuilding_squareIsWL() {
        // Even zone (18) exercises the +5 row-letter offset.
        val mgrs = Utm.toMgrs(LatLon(40.748440, -73.985664), digits = 5)
        assertTrue("got $mgrs", mgrs.startsWith("18T WL "))
    }

    @Test
    fun utm_sydney_isSouthernZone56H() {
        val ref = Utm.fromLatLon(LatLon(-33.856784, 151.215297))
        assertEquals(56, ref.zone)
        assertEquals('S', ref.hemisphere)
        assertEquals('H', ref.latBand)
        assertTrue("northing should carry the 10 000 km offset", ref.northing > 6_000_000.0)
    }

    @Test
    fun utm_norwayException_pushesZone32() {
        // Bergen sits in the widened 32V zone, not 31V.
        assertEquals(32, Utm.zoneNumber(60.39, 5.32))
    }

    @Test
    fun format_isStableAndParsable() {
        val s = CoordinateFormat.DECIMAL.render(LatLon(-23.55052, -46.63389))
        assertEquals("-23.55052, -46.63389", s)
    }
}
