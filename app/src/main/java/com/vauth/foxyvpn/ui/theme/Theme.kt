package com.vauth.foxyvpn.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.vauth.foxyvpn.data.CustomAppPalette
import com.vauth.foxyvpn.data.FoxyColorTheme
import com.vauth.foxyvpn.data.FoxyUiDesignStyle
import kotlin.math.pow

private fun channelToLinear(channel: Float): Double {
    val value = channel.toDouble()
    return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
}

private fun relativeLuminance(color: Color): Double =
    0.2126 * channelToLinear(color.red) +
        0.7152 * channelToLinear(color.green) +
        0.0722 * channelToLinear(color.blue)

private fun contrastRatio(first: Color, second: Color): Double {
    val firstLuminance = relativeLuminance(first)
    val secondLuminance = relativeLuminance(second)
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + 0.05) / (darker + 0.05)
}

private fun readableAccent(input: Color, background: Color): Color {
    if (contrastRatio(input, background) >= 3.0) return input
    val target = if (relativeLuminance(background) > 0.5) Color.Black else Color.White
    for (step in 1..20) {
        val amount = step / 20f
        val adjusted = Color(
            red = input.red + (target.red - input.red) * amount,
            green = input.green + (target.green - input.green) * amount,
            blue = input.blue + (target.blue - input.blue) * amount,
        )
        if (contrastRatio(adjusted, background) >= 3.0) return adjusted
    }
    return target
}

