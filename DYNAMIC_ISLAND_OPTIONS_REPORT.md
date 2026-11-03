# FoxyVPN 1.0.22 — Dynamic Island Options Update

## New controls

The Dynamic Island customization section now includes a dedicated, clean `Precision Size & Motion` card with independent controls for:

- Horizontal size: 75%–135%
- Vertical size: 75%–135%
- Open/close speed: 70%–140%

The values are persisted in `SettingsStore`, applied to the production Dynamic Island renderer, and applied to the live in-app preview. The controls use lightweight Compose sliders and are grouped in one card so the settings page is less cluttered.

## Runtime behavior

Horizontal and vertical scale are applied independently through the graphics layer. The existing manual scale, layout preset, corner radius, and responsive spring settings continue to work. The new speed control adjusts the effective spring stiffness for open/close morphing without changing the visual dimensions.

The live preview and the actual overlay use the same values, preventing misleading previews. Existing motion, wave, halo, indicator, telemetry, quick-action, surface, and haptic customization options remain available. Core A and Core B remain independent; no core merge was introduced.

## Validation

- Unit tests: PASS
- Debug build: PASS
- Lint: PASS
- Release build with R8: PASS
- APK signature verification: PASS
- Package: `com.vauth.foxyvpn.neu`
- Version: `1.0.22` / versionCode `59`

## Final artifact hashes

- Debug APK: `f8e202db2aa7e8527a5cb8d3f777101b5203927af971b83ede8293513abfa13c`
- Release APK: `6344356869b8c1d187a49ebb863fc5c15201aa95fe5589853331abca88d4e51d`
