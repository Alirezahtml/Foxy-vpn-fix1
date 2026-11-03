package com.vauth.foxyvpn.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteMapCoordinatesTest {
    @Test
    fun knownCityUsesItsMappedCoordinates() {
        val frankfurt = resolveLatLonDeg("DE", "fra")
        assertEquals(50.1109, frankfurt!!.first, 0.0001)
        assertEquals(8.6821, frankfurt.second, 0.0001)
    }

    @Test
    fun knownManchesterAliasUsesManchesterCoordinates() {
        val manchester = resolveLatLonDeg("GB", "mnc")
        assertEquals(53.4808, manchester!!.first, 0.0001)
        assertEquals(-2.2426, manchester.second, 0.0001)
    }

    @Test
    fun unknownCityIsNotMisrepresentedByCountryFallback() {
        assertNull(resolveLatLonDeg("DE", "unknown-city"))
    }

    @Test
    fun countryOnlyLocationUsesExplicitCountryReferencePoint() {
        val germany = resolveLatLonDeg("DE", null)
        assertEquals(50.1109, germany!!.first, 0.0001)
        assertEquals(8.6821, germany.second, 0.0001)
    }

    @Test
    fun unknownCountryHasNoInventedCoordinates() {
        assertNull(resolveLatLonDeg("ZZ", null))
    }
}
