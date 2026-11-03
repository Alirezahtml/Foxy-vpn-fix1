# FoxyVPN 1.0.32 — Dropdown, Server Selection & Tunnel Route

## Dynamic Island

Glass UI از خود Dynamic Island و پیش‌نمایش آن حذف شد و ظاهر قبلی بر پایه `pillBrush` و سطح اصلی بازگردانده شد. Glass کارت‌های تنظیمات و پنل‌های عمومی Pixel UI باقی مانده است.

## Dropdown Crash Fix

`DropdownMenu` از `SettingsDropdownRow` حذف شد. این کنترل با پنجره انتخاب امن جایگزین شده است که:

- با `AlertDialog` اجرا می‌شود؛
- ارتفاع محدود و اسکرول داخلی دارد؛
- انتخاب فعلی را با نشانگر مشخص می‌کند؛
- هنگام انتخاب، پیش از callback پنجره را می‌بندد؛
- به Popup positioning وابسته نیست و از محدوده صفحه خارج نمی‌شود.

## Server Selection

انتخاب خودکار دیگر سرور تصادفی برنمی‌گزیند. در حالت Recommended، نتیجه ping ثبت‌شده برای مقصدها استفاده می‌شود و کمترین latency انتخاب می‌گردد؛ اگر هنوز ping موجود نباشد، اولین مقصد معتبر استفاده می‌شود.

انتخاب دستی شهر نیز دیگر random نیست و ابتدا سرور نمایش‌داده‌شده/جست‌وجوشده را انتخاب می‌کند و سپس به اولین مقصد معتبر fallback می‌کند.

## Tunnel Route

خط مسیر از گرادیان و glow شدید به یک خط رنگی یکنواخت با هاله بسیار کم تبدیل شد. انیمیشن مسیر از comet سنگین ۱۶ بخشی به stream سبک ۸ بخشی با tail کوتاه‌تر تبدیل شد. نقطه حرکت کوچک‌تر و خواناتر است و مسیر حرفه‌ای‌تر و خلوت‌تر نمایش داده می‌شود.

## Settings Cards

عمق سه‌بعدی Pixel UI فعال باقی ماند و در حالت عادی elevation کنترل‌شده دارد؛ هنگام لمس به شکل نرم کاهش پیدا می‌کند. Glass کارت‌های Settings نیز حفظ شده است.

## اعتبارسنجی

- Unit tests: موفق
- lintDebug: موفق
- assembleDebug: موفق
- assembleRelease: موفق
- Release signature: معتبر

| مشخصه | مقدار |
|---|---|
| Version name | 1.0.32 |
| Version code | 69 |
| Package | com.vauth.foxyvpn.neu |
| Release SHA-256 | `d8ec2c3fd9d0fe3e88d2511284051d622d54a72fe04ebb12037080f8e0a3c80b` |
