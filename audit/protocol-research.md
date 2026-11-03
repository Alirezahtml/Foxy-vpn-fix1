# بررسی پروتکل‌ها و محدودیت‌های سرویس FoxyVPN

تاریخ بررسی: 2026-10-31

## نتیجه

کد محلی `firefox-core` در پروژه مسیرهای واقعی H2 و H3/QUIC را دارد و fallback از H3 به H2 در تست هسته پوشش داده شده است. HTTP/1.1 در هسته‌ی فعلی پیاده‌سازی نشده، پس باید غیرفعال/غیرقابل انتخاب بماند، نه اینکه به‌عنوان پروتکل فعال معرفی شود. همچنین وجود HTTP/3/QUIC به‌تنهایی تضمین پشتیبانی همه‌ی بازی‌ها یا UDP خام نیست؛ دامنه‌ی tunnel و endpoint واقعی باید مشخص باشد. برچسب اتصال برنامه باید پس از خواندن transport واقعی از هسته نشان داده شود، نه صرفاً بر اساس گزینه‌ی درخواستی.

## منابع

- Fastly، «We Built the Proxy Behind Firefox's New Built-In VPN — Here's How It Works» (2026-06-15): https://www.fastly.com/blog/we-built-proxy-behind-firefoxs-new-built-in-vpn-heres-how-it-works
  می‌گوید rollout اولیه‌ی Firefox built-in VPN از HTTP CONNECT روی HTTP/2 استفاده می‌کند؛ معماری برای تکامل به MASQUE/HTTP/3 طراحی شده است. همچنین توضیح می‌دهد HTTP CONNECT سنتی تونل TCP می‌سازد و MASQUE قابلیت UDP/IP را اضافه می‌کند. این منبع به سرویس Firefox اشاره دارد و تضمین نمی‌کند که هر کلاینت ثالث، همه‌ی انواع ترافیک یا UDP را عبور دهد.
- Firefox، «Browser With Built-In VPN: Secure Browsing»: https://www.firefox.com/en-US/features/browser-with-built-in-vpn-secure-browsing/
  Firefox سرویس built-in خود را پراکسی مرورگر powered by Fastly معرفی می‌کند، نه VPN کامل سیستمی؛ این تمایز نباید با معماری اپ اندرویدی حاضر اشتباه شود، ولی برای جلوگیری از ادعای نادرست در توصیف سرویس مفید است.
- مخزن upstream مورد بررسی برای این کلاینت: https://github.com/armanlife/firefox-vpn-client
  README ادعا می‌کند تونل‌های HTTP/2 و HTTP/3 را ارائه می‌کند و محدودیت شناخته‌شده‌ی TCP-only/no-UDP را برای سرویس فعلی ذکر می‌کند؛ این صفحه منبع پروژه‌ی کلاینت است، نه سند Mozilla.
- منبع هسته در پروژه‌ی محلی: `firefox-core/internal/proxy/upstream_h2.go`, `firefox-core/internal/proxy/upstream_h3.go`, `firefox-core/internal/service/lazy_test.go` و `firefox-core/mobile/mobile.go`.
  کد محلی H2/H3 و fallback را نشان می‌دهد؛ آنچه این شواهد به‌تنهایی ثابت نمی‌کنند اتصال خارجی موفق، استفاده از endpoint منتخب فهرست یا عملکرد UDP در مسیر اپ اندرویدی است.

## تغییرات لازم بر مبنای بررسی

1. فقط transportهای واقعاً حاضر در هسته قابل انتخاب بمانند؛ HTTP/1.1 غیرفعال بماند.
2. هنگام مذاکره عنوان «در حال اتصال/مذاکره» نشان داده شود؛ پس از اتصال، badge از `FirefoxCoreBridge.transport()` گرفته شود تا fallback به H2 درست نمایش داده شود.
3. توضیح Gaming Mode نباید پشتیبانی کامل بازی/UDP را وعده دهد؛ فقط کارکرد اندازه‌گیری‌شده، از جمله MTU، ذکر شود.
4. endpointهای ServerList فقط در صورتی به‌عنوان transport فعال معرفی شوند که مسیر اتصال واقعی آن endpoint را به هسته منتقل و مصرف کند.
