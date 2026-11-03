# FoxyVPN 1.0.21 — Customization, Server Actions, Backup & Restore

## Dynamic Island customization

The crowded Motion and Capsule Surface preset groups were replaced with compact dropdown selectors. Each selector displays a clear title, the current selected value, and a menu containing all localized choices. This reduces visual noise while preserving every existing option and its real SettingsStore binding.

The selector pattern is reusable for additional crowded preset groups and keeps long Persian/Arabic/English labels readable instead of placing many pills side by side.

## Server actions

The server list now has separate actions:

- Tune: toggles sorting mode.
- Refresh: refreshes the official server list and reloads the cached list.
- Real Ping: measures actual latency and sorts the results after completion.

The refresh and ping actions no longer share or invert each other’s behavior.

## Backup and restore

A Backup & Restore panel was added to Accounts. On a clean install with no saved account, it is presented as Restore accounts and settings so the user can immediately recover a previous setup.

The backup file contains a schema version, settings preferences, and saved account records. Restore merges account records and restores primitive settings. Existing encrypted credential storage remains protected locally; an exported backup necessarily contains account access/refresh tokens so it must be treated as sensitive and never shared publicly.

## Validation

Passed:

`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease`

Version: 1.0.21. Version code: 58. Release R8/minification completed. APK signature verification completed before packaging.

Physical-device checks still recommended: restore on a fresh install, expired-token behavior, Xiaomi/MIUI file picker behavior, official server refresh under offline conditions, and large-font/RTL layout.
