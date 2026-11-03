# Core and Protocol UI Fix

The networking-core selector now presents only the neutral labels Core A, Core B, and Smart Selection. No product or implementation vendor name is shown in the core selector, login copy, Telegram proxy copy, capability display names, or user-facing account errors.

A dedicated information button was added beside the Networking Core heading. It explains the actual difference between the two paths: Core A supports the standard HTTP/2 route and can use HTTP/3/QUIC when the active connection supports it; Core B is the implemented H2/HEV TCP/TLS route and supports HTTP/2 only.

The Tunnel Connection Protocol card was reworked for narrow screens. Its title and live badge can wrap cleanly, protocol names use restrained typography and zero letter spacing, descriptions can occupy multiple lines, and the content column keeps a stable width instead of splitting strings such as `HTTP/2` into separated characters.

Live badges were simplified to `HTTP/2` and `HTTP/3 • QUIC` so the Dynamic Island and settings card do not show artificial fallback text. The badge is sourced from the active runtime transport.

HTTP/1.1 remains intentionally disabled. The project contains HTTP/1.1 request/response handling in the local proxy, but neither upstream tunnel runtime implements HTTP/1.1 as an outbound tunnel transport. Enabling it would be misleading and could cause a runtime failure. The protocol policy therefore allows only protocols implemented by the selected runtime: Core B is limited to AUTO/HTTP/2, Core A to AUTO/HTTP/2/HTTP/3, and unsupported HTTP/1.1 stays disabled.

Validation completed successfully: unit tests, lint, debug assembly, release assembly, R8 minification, and APK signature verification.
