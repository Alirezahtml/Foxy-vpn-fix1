# Source Audit

Date: 2026-11-01

## Supplied inputs

- `FoxyVPN-source-1.0.16.zip`: SHA-256 `c609e77b995ff4753db1c05763495139cf2e11e268636dafc452d5fbfb7dd167`.
- `FoxyVPN-1.0.8.zip`: SHA-256 `2c22052d338d29d13d7e6b04841a741192d30ed74eef338a40dc37a3ac5afc86`.
- Immutable extracted copies are under `originals/`; integration work is under `workspace/`.

## Findings

Version 1.0.16 is the selected application base. Its networking implementation is the Firefox Go core packaged as `app/libs/firefox-core.aar`, reached through `FirefoxCoreBridge` and `mobile.Mobile`. `FoxyVpnService` creates the Android TUN, starts the Go core, passes the descriptor to `Mobile.startTun`, and uses `ControlPlaneHttp` socket protection.

Version 1.0.8 contains a different implementation: Netty HTTP/2 upstream (`H2UpstreamSession`), local SOCKS5/HTTP proxy servers, and the HEV SOCKS5 tunnel JNI/native runtime (`HevSocks5Tunnel`). It does not contain the Firefox AAR. Its Gradle build fetches HEV 2.17.1 and uses NDK 26.1.10909125.

The two originals have 573 and 72 files respectively, with 60 same-path files. The same-path application/service files are not safe to overwrite; the integration therefore copied only Core B's unique upstream, proxy, trust, health, and HEV bridge classes into the separate integration workspace.

## Verified limitations

No Android SDK, NDK, or ADB is installed in the execution environment. The baseline Gradle wrapper downloaded Gradle 8.10.2 successfully but stopped before dependency resolution with `SDK location not found`. Real-device, native ABI, VPN permission, server, and data-plane tests are therefore blocked.

The integrated source includes a separate `CoreBAdapter` boundary and the real Core B implementation classes. Full service-level routing of Core B still requires completing the authoritative session-controller wiring and a configured Android/NDK build environment; it is not claimed as end-to-end verified.
