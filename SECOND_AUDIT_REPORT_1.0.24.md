# FoxyVPN 1.0.24 — Second Audit

A second, stricter audit was completed after the first bug-fix pass. The audit covered compilation, unit tests, lint, empty click handlers, backup/restore input handling, protocol/core settings, Dynamic Island persistence, and user-visible branding.

The audit found and fixed remaining visible references to the old core name in Core A connection/error messages, the Core A bridge startup error, the ping verification documentation, and the internal fallback account address. Technical implementation identifiers, service URLs, package/API identifiers, and bridge class names were intentionally preserved because changing them would break authentication or the native core integration; they are not shown as user-facing labels.

Informational rows with intentionally empty click handlers remain non-actionable by design: the version row, ABI/build-information row, and sponsor-policy status row display status only and are not selectable settings.

Validation passed: unit tests, lint, Debug APK build, Release APK build with R8, and APK signature verification. Package is `com.vauth.foxyvpn.neu`, version `1.0.24`, versionCode `61`.

## Hashes from the rebuilt APKs

- Debug APK: `7b43501d3a2a6425949f00cb8638048275ff4e6965c81a279bd493b6f77b04a3`
- Release APK: `92fc502c524b7f56e57b6a48771c395d1c1d5c204be82b6eabcf30b23d284cf1`
