# FoxyVPN UI and Location Update

## Theme icons

The supplied day and night SVG designs were converted to Android VectorDrawable resources:

- `app/src/main/res/drawable/ic_theme_day.xml`
- `app/src/main/res/drawable/ic_theme_night.xml`

HomeScreen selects the correct resource according to the resolved light/dark theme. The paths, stroke colors, rounded stroke caps, joins, and 24dp viewport are preserved from the supplied SVGs.

## UI motion and low-end optimization

Navigation transitions were shortened and softened to 190–220ms with a minimal 0.985 scale delta. Existing button touch feedback remains lightweight. Location ping testing is sequential and capped at 24 targets per manual full scan to avoid saturating weak phones or the network. The existing lazy list still measures only visible city rows automatically.

## Real location ping and sorting

ServerListScreen now includes a real refresh/test control. It measures each target using `PingUtil.measureServerLatency`, which uses ICMP when possible and falls back to TCP/TLS RTT. Results include the actual method and latency. The sort control orders countries and cities from lowest measured latency to highest; unmeasured or failed targets remain after measured targets.

## Verification

The final command `:app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` completed successfully. Debug SHA-256: `0e18616616da793993b7db7474101791f280980c3fed34abb8c58661b529b446`. Release SHA-256: `f04cf233869a4974995f89b2954ec8082d64a48c71d896b80acf1df8897c5fb9`.

Live latency results depend on the user's network and server availability; the app performs the measurement on-device rather than displaying fabricated values.
