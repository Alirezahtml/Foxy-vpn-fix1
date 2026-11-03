# Feature Preservation Matrix

| Area | Source 1.0.16 finding | Integration status | Evidence / limitation |
|---|---|---|---|
| Compose UI and navigation | Existing application UI retained | PRESERVED | Base workspace copied unchanged |
| FxA/token storage | Encrypted token/account store | PRESERVED | No overwrite of auth classes |
| Firefox Core A | AAR + `FirefoxCoreBridge` | PRESERVED | Existing bridge remains the only Core A boundary |
| Android VPN service | `FoxyVpnService` owns current TUN | PRESERVED / PARTIAL | No second VPN service added |
| Protocol setting | H2, H3/QUIC labels existed | CHANGED | Registry now labels H3 as UNVERIFIED rather than treating a label as proof |
| Core selection | Not present in base source | ADDED / PARTIAL | Persistent setting and registry added; UI/session wiring remains incomplete |
| Core B H2/HEV implementation | Present only in 1.0.8 | ADDED / PARTIAL | Unique implementation files and adapter copied; real Android execution blocked |
| Widgets / QS tile / overlay | Existing services | PRESERVED | Manifest and source retained |
| Release signing | Requires external keystore/env | BLOCKED | No release credentials supplied |
