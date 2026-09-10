package io.github.eduardods3.norteia.core.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AngleMathTest {

    @Test
    fun normalize360_wrapsBothWays() {
        assertEquals(10f, AngleMath.normalize360(370f), 1e-4f)
        assertEquals(350f, AngleMath.normalize360(-10f), 1e-4f)
        assertEquals(0f, AngleMath.normalize360(360f), 1e-4f)
    }

    @Test
    fun normalize180_centresOnZero() {
        assertEquals(-170f, AngleMath.normalize180(190f), 1e-4f)
        assertEquals(170f, AngleMath.normalize180(-190f), 1e-4f)
        assertEquals(180f, AngleMath.normalize180(180f), 1e-4f)
    }

    @Test
    fun delta_takesTheShortWayRoundTheSeam() {
        assertEquals(20f, AngleMath.delta(350f, 10f), 1e-4f)
        assertEquals(-20f, AngleMath.delta(10f, 350f), 1e-4f)
        assertEquals(0f, AngleMath.delta(123f, 123f), 1e-4f)
    }

    @Test
    fun lowPass_seedsOnFirstSampleThenEasesAcrossZero() {
        assertEquals(100f, AngleMath.lowPass(null, 100f, 0.2f), 1e-4f)
        // Full step (alpha = 1) from 350° to 10° lands on 10°, not 180°.
        assertEquals(10f, AngleMath.lowPass(350f, 10f, 1f), 1e-3f)
        // Half step lands on the seam.
        assertEquals(0f, AngleMath.lowPass(350f, 10f, 0.5f), 1e-3f)
    }

    @Test
    fun circularMean_of359and1_isZero() {
        val mean = AngleMath.circularMean(floatArrayOf(359f, 1f))
        assertTrue("expected ~0/360 but was $mean", mean < 1f || mean > 359f)
    }

    @Test
    fun isNear_respectsTheSeam() {
        assertTrue(AngleMath.isNear(359f, 1f, 3f))
        assertTrue(!AngleMath.isNear(0f, 10f, 3f))
    }
}
