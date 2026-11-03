# FoxyVPN Integration — Updated Final Report

Date: 2026-11-01

## Completed implementation

The integration now preserves Core A and adds a real Core B runtime path inside the single authoritative `FoxyVpnService`. Core B owns an H2/Netty upstream session, local SOCKS5 relay, Android TUN interface, HEV tun2socks native runtime, protected control-plane sockets, health checks, and deterministic cleanup. Core B never calls `FirefoxCoreBridge`.

The Settings UI now exposes `Core A · Firefox`, `Core B · H2 / HEV`, and `Smart Selection`. Switching the selection persists the choice and requests a generation-safe reconnect. Smart Selection ranks only eligible candidates: it prefers Core B for a selected server under HTTP/2-compatible settings and prefers Core A when HTTP/3 is selected or Core B lacks a selected server. H3 remains explicitly unverified at the registry level.

Core B uses the existing Guardian access-token flow, selected proxy candidate, upstream proxy settings, TLS split setting, excluded applications, and MTU settings. Android socket protection is installed before the H2 session is opened to prevent VPN loops.

## Automated evidence

`./gradlew :app:testDebugUnitTest :app:assembleDebug` completed with **BUILD SUCCESSFUL**. Native HEV build tasks completed for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`. The APK was checked with `aapt dump badging` and `unzip -t`.

Final debug APK SHA-256: `1991e4d0eb907415fb9f83ee61ec6b92bb59bef092065d61afbf85dca483d027`.

## Remaining external verification gate

The code and build are complete for the available environment, but Android device/server verification cannot be honestly marked PASS without a connected device or emulator, VPN permission interaction, a valid signed-in account, a selected compatible server, and live network traffic. Those checks include actual H2/HEV data-plane traffic, reconnect after network change, process death, Doze, IPv6/DNS behavior, and runtime Core switching.

No release APK is produced because no release keystore or signing credentials were supplied. The delivered APK is a debug build.
