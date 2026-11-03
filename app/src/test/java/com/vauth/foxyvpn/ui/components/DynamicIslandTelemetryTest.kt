package com.vauth.foxyvpn.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DynamicIslandTelemetryTest {
    @Test
    fun unchangedPingProducesZeroJitter() {
        val (history, average) = appendPingJitterSample(emptyList(), 42, 42)

        assertEquals(listOf(0), history)
        assertEquals(0, average)
    }

    @Test
    fun jitterIsRollingMeanOfAbsolutePingDeltas() {
        val (history, average) = appendPingJitterSample(listOf(20, 40), 100, 110)

        assertEquals(listOf(20, 40, 10), history)
        assertEquals(23, average)
    }

    @Test
    fun jitterHistoryKeepsOnlyMostRecentSamples() {
        val (history, average) = appendPingJitterSample((1..8).toList(), 20, 25)

        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8, 5), history)
        assertEquals(5, average)
    }

    @Test
    fun invalidPingDoesNotCreateJitterMeasurement() {
        val (history, average) = appendPingJitterSample(listOf(5), 0, 42)

        assertEquals(listOf(5), history)
        assertNull(average)
    }
}
