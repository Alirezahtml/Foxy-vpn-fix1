package com.vauth.foxyvpn.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColorHexTest {
    @Test
    fun parsesSixDigitColorAsOpaqueArgb() {
        assertEquals(0xFF2962FFL, parseOpaqueHexColor("#2962ff"))
        assertEquals(0xFF00E5FFL, parseOpaqueHexColor("00E5FF"))
    }

    @Test
    fun rejectsPartialOrMalformedColors() {
        assertNull(parseOpaqueHexColor("#12F"))
        assertNull(parseOpaqueHexColor("#12GG00"))
        assertNull(parseOpaqueHexColor(""))
    }

    @Test
    fun formatsAndParsesWithoutChangingRgb() {
        val color = 0xFFB40ACDL
        assertEquals("#B40ACD", formatOpaqueHexColor(color))
        assertEquals(color, parseOpaqueHexColor(formatOpaqueHexColor(color)))
    }
}
