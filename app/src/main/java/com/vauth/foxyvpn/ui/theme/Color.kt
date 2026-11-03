package com.vauth.foxyvpn.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.vauth.foxyvpn.data.FoxyColorTheme

// Primary Blue Seed for On/Off Controls & Interactive Buttons
val FoxSeed = Color(0xFF2563EB)

// Light Mode: Inspired by ultra-clean minimal alabaster & pure white cards (#F7F7F9 bg, #FFFFFF cards, #F0F0F3 inner containers)
val NeuLightBg = Color(0xFFF2F5FB)
val NeuLightSurface = Color(0xFFFFFFFF)
val NeuLightSurfaceElevated = Color(0xFFE9EEF7)
val NeuLightShadowLight = Color(0xFFFFFFFF)
val NeuLightShadowDark = Color(0xFFD6DEEA)

val md_theme_light_primary = Color(0xFF2563EB)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDDE8FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001D4A)
val md_theme_light_inversePrimary = Color(0xFF60A5FA)

val md_theme_light_secondary = Color(0xFF0F83D9)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFDDF2FF)
val md_theme_light_onSecondaryContainer = Color(0xFF00224D)

val md_theme_light_tertiary = Color(0xFF059669)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFD1FAE5)
val md_theme_light_onTertiaryContainer = Color(0xFF064E3B)

val md_theme_light_error = Color(0xFFDC2626)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFEE2E2)
val md_theme_light_onErrorContainer = Color(0xFF450A0A)

val md_theme_light_background = NeuLightBg
val md_theme_light_onBackground = Color(0xFF111114)
val md_theme_light_surface = NeuLightSurface
val md_theme_light_onSurface = Color(0xFF111114)
val md_theme_light_surfaceVariant = Color(0xFFE5EAF2)
val md_theme_light_onSurfaceVariant = Color(0xFF5E6B80)
val md_theme_light_surfaceTint = md_theme_light_primary

val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFFAFAFC)
val md_theme_light_surfaceContainer = NeuLightSurface
val md_theme_light_surfaceContainerHigh = Color(0xFFF0F0F4)
val md_theme_light_surfaceContainerHighest = Color(0xFFE6E6EC)
val md_theme_light_surfaceBright = Color(0xFFFFFFFF)
val md_theme_light_surfaceDim = Color(0xFFEBEBF0)

val md_theme_light_outline = Color(0xFF8E8E98)
val md_theme_light_outlineVariant = Color(0xFFE4E4EA)
val md_theme_light_inverseSurface = Color(0xFF141417)
val md_theme_light_inverseOnSurface = Color(0xFFF8F8FA)
val md_theme_light_scrim = Color(0xFF000000)

// Dark Mode: Inspired by deep matte obsidian black (#050507 bg, #141417 matte charcoal cards, #1F1F24 inner containers)
val NeuDarkBg = Color(0xFF0A1020)
val NeuDarkSurface = Color(0xFF111827)
val NeuDarkSurfaceElevated = Color(0xFF1B263A)
val NeuDarkShadowLight = Color(0xFF273650)
val NeuDarkShadowDark = Color(0xFF060B14)

val md_theme_dark_primary = Color(0xFF60A5FA)
val md_theme_dark_onPrimary = Color(0xFFFFFFFF)
val md_theme_dark_primaryContainer = Color(0xFF153B70)
val md_theme_dark_onPrimaryContainer = Color(0xFFDCEBFF)
val md_theme_dark_inversePrimary = Color(0xFF0066FF)

val md_theme_dark_secondary = Color(0xFF38BDF8)
val md_theme_dark_onSecondary = Color(0xFF001E42)
val md_theme_dark_secondaryContainer = Color(0xFF123B57)
val md_theme_dark_onSecondaryContainer = Color(0xFFDCEBFF)

val md_theme_dark_tertiary = Color(0xFF10B981)
val md_theme_dark_onTertiary = Color(0xFF002C1E)
val md_theme_dark_tertiaryContainer = Color(0xFF064E3B)
val md_theme_dark_onTertiaryContainer = Color(0xFFD1FAE5)

val md_theme_dark_error = Color(0xFFF43F5E)
val md_theme_dark_onError = Color(0xFF4C0519)
val md_theme_dark_errorContainer = Color(0xFF881337)
val md_theme_dark_onErrorContainer = Color(0xFFFFE4E6)

val md_theme_dark_background = NeuDarkBg
val md_theme_dark_onBackground = Color(0xFFFFFFFF)
val md_theme_dark_surface = NeuDarkSurface
val md_theme_dark_onSurface = Color(0xFFFFFFFF)
val md_theme_dark_surfaceVariant = Color(0xFF1B263A)
val md_theme_dark_onSurfaceVariant = Color(0xFF9AAAC1)
val md_theme_dark_surfaceTint = md_theme_dark_primary

val md_theme_dark_surfaceContainerLowest = Color(0xFF070C17)
val md_theme_dark_surfaceContainerLow = Color(0xFF0D1422)
val md_theme_dark_surfaceContainer = NeuDarkSurface
val md_theme_dark_surfaceContainerHigh = Color(0xFF1B263A)
val md_theme_dark_surfaceContainerHighest = Color(0xFF25334A)
val md_theme_dark_surfaceBright = Color(0xFF2B3B54)
val md_theme_dark_surfaceDim = Color(0xFF080D18)

val md_theme_dark_outline = Color(0xFF53647D)
val md_theme_dark_outlineVariant = Color(0xFF2A3950)
val md_theme_dark_inverseSurface = Color(0xFFFFFFFF)
val md_theme_dark_inverseOnSurface = Color(0xFF111114)
val md_theme_dark_scrim = Color(0xFF000000)

data class FoxyStatusColors(
    val connected: Color,
    val connecting: Color,
    val disconnected: Color,
)

// Successful connection is always Emerald Green; Connecting / On-Off controls are Blue
val LightStatusColors = FoxyStatusColors(
    connected = Color(0xFF10B981),
    connecting = Color(0xFF0066FF),
    disconnected = Color(0xFF71717A),
)

val DarkStatusColors = FoxyStatusColors(
    connected = Color(0xFF10B981),
    connecting = Color(0xFF1E88FF),
    disconnected = Color(0xFF8E8E98),
)

fun statusColorsForTheme(darkTheme: Boolean, colorTheme: FoxyColorTheme): FoxyStatusColors {
    return if (darkTheme) {
        DarkStatusColors.copy(
            connected = Color(0xFF10B981),
            connecting = Color(0xFF1E88FF),
        )
    } else {
        LightStatusColors.copy(
            connected = Color(0xFF10B981),
            connecting = Color(0xFF0066FF),
        )
    }
}

val LocalFoxyStatusColors = staticCompositionLocalOf { DarkStatusColors }
