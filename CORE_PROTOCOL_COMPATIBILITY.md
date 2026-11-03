# Core / Protocol Compatibility

Status values follow the requested vocabulary. `VERIFIED` is intentionally not used for a combination without end-to-end evidence.

| Core | Protocol | Status | Reason |
|---|---|---|---|
| Core A — Firefox | HTTP/2 over TLS/TCP | PARTIAL | Real bridge methods and TUN path exist; Android/server/data-plane execution not run here |
| Core A — Firefox | HTTP/3 / QUIC | UNVERIFIED | UI/bridge transport label exists, but no H3 evidence was available in the supplied environment |
| Core B — H2/HEV | HTTP/2 over TLS/TCP | PARTIAL | Real Netty `H2UpstreamSession` and HEV JNI bridge copied; native build and live server verification blocked |
| Core B — H3 / QUIC | UNSUPPORTED | No QUIC engine was found in source 1.0.8 |

Hard rules implemented in `CoreCapabilities.kt`: SMART is not a networking core; quarantined servers are rejected; unregistered core/protocol pairs are rejected; server protocol advertisements are checked when present; ranking cannot override failed eligibility.
