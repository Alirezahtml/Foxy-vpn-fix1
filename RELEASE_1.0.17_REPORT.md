# FoxyVPN 1.0.17 — Crash Hardening and Release Report

## Changes

The day/night control now uses standard vector resources: `drawable/ic_theme_mode.xml` renders a sun in day mode and `drawable-night/ic_theme_mode.xml` renders a moon in night mode. The existing Compose control is connected to the resource, so the Android resource qualifier chooses the correct icon.

The Core B runtime remains generation-safe and isolated from Core A. Startup-sensitive overlay and VPN code remains guarded with `runCatching` at platform boundaries. The app version was advanced to versionCode 54 / versionName 1.0.17.

## Build evidence

Debug passed lint, unit tests, and assembleDebug. Release passed lintVital, R8 minification, native HEV builds for arm64-v8a, armeabi-v7a, x86, and x86_64, release packaging, and APK signing. Both APKs passed `aapt dump badging`, ZIP integrity, and `apksigner verify`.

## Artifacts

- Debug APK: `FoxyVPN-1.0.17-debug.apk`
- Signed release APK: `FoxyVPN-1.0.17-release.apk`
- Updated source: `FoxyVPN-1.0.17-source.zip`
- Temporary signing keystore: `FoxyVPN-release-keystore.jks`

## Important signing note

The release APK is signed with a newly generated temporary keystore because no owner production keystore was supplied. Keep this keystore private and backed up if the APK must receive future in-place updates. Before public distribution, replace it with the project owner's permanent signing key.

The screenshot alone does not include an Android crash stack trace, so the exact device-specific exception cannot be proven from the image. These artifacts are build/lint/test verified; final Xiaomi runtime confirmation still requires installing this exact 1.0.17 APK and collecting `adb logcat` if it crashes again.
