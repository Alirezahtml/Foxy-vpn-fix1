package com.vauth.foxyvpn.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.vauth.foxyvpn.data.CustomAppPalette
import com.vauth.foxyvpn.data.FoxyColorTheme
import com.vauth.foxyvpn.data.FoxyUiDesignStyle
import com.vauth.foxyvpn.data.HeroWaveAnimationStyle
import com.vauth.foxyvpn.data.SettingsStore

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

class ThemeController(private val settingsStore: SettingsStore) {

    var mode: ThemeMode by mutableStateOf(settingsStore.themeMode)
        private set

    var colorTheme: FoxyColorTheme by mutableStateOf(settingsStore.appColorTheme)
        private set

    var customAccentArgb: Long? by mutableStateOf(settingsStore.appCustomAccentArgb)
        private set

    var customPalette: CustomAppPalette? by mutableStateOf(settingsStore.appCustomPalette)
        private set

    var uiDesignStyle: FoxyUiDesignStyle by mutableStateOf(settingsStore.appUiDesignStyle)
        private set

    var heroWaveStyle: HeroWaveAnimationStyle by mutableStateOf(settingsStore.heroWaveAnimationStyle)
        private set

    fun resolveDark(systemInDarkTheme: Boolean): Boolean = when (mode) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    fun set(next: ThemeMode) {
        if (next == mode) return
        mode = next
        settingsStore.themeMode = next
    }

    fun toggle(systemInDarkTheme: Boolean) {
        set(if (resolveDark(systemInDarkTheme)) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    fun applyColorTheme(next: FoxyColorTheme) {
        val hadCustomColors = customPalette != null || customAccentArgb != null
        if (next == colorTheme && !hadCustomColors) return
        colorTheme = next
        settingsStore.appColorTheme = next
        if (hadCustomColors) resetCustomPalette()
    }

    fun applyCustomAccentArgb(argb: Long) {
        val opaqueArgb = 0xFF000000L or (argb and 0x00FFFFFFL)
        val updatedPalette = customPalette?.copy(primaryArgb = opaqueArgb)
        if (opaqueArgb == customAccentArgb && updatedPalette == customPalette) return
        customAccentArgb = opaqueArgb
        settingsStore.appCustomAccentArgb = opaqueArgb
        if (updatedPalette != customPalette) {
            customPalette = updatedPalette
            settingsStore.appCustomPalette = updatedPalette
        }
    }

    fun resetCustomAccentArgb() {
        val resetPalette = customPalette?.copy(primaryArgb = colorTheme.primaryLightHex)
        if (customAccentArgb == null && resetPalette == customPalette) return
        customAccentArgb = null
        settingsStore.appCustomAccentArgb = null
        if (resetPalette != customPalette) {
            customPalette = resetPalette
            settingsStore.appCustomPalette = resetPalette
        }
    }

    fun applyCustomPalette(next: CustomAppPalette) {
        customPalette = next
        settingsStore.appCustomPalette = next
        customAccentArgb = next.primaryArgb
        settingsStore.appCustomAccentArgb = next.primaryArgb
    }
    fun resetCustomPalette() {
        customPalette = null
        settingsStore.appCustomPalette = null
        customAccentArgb = null
        settingsStore.appCustomAccentArgb = null
    }

    fun cycleColorTheme() {
        val entries = FoxyColorTheme.entries
        val nextIdx = (entries.indexOf(colorTheme) + 1) % entries.size
        applyColorTheme(entries[nextIdx])
    }

    fun applyUiDesignStyle(next: FoxyUiDesignStyle) {
        if (next !in FoxyUiDesignStyle.userSelectableEntries) return
        if (next == uiDesignStyle) return
        uiDesignStyle = next
        settingsStore.appUiDesignStyle = next
        if (next == FoxyUiDesignStyle.DARK_LUXURY && mode != ThemeMode.DARK) {
            set(ThemeMode.DARK)
        }
    }

    fun selectUiDesignStyle(next: FoxyUiDesignStyle) = applyUiDesignStyle(next)

    fun cycleUiDesignStyle() {
        val entries = FoxyUiDesignStyle.userSelectableEntries
        val nextIdx = (entries.indexOf(uiDesignStyle) + 1) % entries.size
        applyUiDesignStyle(entries[nextIdx])
    }

    fun applyHeroWaveStyle(next: HeroWaveAnimationStyle) {
        if (next == heroWaveStyle) return
        heroWaveStyle = next
        settingsStore.heroWaveAnimationStyle = next
    }

    fun cycleHeroWaveStyle() {
        val entries = HeroWaveAnimationStyle.entries
        val nextIdx = (entries.indexOf(heroWaveStyle) + 1) % entries.size
        applyHeroWaveStyle(entries[nextIdx])
    }
}

@Composable
fun rememberThemeController(settingsStore: SettingsStore): ThemeController =
    remember(settingsStore) { ThemeController(settingsStore) }
