# FoxyVPN 1.0.25 — Two Complete UI Skins

The UI design selector now exposes exactly two application skins: `Current Design` and `Pixel UI`. Older design values stored by previous builds are safely normalized to `Current Design`, while an explicitly stored Pixel selection remains Pixel UI.

Pixel UI now uses the dedicated tonal Material palette, flat Material surfaces instead of neumorphic blur, Pixel-specific rounded cards and buttons, clean background treatment, and the same responsive layout across the app. The shared card and button primitives adapt from the design composition local, so the appearance is consistent across Settings, Home, server selection, tools, dialogs, and Dynamic Island controls.

The selector labels and descriptions were rewritten in Persian, Arabic, and English so they describe the two actual choices without listing obsolete skins. The tunnel protocol title was also corrected from the unrelated “Live Switch” wording to the precise `Tunnel connection protocol` / equivalent localized text. The Dynamic Island expand/collapse transition now uses the configured animation-speed-adjusted spring stiffness, while retaining the stable overlay canvas strategy for low-end devices.

The two networking cores remain independent. No core or protocol implementation was merged as part of this UI change.

## Validation

Unit tests, lint, Debug build, Release build with R8, and APK signature verification all passed. Package: `com.vauth.foxyvpn.neu`. Version: `1.0.25`, versionCode `62`.

## Hashes

- Debug APK: `efc21e592255002eca3ffdd389972b9519c925779f8ddd5ddc5b3497464ceb2b`
- Release APK: `39e280cbe54ae961cb2deb3f014762558e015026f8136512d5ba95c3a753596d`
