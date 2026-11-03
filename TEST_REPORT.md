# Updated Test Report

## Passed in sandbox

The combined Gradle command `:app:testDebugUnitTest :app:assembleDebug` passed. Registry regression tests passed for SMART policy separation, quarantined-server rejection, and deterministic Smart ranking. Kotlin compilation passed after adding the Core B runtime and Settings UI. Native HEV compilation passed for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`. APK metadata and ZIP integrity checks passed.

## Implemented but not device-verified

Core B runtime lifecycle is now wired into `FoxyVpnService`: H2 upstream creation, Guardian token use, protected sockets, local SOCKS5, Android TUN establishment, HEV start/stop, Core B health checks, and cleanup on disconnect. The Settings UI can select Core A, Core B, or Smart and requests a safe reconnect. These paths still require a real Android device and compatible live account/server to verify data-plane behavior.

## External gates

Device installation, VPN permission, real server authentication, H2/HEV traffic, network handover, process death, Doze, IPv6/DNS leak behavior, and long-running performance remain unexecuted because no Android device/emulator or live test credentials are available. Release signing also remains unavailable without a release keystore.
