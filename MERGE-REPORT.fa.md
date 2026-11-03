# گزارش نهایی ادغام FoxyVPN و Firefox Go core

## نتیجه

هستهٔ دیتاپلین Android به `firefox-core` منتقل شد. سرویس VPN دیگر مسیر قدیمی Foxy شامل HEV/TUN native، تونل H2 مبتنی بر Netty و سرورهای Kotlin برای SOCKS/HTTP را اجرا نمی‌کند؛ کتابخانه‌های HEV و کدهای مربوط به مسیر قدیمی حذف شدند. رابط Compose، ورود Firefox Account، `TokenStore` رمزنگاری‌شده و جریان اصلی تنظیمات Foxy حفظ شده‌اند.

یک bridge باریک و درون‌فرایندی با gomobile، حساب‌ها را از `TokenStore` به هسته همگام می‌کند، انتخاب/حذف حساب را اعمال می‌کند و descriptor رابط TUN اندروید را به Go core می‌دهد. کشور انتخابی به تنظیم کشور هسته نگاشت می‌شود. HTTP/3/QUIC و HTTP/2 به‌عنوان fallback واقعی هسته قابل انتخاب‌اند و نشانگر رابط، پروتکل مذاکره‌شده را از هسته می‌خواند. شمارنده‌های Network Tools نیز از tracker هسته گرفته می‌شوند. SOCKS5 و HTTP روی پورت loopback هسته سرو می‌شوند؛ در صورت فعال بودن پورت جداگانهٔ HTTP در Foxy، forwarder محلی آن را به listener هسته می‌رساند.

گزینهٔ HTTP/1.1 با TLS fragmentation به‌عنوان تونل VPN فعال نیست، چون متعلق به موتور حذف‌شدهٔ Foxy بود. ابزار TLS موجود برای تنظیمات/درخواست‌های control-plane که از آن استفاده می‌کنند حفظ شده است. هستهٔ Firefox انتخاب کشور را به relay واقعی واگذار می‌کند و host/port ثابت Foxy را pin نمی‌کند.

## بررسی ساخت

- `go test ./...`: موفق، همهٔ بسته‌های Go.
- `go vet ./...`: موفق.
- `./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease -PenableAbiSplits=true --no-daemon --stacktrace`: موفق.
- `apksigner verify`: هر پنج APK release تأیید شدند؛ هرکدام یک signer و امضای APK v2 دارند.
- `aapt`: `applicationId=com.vauth.foxyvpn.neu`، نسخهٔ `1.0.12`، `versionCode=49` و `minSdk=26`.
- APK جهانی شامل `libgojni.so` برای `arm64-v8a`، `armeabi-v7a`، `x86` و `x86_64` است؛ هیچ کتابخانهٔ HEV در آن نیست.
- AAR تولیدشده، APIهای `transport`، `setTransport`، `proxyConnectionCounts` و ورود/تأیید/تبادل توکن FxA را به‌صورت درون‌فرایندی در اختیار Kotlin می‌گذارد.
- در Android، داشبورد HTTP استفاده نمی‌شود؛ پورت ثابت پیش‌فرض `127.0.0.1:10820` با پورت آزاد سیستم روی loopback جایگزین می‌شود تا تداخل با برنامهٔ دیگر مانع شروع core نشود.

محیط ساخت `adb` یا دستگاه Android در دسترس نداشت؛ بنابراین نصب روی گوشی و آزمون واقعی ورود، مجوز VPN، handshake شبکه، DNS/TUN و اتصال QUIC در این اجرا انجام نشد. موفقیت build و تست‌های واحد را نباید معادل آزمون end-to-end روی دستگاه دانست.

## امنیت و امضا

ورود همچنان از مسیر FxA موجود در Foxy انجام می‌شود و TokenStore رمزنگاری‌شدهٔ Foxy باقی می‌ماند. برای اجرای مستقل، Go core یک کپی credential در مسیر خصوصی sandbox خود برنامه نگه می‌دارد؛ این کپی از طریق dashboard/HTTP یا log منتقل نمی‌شود، اما به‌طور جداگانه با کلید `TokenStore` رمزنگاری نشده است و باید همانند دادهٔ خصوصی برنامه نگهداری شود.