private fun readableForeground(background: Color): Color =
    if (contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)) Color.Black else Color.White

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    inversePrimary = md_theme_light_inversePrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    surfaceTint = md_theme_light_surfaceTint,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    inversePrimary = md_theme_dark_inversePrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    surfaceTint = md_theme_dark_surfaceTint,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    scrim = md_theme_dark_scrim,
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun FoxyVpnTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appLanguage: AppLanguage = AppLanguage.EN,
    colorTheme: FoxyColorTheme = FoxyColorTheme.CYBER_FOX,
    customAccentArgb: Long? = null,
    customPalette: CustomAppPalette? = null,
    uiDesignStyle: FoxyUiDesignStyle = FoxyUiDesignStyle.PIXEL_GOOGLE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val statusColors = statusColorsForTheme(darkTheme, colorTheme)
    val pixelBackground = if (darkTheme) Color(0xFF141218) else Color(0xFFFFF7FF)
    val pixelSurface = if (darkTheme) Color(0xFF211F26) else Color(0xFFFFF7FF)
    val pixelElevated = if (darkTheme) Color(0xFF2B2930) else Color(0xFFF3EDF7)
    val pixelPrimary = if (darkTheme) Color(0xFFD0BCFF) else Color(0xFF6750A4)
    val surfBackground = if (darkTheme) Color(0xFF16253B) else Color(0xFF5B72EA)
    val surfSurface = if (darkTheme) Color(0xFF24344D) else Color(0xFFF9FBFF)
    val surfElevated = if (darkTheme) Color(0xFF304560) else Color(0xFFEAF0FF)
    val surfPrimary = if (darkTheme) Color(0xFF7DD3FC) else Color(0xFF5368E8)
    val targetBg = customPalette?.let { Color(if (darkTheme) it.darkBackgroundArgb else it.lightBackgroundArgb) }
        ?: when {
            uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF -> surfBackground
            uiDesignStyle == FoxyUiDesignStyle.PIXEL_GOOGLE -> pixelBackground
            else -> Color(if (darkTheme) colorTheme.darkBgHex else colorTheme.lightBgHex)
        }
    val rawPrimary = readableAccent(
        customPalette?.primaryArgb?.let(::Color)
            ?: customAccentArgb?.let(::Color)
            ?: when {
                uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF -> surfPrimary
                uiDesignStyle == FoxyUiDesignStyle.PIXEL_GOOGLE -> pixelPrimary
                else -> Color(if (darkTheme) colorTheme.primaryDarkHex else colorTheme.primaryLightHex)
            },
        targetBg,
    )
    val targetSecondary = readableAccent(
        customPalette?.secondaryArgb?.let(::Color) ?: Color(colorTheme.secondaryAccentHex),
        targetBg,
    )
    val targetSurface = customPalette?.let { Color(if (darkTheme) it.darkSurfaceArgb else it.lightSurfaceArgb) }
        ?: when {
            uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF -> surfSurface
            uiDesignStyle == FoxyUiDesignStyle.PIXEL_GOOGLE -> pixelSurface
            else -> Color(if (darkTheme) colorTheme.darkSurfaceHex else colorTheme.lightSurfaceHex)
        }
    val targetElevated = customPalette?.let { Color(if (darkTheme) it.darkElevatedArgb else it.lightElevatedArgb) }
        ?: when {
            uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF -> surfElevated
            uiDesignStyle == FoxyUiDesignStyle.PIXEL_GOOGLE -> pixelElevated
            else -> Color(if (darkTheme) colorTheme.darkElevatedHex else colorTheme.lightElevatedHex)
        }

    val animatedPrimary by animateColorAsState(
        targetValue = rawPrimary,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themePrimary",
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themeSecondary",
    )
    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themeBg",
    )
    val animatedSurface by animateColorAsState(
        targetValue = targetSurface,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themeSurface",
    )
    val animatedElevated by animateColorAsState(
        targetValue = targetElevated,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themeElevated",
    )
    val animatedContainer by animateColorAsState(
        targetValue = if (darkTheme) {
            rawPrimary.copy(alpha = 0.22f)
        } else {
            rawPrimary.copy(alpha = 0.14f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "themeContainer",
    )

    val baseScheme = if (darkTheme) DarkColors else LightColors

    val colorScheme = baseScheme.copy(
        primary = animatedPrimary,
        onPrimary = readableForeground(animatedPrimary),
        primaryContainer = animatedContainer,
        onPrimaryContainer = readableForeground(animatedContainer),
        secondary = animatedSecondary,
        onSecondary = readableForeground(animatedSecondary),
        background = animatedBg,
        onBackground = readableForeground(animatedBg),
        surface = animatedSurface,
        onSurface = readableForeground(animatedSurface),
        surfaceContainer = animatedSurface,
        surfaceContainerHigh = animatedElevated,
        surfaceVariant = animatedElevated,
        onSurfaceVariant = readableForeground(animatedElevated).copy(alpha = 0.78f),
        surfaceTint = Color.Transparent,
    )

    val surfText = if (darkTheme) Color(0xFFF4F8FF) else Color(0xFF17315A)
    val surfSecondaryText = surfText.copy(alpha = 0.72f)
    val neuColors = if (darkTheme) {
        NeumorphicColors(
            background = animatedBg,
            surface = animatedSurface,
            surfaceElevated = animatedElevated,
            shadowLight = NeuDarkShadowLight,
            shadowDark = NeuDarkShadowDark,
            accent = animatedPrimary,
            textPrimary = if (uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF) surfText else readableForeground(animatedBg),
            textSecondary = if (uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF) surfSecondaryText else readableForeground(animatedBg).copy(alpha = 0.72f),
            isDark = true,
            designStyle = uiDesignStyle,
        )
    } else {
        NeumorphicColors(
            background = animatedBg,
            surface = animatedSurface,
            surfaceElevated = animatedElevated,
            shadowLight = NeuLightShadowLight,
            shadowDark = NeuLightShadowDark,
            accent = animatedPrimary,
            textPrimary = if (uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF) surfText else readableForeground(animatedBg),
            textSecondary = if (uiDesignStyle == FoxyUiDesignStyle.SAFETY_SURF) surfSecondaryText else readableForeground(animatedBg).copy(alpha = 0.72f),
            isDark = false,
            designStyle = uiDesignStyle,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val strings = foxyStringsFor(appLanguage)
    val fontHierarchy = fontHierarchyFor(appLanguage)
    val typography = foxyTypographyFor(appLanguage)
    val layoutDirection = if (appLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalFoxyStatusColors provides statusColors,
        LocalNeumorphicColors provides neuColors,
        LocalFoxyStrings provides strings,
        LocalFoxyFonts provides fontHierarchy,
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = typography,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
