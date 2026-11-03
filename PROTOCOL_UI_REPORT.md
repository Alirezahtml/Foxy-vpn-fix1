# Protocol and UI Update

## Icon decision

The custom sun/moon vector resources were removed. HomeScreen now uses the official Material `Icons.Filled.LightMode` and `Icons.Filled.DarkMode` icons exactly as requested.

## Core/protocol policy

`CoreProtocolPolicy` is now the single UI/runtime policy. Core B enables only `AUTO` and `HTTP2_TCP`; `HTTP3_QUIC` is visibly disabled. Core A enables HTTP/2 and HTTP/3 options according to the existing protocol support flags. Smart keeps both choices available until the runtime chooses an eligible core.

When the user changes to a core that cannot support the current protocol, the setting is immediately normalized to HTTP/2 and the tunnel receives a safe protocol update. The service also rejects/normalizes an invalid H3 request while Core B is active instead of showing a false protocol state.

## Dynamic Island

The protocol badge is sourced from `FoxyVpnService.activeProtocolBadge`. Core B publishes `HTTP/2 • HEV` only after its H2 session and HEV tunnel are started. Core A publishes its actual transport result (`HTTP/2 (core/fallback)` or `HTTP/3 QUIC`) from `FirefoxCoreBridge.transport()`. The badge resets to `AUTO` on disconnect and uses a short fade transition when the real value changes. No decorative protocol value is injected.

## Verification

The combined command `:app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` passed. Debug APK SHA-256: `8abe6058deda110ef661888587a562484743f544e21892fae435519d736df947`. Release APK SHA-256: `19407eb651c4685738196c1b4c862fe382c5ef64787cdf094755cac42732a009`.
