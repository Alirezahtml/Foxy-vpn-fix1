# FoxyVPN 1.0.27 — Pixel UI Deep Redesign

## What changed

This update moves Pixel UI beyond a palette switch. The shared UI primitives used throughout Home, Settings, server selection, tools, dialogs, logs, and Dynamic Island now resolve their surface, shape, elevation, and interaction behavior from the selected design.

In Pixel UI, cards use larger expressive rounded corners and tonal Material surface containers instead of the previous neumorphic blur and bevel. Pixel cards have no decorative shadow, use a quieter container hierarchy, and compress slightly on press. Buttons use tonal Material containers, larger touch-safe padding, pill-like geometry, and a subtle press scale. Icon buttons use tonal surface containers, selected-state containers, zero decorative shadow, and a short press scale animation. The existing design keeps its previous tactile treatment.

The home screen now reduces the heavy world-map layer to a quiet atmospheric layer in Pixel UI and adds restrained tonal background shapes through the shared design atmosphere overlay. This keeps the connection hero visible without making every screen look like a poster or a neumorphic dashboard.

The implementation keeps the existing Material 3 Expressive motion scheme and the Dynamic Island's independent spring controls. Dynamic Island open/close motion continues to use the configured speed-adjusted spring, while the new Pixel surfaces and state behavior are applied to its surrounding controls.

## Design basis

The redesign follows Material You's emphasis on expression and fluidity, consistent dynamic-color usage, and motion consistency. Android's official guidance specifically calls out dynamic color, motion, consistent ripple/overscroll behavior, and adaptive edge-to-edge layouts. Material 3 Expressive identifies color, shape, size, motion, and containment as separate design dimensions rather than treating color as the whole design. The M3 navigation guidance also favors surface-based navigation without drop shadows and uses contrasting active indicators.

Sources: [1] [2] [3] [4]

## Validation

Unit tests, lint, Debug build, Release build, and APK signature verification passed. Package is `com.vauth.foxyvpn.neu`, version `1.0.27`, versionCode `64`.

## APK hashes

- Debug APK: `5ade5634890d67a06b0972fa3ea5fd82085424960f8a6f4831129cd43145eb5f`
- Release APK: `09e96fbf4c3f450cf6f2bee593c7c5c2ff171b933b4cd40db0a738915fa99d60`

## References

[1]: https://m3.material.io/ "Material Design 3 official home"
[2]: https://design.google/library/expressive-material-design-google-research "Google Design: research behind Material 3 Expressive"
[3]: https://source.android.com/docs/core/display/material "AOSP: Material You design"
[4]: https://m3.material.io/components/navigation-bar/overview "Material 3 navigation bar guidance"
