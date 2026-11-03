# FoxyVPN 1.0.23 — Pixel UI

## What changed

A complete UI design switch was added to the Appearance section. Users can now change the app design at runtime through the existing persisted `FoxyUiDesignStyle` setting.

The selector supports the existing design styles and includes the dedicated `Google Pixel UI` option. The Pixel option is a real app-wide visual mode, not a decorative preview:

- Pixel Material You-inspired tonal light and dark surfaces
- Pixel purple primary tones (`#6750A4` light / `#D0BCFF` dark)
- Rounded pill-like controls and large Material surfaces
- No heavy neumorphic blur or bevel treatment on Pixel cards
- Live switching through `ThemeController`
- Persistent selection through `SettingsStore`
- RTL/localized labels in Persian, Arabic, and English

The networking cores, protocol policy, VPN lifecycle, Dynamic Island telemetry, server management, and backup/restore logic were not merged or altered by this UI feature.

## Version and validation

- Version: `1.0.23`
- Version code: `60`
- Package: `com.vauth.foxyvpn.neu`
- Unit tests: PASS
- Lint: PASS
- Debug build: PASS
- Release build with R8: PASS
- Release APK signature verification: PASS

## Final APK hashes

- Debug APK: `ac60abde946fcf2286224cfe612d8c7cd8be5107cfb59de1d7a6f534979c21c9`
- Release APK: `27ac87fe70cf276c2ff4991cd72551e7f02aceda24861ad867f8aeddb20e30e2`
