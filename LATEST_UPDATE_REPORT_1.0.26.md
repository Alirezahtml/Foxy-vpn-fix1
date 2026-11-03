# FoxyVPN 1.0.26 — Final Audit and Guide Update

## Changes

- Updated the in-app Networking Core Guide in Persian, Arabic, and English.
- The guide now explains that Core A and Core B are independent paths.
- Core A is documented as HTTP/2 over TLS/TCP, with HTTP/3/QUIC described honestly as experimental and falling back to HTTP/2 when H3 cannot connect.
- Core B is documented as the independent H2/HEV path over TLS/TCP and HTTP/2, requiring a compatible server and native HEV runtime.
- HTTP/1.1 is explicitly explained as disabled for outbound tunnel transport. HTTP/1.1 used by the local proxy is documented as an internal proxy protocol, not an outbound tunnel protocol.
- The guide now explains that changing the core enables only protocols eligible for that core, and that Auto considers the selected core and server compatibility.
- Rechecked user-facing UI text for obsolete protocol wording and old branding references; no user-facing matches were found in the scanned UI source.
- Preserved the two-skin UI architecture from 1.0.25: Current Design and Pixel UI only.

## Validation

- `:app:testDebugUnitTest` passed.
- `:app:lintDebug` passed.
- Debug APK build passed.
- Release APK build passed.
- Debug and Release APK signatures verified with `apksigner`.
- Package: `com.vauth.foxyvpn.neu`
- Version: `1.0.26`
- Version code: `63`

## APK hashes

- Debug APK: `763733bbc62bdf7580252bff2e6d0baf0f6cdef07143b46b7a67b0aa13980c91`
- Release APK: `4acda149fdd31ef3646d83ba5c4e1a1a24bd7fded45cae9b9c1b46be6c06f344`
