package io.github.eduardods3.norteia.core.astro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.time.Instant
import org.junit.Test

class SunMoonTest {

    /** 2024-06-21 11:00 UTC ≈ solar noon in London on the June solstice. */
    private val londonNoonSolstice = Instant.parse("2024-06-21T11:03:00Z").toEpochMilli()
    private val londonLat = 51.4779
    private val londonLon = -0.0015

    @Test
    fun sun_isHighAndDueSouthAtLondonSolsticeNoon() {
        val sun = SunMoon.sun(londonNoonSolstice, londonLat, londonLon)
        // Max altitude ≈ 90 - lat + obliquity ≈ 61.9°.
        assertEquals(61.9, sun.position.altitudeDeg, 3.0)
        // Due south.
        assertEquals(180.0, sun.position.azimuthDeg, 6.0)
    }

    @Test
    fun sun_risesInTheNortheastAtLondonMidsummer() {
        val sun = SunMoon.sun(londonNoonSolstice, londonLat, londonLon)
        assertNotNull(sun.sunriseAzimuthDeg)
        assertNotNull(sun.sunsetAzimuthDeg)
        val rise = sun.sunriseAzimuthDeg!!
        val set = sun.sunsetAzimuthDeg!!
        assertTrue("sunrise az was $rise", rise in 40.0..65.0)   // ≈ NE
        assertTrue("sunset az was $set", set in 295.0..320.0)    // ≈ NW
    }

    @Test
    fun moon_returnsPlausibleValues() {
        val moon = SunMoon.moon(londonNoonSolstice, londonLat, londonLon)
        assertTrue(moon.position.azimuthDeg in 0.0..360.0)
        assertTrue(moon.position.altitudeDeg in -90.0..90.0)
        assertTrue(moon.illuminatedFraction in 0.0..1.0)
        assertNotNull(moon.phase)
    }

    @Test
    fun polarDay_hasNoSunriseAzimuth() {
        // Longyearbyen, Svalbard, on the June solstice: sun never sets.
        val svalbard = SunMoon.sun(londonNoonSolstice, 78.22, 15.65)
        assertTrue(svalbard.sunriseAzimuthDeg == null)
    }
}
