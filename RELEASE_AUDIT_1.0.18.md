# FoxyVPN 1.0.18 — Server Refresh and Release Audit

## Implemented

The app now refreshes the official Mozilla VPN server collection manually and automatically. The manual control is available in Settings → Smart Network & Security Toolkit as “Update now”. Automatic refresh uses WorkManager every 12 hours when network connectivity is available, with exponential retry. The user can disable automatic refresh.

The source used by the app is Mozilla Remote Settings:
`https://firefox.settings.services.mozilla.com/v1/buckets/main/collections/vpn-serverlist/records`

The refresh is strict: HTTP errors, empty responses, malformed records, or a response that produces no valid countries are rejected. A previously valid cache is not overwritten. The current endpoint was reachable during the audit and returned an object with a `data` array containing 35 country records. The existing parser matches the current record shape: country records contain `code`, `name`, `cities`, and city server entries contain `hostname` and `port`.

Mozilla’s official server article states that Mozilla VPN uses Mullvad infrastructure and links to Mullvad’s server list. The app therefore consumes the Mozilla-maintained Remote Settings collection rather than scraping an HTML page or silently inventing locations. [1] [2] [3]

## Verification completed

The following command passed:

`./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease`

The resulting application is versionCode 55 / versionName 1.0.18. Debug and Release APKs passed APK signature verification. R8/minification and native HEV builds completed in the release variant.

## Remaining release gates

The build is technically ready for a controlled beta or private distribution, but it is not honest to call it fully production-certified yet. A real Android-device test is still required for Xiaomi/MIUI startup, VPN permission, WorkManager execution after reboot, background restrictions, network handover, DNS/IPv6 leak checks, Core A/Core B data-plane traffic, and server-list replacement while a previously selected server disappears.

The release APK is signed with the temporary keystore already supplied with this workspace, not the owner’s permanent production key. Public Play Store or long-term OTA distribution should use the owner keystore and verified signing identity. HTTP/1.1 remains explicitly unsupported, and Firefox Core HTTP/3/QUIC remains marked unverified in the capability registry. These are known limitations, not hidden claims of support.

No fabricated “server added” record is used. New valid records returned by the official Mozilla collection are accepted into the cache on the next successful manual/automatic refresh and appear the next time the location list loads.

## Findings

- Critical build blockers: none found in the sandbox build.
- Server refresh: implemented with official endpoint, cache protection, manual trigger, and background scheduling.
- Security concern: release signing key must be replaced with the owner’s permanent key before public distribution.
- Runtime concern: device and MIUI validation remains outstanding.
- Protocol concern: HTTP/1.1 unsupported; H3/QUIC unverified and must not be advertised as guaranteed.

## References

[1]: https://www.mozilla.org/en-US/products/vpn/resource-center/vpn-servers-around-the-world/ "Mozilla VPN Servers Around the World"
[2]: https://github.com/mozilla-mobile/mozilla-vpn-client/wiki/Command-line-interface "Mozilla VPN client server list documentation"
[3]: https://api.mullvad.net/www/relays/all/ "Mullvad machine-readable relay list"
