package com.vauth.foxyvpn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.R

// ============================================================================
// 3-TIER FONT HIERARCHY SYSTEM (EN / FA / AR)
// Tier 1: Headings, Brand & Hero Numbers
// Tier 2: Body, Menus & Primary Controls
// Tier 3: Labels, Captions, Badges & Technical / IP Data
// ============================================================================

private val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
)

private val InterFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
)

private val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Light),
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold),
)

private val VazirmatnHeadingFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Bold),
    Font(R.font.vazirmatn, FontWeight.ExtraBold),
    Font(R.font.vazirmatn, FontWeight.Black),
)

private val VazirmatnBodyFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn, FontWeight.Medium),
    Font(R.font.vazirmatn, FontWeight.SemiBold),
)

private val VazirmatnLabelFamily = FontFamily(
    Font(R.font.readex_pro, FontWeight.Light),
    Font(R.font.vazirmatn, FontWeight.Light),
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn, FontWeight.Medium),
)

private val CairoHeadingFamily = FontFamily(
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold),
    Font(R.font.cairo, FontWeight.ExtraBold),
)

private val ReadexProBodyFamily = FontFamily(
    Font(R.font.readex_pro, FontWeight.Normal),
    Font(R.font.readex_pro, FontWeight.Medium),
    Font(R.font.readex_pro, FontWeight.SemiBold),
)

private val AlmaraiLabelFamily = FontFamily(
    Font(R.font.almarai, FontWeight.Light),
    Font(R.font.almarai, FontWeight.Normal),
    Font(R.font.almarai, FontWeight.Bold),
)

val AmsterdamSignatureFontFamily = FontFamily(
    Font(R.font.alex_brush, FontWeight.Normal),
)

data class FoxyFontHierarchy(
    val headingFont: FontFamily,
    val bodyFont: FontFamily,
    val labelFont: FontFamily,
    val monoFont: FontFamily,
    val brandSignatureFont: FontFamily = AmsterdamSignatureFontFamily,
)

val EnglishFontHierarchy = FoxyFontHierarchy(
    headingFont = PlusJakartaSansFamily,
    bodyFont = InterFamily,
    labelFont = JetBrainsMonoFamily,
    monoFont = JetBrainsMonoFamily,
    brandSignatureFont = AmsterdamSignatureFontFamily,
)

val PersianFontHierarchy = FoxyFontHierarchy(
    headingFont = VazirmatnHeadingFamily,
    bodyFont = VazirmatnBodyFamily,
    labelFont = VazirmatnLabelFamily,
    monoFont = VazirmatnBodyFamily,
    brandSignatureFont = AmsterdamSignatureFontFamily,
)

val ArabicFontHierarchy = FoxyFontHierarchy(
    headingFont = CairoHeadingFamily,
    bodyFont = ReadexProBodyFamily,
    labelFont = AlmaraiLabelFamily,
    monoFont = ReadexProBodyFamily,
    brandSignatureFont = AmsterdamSignatureFontFamily,
)

fun fontHierarchyFor(language: AppLanguage): FoxyFontHierarchy = when (language) {
    AppLanguage.EN -> EnglishFontHierarchy
    AppLanguage.FA -> PersianFontHierarchy
    AppLanguage.AR -> ArabicFontHierarchy
}

val LocalFoxyFonts = staticCompositionLocalOf { EnglishFontHierarchy }

val currentFoxyFonts: FoxyFontHierarchy
    @Composable
    @ReadOnlyComposable
    get() = LocalFoxyFonts.current

fun foxyTypographyFor(language: AppLanguage): Typography {
    val h = fontHierarchyFor(language)
    return Typography(
        // Tier 1: Display & Headlines (Plus Jakarta Sans / Vazirmatn Bold-Black / Cairo Bold)
        displayLarge = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 54.sp,
            lineHeight = 62.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            lineHeight = 50.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 42.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 30.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = h.headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            lineHeight = 28.sp,
        ),
        // Tier 2: Body & Menus (Inter Medium-Regular / Vazirmatn Medium / Readex Pro Medium)
        titleMedium = TextStyle(
            fontFamily = h.bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = h.bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = h.bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = h.bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        // Tier 3: Labels, Secondary & Technical Data (JetBrains Mono / Vazirmatn-Readex Light / Almarai Light)
        bodySmall = TextStyle(
            fontFamily = h.labelFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = h.labelFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = h.labelFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = h.labelFont,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 15.sp,
        ),
    )
}

val FoxyTypography = foxyTypographyFor(AppLanguage.EN)
