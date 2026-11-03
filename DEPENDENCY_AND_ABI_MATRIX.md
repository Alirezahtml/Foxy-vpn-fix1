# Dependency and ABI Matrix

| Component | Source | Required dependency/tool | Status |
|---|---|---|---|
| Firefox Core A | `app/libs/firefox-core.aar` | Android AAR/JNI packaging | Present in workspace |
| Core B upstream | `H2UpstreamSession.kt` | Netty handler, HTTP/2, transport, proxy 4.1.115.Final | Added to integration Gradle |
| Core B TUN | HEV SOCKS5 tunnel 2.17.1 | Git fetch + Android NDK 26.1.10909125 | Source fetch configured; SDK/NDK unavailable |
| Kotlin/Android | Base project | AGP 8.7.2, Kotlin 2.0.21, compile/target SDK 35 | Gradle configuration present |
| ABIs | Both projects | arm64-v8a, armeabi-v7a, x86_64, x86 | Declared; native packaging not built |

No ABI artifact or APK hash is reported because the build could not reach compilation without an Android SDK.
