package com.vauth.foxyvpn.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunnelMapCoordinatesTest {
    @Test
    fun manchesterCodeUsesManchesterCoordinates() {
        assertTrue(isKnownRouteMapCityCode("mnc"))
        val point = resolveLatLonDeg("GB", "mnc")
        assertNotNull(point)
        assertEquals(53.4808, point!!.first, 0.0001)
        assertEquals(-2.2426, point.second, 0.0001)
    }

    @Test
    fun cityCodesAreTrimmedAndCaseInsensitive() {
        assertTrue(isKnownRouteMapCityCode(" MNC "))
    }
}
