package com.vauth.foxyvpn.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class CustomAppPaletteTest {
    @Test
    fun paletteRoundTripsThroughPersistenceEncoding() {
        val original = CustomAppPalette.from(FoxyColorTheme.CYBER_FOX)
            .withColor(AppPaletteRole.PRIMARY, 0xFFAA33CCL)
            .withColor(AppPaletteRole.DARK_BACKGROUND, 0xFF101820L)

        assertEquals(original, CustomAppPalette.decode(original.encode()))
    }

    @Test
    fun invalidOrIncompletePaletteIsRejected() {
        assertNull(CustomAppPalette.decode(null))
        assertNull(CustomAppPalette.decode("not-a-palette"))
        assertNull(CustomAppPalette.decode("FF000000,FF000000"))
    }

    @Test
    fun customPaletteCanBeSeededFromExistingTheme() {
        val palette = CustomAppPalette.from(FoxyColorTheme.CYBER_FOX)
        assertNotNull(palette)
        assertEquals(FoxyColorTheme.CYBER_FOX.primaryLightHex, palette.primaryArgb)
        assertEquals(FoxyColorTheme.CYBER_FOX.darkSurfaceHex, palette.darkSurfaceArgb)
    }
}