APKهای release این اجرا با کلید موقت debug-style امضا شده‌اند تا نصب و آزمون ممکن باشد. برای انتشار عمومی، انتشار در فروشگاه یا امکان به‌روزرسانی نصب رسمی، APKها را با keystore انتشار خودتان دوباره امضا کنید؛ این امضا نشان‌دهندهٔ مالکیت/اصالت ناشر نیست.

### اصلاح ورود HTTP 406

نسخهٔ 1.0.10 با درخواست مستقیم Kotlin/OkHttp همچنان 406 گرفت. برای جداسازی مشکل، آزمون زندهٔ challenge هستهٔ Go با ایمیل آزمایشی در دامنهٔ `.invalid` اجرا شد: challenge پذیرفته شد و درخواست آزمایشی به خطای عادی FxA (HTTP 400، errno 102، حساب ناموجود) رسید، نه 406. این آزمون از محیط ساخت بود و نه گوشی یا شبکهٔ کاربر. بر همین مبنا، نسخهٔ 1.0.11 ورود با گذرواژه، تأیید و ارسال مجدد کد، تبادل OAuth و تمدید توکن را از کلاینت challenge-aware Go core عبور می‌دهد؛ رابط Foxy و TokenStore رمزنگاری‌شده حفظ شده‌اند. ثبت‌نام جدید هنوز از درخواست Kotlin استفاده می‌کند و ممکن است محدودیت جداگانهٔ 406 داشته باشد. سه تست بازگشتی scope کوکی نیز حفظ شده‌اند.

مستند FxA برای `/account/login` خطاهای کاربردی را به‌صورت HTTP 400 با errnoهایی مانند 102، 103 و 125 تعریف می‌کند؛ در نتیجه 406 خام با پاسخ خطای معمول ورود یکی نیست و با رد شدن challenge لبهٔ شبکه سازگارتر است.[2] هنوز آزمون واقعی ورود روی گوشی لازم است: آزمون Go فقط نشان می‌دهد مسیر Go از محیط ساخت توانسته از challenge عبور کند؛ نتیجه روی شبکهٔ کاربر تضمین نشده است.

### اصلاح شروع core و پورت 10820

کاربر گزارش داد core به دلیل اشغال بودن `127.0.0.1:10820` متوقف می‌شود. این پورت برای داشبورد desktop است و اپ Android از dashboard استفاده نمی‌کند؛ بنابراین در نسخهٔ 1.0.12 کد Android پیش از ساخت daemon مقدار dashboard را به `127.0.0.1:0` تنظیم می‌کند. سیستم یک پورت محلی آزاد انتخاب می‌کند و listener همچنان فقط loopback است. تست regression در Go حفظ تنظیم proxy و پیکربندی loopback/پورت پویا را بررسی می‌کند. این تغییر پورت `10820` را از فرایندهای دیگر نمی‌گیرد یا متوقف نمی‌کند و باید روی گوشی کاربر تأیید شود.

## خروجی‌ها

پنج APK نهایی در پوشهٔ `app/build/outputs/apk/release/` ساخته شده‌اند: universal، `arm64-v8a`، `armeabi-v7a`، `x86` و `x86_64`. دو آرشیو جداگانه تحویل شده‌اند: یکی شامل هر پنج APK و دیگری شامل پروژهٔ منبع ادغام‌شده به‌همراه AAR لازم برای build مستقیم. فایل محیط محلی `.env`، keystoreها، cacheها و خروجی‌های build در ZIP منبع قرار نگرفته‌اند؛ `.env.example` و هر دو اعلان مجوز MIT و GPLv3 باقی هستند.

برای بازتولید خروجی از پروژهٔ منبع، پیش‌نیازهای pin‌شده در README و `scripts/build-android.sh` را فراهم کنید و اجرا کنید:

```bash
./scripts/build-android.sh
```

این اسکریپت تست‌های Go و `go vet` را اجرا، AAR چندمعماری را تولید و نسخه‌های debug/release را می‌سازد. برای release واقعی، متغیرهای keystore انتشار را تنظیم کنید.

## منابع

[1]: https://www.fastly.com/documentation/guides/security/client-challenges/getting-started-with-client-challenges/ "Fastly client challenges"
[2]: https://mozilla.github.io/ecosystem-platform/api/ "Firefox Accounts Auth Server API"
