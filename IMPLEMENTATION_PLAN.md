# Implementation Plan and Current Gate Status

1. Preserve both archives and hashes — **PASS**.
2. Audit Gradle, manifests, service ownership, native components, and core paths — **PASS**.
3. Keep Core A in its existing Firefox bridge — **PASS**.
4. Add Core B's real Netty/HEV implementation in separate files and a dedicated adapter — **PARTIAL**.
5. Add persistent core preference and hard eligibility/registry — **PARTIAL**.
6. Wire one authoritative service session controller across both cores — **NOT COMPLETE**.
7. Add UI controls for manual core choice and Smart Recommendation confirmation — **NOT COMPLETE**.
8. Build, unit test, native compile, install, and run real data-plane tests — **BLOCKED** by missing SDK/NDK/device/server.
9. Produce signed release — **BLOCKED** by missing keystore/credentials.

The workspace is not production-ready. The correct next engineering step is to install/configure Android SDK 35, build-tools, NDK 26.1.10909125, and an Android test device/server, then finish service-controller wiring before claiming Core B connectivity.
