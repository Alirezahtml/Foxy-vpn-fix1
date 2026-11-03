# FoxyVPN 1.0.19 — UI Icons and Motion Update

## New vector icons

The supplied SVG for real latency testing was converted to `app/src/main/res/drawable/ic_real_ping.xml` and connected to the real server-list test button. The icon uses the supplied gauge/needle geometry and 24dp viewport.

The supplied SVG for Telegram SOCKS5 proxy was converted to `app/src/main/res/drawable/ic_telegram_proxy.xml` and connected to the Telegram proxy row in Settings → Smart Network & Security Toolkit. It contains the two server panels, green status indicators, security shield, and check mark from the supplied design.

## Motion updates

The real ping result badge now uses a short fade transition when its measured value or state changes. The full-test control has lightweight progress feedback and a short rotation transition. Existing touch-bounce and page transitions remain enabled without adding heavy continuous effects to the location list.

## Validation

The following command completed successfully:

`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease`

Version: 1.0.19. Version code: 56. R8/minification completed for Release. The Debug and Release APKs were created after the final source changes.

This build does not replace physical-device validation. Xiaomi/MIUI VPN permission, background WorkManager behavior, VPN reconnect, DNS/IPv6 leak tests, and accessibility review still require an actual test device before public production release.
