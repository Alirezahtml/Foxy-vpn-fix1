# تحقیق پروتکل برای به‌روزرسانی FoxyVPN

تاریخ بررسی: 2026-11-01

## نتیجه برای HTTP/1.1

هسته‌ی محلی FoxyVPN در `firefox-core/internal/service/daemon.go`، متد `SetTransport` را فقط برای `h2` و `h3` قبول می‌کند. `NewController` در `firefox-core/internal/proxy/controller.go` فقط sessionهای H2 یا H3 می‌سازد. مسیر H2، ALPN را به `h2` محدود می‌کند و اگر H2 مذاکره نشود خطا می‌دهد. بنابراین UI را نمی‌توان با تغییر label به HTTP/1.1 واقعی تبدیل کرد.

HTTP/1.1 می‌تواند CONNECT استاندارد داشته باشد، ولی پشتیبانی عمومی این روش به معنای پشتیبانی endpoint مشخص Mozilla/Fastly نیست. منبع Fastly می‌گوید rollout اولیه از HTTP CONNECT روی HTTP/2 استفاده می‌کند و برنامه‌ی تکامل را به MASQUE/HTTP/3 توضیح می‌دهد؛ در این سند HTTP/1.1 به‌عنوان mode پشتیبانی‌شده‌ی endpoint معرفی نشده است. پس این به‌روزرسانی گزینه‌ی H1.1 را فعال یا به H2 map نمی‌کند؛ برچسب باید صریح و غیرفعال بماند تا اتصال ناموفق یا ادعای ساختگی ایجاد نشود.

## منابع

- Fastly, “We Built the Proxy Behind Firefox's New Built-In VPN — Here's How It Works”, 2026-06-15. سند می‌گوید rollout اولیه از HTTP CONNECT over HTTP/2 استفاده می‌کند و مسیر آینده‌ی معماری MASQUE/HTTP/3 است: https://www.fastly.com/blog/we-built-proxy-behind-firefoxs-new-built-in-vpn-heres-how-it-works
- IETF RFC 9110, “HTTP Semantics”, استاندارد معناشناسی HTTP و روش CONNECT: https://www.rfc-editor.org/info/rfc9110/
- مخزن عمومی پروژه‌ی upstream مورد بررسی، که README آن HTTP/2 و HTTP/3 را فهرست می‌کند: https://github.com/armanlife/firefox-vpn-client
- فهرست کد proxy در upstream، با فایل‌های `upstream_h2.go` و `upstream_h3.go` و بدون `upstream_h1.go`: https://github.com/armanlife/firefox-vpn-client/tree/main/core/internal/proxy
