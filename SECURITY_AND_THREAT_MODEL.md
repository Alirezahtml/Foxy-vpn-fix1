# Security and Threat Model

## Protected assets

FxA access/refresh tokens, VPN credentials and bearer tokens, private server configuration, TUN traffic, TLS trust decisions, and user-selected routing/core policy.

## Important controls observed or retained

- Core A uses the supplied mobile bridge and does not bypass TLS in the inspected adapter.
- Core B retains the supplied TLS trust path and `VpnService.protect()` control-plane socket protection.
- Encrypted preferences are retained for credentials.
- Logs must not contain tokens or private keys; no new secret logging was added.
- Core/protocol compatibility is a hard eligibility check, not a score bonus.
- No certificate error or invalid credential is converted into success by the added registry.

## Findings

The source contains an H3/QUIC UI option and transport label, but the supplied evidence does not prove end-to-end H3 on Android; it is marked UNVERIFIED. The integration does not silently downgrade that status.

The single existing `FoxyVpnService` remains the current TUN owner. A full race-safe cross-core session controller and actual Core B service routing are not yet verified. This is a release blocker for production claims.

No release keystore or signing credentials were supplied. Release signing is therefore unavailable.
