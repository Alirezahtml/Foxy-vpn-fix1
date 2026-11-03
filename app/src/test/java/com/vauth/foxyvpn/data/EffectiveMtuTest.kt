package com.vauth.foxyvpn.data

import org.junit.Assert.assertEquals
import org.junit.Test

class EffectiveMtuTest {
    @Test
    fun gamingModeUsesReducedMtuWhenAutomatic() {
        assertEquals(1360, computeEffectiveMtu(gamingModeEnabled = true, mtuModeAuto = true, configuredMtu = 1480))
    }

    @Test
    fun manualMtuTakesPrecedenceOverGamingMode() {
        assertEquals(1450, computeEffectiveMtu(gamingModeEnabled = true, mtuModeAuto = false, configuredMtu = 1450))
    }

    @Test
    fun automaticModeUsesSafeDefaultWhenGamingModeIsOff() {
        assertEquals(1400, computeEffectiveMtu(gamingModeEnabled = false, mtuModeAuto = true, configuredMtu = 1480))
    }

    @Test
    fun manualMtuIsClampedToSupportedRange() {
        assertEquals(1280, computeEffectiveMtu(gamingModeEnabled = false, mtuModeAuto = false, configuredMtu = 900))
        assertEquals(1500, computeEffectiveMtu(gamingModeEnabled = false, mtuModeAuto = false, configuredMtu = 2000))
    }
}
