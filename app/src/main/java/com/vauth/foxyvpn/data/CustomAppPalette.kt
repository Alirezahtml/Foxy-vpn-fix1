package com.vauth.foxyvpn.data

/** A user-owned palette with separate neutral surfaces for light and dark appearance. */
data class CustomAppPalette(
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val lightBackgroundArgb: Long,
    val lightSurfaceArgb: Long,
    val lightElevatedArgb: Long,
    val darkBackgroundArgb: Long,
    val darkSurfaceArgb: Long,
    val darkElevatedArgb: Long,
) {
    fun color(role: AppPaletteRole): Long = when (role) {
        AppPaletteRole.PRIMARY -> primaryArgb
        AppPaletteRole.SECONDARY -> secondaryArgb
        AppPaletteRole.LIGHT_BACKGROUND -> lightBackgroundArgb
        AppPaletteRole.LIGHT_SURFACE -> lightSurfaceArgb
        AppPaletteRole.LIGHT_ELEVATED -> lightElevatedArgb
        AppPaletteRole.DARK_BACKGROUND -> darkBackgroundArgb
        AppPaletteRole.DARK_SURFACE -> darkSurfaceArgb
        AppPaletteRole.DARK_ELEVATED -> darkElevatedArgb
    }

    fun withColor(role: AppPaletteRole, argb: Long): CustomAppPalette {
        val opaque = 0xFF000000L or (argb and 0x00FFFFFFL)
        return when (role) {
            AppPaletteRole.PRIMARY -> copy(primaryArgb = opaque)
            AppPaletteRole.SECONDARY -> copy(secondaryArgb = opaque)
            AppPaletteRole.LIGHT_BACKGROUND -> copy(lightBackgroundArgb = opaque)
            AppPaletteRole.LIGHT_SURFACE -> copy(lightSurfaceArgb = opaque)
            AppPaletteRole.LIGHT_ELEVATED -> copy(lightElevatedArgb = opaque)
            AppPaletteRole.DARK_BACKGROUND -> copy(darkBackgroundArgb = opaque)
            AppPaletteRole.DARK_SURFACE -> copy(darkSurfaceArgb = opaque)
            AppPaletteRole.DARK_ELEVATED -> copy(darkElevatedArgb = opaque)
        }
    }

    fun encode(): String = AppPaletteRole.entries.joinToString(",") { role ->
        color(role).toString(16).uppercase().padStart(8, '0')
    }

    companion object {
        fun from(theme: FoxyColorTheme): CustomAppPalette = CustomAppPalette(
            primaryArgb = theme.primaryLightHex,
            secondaryArgb = theme.secondaryAccentHex,
            lightBackgroundArgb = theme.lightBgHex,
            lightSurfaceArgb = theme.lightSurfaceHex,
            lightElevatedArgb = theme.lightElevatedHex,
            darkBackgroundArgb = theme.darkBgHex,
            darkSurfaceArgb = theme.darkSurfaceHex,
            darkElevatedArgb = theme.darkElevatedHex,
        )

        fun decode(value: String?): CustomAppPalette? {
            if (value.isNullOrBlank()) return null
            val parts = value.split(',')
            if (parts.size != AppPaletteRole.entries.size) return null
            val colors = parts.map { part ->
                val normalized = part.trim().removePrefix("#")
                if (normalized.length != 8 || normalized.any { it.digitToIntOrNull(16) == null }) return null
                val parsed = normalized.toLongOrNull(16) ?: return null
                if ((parsed ushr 24) != 0xFFL) return null
                parsed
            }
            return CustomAppPalette(
                primaryArgb = colors[0],
                secondaryArgb = colors[1],
                lightBackgroundArgb = colors[2],
                lightSurfaceArgb = colors[3],
                lightElevatedArgb = colors[4],
                darkBackgroundArgb = colors[5],
                darkSurfaceArgb = colors[6],
                darkElevatedArgb = colors[7],
            )
        }
    }
}

enum class AppPaletteRole {
    PRIMARY,
    SECONDARY,
    LIGHT_BACKGROUND,
    LIGHT_SURFACE,
    LIGHT_ELEVATED,
    DARK_BACKGROUND,
    DARK_SURFACE,
    DARK_ELEVATED,
}
