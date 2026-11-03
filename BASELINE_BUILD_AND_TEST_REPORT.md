# Baseline Build and Test Report

Command:

```text
./gradlew :app:testDebugUnitTest --stacktrace
```

Result: **BLOCKED** before compilation.

Gradle wrapper downloaded Gradle 8.10.2 successfully. The first Android build then failed with:

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable
or by setting sdk.dir in local.properties.
```

Environment evidence:

- Java 21.0.12 is installed.
- `gradle` is not globally installed, but the supplied wrapper works.
- `adb` is not installed.
- No Android SDK directory was found in the inspected standard locations.

Therefore unit tests, lint, APK packaging, native compilation, installation, VPN permission flow, and end-to-end traffic tests are **BLOCKED**, not PASS.
