# FoxyVPN 1.0.20 — Global Layout Fix

## Problem addressed

Long titles and descriptions were being compressed beside switches and other controls. Dynamic Island Studio also forced badge labels into one line with early ellipsis, and the top category tabs had insufficient height for localized labels.

## Fixes

- Shared settings tabs now support two lines, centered text, and a minimum 48dp height instead of forcing one-line ellipsis.
- Dynamic Island Studio’s two-column option cards now have a minimum 64dp height.
- Badge labels support two wrapped lines with readable line height instead of being truncated to one line.
- The existing vertical scroll container remains responsible for longer content, so added text height does not clip lower sections.
- Shared setting rows continue to use weighted text columns and dedicated switch space, allowing titles and descriptions to wrap naturally across Persian, Arabic, and English.

The fix targets shared layout patterns so it applies across Settings sections that use the same components, not only the screenshot’s Dynamic Island page.

## Verification

Passed command:

`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease`

Version: 1.0.20. Version code: 57. R8/minification completed for Release. The final APKs were rebuilt after the layout changes.

Physical-device validation is still recommended for very narrow displays, large system font/accessibility settings, RTL languages, and Xiaomi/MIUI rendering. The layout is designed to wrap rather than truncate, but final visual verification should be done on the target phone.
