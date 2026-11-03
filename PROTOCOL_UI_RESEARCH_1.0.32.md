# تحقیق فنی پیش از اصلاح پروتکل و UI

## نتیجه اصلی درباره HTTP/3

مستندات رسمی Android می‌گوید Cronet به‌صورت native از HTTP، HTTP/2 و HTTP/3 روی QUIC پشتیبانی می‌کند [1]. اما وجود Cronet در برنامه به‌تنهایی HTTP/3 تونل VPN را فعال نمی‌کند؛ باید همان transport در مسیر upstream و سرور مقصد استفاده شود.

در پروژه فعلی، Core A از یک bridge باینری جداگانه استفاده می‌کند و رابط Kotlin فقط `setTransport("h3")` را به آن ارسال می‌کند. در کد موجود، H3 به‌عنوان `UNVERIFIED` ثبت شده و خود registry صراحتاً می‌گوید شواهد end-to-end برای H3 وجود ندارد. همچنین رشته‌های باینری نشان می‌دهند upstream فعلی برای proxy، TCP-only است و UDP خام/QUIC عمومی را عبور نمی‌دهد. مسیر `Socks5UdpDnsRelay` نیز برای ترافیک غیر-DNS اعلام می‌کند که تونل TCP-only است.

طبق مستندات رسمی `VpnService`، VPN باید بسته‌های IP را از رابط مجازی دریافت و از طریق tunnel پردازش کند و برای socketهای upstream باید آن‌ها را با `protect` از برگشت به خود VPN خارج کند [2]. بنابراین تغییر برچسب از H2 به H3، یا افزودن یک درخواست HTTP/3 جداگانه با Cronet، معادل پیاده‌سازی H3 برای کل تونل نیست.

نتیجه عملی: تا وقتی transport native واقعی برای مسیر upstream، UDP/QUIC relay سازگار و تست end-to-end در دسترس نباشد، فعال‌کردن H3 در UI نادرست است. اصلاح ایمن این نسخه باید H3 را غیرفعال و علت را شفاف نمایش دهد تا با انتخاب آن crash یا fallback پنهان رخ ندهد. H2/HEV و H2 Core A تنها مسیرهای verified/partially verified باقی می‌مانند.

## Game Compatibility

گزینه Gaming Mode از قبل در پروژه وجود دارد و به SettingsStore، MTU مؤثر 1360 در حالت خودکار، اعمال runtime شبکه، توقف اسکن سرورها هنگام benchmark و تحلیل واقعی RTT/jitter/loss متصل است. بنابراین افزودن یک سوییچ نمایشی جدید لازم نیست. اصلاح باید همین گزینه را به‌عنوان «سازگاری بازی» نگه دارد و ادعای پشتیبانی UDP خام را نکند؛ چون relay فعلی TCP-only است.

## جهت UI/UX

بهبودهای قابل‌اعتماد این مرحله باید روی سلسله‌مراتب بصری، فاصله‌گذاری، stateهای فعال/غیرفعال، حرکت کوتاه و قابل‌پیش‌بینی و کاهش عناصر تزئینی سنگین متمرکز باشد. کارت مسیر باید یک مسیر خوانا، دو marker واضح و telemetry قابل‌تشخیص داشته باشد؛ animation نباید وضعیت اتصال را جعل کند.

## References

[1]: https://developer.android.com/develop/connectivity/cronet "Perform network operations using Cronet"
[2]: https://developer.android.com/reference/android/net/VpnService "VpnService API reference"
