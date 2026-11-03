#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
: "${ANDROID_HOME:=${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
: "${ANDROID_SDK_ROOT:=$ANDROID_HOME}"
: "${ANDROID_NDK_HOME:=$ANDROID_HOME/ndk/30.0.16248370}"
export ANDROID_HOME ANDROID_SDK_ROOT ANDROID_NDK_HOME

command -v go >/dev/null || { echo "Go 1.27.2 is required" >&2; exit 1; }
command -v gomobile >/dev/null || { echo "gomobile is required (go install golang.org/x/mobile/cmd/gomobile@v0.0.0-20260908204917-8b95e45f8d3e)" >&2; exit 1; }
command -v gobind >/dev/null || { echo "gobind is required (go install golang.org/x/mobile/cmd/gobind@v0.0.0-20260908204917-8b95e45f8d3e)" >&2; exit 1; }
test -d "$ANDROID_NDK_HOME" || { echo "Android NDK not found at $ANDROID_NDK_HOME" >&2; exit 1; }
test -x "$ROOT/gradlew" || { echo "Gradle wrapper is missing" >&2; exit 1; }
gomobile init

cd "$ROOT/firefox-core"
gofmt -w mobile/mobile.go internal/service/daemon.go internal/fxa/fxa.go
go test ./...
go vet ./...
mkdir -p "$ROOT/app/libs"
gomobile bind -target=android -androidapi 26 -o "$ROOT/app/libs/firefox-core.aar" ./mobile

test -f "$ROOT/release-keystore.jks" || {
    echo "A persistent release-keystore.jks is required; refusing to create a disposable signing key." >&2
    exit 1
}
for signing_var in SIGNING_KEY_ALIAS SIGNING_KEY_PASSWORD SIGNING_STORE_PASSWORD; do
    if [[ -z "${!signing_var:-}" ]]; then
        echo "$signing_var must be set to sign the release APK with its persistent key." >&2
        exit 1
    fi
done

cd "$ROOT"
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease -PenableAbiSplits=true --no-daemon --stacktrace
printf '\nAPK outputs:\n'
find app/build/outputs/apk -type f -name '*.apk' -printf '%p (%k KB)\n' | sort
