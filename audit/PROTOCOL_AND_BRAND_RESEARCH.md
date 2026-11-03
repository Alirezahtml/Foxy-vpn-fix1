# شواهد پژوهش پروتکل و نشان‌های اجتماعی

آخرین بازبینی: 2026-10-31

## پروتکل تونل

در سورس فعلی Firefox Go core، هسته فقط `h2` و `h3` را می‌پذیرد؛ پیاده‌سازی upstream برای H2 از TCP/TLS و ALPN `h2` استفاده می‌کند و مسیر H3 از QUIC/UDP و HTTP/3. آزمون موجود `TestHTTP3FallsBackToHTTP2` فقط fallback هنگام شکست آغاز نشست H3 را اثبات می‌کند. این یافته از سورس محلی پروژه است، نه از برچسب UI.

مقالهٔ Fastly دربارهٔ زیرساخت VPN داخلی Firefox می‌گوید راه‌اندازی اولیه با HTTP CONNECT روی HTTP/2 بوده و معماری برای حرکت به سمت MASQUE روی HTTP/3 در حال توسعه است. این توضیح به‌خودی‌خود پشتیبانی HTTP/1.1 را در endpointهای Firefox/Fastly تأیید نمی‌کند. RFC 9110 CONNECT را تعریف می‌کند، اما می‌گوید به‌جز GET و HEAD سایر روش‌ها optional هستند و سرور می‌تواند CONNECT را پیاده‌سازی نکند؛ RFC 9112 تنها نحو HTTP/1.1 را مشخص می‌کند. پس استانداردسازی CONNECT در H1.1 دلیل کافی برای فعال‌کردن client-side H1.1 در برابر backend فعلی نیست. تصمیم ایمن: H1.1 تا زمان دسترسی/تأیید backend و آزمون end-to-end فعال یا تبلیغ نشود.

## دارایی‌های برند اجتماعی

راهنمای رسمی Meta می‌گوید برای Instagram فقط از لوگوها/تصاویر دانلودشده از Brand Resource Center استفاده و قواعد آن رعایت شود؛ استفاده نباید شراکت یا تأیید رسمی را القا کند. صفحهٔ رسمی Telegram لوگوهایش را برای استفاده‌هایی مانند دکمهٔ forward مجاز می‌داند، مشروط به اینکه کاربران تصور نکنند برنامه رسماً متعلق به Telegram است. در UI باید نشان‌ها کوچک و در نقش لینک شبکهٔ اجتماعی FoxyVPN باقی بمانند، نه برند/تأییدیهٔ مشترک.

## منابع

- [1] [Fastly: We Built the Proxy Behind Firefox's New Built-In VPN](https://www.fastly.com/blog/we-built-proxy-behind-firefoxs-new-built-in-vpn-heres-how-it-works) — توضیح پروتکل/معماری از اپراتور proxy.
- [2] [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html) — تعریف CONNECT و optional بودن روش‌های دیگر برای serverها.
- [3] [RFC 9112: HTTP/1.1](https://www.rfc-editor.org/rfc/rfc9112.html) — نحو H1.1 و framing تونل CONNECT.
- [4] [Instagram Brand Resources](https://www.meta.com/brand/resources/instagram/instagram-brand/) — دارایی‌های تأییدشده و راهنمای استفاده.
- [5] [Telegram Logos and App Screenshots](https://telegram.org/tour/screenshots) — صفحهٔ رسمی دانلود لوگو و شرط عدم القای وابستگی رسمی.
