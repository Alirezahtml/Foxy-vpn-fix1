package com.vauth.foxyvpn.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.vauth.foxyvpn.ui.theme.AppLanguage
import com.vauth.foxyvpn.ui.theme.ThemeMode
import com.vauth.foxyvpn.vpn.CoreId
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.widget.QuickConnectWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class CoreSelection {
    CORE_A,
    CORE_B_H2_HEV,
    SMART,
}

enum class FoxyColorTheme(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
    val subtitleEn: String,
    val subtitleFa: String,
    val subtitleAr: String,
    val primaryLightHex: Long,
    val primaryDarkHex: Long,
    val secondaryAccentHex: Long,
    val lightBgHex: Long,
    val lightSurfaceHex: Long,
    val lightElevatedHex: Long,
    val darkBgHex: Long,
    val darkSurfaceHex: Long,
    val darkElevatedHex: Long,
) {
    CYBER_FOX(
        label = "Balanced neutral base",
        persianLabel = "پایهٔ خنثی و متعادل",
        arabicLabel = "قاعدة محايدة ومتوازنة",
        subtitleEn = "Neutral slate surfaces with clear blue actions and green connection status",
        subtitleFa = "سطوح خنثی و مرتب با دکمه‌های آبی و نشانگر اتصال سبز",
        subtitleAr = "أسطح محايدة مع عناصر زرقاء وحالة اتصال خضراء واضحة",
        primaryLightHex = 0xFF0066FF,
        primaryDarkHex = 0xFF1E88FF,
        secondaryAccentHex = 0xFF10B981,
        lightBgHex = 0xFFF6F6F8,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFF0F1F4,
        darkBgHex = 0xFF050506,
        darkSurfaceHex = 0xFF121215,
        darkElevatedHex = 0xFF1D1D21,
    ),
    OCEAN_SAPPHIRE(
        label = "Deklik Deep Slate & Cobalt Blue",
        persianLabel = "پالت ذغالی مات و آبی کبالت",
        arabicLabel = "لوحة الرمادي الداكن والأزرق الكوبالت",
        subtitleEn = "OLED pitch black #000000 & porcelain white #FFFFFF with cobalt blue accents",
        subtitleFa = "مشکی مطلق OLED در شب و سفید صدفی در روز با دکمه‌های آبی کبالت بدون رنگ‌پاشی روی صفحه",
        subtitleAr = "أسود مطلق ليلاً وأبيض نقي نهاراً مع أزرار زرقاء كوبالت",
        primaryLightHex = 0xFF0055E5,
        primaryDarkHex = 0xFF2979FF,
        secondaryAccentHex = 0xFF10B981,
        lightBgHex = 0xFFF7F8FA,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFEEF0F5,
        darkBgHex = 0xFF000000,
        darkSurfaceHex = 0xFF101013,
        darkElevatedHex = 0xFF1A1A1F,
    ),
    EMERALD_AURORA(
        label = "Minimal Graphite & Emerald Shield",
        persianLabel = "پالت گرافیت خنثی و سبز زمردی",
        arabicLabel = "لوحة الجرافيت المحايد والأخضر الزمردي",
        subtitleEn = "Neutral graphite dark & pure white light cards with emerald & blue accents",
        subtitleFa = "سطوح گرافیتی خنثی و سفید خالص بدون رنگ‌گرفتگی صفحه با تاکید سبز زمردی",
        subtitleAr = "أسطح جرافيت محايدة وبيضاء نقية مع لمسات زمردية",
        primaryLightHex = 0xFF059669,
        primaryDarkHex = 0xFF10B981,
        secondaryAccentHex = 0xFF1E88FF,
        lightBgHex = 0xFFF5F7F6,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFEDF1EF,
        darkBgHex = 0xFF060707,
        darkSurfaceHex = 0xFF121414,
        darkElevatedHex = 0xFF1C1F1E,
    ),
    ROYAL_AMETHYST(
        label = "Minimal Obsidian & Indigo Violet",
        persianLabel = "پالت ابسیدین خنثی و نیلی مدرن",
        arabicLabel = "لوحة الأوبسيديان المحايد والنيلي الحديث",
        subtitleEn = "Clean neutral obsidian & white cards with indigo-violet controls",
        subtitleFa = "کارت‌های خنثی و تمیز بدون رنگ‌پاشی پس‌زمینه با دکمه‌های نیلی-بنفش",
        subtitleAr = "بطاقات محايدة نظيفة مع أزرار نيلية بنفسجية",
        primaryLightHex = 0xFF4F46E5,
        primaryDarkHex = 0xFF6366F1,
        secondaryAccentHex = 0xFF10B981,
        lightBgHex = 0xFFF6F6F9,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFEEEEF4,
        darkBgHex = 0xFF060608,
        darkSurfaceHex = 0xFF131317,
        darkElevatedHex = 0xFF1D1D23,
    ),
    ROSE_VELVET(
        label = "Minimal Carbon & Crimson Accent",
        persianLabel = "پالت کربن مات و سرخ مدرن",
        arabicLabel = "لوحة الكربون المحايد والقرمزي الحديث",
        subtitleEn = "Untinted carbon dark & pure white light surfaces with crimson controls",
        subtitleFa = "سطوح کربنی و سفید بدون رنگ‌پاشی روی صفحه با دکمه‌های سرخ مدرن",
        subtitleAr = "أسطح كربونية وبيضاء نقية مع أزرار قرمزية",
        primaryLightHex = 0xFFE11D48,
        primaryDarkHex = 0xFFF43F5E,
        secondaryAccentHex = 0xFF10B981,
        lightBgHex = 0xFFF8F6F7,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFF2EEF0,
        darkBgHex = 0xFF070606,
        darkSurfaceHex = 0xFF141213,
        darkElevatedHex = 0xFF1F1C1D,
    ),
    SOLAR_GOLD(
        label = "Minimal Warm Stone & Royal Blue",
        persianLabel = "پالت سنگ گرم و آبی سلطنتی",
        arabicLabel = "لوحة الحجر الدافئ والأزرق الملكي",
        subtitleEn = "Warm alabaster light & matte charcoal dark cards with royal blue controls",
        subtitleFa = "ترکیب سفید گرم و ذغالی مات بدون هاله رنگی مزاحم با دکمه‌های آبی",
        subtitleAr = "أبيض دافئ ورمادي داكن غير لامع مع أزرار زرقاء",
        primaryLightHex = 0xFF1D4ED8,
        primaryDarkHex = 0xFF3B82F6,
        secondaryAccentHex = 0xFF10B981,
        lightBgHex = 0xFFF7F6F3,
        lightSurfaceHex = 0xFFFFFFFF,
        lightElevatedHex = 0xFFEFECE6,
        darkBgHex = 0xFF070706,
        darkSurfaceHex = 0xFF141412,
        darkElevatedHex = 0xFF1F1E1B,
    ),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }

    fun localizedSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> subtitleEn
        AppLanguage.FA -> subtitleFa
        AppLanguage.AR -> subtitleAr
    }
}

enum class FoxyUiDesignStyle(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
    val subtitleEn: String,
    val subtitleFa: String,
    val subtitleAr: String,
) {
    PIXEL_GOOGLE(
        label = "Pixel UI",
        persianLabel = "Pixel UI",
        arabicLabel = "Pixel UI",
        subtitleEn = "The main design: clean Pixel-inspired surfaces and fluid motion",
        subtitleFa = "طرح اصلی: ظاهر تمیز الهام‌گرفته از Pixel با حرکت روان",
        subtitleAr = "التصميم الأساسي: أسطح نظيفة مستوحاة من Pixel وحركة سلسة",
    ),
    SAFETY_SURF(
        label = "Safety Surf UI",
        persianLabel = "طرح Safety Surf",
        arabicLabel = "تصميم Safety Surf",
        subtitleEn = "Blue connection dashboard with wave controls and server cards",
        subtitleFa = "داشبورد آبی اتصال با موج، دکمه مرکزی و کارت‌های سرور",
        subtitleAr = "لوحة اتصال زرقاء مع موج وزر مركزي وبطاقات خوادم",
    ),
    CLAYMORPHISM(
        label = "Claymorphism",
        persianLabel = "کلی‌مورفیسم",
        arabicLabel = "كليمورفيزم ثلاثي الأبعاد",
        subtitleEn = "3D inflated matte clay cards with dual inner bevels",
        subtitleFa = "کارت‌های سه‌بعدی برجسته و نرم با لبه‌های حجمی",
        subtitleAr = "بطاقات ثلاثية الأبعاد ناعمة بحواف بارزة",
    ),
    DARK_LUXURY(
        label = "Dark Luxury",
        persianLabel = "دارک لاکشری",
        arabicLabel = "الفخامة الداكنة والذهبي",
        subtitleEn = "Obsidian velvet surfaces with brushed champagne gold borders",
        subtitleFa = "سطوح مخملی مشکی با قاب و خطوط طلایی لوکس",
        subtitleAr = "أسطح مخملية داكنة مع إطارات ذهبية فاخرة",
    ),
    NEO_BRUTALISM(
        label = "Neo-Brutalism",
        persianLabel = "نئو بروتالیسم",
        arabicLabel = "نيو بروتاليزم عصري",
        subtitleEn = "Bold ink borders, high-contrast geometry & hard offset shadows",
        subtitleFa = "خطوط ضخیم کنتراست بالا و سایه‌های هندسی تیز و مدرن",
        subtitleAr = "حدود جريئة عالية التباين وظلال هندسية حادة",
    ),
    BENTO_UI(
        label = "Bento UI",
        persianLabel = "بنتو",
        arabicLabel = "واجهات بنتو الشبكية",
        subtitleEn = "Modular Apple-style Bento compartments & ultra-clean glass hairlines",
        subtitleFa = "چیدمان ماژولار و تمیز به سبک باکس‌های بنتو",
        subtitleAr = "تصميم شبكي معياري نظيف بأسلوب أبل بنتو",
    ),
    EDITORIAL_DESIGN(
        label = "Editorial Design",
        persianLabel = "ادیتوریال",
        arabicLabel = "التصميم التحريري الفاخر",
        subtitleEn = "Architectural magazine grid, fine rule lines & warm gallery tones",
        subtitleFa = "سبک مجله‌ای و معماری با خطوط ظریف و کنتراست تایپوگرافی",
        subtitleAr = "تصميم تحريري معماري بخطوط دقيقة وتباين أنيق",
    ),
    NEUMORPHISM(
        label = "Current Design",
        persianLabel = "طرح فعلی",
        arabicLabel = "التصميم الحالي",
        subtitleEn = "The existing FoxyVPN visual language and controls",
        subtitleFa = "ظاهر و کنترل‌های فعلی برنامه",
        subtitleAr = "المظهر وعناصر التحكم الحالية للتطبيق",
    ),
    ;

    companion object {
        /** The only complete application skins exposed to users. */
        val userSelectableEntries: List<FoxyUiDesignStyle> = listOf(PIXEL_GOOGLE)
    }

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }

    fun localizedSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> subtitleEn
        AppLanguage.FA -> subtitleFa
        AppLanguage.AR -> subtitleAr
    }
}

enum class HeroWaveAnimationStyle(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    HARMONIC_ORBIT("Harmonic Waves & Orbit", "امواج هارمونیک و مدار", "موجات توافقية ومدار"),
    SONAR_PULSE("Deep Sonar Radar", "رادار سونار عمیق", "نبض السونار العميق"),
    PLASMA_VORTEX("Plasma Energy Vortex", "گرداب انرژی پلاسما", "دوامة طاقة البلازما"),
    STARFIELD_NOVA("Cosmic Nova Burst", "ابرنواختر کیهانی", "انفجار النوفا الكوني"),
    CYBER_HEX_SHIELD("Cyber Hexagon Shield", "سپر شش‌ضلعی سایبری", "درع السداسي السيبراني"),
    QUANTUM_HELIX("Quantum Double Helix", "مارپیچ کوانتومی دوگانه", "حلزون كمومي مزدوج"),
    LIQUID_SPECTRUM("3D Acoustic Spectrum", "طیف آکوستیک سه‌بعدی", "طيف الموجات الصوتية"),
    AURORA_CROWN("Magnetic Crown Rings", "تاج مغناطیسی شفق", "حلقات التاج المغناطيسي"),
    CYBER_FOX_AURORA("Cyber Fox Arctic Aura", "هاله روباه آبی سایبری", "هالة الثعلب الأزرق"),
    ORBITAL_GYROSCOPE("3D Orbital Gyroscope", "ژیروسکوپ سه‌بعدی مداری", "جيروسكوب مداري ثلاثي الأبعاد"),
    CRYSTAL_RESONANCE("Glacier Crystal Pulse", "رزونانس کریستال یخی", "رنين الكريستال الجليدي"),
    GRAVITY_SINGULARITY("Quantum Gravity Well", "میدان گرانش کوانتومی", "حقل الجاذبية الكمومية"),
    HYPERDRIVE_WARP("Hyperdrive Warp Tunnel", "تونل پرش نوری هایپردرایو", "نفق القفز الضوئي"),
    NEON_PRISM_MATRIX("3D Prism Cyber Matrix", "ماتریکس منشوری سه‌بعدی", "مصفوفة المنشور ثلاثي الأبعاد"),
    CYBER_PULSE_TURBINE("Turbo Reactor Turbine", "توربین رآکتور سایبری", "توربين المفاعل السيبراني"),
    AURORA_RIBBON_SILK("Silk Aurora Ribbon Waves", "امواج ابریشمی شفق قطبی", "أمواج الشفق الحريرية"),
    NEURAL_SYNAPSE_GRID("Neural Synapse Grid", "شبکه سیناپس عصبی", "شبكة المشابك العصبية"),
    HOLOGRAPHIC_SHIELD_RING("Holographic Tech Shield", "سپر هولوگرافیک محافظ", "درع هولوغرافي تقني"),
    CELESTIAL_SOLAR_CORONA("Celestial Solar Corona", "تاج خورشیدی کیهانی", "الإكليل الشمسي الكوني"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class WaveColorMode(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
    val colorHex: Long,
) {
    AUTO_STATUS("Auto Status Color", "هماهنگ با وضعیت اتصال", "تلقائي حسب الحالة", 0L),
    SUNSET_ORANGE("Foxy Ice Cyan", "آبی یخی درخشان فاکسی", "سماوي فوكسي الجليدي", 0xFF00D2FF),
    ROYAL_BLUE("Electric Royal Blue", "آبی الکتریک سلطنتی", "أزرق ملكي", 0xFF2563EB),
    EMERALD_MINT("Glacier Cyan Mint", "فیروزه‌ای یخی نئون", "سماوي جليدي نيون", 0xFF38F9D7),
    CYBER_VIOLET("Deep Indigo Sapphire", "نیلی یاقوتی مدرن", "نيلي ياقوتي حديث", 0xFF6366F1),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class DynamicIslandLedStyle(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    CYBER_RAINBOW("Prism Flow (Blue • Cyan • Violet)", "جریان منشوری (آبی • فیروزه‌ای • بنفش)", "تدرج المنشور (أزرق • سماوي • بنفسجي)"),
    FOXY_FIRE("Aurora Flow (Blue • Violet • Rose)", "جریان شفق (آبی • بنفش • سرخابی)", "موج الشفق (أزرق • بنفسجي • وردي)"),
    EMERALD_PULSE("Cyber Emerald (Blue • Cyan • Emerald)", "زمرد سایبری (آبی • فیروزه‌ای • سبز زمردی)", "زمرد سيبراني (أزرق • سماوي • أخضر زمردي)"),
    POLICE_STROBE("Dual Cobalt & Ice Strobe", "پالس دوگانه کبالت و یخی", "نبض الكوبالت والجليد"),
    SOLAR_GOLD("Solar Supernova (Gold • Coral • Violet)", "ابرنواختر طلایی (طلایی • مرجانی • بنفش)", "سوبرنوفا ذهبي (ذهبي • مرجاني • بنفسجي)"),
    AURORA_BOREALIS("Full Prism Spectrum (360° Rainbow)", "طیف منشور کامل (چند رنگ ۳۶۰ درجه)", "طيف المنشور الكامل (٣٦٠ درجة)"),
    OCEAN_BIOLUME("Ocean Bioluminescence", "درخشش اعماق اقیانوس", "توهج أعماق المحيط"),
    ROYAL_NEBULA("Galaxy Indigo Nebula", "سحابی نیلی کهکشانی", "سديم المجرة النيلي"),
    CYBERPUNK_SYNTH("Cyber Neon Blue", "نئون آبی سایبری", "نيون أزرق سيبراني"),
    LIQUID_MERCURY("Liquid Chrome Silver", "نقره‌ای جیوه مایع", "فضة الكروم السائل"),
    ARCTIC_FROST("Arctic Ice Crystal", "کریستال یخی قطبی", "كريستال الجليد القطبي"),
    SUNSET_MIAMI("Deep Horizon Blue", "افق آبی اقیانوسی", "أفق المحيط الأزرق"),
    QUANTUM_PLASMA("Quantum Plasma Beam", "پرتو پلاسمای کوانتومی", "شعاع البلازما الكمومي"),
    SAKURA_BLOSSOM("Pure Starlight White", "درخشش سفید ستاره‌ای", "توهج النجوم الأبيض"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class DynamicIslandHaloColorMode(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    MULTI_COLOR(
        "Animated multi-color gradient",
        "گرادیان چندرنگ متحرک",
        "تدرج متعدد الألوان متحرك",
    ),
    SINGLE_COLOR(
        "Single Solid Color",
        "تک رنگی خالص (انتخاب رنگ دلخواه)",
        "لون واحد ثابت",
    ),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class DynamicIslandHaloRotationType(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    CLOCKWISE_SWEEP(
        "Clockwise orbit",
        "چرخش پیوستهٔ ساعتگرد",
        "دوران مستمر باتجاه عقارب الساعة",
    ),
    COUNTER_CLOCKWISE_SWEEP(
        "Counter-Clockwise Orbit",
        "چرخش پادساعتگرد ممتد",
        "دوران عكس عقارب الساعة",
    ),
    DUAL_COMET_ORBIT(
        "Dual Luminous Comets",
        "دو دنباله‌دار نوری متقابل",
        "مذنبان ضوئيان متقابلان",
    ),
    BREATHING_PENDULUM(
        "Pendulum Wave & Breathe",
        "موج رفت‌وبرگشتی و تنفسی",
        "موجة متأرجحة وتنفس ضوئي",
    ),
    PULSE_AURA_RING(
        "Shadow Halo Pulse",
        "تپش هاله سایه‌ای دور داینامیک",
        "نبض الهالة المحيطية",
    ),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class DynamicIslandWaveStyle(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    SIGNAL_TOWER("Signal Tower", "موج دکل مخابراتی", "موجة برج الإشارة"),
    EQUALIZER_BARS("Audio Equalizer", "موج اکولایزر صوتی", "موازن الصوت المتحرك"),
    SINE_WAVE("Harmonic Sine Wave", "موج سینوسی روان", "موجة جيبية انسيابية"),
    RADAR_PULSE("Radar Sonar Rings", "پالس راداری دایره‌ای", "نبض الرادار الدائري"),
    ORBIT_DOTS("Orbital Particles", "ذرات چرخشی مدار", "جسيمات مدارية متحركة"),
    HEARTBEAT_WAVE("Live Heartbeat", "موج ضربان قلب", "موجة نبض القلب"),
    DUAL_HELIX("DNA Double Helix", "موج مارپیچ دوگانه", "موجة الحلزون المزدوج"),
    CYBER_SPECTRUM("Studio Spectrum", "آنالایزر طیفی استودیو", "محلل الطيف الاحترافي"),
    SONIC_RIPPLE("Sonic Concentric", "موج صوتی متمرکز", "تموجات صوتية متمركزة"),
    LASER_MATRIX("Laser Matrix Pulse", "ماتریکس لیزری زنده", "نبض مصفوفة الليزر"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

enum class ConnectionProtocolMode(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
    val descriptionEn: String,
    val descriptionFa: String,
    val descriptionAr: String,
    val isSupported: Boolean = true,
) {
    AUTO(
        label = "Smart Auto · H3/H2",
        persianLabel = "خودکار هوشمند · H3/H2",
        arabicLabel = "تلقائي ذكي · H3/H2",
        descriptionEn = "Races HTTP/3 QUIC first and automatically falls back to HTTP/2 TCP/TLS + MASQUE.",
        descriptionFa = "ابتدا با HTTP/3 QUIC متصل می‌شود و در صورت محدودیت شبکه خودکار به HTTP/2 + MASQUE برمی‌گردد.",
        descriptionAr = "يتصل عبر HTTP/3 QUIC أولاً ويتراجع تلقائياً إلى HTTP/2 + MASQUE عند الحاجة.",
        isSupported = true,
    ),
    HTTP2_TCP(
        label = "HTTP/2 · TCP/TLS + MASQUE",
        persianLabel = "HTTP/2 · TCP/TLS + MASQUE",
        arabicLabel = "HTTP/2 · TCP/TLS + MASQUE",
        descriptionEn = "Multiplexed HTTP/2 over TLS/TCP with RFC 9298 connect-udp MASQUE support.",
        descriptionFa = "اتصال چندجریانی پایدار HTTP/2 روی TLS/TCP با پشتیبانی کامل MASQUE UDP.",
        descriptionAr = "اتصال HTTP/2 متعدد التدفقات عبر TLS/TCP مع دعم MASQUE UDP.",
        isSupported = true,
    ),
    HTTP3_QUIC(
        label = "HTTP/3 · QUIC + MASQUE",
        persianLabel = "HTTP/3 · QUIC + MASQUE",
        arabicLabel = "HTTP/3 · QUIC + MASQUE",
        descriptionEn = "Low-latency HTTP/3 over QUIC/UDP with native MASQUE datagrams and automatic H2 fallback.",
        descriptionFa = "پروتکل پرسرعت HTTP/3 بر پایه QUIC/UDP با تاخیر بسیار پایین و پشتیبانی MASQUE.",
        descriptionAr = "بروتوكول HTTP/3 فائق السرعة عبر QUIC/UDP مع دعم MASQUE.",
        isSupported = true,
    ),
    HTTP11_TLS(
        label = "HTTP/1.1 · TLS + Fragment",
        persianLabel = "HTTP/1.1 · TLS + فرگمنت",
        arabicLabel = "HTTP/1.1 · TLS + تجزئة",
        descriptionEn = "Direct HTTP/1.1 CONNECT tunnel over TLS/TCP with record/packet fragmentation for strict networks.",
        descriptionFa = "تانل مستقیم HTTP/1.1 CONNECT روی TLS همراه با تکه‌تکه‌سازی بسته برای شبکه‌های بسیار محدود.",
        descriptionAr = "نفق HTTP/1.1 CONNECT مباشر عبر TLS مع تجزئة الحزم للشبكات المقيدة بشدة.",
        isSupported = true,
    ),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn
        AppLanguage.FA -> descriptionFa
        AppLanguage.AR -> descriptionAr
    }

    val shortBadge: String
        get() = when (this) {
            AUTO -> "AUTO (H3/H2)"
            HTTP2_TCP -> "HTTP/2"
            HTTP3_QUIC -> "H3/QUIC"
            HTTP11_TLS -> "HTTP/1.1"
        }

    /** Transport identifiers used by the unified sing-box MASQUE core. */
    val coreTransport: String
        get() = when (this) {
            AUTO -> "auto"
            HTTP3_QUIC -> "h3"
            HTTP2_TCP -> "h2"
            HTTP11_TLS -> "h1"
        }

    companion object {
        val selectableEntries: List<ConnectionProtocolMode>
            get() = entries.filter { it.isSupported }

        fun fromStored(raw: String?): ConnectionProtocolMode {
            val matched = entries.firstOrNull { it.name == raw }
            return if (matched != null && matched.isSupported) matched else AUTO
        }
    }
}

enum class ConnectionProfile(
    val labelEn: String,
    val labelFa: String,
    val labelAr: String,
    val descriptionEn: String,
    val descriptionFa: String,
    val descriptionAr: String,
) {
    STABLE("Stable", "پایدار", "مستقر", "Prioritizes recovery and keep-alive checks.", "بازیابی و بررسی زنده‌بودن تونل را در اولویت می‌گذارد.", "يعطي الأولوية للاستعادة وفحوصات بقاء النفق."),
    FAST("Fast", "سریع", "سريع", "Uses responsive health checks without battery throttling.", "بررسی‌های سریع‌تر را بدون محدودکردن مصرف اجرا می‌کند.", "يستخدم فحوصات سريعة دون خفض الأداء."),
    LOW_DATA("Low data & battery", "کم‌مصرف", "توفير البيانات والبطارية", "Reduces telemetry while keeping the tunnel alive.", "پایش و آمار سرعت را کم می‌کند و تونل را فعال نگه می‌دارد.", "يقلل القياس مع إبقاء النفق فعالاً.");

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.FA -> labelFa
        AppLanguage.AR -> labelAr
    }

    fun localizedDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn
        AppLanguage.FA -> descriptionFa
        AppLanguage.AR -> descriptionAr
    }
}

enum class DynamicIslandDoubleTapAction(
    val labelEn: String,
    val labelFa: String,
    val labelAr: String,
) {
    EXPAND("Expand / Collapse", "باز و بسته کردن پنل", "فتح وإغلاق اللوحة"),
    TOGGLE_VPN("Quick Connect / Disconnect", "قطع و وصل سریع فیلترشکن", "اتصال أو قطع سريع"),
    OPEN_SERVERS("Open Server List", "باز کردن لیست سرورها", "فتح قائمة الخوادم"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.FA -> labelFa
        AppLanguage.AR -> labelAr
    }
}

enum class DynamicIslandIndicatorStyle(
    val labelEn: String,
    val labelFa: String,
    val labelAr: String,
) {
    TOWER_WAVE("Tower Wave", "موج دکل زنده", "موجة البرج"),
    PULSE_ORB("Pulse Orb", "گوی نوری تپنده", "الكرة النابضة"),
    RADAR_SWEEP("Radar Sweep", "اسکنر راداری", "ماسح الرادار"),
    EQUALIZER_BARS("Equalizer", "اکولایزر شبکه", "موازن الشبكة"),
    SHIELD_GUARD("Cyber Shield", "سپر امنیتی", "الدرع الأمني"),
    SIGNAL_BARS("Signal Antenna", "آنتن سیگنال", "أبراج الإشارة"),
    MINIMAL_RING("Minimal Ring", "حلقه نئون", "حلقة نيون"),
    CYBER_FOX_PULSE("Cyber Fox Eye", "چشم روباه آبی", "عين الثعلب الأزرق"),
    DNA_HELIX_MINI("Quantum Helix", "مارپیچ کوانتومی", "الحلزون الكمومي"),
    PACKET_MATRIX("Packet Matrix", "ماتریکس بسته", "مصفوفة الحزم"),
    SONIC_HEARTBEAT("Tunnel Heartbeat", "ضربان تونل", "نبض النفق"),
    ORBIT_SATELLITE("Satellite Orbit", "مدار ماهواره‌ای", "مدار الأقمار"),
    LASER_SCANNER("Laser Cyber Scanner", "اسکنر لیزری سایبری", "الماسح الليزري"),
    VORTEX_SPIRAL("Quantum Vortex Spiral", "مارپیچ گرداب کوانتومی", "دوامة الفورتكس"),
    INFINITY_LOOP("Neon Infinity Loop", "حلقه بی‌نهایت نئون", "حلقة اللانهاية"),
    CYBER_PRISM_CORE("Prism Energy Core", "هسته منشوری انرژی", "نواة المنشور"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.FA -> labelFa
        AppLanguage.AR -> labelAr
    }
}

enum class DynamicIslandSurfaceMode(
    val labelEn: String,
    val labelFa: String,
    val labelAr: String,
) {
    OBSIDIAN_MATTE("Obsidian Matte", "مشکی مات ابسیدین", "أسود مطفي"),
    CYBER_BLUE_GLASS("Cyber Blue Glass", "شیشه‌ای آبی فاکسی", "زجاجي أزرق"),
    PURE_OLED_BLACK("Pure OLED Black", "مشکی خالص اولد", "أسود أوليد نقي"),
    ARCTIC_FROST("Arctic Ice Contrast", "یخی و سفید مینیمال", "جليدي أبيض"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.FA -> labelFa
        AppLanguage.AR -> labelAr
    }
}

enum class DynamicIslandMotionStyle(
    val label: String,
    val persianLabel: String,
    val arabicLabel: String,
) {
    LIQUID_SPRING("Liquid Silk 120Hz", "فیزیک مایع ابریشمی ۱۲۰ هرتز", "فيزياء الحرير السائل ١٢٠ هرتز"),
    ZERO_GRAVITY("Zero-Gravity Float", "شناور بی‌وزنی فضایی", "طفو انعدام الجاذبية"),
    BREATHING_HALO("Breathing Aura Halo", "تنفس نوری آرامش‌بخش", "توهج التنفس الهادئ"),
    SONIC_WAVE_SYNC("Sonic Wave Sync", "موج صوتی هماهنگ با ترافیک", "موجة صوتية متزامنة"),
    HEARTBEAT_PULSE("Live Heartbeat Pulse", "ضربان قلب زنده", "نبض القلب الحي"),
    PRISM_SHIMMER("Crystal Prism Shimmer", "درخشش منشوری کریستال", "بريق المنشور الكريستالي"),
    MAGNETIC_ELASTIC("Magnetic Elastic Morph", "کشسانی مغناطیسی نرم", "مرونة مغناطيسية ناعمة"),
    STARLIGHT_ORBIT("Starlight Orbital", "مدار ستاره‌ای درخشان", "مدار النجوم المضيئة"),
    CYBER_GLITCH_PULSE("Cyber Matrix Pulse", "پالس ماتریکس سایبری", "نبض المصفوفة السيبرانية"),
    AURORA_WAVE_FLOW("Aurora Ribbon Flow", "موج شفق قطبی روان", "تدفق الشفق القطبي"),
    GRAVITY_PENDULUM("Smooth Pendulum Swing", "نوسان آونگی نرم", "تأرجح البندول الناعم"),
    QUANTUM_PARTICLES("Quantum Energy Field", "میدان ذرات کوانتومی", "حقل الجسيمات الكمومية"),
    CRYO_FROST_GLIDE("Cryo Ice Glide", "شناوری یخی سایبری", "انزلاق الجليد السيبراني"),
    ELECTRIC_COBALT_FLUX("Electric Cobalt Flux", "شار مغناطیسی آبی", "تدفق الكوبالت الكهربائي"),
    HYPER_WARP_DRIVE("Hyper Warp Drive", "موج پرش کیهانی", "محرك القفز الفضائي"),
    LIQUID_NEON_RIPPLE("Liquid Neon Ripple", "تموج نئون مایع", "تموج النيون السائل"),
    CYBER_TURBINE_SPIN("Cyber Turbine Spin", "چرخش توربین سایبری", "دوران التوربين السيبراني"),
    ASTRAL_NOVA_PULSE("Astral Nova Pulse", "تپش اخترنواختر", "نبض النوفا النجمي"),
    ;

    fun localizedLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> label
        AppLanguage.FA -> persianLabel
        AppLanguage.AR -> arabicLabel
    }
}

internal fun computeEffectiveMtu(
    gamingModeEnabled: Boolean,
    mtuModeAuto: Boolean,
    configuredMtu: Int,
    defaultMtu: Int = 1400,
): Int = when {
    !mtuModeAuto -> configuredMtu.coerceIn(1280, 1500)
    gamingModeEnabled -> 1360
    else -> defaultMtu
}

class SettingsStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("foxyvpn_settings", Context.MODE_PRIVATE)
    private val _islandConfigRevision = MutableStateFlow(0)
    val islandConfigRevision: StateFlow<Int> = _islandConfigRevision

    private val credentialsPrefs by lazy {
        createSafeEncryptedPrefs(appContext, "foxyvpn_settings_credentials")
    }

    var appLanguage: AppLanguage
        get() {
            val saved = prefs.getString(KEY_APP_LANGUAGE, null)
            return AppLanguage.entries.firstOrNull { it.name == saved || it.code == saved }
                ?: when (Locale.getDefault().language) {
                    "fa" -> AppLanguage.FA
                    "ar" -> AppLanguage.AR
                    else -> AppLanguage.EN
                }
        }
        set(value) {
            prefs.edit().putString(KEY_APP_LANGUAGE, value.name).apply()
            _islandConfigRevision.value++
        }

    var themeMode: ThemeMode
        get() = ThemeMode.entries.firstOrNull {
            it.name == prefs.getString(KEY_THEME_MODE, null)
        } ?: ThemeMode.SYSTEM
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
            QuickConnectWidgetProvider.updateAll(appContext, FoxyVpnService.state.value)
            _islandConfigRevision.value++
        }

    var appColorTheme: FoxyColorTheme
        get() = FoxyColorTheme.entries.firstOrNull {
            it.name == prefs.getString(KEY_APP_COLOR_THEME, null)
        } ?: FoxyColorTheme.CYBER_FOX
        set(value) {
            prefs.edit().putString(KEY_APP_COLOR_THEME, value.name).apply()
            _islandConfigRevision.value++
        }

    var appCustomAccentArgb: Long?
        get() = if (prefs.contains(KEY_APP_CUSTOM_ACCENT_ARGB)) prefs.getLong(KEY_APP_CUSTOM_ACCENT_ARGB, 0L) else null
        set(value) {
            val editor = prefs.edit()
            if (value == null) {
                editor.remove(KEY_APP_CUSTOM_ACCENT_ARGB)
            } else {
                editor.putLong(KEY_APP_CUSTOM_ACCENT_ARGB, 0xFF000000L or (value and 0x00FFFFFFL))
            }
            editor.apply()
            _islandConfigRevision.value++
        }

    var appCustomPalette: CustomAppPalette?
        get() = CustomAppPalette.decode(prefs.getString(KEY_APP_CUSTOM_PALETTE, null))
        set(value) {
            val editor = prefs.edit()
            if (value == null) editor.remove(KEY_APP_CUSTOM_PALETTE)
            else editor.putString(KEY_APP_CUSTOM_PALETTE, value.encode())
            editor.apply()
            _islandConfigRevision.value++
        }

    var appUiDesignStyle: FoxyUiDesignStyle
        get() = FoxyUiDesignStyle.PIXEL_GOOGLE
        set(@Suppress("UNUSED_PARAMETER") value) {
            // Pixel UI is the single supported application shell.
            prefs.edit().putString(KEY_APP_UI_DESIGN_STYLE, FoxyUiDesignStyle.PIXEL_GOOGLE.name).apply()
            _islandConfigRevision.value++
        }

    var uiDesignStyle: FoxyUiDesignStyle
        get() = appUiDesignStyle
        set(value) { appUiDesignStyle = value }

    var heroWaveAnimationStyle: HeroWaveAnimationStyle
        get() = HeroWaveAnimationStyle.entries.firstOrNull {
            it.name == prefs.getString(KEY_HERO_WAVE_ANIM_STYLE, null)
        } ?: HeroWaveAnimationStyle.HARMONIC_ORBIT
        set(value) {
            prefs.edit().putString(KEY_HERO_WAVE_ANIM_STYLE, value.name).apply()
            _islandConfigRevision.value++
        }

    var waveSpeedMultiplier: Float
        get() = runCatching { prefs.getFloat("wave_speed_multiplier", 1.0f) }.getOrDefault(1.0f).coerceIn(0.4f, 2.5f)
        set(value) {
            prefs.edit().putFloat("wave_speed_multiplier", value.coerceIn(0.4f, 2.5f)).apply()
            _islandConfigRevision.value++
        }

    var waveAmplitudeScale: Float
        get() = runCatching { prefs.getFloat("wave_amplitude_scale", 1.0f) }.getOrDefault(1.0f).coerceIn(0.5f, 1.8f)
        set(value) {
            prefs.edit().putFloat("wave_amplitude_scale", value.coerceIn(0.5f, 1.8f)).apply()
            _islandConfigRevision.value++
        }

    var waveGlowEnabled: Boolean
        get() = prefs.getBoolean("wave_glow_enabled", true)
        set(value) {
            prefs.edit().putBoolean("wave_glow_enabled", value).apply()
            _islandConfigRevision.value++
        }

    var waveColorMode: WaveColorMode
        get() = WaveColorMode.entries.firstOrNull {
            it.name == prefs.getString("wave_color_mode", null)
        } ?: WaveColorMode.AUTO_STATUS
        set(value) {
            prefs.edit().putString("wave_color_mode", value.name).apply()
            _islandConfigRevision.value++
        }

    var connectionProtocolMode: ConnectionProtocolMode
        get() {
            val raw = ConnectionProtocolMode.entries.firstOrNull {
                it.name == prefs.getString("connection_protocol_mode", null)
            } ?: ConnectionProtocolMode.AUTO
            return if (raw.isSupported) raw else ConnectionProtocolMode.AUTO
        }
        set(value) {
            val safeValue = if (value.isSupported) value else ConnectionProtocolMode.AUTO
            prefs.edit().putString("connection_protocol_mode", safeValue.name).apply()
            _islandConfigRevision.value++
        }

    var connectionProfile: ConnectionProfile
        get() = ConnectionProfile.entries.firstOrNull {
            it.name == prefs.getString(KEY_CONNECTION_PROFILE, null)
        } ?: ConnectionProfile.STABLE
        set(value) {
            prefs.edit().putString(KEY_CONNECTION_PROFILE, value.name).apply()
            when (value) {
                ConnectionProfile.STABLE -> {
                    autoReconnectEnabled = true
                    keepAliveEnabled = true
                    batterySaverModeEnabled = false
                }
                ConnectionProfile.FAST -> {
                    autoReconnectEnabled = true
                    keepAliveEnabled = true
                    batterySaverModeEnabled = false
                }
                ConnectionProfile.LOW_DATA -> {
                    autoReconnectEnabled = true
                    keepAliveEnabled = true
                    batterySaverModeEnabled = true
                }
            }
            _islandConfigRevision.value++
        }

    var adBlockEnabled: Boolean
        get() = prefs.getBoolean("ad_block_enabled", false)
        set(value) {
            prefs.edit().putBoolean("ad_block_enabled", value).apply()
        }

    /** User preference; SMART never silently changes the saved manual preference. */
    var coreSelection: CoreSelection
        get() = CoreSelection.entries.firstOrNull {
            it.name == prefs.getString(KEY_CORE_SELECTION, null)
        } ?: CoreSelection.CORE_A
        set(value) {
            prefs.edit().putString(KEY_CORE_SELECTION, value.name).apply()
            _islandConfigRevision.value++
        }

    fun selectedCoreId(): CoreId = when (coreSelection) {
        CoreSelection.CORE_A -> CoreId.CORE_A
        CoreSelection.CORE_B_H2_HEV -> CoreId.CORE_B_H2_HEV
        CoreSelection.SMART -> CoreId.SMART
    }

    var autoSwitchAccountByQuotaEnabled: Boolean
        get() = prefs.getBoolean("auto_switch_account_by_quota", true)
        set(value) {
            prefs.edit().putBoolean("auto_switch_account_by_quota", value).apply()
        }

    var autoSwitchQuotaThresholdPercent: Int
        get() = prefs.getInt("auto_switch_quota_threshold_percent", 85).coerceIn(50, 99)
        set(value) {
            prefs.edit().putInt("auto_switch_quota_threshold_percent", value.coerceIn(50, 99)).apply()
        }

    var dynamicIslandWaveStyle: DynamicIslandWaveStyle
        get() = DynamicIslandWaveStyle.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_wave_style", null)
        } ?: DynamicIslandWaveStyle.SIGNAL_TOWER
        set(value) {
            prefs.edit().putString("dynamic_island_wave_style", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandBgAlphaPercent: Int
        get() = prefs.getInt("dynamic_island_bg_alpha_percent", 96).coerceIn(60, 100)
        set(value) {
            prefs.edit().putInt("dynamic_island_bg_alpha_percent", value.coerceIn(60, 100)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandAutoCollapseSec: Int
        get() = prefs.getInt("dynamic_island_auto_collapse_sec", 0).coerceIn(0, 60)
        set(value) {
            prefs.edit().putInt("dynamic_island_auto_collapse_sec", value.coerceIn(0, 60)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandCompactTelemetry: Boolean
        get() = prefs.getBoolean("dynamic_island_compact_telemetry", false)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_compact_telemetry", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowProtocolBadge: Boolean
        get() = prefs.getBoolean("dynamic_island_show_protocol_badge", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_protocol_badge", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandHapticEnabled: Boolean
        get() = prefs.getBoolean("dynamic_island_haptic_enabled", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_haptic_enabled", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandLedCustomColorHex: Long
        get() = prefs.getLong("dynamic_island_led_custom_color_hex", 0L)
        set(value) {
            prefs.edit().putLong("dynamic_island_led_custom_color_hex", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandCollapsedHeightDp: Int
        get() = prefs.getInt("dynamic_island_collapsed_height_dp", 44).coerceIn(32, 56)
        set(value) {
            prefs.edit().putInt("dynamic_island_collapsed_height_dp", value.coerceIn(32, 56)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandExpandedWidthPercent: Int
        get() = prefs.getInt("dynamic_island_expanded_width_percent", 96).coerceIn(82, 100)
        set(value) {
            prefs.edit().putInt("dynamic_island_expanded_width_percent", value.coerceIn(82, 100)).apply()
            _islandConfigRevision.value++
        }

    /** Independent visual width scale. Kept separate from the layout preset for precise tuning. */
    var dynamicIslandWidthScalePercent: Int
        get() = prefs.getInt("dynamic_island_width_scale_percent", 100).coerceIn(75, 135)
        set(value) {
            prefs.edit().putInt("dynamic_island_width_scale_percent", value.coerceIn(75, 135)).apply()
            _islandConfigRevision.value++
        }

    /** Independent visual height scale so tall phones and compact layouts can be tuned separately. */
    var dynamicIslandHeightScalePercent: Int
        get() = prefs.getInt("dynamic_island_height_scale_percent", 100).coerceIn(75, 135)
        set(value) {
            prefs.edit().putInt("dynamic_island_height_scale_percent", value.coerceIn(75, 135)).apply()
            _islandConfigRevision.value++
        }

    /** Controls the responsiveness of open/close morphing without changing the visual size. */
    var dynamicIslandAnimationSpeedPercent: Int
        get() = prefs.getInt("dynamic_island_animation_speed_percent", 100).coerceIn(70, 140)
        set(value) {
            prefs.edit().putInt("dynamic_island_animation_speed_percent", value.coerceIn(70, 140)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandFontScalePercent: Int
        get() = prefs.getInt("dynamic_island_font_scale_percent", 100).coerceIn(80, 125)
        set(value) {
            prefs.edit().putInt("dynamic_island_font_scale_percent", value.coerceIn(80, 125)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandBorderWidthDp: Float
        get() = runCatching { prefs.getFloat("dynamic_island_border_width_dp", 1.2f) }.getOrDefault(1.2f).coerceIn(0.5f, 3.0f)
        set(value) {
            prefs.edit().putFloat("dynamic_island_border_width_dp", value.coerceIn(0.5f, 3.0f)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShadowDepthDp: Int
        get() = prefs.getInt("dynamic_island_shadow_depth_dp", 8).coerceIn(0, 20)
        set(value) {
            prefs.edit().putInt("dynamic_island_shadow_depth_dp", value.coerceIn(0, 20)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandSpringDampingPercent: Int
        get() = prefs.getInt("dynamic_island_spring_damping_percent", 82).coerceIn(55, 95)
        set(value) {
            prefs.edit().putInt("dynamic_island_spring_damping_percent", value.coerceIn(55, 95)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandSpringStiffness: Int
        get() = prefs.getInt("dynamic_island_spring_stiffness", 340).coerceIn(180, 600)
        set(value) {
            prefs.edit().putInt("dynamic_island_spring_stiffness", value.coerceIn(180, 600)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandQuickAppIconSizeDp: Int
        get() = prefs.getInt("dynamic_island_quick_app_icon_size_dp", 36).coerceIn(28, 48)
        set(value) {
            prefs.edit().putInt("dynamic_island_quick_app_icon_size_dp", value.coerceIn(28, 48)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowAppNames: Boolean
        get() = prefs.getBoolean("dynamic_island_show_app_names", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_app_names", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandDoubleTapAction: DynamicIslandDoubleTapAction
        get() = DynamicIslandDoubleTapAction.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_double_tap_action", null)
        } ?: DynamicIslandDoubleTapAction.EXPAND
        set(value) {
            prefs.edit().putString("dynamic_island_double_tap_action", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandIndicatorStyle: DynamicIslandIndicatorStyle
        get() = DynamicIslandIndicatorStyle.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_indicator_style", null)
        } ?: DynamicIslandIndicatorStyle.TOWER_WAVE
        set(value) {
            prefs.edit().putString("dynamic_island_indicator_style", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowJitterBadge: Boolean
        get() = prefs.getBoolean("dynamic_island_show_jitter_badge", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_jitter_badge", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowSecurityBadge: Boolean
        get() = prefs.getBoolean("dynamic_island_show_security_badge", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_security_badge", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowNetworkActions: Boolean
        get() = prefs.getBoolean("dynamic_island_show_network_actions", true)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_network_actions", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandSurfaceMode: DynamicIslandSurfaceMode
        get() = DynamicIslandSurfaceMode.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_surface_mode", null)
        } ?: DynamicIslandSurfaceMode.OBSIDIAN_MATTE
        set(value) {
            prefs.edit().putString("dynamic_island_surface_mode", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowQuickStyleDeck: Boolean
        get() = prefs.getBoolean("dynamic_island_show_quick_style_deck_v2", false)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_quick_style_deck_v2", value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowTelegramGamingActions: Boolean
        get() = prefs.getBoolean("dynamic_island_show_tg_gaming_actions_v2", false)
        set(value) {
            prefs.edit().putBoolean("dynamic_island_show_tg_gaming_actions_v2", value).apply()
            _islandConfigRevision.value++
        }

    var exitCheckEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXIT_CHECK, true)
        set(value) { prefs.edit().putBoolean(KEY_EXIT_CHECK, value).apply() }

    var mtuModeAuto: Boolean
        get() = prefs.getBoolean("mtu_mode_auto", true)
        set(value) {
            prefs.edit().putBoolean("mtu_mode_auto", value).apply()
            _islandConfigRevision.value++
        }

    var mtu: Int
        get() = prefs.getInt(KEY_CUSTOM_MTU, DEFAULT_MTU)
        set(value) {
            prefs.edit().putInt(KEY_CUSTOM_MTU, value.coerceIn(MIN_MTU, MAX_MTU)).apply()
            _islandConfigRevision.value++
        }

    val effectiveMtu: Int
        get() = computeEffectiveMtu(gamingModeEnabled, mtuModeAuto, mtu, DEFAULT_MTU)

    var gamingModeEnabled: Boolean
        get() = prefs.getBoolean("gaming_mode_enabled", false)
        set(value) {
            prefs.edit().putBoolean("gaming_mode_enabled", value).apply()
            _islandConfigRevision.value++
        }

    var telegramProxyModeEnabled: Boolean
        get() = prefs.getBoolean("telegram_proxy_mode_enabled", false)
        set(value) {
            prefs.edit().putBoolean("telegram_proxy_mode_enabled", value).apply()
            if (value) {
                // Force loopback binding so local Telegram proxy is never exposed on public networks
                socksBindAddress = DEFAULT_SOCKS_BIND_ADDRESS
            }
            _islandConfigRevision.value++
        }

    fun resetNetworkTuningToSafeDefaults() {
        prefs.edit()
            .putBoolean("mtu_mode_auto", true)
            .putInt(KEY_CUSTOM_MTU, DEFAULT_MTU)
            .putBoolean("gaming_mode_enabled", false)
            .putString(KEY_DNS_PROVIDER, DnsProvider.SYSTEM_DEFAULT.name)
            .putString(KEY_BOOTSTRAP_DNS_PROVIDER, BootstrapDnsProvider.AUTOMATIC.name)
            .putString(KEY_TLS_SPLIT_MODE, TlsSplitMode.OFF.name)
            .putString(KEY_CUSTOM_EDGE_ADDRESS, "")
            .putString(KEY_SOCKS_BIND_ADDRESS, DEFAULT_SOCKS_BIND_ADDRESS)
            .apply()
        _islandConfigRevision.value++
    }

    var cachedRealIp: String?
        get() = prefs.getString(KEY_CACHED_REAL_IP, null)
        set(value) { prefs.edit().putString(KEY_CACHED_REAL_IP, value).apply() }

    var cachedRealCountry: String?
        get() = prefs.getString(KEY_CACHED_REAL_COUNTRY, null)
        set(value) { prefs.edit().putString(KEY_CACHED_REAL_COUNTRY, value).apply() }

    var cachedQuotaRemaining: Long?
        get() = if (prefs.contains(KEY_CACHED_QUOTA_REMAINING)) prefs.getLong(KEY_CACHED_QUOTA_REMAINING, -1L).takeIf { it >= 0 } else null
        set(value) {
            if (value != null && value >= 0) prefs.edit().putLong(KEY_CACHED_QUOTA_REMAINING, value).apply()
            else prefs.edit().remove(KEY_CACHED_QUOTA_REMAINING).apply()
        }

    var cachedQuotaMax: Long?
        get() = if (prefs.contains(KEY_CACHED_QUOTA_MAX)) prefs.getLong(KEY_CACHED_QUOTA_MAX, -1L).takeIf { it >= 0 } else null
        set(value) {
            if (value != null && value >= 0) prefs.edit().putLong(KEY_CACHED_QUOTA_MAX, value).apply()
            else prefs.edit().remove(KEY_CACHED_QUOTA_MAX).apply()
        }

    var cachedIsUnlimited: Boolean
        get() = prefs.getBoolean(KEY_CACHED_IS_UNLIMITED, false)
        set(value) { prefs.edit().putBoolean(KEY_CACHED_IS_UNLIMITED, value).apply() }

    fun clearAccountSession() {
        prefs.edit()
            .remove(KEY_CACHED_REAL_IP)
            .remove(KEY_CACHED_REAL_COUNTRY)
            .remove(KEY_CACHED_QUOTA_REMAINING)
            .remove(KEY_CACHED_QUOTA_MAX)
            .remove(KEY_CACHED_IS_UNLIMITED)
            .apply()
    }

    var socksBindAddress: String
        get() = prefs.getString(KEY_SOCKS_BIND_ADDRESS, DEFAULT_SOCKS_BIND_ADDRESS) ?: DEFAULT_SOCKS_BIND_ADDRESS
        set(value) {
            prefs.edit().putString(KEY_SOCKS_BIND_ADDRESS, value.ifBlank { DEFAULT_SOCKS_BIND_ADDRESS }).apply()
        }

    var socksPort: Int
        get() = prefs.getInt(KEY_SOCKS_PORT, DEFAULT_SOCKS_PORT)
        set(value) { prefs.edit().putInt(KEY_SOCKS_PORT, value.coerceIn(1, 65_535)).apply() }

    var httpProxyEnabled: Boolean
        get() = prefs.getBoolean(KEY_HTTP_PROXY_ENABLED, false)
        set(value) { prefs.edit().putBoolean(KEY_HTTP_PROXY_ENABLED, value).apply() }

    var httpProxyPort: Int
        get() = prefs.getInt(KEY_HTTP_PROXY_PORT, DEFAULT_HTTP_PROXY_PORT)
        set(value) { prefs.edit().putInt(KEY_HTTP_PROXY_PORT, value.coerceIn(1, 65_535)).apply() }

    var dnsProvider: DnsProvider
        get() = DnsProvider.entries.firstOrNull {
            it.name == prefs.getString(KEY_DNS_PROVIDER, null)
        } ?: DnsProvider.SYSTEM_DEFAULT
        set(value) { prefs.edit().putString(KEY_DNS_PROVIDER, value.name).apply() }

    var customDohUrl: String
        get() = prefs.getString(KEY_CUSTOM_DOH_URL, "") ?: ""
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isNotEmpty() && !isValidDohUrl(trimmed)) return
            prefs.edit().putString(KEY_CUSTOM_DOH_URL, trimmed).apply()
        }

    val effectiveDohUrl: String?
        get() = when {
            dnsProvider == DnsProvider.CUSTOM -> customDohUrl.takeIf { isValidDohUrl(it) }
            dnsProvider != DnsProvider.SYSTEM_DEFAULT -> dnsProvider.dohUrl
            adBlockEnabled -> DnsProvider.ADGUARD.dohUrl
            else -> null
        }

    var bootstrapDnsProvider: BootstrapDnsProvider
        get() = BootstrapDnsProvider.entries.firstOrNull {
            it.name == prefs.getString(KEY_BOOTSTRAP_DNS_PROVIDER, null)
        } ?: BootstrapDnsProvider.AUTOMATIC
        set(value) { prefs.edit().putString(KEY_BOOTSTRAP_DNS_PROVIDER, value.name).apply() }

    var customBootstrapIp: String
        get() = prefs.getString(KEY_CUSTOM_BOOTSTRAP_IP, "") ?: ""
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isNotEmpty() && !isValidIpAddress(trimmed)) return
            prefs.edit().putString(KEY_CUSTOM_BOOTSTRAP_IP, trimmed).apply()
        }

    val effectiveBootstrapAddresses: List<String>
        get() = when (bootstrapDnsProvider) {
            BootstrapDnsProvider.CUSTOM -> {
                val ip = customBootstrapIp.trim()
                if (isValidIpAddress(ip)) listOf(ip) else BootstrapDnsProvider.AUTOMATIC.addresses
            }
            BootstrapDnsProvider.SYSTEM -> emptyList()
            else -> bootstrapDnsProvider.addresses
        }

    var proxyOnlyMode: Boolean
        get() = prefs.getBoolean(KEY_PROXY_ONLY_MODE, false)
        set(value) { prefs.edit().putBoolean(KEY_PROXY_ONLY_MODE, value).apply() }

    var customEdgeAddress: String
        get() = prefs.getString(KEY_CUSTOM_EDGE_ADDRESS, "") ?: ""
        set(value) {
            val normalized = value.trim().lowercase()
            if (normalized.isNotEmpty() && !isValidEdgeHost(normalized)) return
            prefs.edit().putString(KEY_CUSTOM_EDGE_ADDRESS, normalized).apply()
        }

    val effectiveCustomEdgeAddress: String?
        get() = customEdgeAddress.takeIf { isValidEdgeHost(it) }

    var upstreamProxyEnabled: Boolean
        get() = prefs.getBoolean(KEY_UPSTREAM_PROXY_ENABLED, false)
        set(value) { prefs.edit().putBoolean(KEY_UPSTREAM_PROXY_ENABLED, value).apply() }

    var upstreamProxyType: UpstreamProxyType
        get() = UpstreamProxyType.entries.firstOrNull {
            it.name == prefs.getString(KEY_UPSTREAM_PROXY_TYPE, null)
        } ?: UpstreamProxyType.SOCKS5
        set(value) { prefs.edit().putString(KEY_UPSTREAM_PROXY_TYPE, value.name).apply() }

    var upstreamProxyHost: String
        get() = prefs.getString(KEY_UPSTREAM_PROXY_HOST, "") ?: ""
        set(value) { prefs.edit().putString(KEY_UPSTREAM_PROXY_HOST, value.trim()).apply() }

    var upstreamProxyPort: Int
        get() = prefs.getInt(KEY_UPSTREAM_PROXY_PORT, DEFAULT_UPSTREAM_PROXY_PORT)
        set(value) { prefs.edit().putInt(KEY_UPSTREAM_PROXY_PORT, value.coerceIn(1, 65_535)).apply() }

    var upstreamProxyUsername: String
        get() = credentialsPrefs.getString(KEY_UPSTREAM_PROXY_USERNAME, "") ?: ""
        set(value) { credentialsPrefs.edit().putString(KEY_UPSTREAM_PROXY_USERNAME, value).apply() }

    var upstreamProxyPassword: String
        get() = credentialsPrefs.getString(KEY_UPSTREAM_PROXY_PASSWORD, "") ?: ""
        set(value) { credentialsPrefs.edit().putString(KEY_UPSTREAM_PROXY_PASSWORD, value).apply() }

    var excludedApps: Set<String>
        get() = prefs.getStringSet(KEY_EXCLUDED_APPS, emptySet())?.toSet() ?: emptySet()
        set(value) {
            prefs.edit().putStringSet(KEY_EXCLUDED_APPS, HashSet(value)).apply()
        }

    var includedApps: Set<String>
        get() = prefs.getStringSet("included_apps", emptySet())?.toSet() ?: emptySet()
        set(value) {
            prefs.edit().putStringSet("included_apps", HashSet(value)).apply()
        }

    var splitTunnelMode: SplitTunnelMode
        get() = SplitTunnelMode.entries.firstOrNull {
            it.name == prefs.getString("split_tunnel_mode", null)
        } ?: SplitTunnelMode.EXCLUDE
        set(value) {
            prefs.edit().putString("split_tunnel_mode", value.name).apply()
        }

    var tlsSplitMode: TlsSplitMode
        get() = TlsSplitMode.entries.firstOrNull {
            it.name == prefs.getString(KEY_TLS_SPLIT_MODE, null)
        } ?: TlsSplitMode.OFF
        set(value) { prefs.edit().putString(KEY_TLS_SPLIT_MODE, value.name).apply() }

    var utlsFingerprint: UtlsFingerprint
        get() = UtlsFingerprint.entries.firstOrNull {
            it.name == prefs.getString("utls_fingerprint", null) ||
                it.id == prefs.getString("utls_fingerprint", null)
        } ?: UtlsFingerprint.CHROME
        set(value) { prefs.edit().putString("utls_fingerprint", value.name).apply() }

    var fragmentFallbackDelay: String
        get() = prefs.getString("fragment_fallback_delay", "250ms")?.ifBlank { "250ms" } ?: "250ms"
        set(value) { prefs.edit().putString("fragment_fallback_delay", value.trim().ifBlank { "250ms" }).apply() }

    var tcpFastOpen: Boolean
        get() = prefs.getBoolean("tcp_fast_open", false)
        set(value) { prefs.edit().putBoolean("tcp_fast_open", value).apply() }

    var maxConcurrentStreams: Int
        get() = prefs.getInt("max_concurrent_streams", 128).coerceIn(16, 512)
        set(value) { prefs.edit().putInt("max_concurrent_streams", value.coerceIn(16, 512)).apply() }

    var ipv6Enabled: Boolean
        get() = prefs.getBoolean("ipv6_enabled", true)
        set(value) { prefs.edit().putBoolean("ipv6_enabled", value).apply() }

    var bypassLocalNetwork: Boolean
        get() = prefs.getBoolean("bypass_local_network", true)
        set(value) { prefs.edit().putBoolean("bypass_local_network", value).apply() }

    var dynamicIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_ENABLED, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandOverlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_OVERLAY_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_OVERLAY_ENABLED, value).apply()
            _islandConfigRevision.value++
        }

    /**
     * Manual 0% to 100% linear size slider for Dynamic Island.
     */
    var dynamicIslandScalePercent: Int
        get() = prefs.getInt(KEY_DYNAMIC_ISLAND_SCALE_PERCENT, 55).coerceIn(0, 100)
        set(value) {
            val clamped = value.coerceIn(0, 100)
            prefs.edit().putInt(KEY_DYNAMIC_ISLAND_SCALE_PERCENT, clamped).apply()
            // Sync preset enum for backwards compatibility
            val preset = when {
                clamped < 34 -> DynamicIslandSize.COMPACT
                clamped < 68 -> DynamicIslandSize.REGULAR
                else -> DynamicIslandSize.EXPANDED
            }
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_SIZE, preset.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandSize: DynamicIslandSize
        get() = DynamicIslandSize.entries.firstOrNull {
            it.name == prefs.getString(KEY_DYNAMIC_ISLAND_SIZE, null)
        } ?: DynamicIslandSize.REGULAR
        set(value) {
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_SIZE, value.name).apply()
            val mappedPercent = when (value) {
                DynamicIslandSize.COMPACT -> 20
                DynamicIslandSize.REGULAR -> 55
                DynamicIslandSize.EXPANDED -> 90
            }
            prefs.edit().putInt(KEY_DYNAMIC_ISLAND_SCALE_PERCENT, mappedPercent).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandCornerRadius: Int
        get() = prefs.getInt(KEY_DYNAMIC_ISLAND_CORNER_RADIUS, 28)
        set(value) {
            prefs.edit().putInt(KEY_DYNAMIC_ISLAND_CORNER_RADIUS, value.coerceIn(14, 38)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandPosition: DynamicIslandPosition
        get() = DynamicIslandPosition.entries.firstOrNull {
            it.name == prefs.getString(KEY_DYNAMIC_ISLAND_POSITION, null)
        } ?: DynamicIslandPosition.TOP_CENTER
        set(value) {
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_POSITION, value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandTheme: DynamicIslandTheme
        get() = DynamicIslandTheme.entries.firstOrNull {
            it.name == prefs.getString(KEY_DYNAMIC_ISLAND_THEME, null)
        } ?: DynamicIslandTheme.DARK
        set(value) {
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_THEME, value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowSpeed: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_SPEED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_SPEED, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowPing: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_PING, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_PING, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowFlag: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_FLAG, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_FLAG, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowTimer: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_TIMER, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_TIMER, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandAnimation: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_ANIMATION, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_ANIMATION, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandPinnedApps: Set<String>
        get() = prefs.getStringSet(KEY_DYNAMIC_ISLAND_PINNED_APPS, null)?.toSet() ?: DEFAULT_PINNED_ISLAND_APPS
        set(value) {
            prefs.edit().putStringSet(KEY_DYNAMIC_ISLAND_PINNED_APPS, HashSet(value)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowQuickApps: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_QUICK_APPS, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_QUICK_APPS, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandLedBorderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_LED_BORDER, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_LED_BORDER, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandLedStyle: DynamicIslandLedStyle
        get() = DynamicIslandLedStyle.entries.firstOrNull {
            it.name == prefs.getString(KEY_DYNAMIC_ISLAND_LED_STYLE, null)
        } ?: DynamicIslandLedStyle.CYBER_RAINBOW
        set(value) {
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_LED_STYLE, value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandLedSpeed: Float
        get() = runCatching { prefs.getFloat(KEY_DYNAMIC_ISLAND_LED_SPEED, 1.0f) }.getOrDefault(1.0f).coerceIn(0.3f, 3.0f)
        set(value) {
            prefs.edit().putFloat(KEY_DYNAMIC_ISLAND_LED_SPEED, value.coerceIn(0.3f, 3.0f)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandHaloColorMode: DynamicIslandHaloColorMode
        get() = DynamicIslandHaloColorMode.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_halo_color_mode", null)
        } ?: DynamicIslandHaloColorMode.MULTI_COLOR
        set(value) {
            prefs.edit().putString("dynamic_island_halo_color_mode", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandHaloRotationType: DynamicIslandHaloRotationType
        get() = DynamicIslandHaloRotationType.entries.firstOrNull {
            it.name == prefs.getString("dynamic_island_halo_rotation_type", null)
        } ?: DynamicIslandHaloRotationType.CLOCKWISE_SWEEP
        set(value) {
            prefs.edit().putString("dynamic_island_halo_rotation_type", value.name).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandHaloShadowDistanceDp: Float
        get() = runCatching { prefs.getFloat("dynamic_island_halo_shadow_distance_dp", 12f) }.getOrDefault(12f).coerceIn(2f, 28f)
        set(value) {
            prefs.edit().putFloat("dynamic_island_halo_shadow_distance_dp", value.coerceIn(2f, 28f)).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowTowerWave: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_TOWER_WAVE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_TOWER_WAVE, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowServerName: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_SERVER_NAME, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_SERVER_NAME, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowSessionData: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_SESSION_DATA, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_SESSION_DATA, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandShowIpBadge: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_SHOW_IP_BADGE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_SHOW_IP_BADGE, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandDraggable: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_ISLAND_DRAGGABLE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_ISLAND_DRAGGABLE, value).apply()
            _islandConfigRevision.value++
        }

    var dynamicIslandOffsetX: Float
        get() = runCatching { prefs.getFloat(KEY_DYNAMIC_ISLAND_OFFSET_X, 0f) }.getOrDefault(0f)
        set(value) {
            prefs.edit().putFloat(KEY_DYNAMIC_ISLAND_OFFSET_X, value).apply()
        }

    var dynamicIslandOffsetY: Float
        get() = runCatching { prefs.getFloat(KEY_DYNAMIC_ISLAND_OFFSET_Y, 0f) }.getOrDefault(0f)
        set(value) {
            prefs.edit().putFloat(KEY_DYNAMIC_ISLAND_OFFSET_Y, value).apply()
        }

    fun resetDynamicIslandPosition() {
        prefs.edit()
            .putFloat(KEY_DYNAMIC_ISLAND_OFFSET_X, 0f)
            .putFloat(KEY_DYNAMIC_ISLAND_OFFSET_Y, 0f)
            .apply()
        _islandConfigRevision.value++
    }

    // Scheduled Auto-Connect / Auto-Disconnect Timer
    var scheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply()
        }

    var scheduleStartHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_HOUR, 22).coerceIn(0, 23)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULE_START_HOUR, value.coerceIn(0, 23)).apply()
        }

    var scheduleStartMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_MINUTE, 0).coerceIn(0, 59)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULE_START_MINUTE, value.coerceIn(0, 59)).apply()
        }

    var scheduleEndHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_HOUR, 7).coerceIn(0, 23)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULE_END_HOUR, value.coerceIn(0, 23)).apply()
        }

    var scheduleEndMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_MINUTE, 0).coerceIn(0, 59)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULE_END_MINUTE, value.coerceIn(0, 59)).apply()
        }

    var serverListAutoRefresh: Boolean
        get() = prefs.getBoolean(KEY_SERVER_LIST_AUTO_REFRESH, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVER_LIST_AUTO_REFRESH, value).apply()

    var lastServerListRefreshEpochMs: Long
        get() = prefs.getLong(KEY_LAST_SERVER_LIST_REFRESH, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SERVER_LIST_REFRESH, value).apply()

    // Smart Idle Auto-Disconnect (Battery & Data Saver)
    var idleDisconnectEnabled: Boolean
        get() = prefs.getBoolean(KEY_IDLE_DISCONNECT_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IDLE_DISCONNECT_ENABLED, value).apply()
        }

    var idleTimeoutMinutes: Int
        get() = prefs.getInt(KEY_IDLE_TIMEOUT_MINUTES, 15).coerceIn(1, 180)
        set(value) {
            prefs.edit().putInt(KEY_IDLE_TIMEOUT_MINUTES, value.coerceIn(1, 180)).apply()
        }

    // Additional Smart Practical Options
    var sessionDataLimitEnabled: Boolean
        get() = prefs.getBoolean(KEY_SESSION_DATA_LIMIT_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SESSION_DATA_LIMIT_ENABLED, value).apply()
        }

    var sessionDataLimitMb: Int
        get() = prefs.getInt(KEY_SESSION_DATA_LIMIT_MB, 1024).coerceIn(50, 50_000)
        set(value) {
            prefs.edit().putInt(KEY_SESSION_DATA_LIMIT_MB, value.coerceIn(50, 50_000)).apply()
        }

    var smartPingAutoSwitch: Boolean
        get() = prefs.getBoolean(KEY_SMART_PING_AUTO_SWITCH, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SMART_PING_AUTO_SWITCH, value).apply()
        }

    var autoConnectOnLaunch: Boolean
        get() = prefs.getBoolean(KEY_AUTO_CONNECT_ON_LAUNCH, false)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_CONNECT_ON_LAUNCH, value).apply()
        }

    var stealthNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_STEALTH_NOTIFICATION_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_STEALTH_NOTIFICATION_ENABLED, value).apply()
        }

    var keepAliveEnabled: Boolean
        get() = prefs.getBoolean(KEY_KEEP_ALIVE_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_KEEP_ALIVE_ENABLED, value).apply()
        }

    var keepAliveIntervalSec: Int
        get() = prefs.getInt(KEY_KEEP_ALIVE_INTERVAL_SEC, 20).coerceIn(10, 120)
        set(value) {
            prefs.edit().putInt(KEY_KEEP_ALIVE_INTERVAL_SEC, value.coerceIn(10, 120)).apply()
        }

    var autoReconnectEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RECONNECT_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_RECONNECT_ENABLED, value).apply()
        }

    var dynamicIslandMotionStyle: DynamicIslandMotionStyle
        get() = DynamicIslandMotionStyle.entries.firstOrNull {
            it.name == prefs.getString(KEY_DYNAMIC_ISLAND_MOTION_STYLE, null)
        } ?: DynamicIslandMotionStyle.LIQUID_SPRING
        set(value) {
            prefs.edit().putString(KEY_DYNAMIC_ISLAND_MOTION_STYLE, value.name).apply()
            _islandConfigRevision.value++
        }

    /** Android network metered-status policy; this does not change carrier billing or link speed. */
    var unmeteredVpnModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_UNMETERED_VPN_MODE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_UNMETERED_VPN_MODE, value).apply()
            _islandConfigRevision.value++
        }

    /**
     * Smart Battery Saver / Eco Mode: Adaptively throttles background telemetry & ping polling
     * when traffic is idle or screen is off, releases continuous high-drain Wi-Fi locks,
     * and optimizes shader frame pacing.
     */
    var batterySaverModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_SAVER_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_BATTERY_SAVER_MODE, value).apply()
            _islandConfigRevision.value++
        }


    enum class DynamicIslandSize(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
        val heightDp: Int,
        val widthDp: Int,
    ) {
        COMPACT("Compact", "کوچک", "مصغّر", 36, 170),
        REGULAR("Regular", "متوسط", "قياسي", 44, 230),
        EXPANDED("Large", "بزرگ", "كبير", 52, 280),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }
    }

    enum class DynamicIslandPosition(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
    ) {
        TOP_CENTER("Top Center", "بالا وسط", "أعلى الوسط"),
        TOP_START("Top Start", "بالا ابتدا", "أعلى البداية"),
        TOP_END("Top End", "بالا انتها", "أعلى النهاية"),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }
    }

    enum class DynamicIslandTheme(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
    ) {
        DARK("Native Deep Glass", "شیشه‌ای تیره نیتیو", "زجاجي داكن أصلي"),
        NEUMORPHIC("Soft Translucent", "نیمه‌شفاف نرم", "شفاف ناعم"),
        ACCENT("Brand Accent Glow", "درخشش رنگ برند", "توهج لون العلامة"),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }
    }

    enum class SplitTunnelMode {
        EXCLUDE,
        INCLUDE,
    }

    enum class UtlsFingerprint(
        val id: String,
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
    ) {
        CHROME("chrome", "Chrome (Recommended)", "کروم (پیشنهادی)", "كروم (موصى به)"),
        FIREFOX("firefox", "Firefox", "فایرفاکس", "فايرفوكس"),
        SAFARI("safari", "Safari", "سافاری", "سفاري"),
        IOS("ios", "iOS Mobile", "آی‌اواس (آیفون)", "آي أو إس"),
        EDGE("edge", "Microsoft Edge", "مایکروسافت اج", "مايكروسافت إيدج"),
        RANDOMIZED("randomized", "Randomized Stealth", "تصادفی ضدتشخیص", "عشوائي متخفي"),
        ;

        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }
    }

    enum class TlsSplitMode(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
        val description: String,
        val persianDescription: String,
        val arabicDescription: String,
    ) {
        OFF(
            label = "Disabled",
            persianLabel = "غیرفعال",
            arabicLabel = "معطّل",
            description = "Standard TLS handshake without fragmentation",
            persianDescription = "ارتباط استاندارد بدون تکه‌تکه‌سازی بسته",
            arabicDescription = "اتصال قياسي بدون تجزئة الحزم",
        ),
        FIRST_BYTE(
            label = "Record Fragment",
            persianLabel = "فرگمنت رکورد TLS",
            arabicLabel = "تجزئة سجل TLS",
            description = "Splits TLS ClientHello into multiple TLS records",
            persianDescription = "برش رکوردهای TLS ClientHello برای عبور از فیلترینگ",
            arabicDescription = "تقسيم سجلات TLS لتجاوز الحجب",
        ),
        SNI(
            label = "Packet / SNI Fragment",
            persianLabel = "تکه‌تکه‌سازی بسته و دامنه (SNI)",
            arabicLabel = "تجزئة الحزمة واسم النطاق",
            description = "Fragments the TCP/TLS ClientHello packets across segments",
            persianDescription = "تکه‌تکه‌سازی بسته‌های دامنه برای دور زدن بازرسی عمیق بسته (DPI)",
            arabicDescription = "تجزئة امتداد اسم النطاق عبر الحزم لتجاوز الفحص العميق",
        ),
        COMBINED(
            label = "Combined Record + Packet Fragment",
            persianLabel = "ترکیبی (فرگمنت رکورد + بسته TLS)",
            arabicLabel = "مزدوج (تجزئة السجل والحزمة)",
            description = "Applies both TLS record fragmentation and TCP packet fragmentation",
            persianDescription = "اعمال همزمان فرگمنت رکورد TLS و تکه‌تکه‌سازی بسته TCP برای شبکه‌های شدیداً محدود",
            arabicDescription = "تطبيق تجزئة سجل TLS وتجزئة حزم TCP معاً للشبكات المقيدة بشدة",
        ),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }

        fun localizedDescription(isRtl: Boolean): String = if (isRtl) persianDescription else description
        fun localizedDescription(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> description
            AppLanguage.FA -> persianDescription
            AppLanguage.AR -> arabicDescription
        }
    }

    enum class UpstreamProxyType(
        val enLabel: String,
        val faLabel: String,
        val arLabel: String,
    ) {
        SOCKS5("SOCKS5", "پراکسی ساکس ۵", "وكيل سوكس ٥"),
        HTTP("HTTP", "پراکسی وب", "وكيل ويب"),
        ;

        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> enLabel
            AppLanguage.FA -> faLabel
            AppLanguage.AR -> arLabel
        }
    }

    enum class DnsProvider(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
        val dohUrl: String?,
    ) {
        SYSTEM_DEFAULT(
            label = "Default",
            persianLabel = "پیش‌فرض سرور",
            arabicLabel = "افتراضي الخادم",
            dohUrl = null,
        ),
        CLOUDFLARE(
            label = "Cloudflare",
            persianLabel = "کلودفلر",
            arabicLabel = "كلاود فلير",
            dohUrl = "https://1.1.1.1/dns-query",
        ),
        GOOGLE(
            label = "Google",
            persianLabel = "گوگل",
            arabicLabel = "جوجل",
            dohUrl = "https://8.8.8.8/dns-query",
        ),
        QUAD9(
            label = "Quad9",
            persianLabel = "کواد۹",
            arabicLabel = "كواد ٩",
            dohUrl = "https://9.9.9.9/dns-query",
        ),
        ADGUARD(
            label = "AdGuard",
            persianLabel = "ادگارد",
            arabicLabel = "أدجارد",
            dohUrl = "https://dns.adguard-dns.com/dns-query",
        ),
        CUSTOM(
            label = "Custom DoH",
            persianLabel = "دی‌ان‌اس امن سفارشی",
            arabicLabel = "نظام أسماء مشفّر مخصص",
            dohUrl = null,
        ),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }
    }

    enum class BootstrapDnsProvider(
        val label: String,
        val persianLabel: String,
        val arabicLabel: String,
        val description: String,
        val persianDescription: String,
        val arabicDescription: String,
        val addresses: List<String>,
    ) {
        AUTOMATIC(
            label = "Automatic",
            persianLabel = "خودکار (پیشنهادی)",
            arabicLabel = "تلقائي (موصى به)",
            description = "Races all default DoH endpoints",
            persianDescription = "استفاده همزمان از سریع‌ترین سرورهای دی‌ان‌اس",
            arabicDescription = "يختار أسرع خوادم نظام الأسماء تلقائياً",
            addresses = listOf("9.9.9.9", "1.1.1.1", "8.8.8.8"),
        ),
        QUAD9(
            label = "Quad9",
            persianLabel = "کواد۹",
            arabicLabel = "كواد ٩",
            description = "9.9.9.9, 149.112.112.112",
            persianDescription = "۹.۹.۹.۹ ، ۱۴۹.۱۱۲.۱۱۲.۱۱۲",
            arabicDescription = "٩.٩.٩.٩ ، ١٤٩.١١٢.١١٢.١١٢",
            addresses = listOf("9.9.9.9", "149.112.112.112"),
        ),
        CLOUDFLARE(
            label = "Cloudflare",
            persianLabel = "کلودفلر",
            arabicLabel = "كلاود فلير",
            description = "1.1.1.1, 1.0.0.1",
            persianDescription = "۱.۱.۱.۱ ، ۱.۰.۰.۱",
            arabicDescription = "١.١.١.١ ، ١.٠.٠.١",
            addresses = listOf("1.1.1.1", "1.0.0.1"),
        ),
        GOOGLE(
            label = "Google",
            persianLabel = "گوگل",
            arabicLabel = "جوجل",
            description = "8.8.8.8, 8.8.4.4",
            persianDescription = "۸.۸.۸.۸ ، ۸.۸.۴.۴",
            arabicDescription = "٨.٨.٨.٨ ، ٨.٨.٤.٤",
            addresses = listOf("8.8.8.8", "8.8.4.4"),
        ),
        SYSTEM(
            label = "System DNS",
            persianLabel = "دی‌ان‌اس سیستم",
            arabicLabel = "نظام أسماء الجهاز",
            description = "Local network DNS",
            persianDescription = "استفاده از دی‌ان‌اس شبکه فعلی دستگاه",
            arabicDescription = "استخدام نظام الأسماء المحلي للشبكة",
            addresses = emptyList(),
        ),
        CUSTOM(
            label = "Custom IP",
            persianLabel = "آی‌پی سفارشی",
            arabicLabel = "عنوان آي بي مخصص",
            description = "Specify a custom DoH IP resolver address",
            persianDescription = "وارد کردن آدرس آی‌پی دلخواه برای دی‌ان‌اس",
            arabicDescription = "تحديد عنوان آي بي مخصص لمحلل الأسماء",
            addresses = emptyList(),
        ),
        ;

        fun localizedLabel(isRtl: Boolean): String = if (isRtl) persianLabel else label
        fun localizedLabel(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> label
            AppLanguage.FA -> persianLabel
            AppLanguage.AR -> arabicLabel
        }

        fun localizedDescription(isRtl: Boolean): String = if (isRtl) persianDescription else description
        fun localizedDescription(lang: AppLanguage): String = when (lang) {
            AppLanguage.EN -> description
            AppLanguage.FA -> persianDescription
            AppLanguage.AR -> arabicDescription
        }
    }

    /** Export non-secret preferences for an explicit user-created backup file. */
    fun exportBackupJson(): JSONObject {
        val result = JSONObject()
        prefs.all.forEach { (key, value) ->
            when (value) {
                is Set<*> -> result.put(key, JSONArray(value.filterIsInstance<String>()))
                is String, is Boolean, is Int, is Long, is Float, is Double -> result.put(key, value)
            }
        }
        return result
    }

    /** Restore primitive preferences from a validated backup object. */
    fun importBackupJson(source: JSONObject) {
        val editor = prefs.edit()
        source.keys().forEach { key ->
            when (val value = source.opt(key)) {
                is JSONArray -> editor.putStringSet(key, buildSet {
                    for (i in 0 until value.length()) add(value.optString(i))
                })
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
                is String -> editor.putString(key, value)
            }
        }
        editor.apply()
    }

    companion object {
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_EXIT_CHECK = "exit_check_enabled"
        private const val KEY_CUSTOM_MTU = "custom_mtu"
        private const val KEY_CACHED_REAL_IP = "cached_real_ip"
        private const val KEY_CACHED_REAL_COUNTRY = "cached_real_country"
        private const val KEY_CACHED_QUOTA_REMAINING = "cached_quota_remaining"
        private const val KEY_CACHED_QUOTA_MAX = "cached_quota_max"
        private const val KEY_CACHED_IS_UNLIMITED = "cached_is_unlimited"
        const val DEFAULT_MTU = 1400
        const val MIN_MTU = 576
        const val MAX_MTU = 9000
        private const val KEY_SOCKS_BIND_ADDRESS = "socks_bind_address"
        private const val KEY_SOCKS_PORT = "socks_port"
        private const val KEY_HTTP_PROXY_ENABLED = "http_proxy_enabled"
        private const val KEY_HTTP_PROXY_PORT = "http_proxy_port"
        private const val KEY_DNS_PROVIDER = "dns_provider"
        private const val KEY_CUSTOM_DOH_URL = "custom_doh_url"
        private const val KEY_BOOTSTRAP_DNS_PROVIDER = "bootstrap_dns_provider"
        private const val KEY_CUSTOM_BOOTSTRAP_IP = "custom_bootstrap_ip"
        private const val KEY_PROXY_ONLY_MODE = "proxy_only_mode"
        private const val KEY_CUSTOM_EDGE_ADDRESS = "custom_edge_address"
        private const val KEY_UPSTREAM_PROXY_ENABLED = "upstream_proxy_enabled"
        private const val KEY_UPSTREAM_PROXY_TYPE = "upstream_proxy_type"
        private const val KEY_UPSTREAM_PROXY_HOST = "upstream_proxy_host"
        private const val KEY_UPSTREAM_PROXY_PORT = "upstream_proxy_port"
        private const val KEY_UPSTREAM_PROXY_USERNAME = "upstream_proxy_username"
        private const val KEY_UPSTREAM_PROXY_PASSWORD = "upstream_proxy_password"
        private const val KEY_EXCLUDED_APPS = "excluded_apps"
        private const val KEY_TLS_SPLIT_MODE = "tls_split_mode"
        private const val KEY_CONNECTION_PROFILE = "connection_profile"
        private const val KEY_DYNAMIC_ISLAND_ENABLED = "dynamic_island_enabled"
        private const val KEY_DYNAMIC_ISLAND_OVERLAY_ENABLED = "dynamic_island_overlay_enabled"
        private const val KEY_DYNAMIC_ISLAND_SCALE_PERCENT = "dynamic_island_scale_percent"
        private const val KEY_DYNAMIC_ISLAND_SIZE = "dynamic_island_size"
        private const val KEY_DYNAMIC_ISLAND_CORNER_RADIUS = "dynamic_island_corner_radius"
        private const val KEY_DYNAMIC_ISLAND_POSITION = "dynamic_island_position"
        private const val KEY_DYNAMIC_ISLAND_THEME = "dynamic_island_theme"
        private const val KEY_DYNAMIC_ISLAND_SHOW_SPEED = "dynamic_island_show_speed"
        private const val KEY_DYNAMIC_ISLAND_SHOW_PING = "dynamic_island_show_ping"
        private const val KEY_DYNAMIC_ISLAND_SHOW_FLAG = "dynamic_island_show_flag"
        private const val KEY_DYNAMIC_ISLAND_SHOW_TIMER = "dynamic_island_show_timer"
        private const val KEY_DYNAMIC_ISLAND_ANIMATION = "dynamic_island_animation"
        private const val KEY_DYNAMIC_ISLAND_PINNED_APPS = "dynamic_island_pinned_apps"
        private const val KEY_DYNAMIC_ISLAND_LED_BORDER = "dynamic_island_led_border"
        private const val KEY_DYNAMIC_ISLAND_LED_STYLE = "dynamic_island_led_style"
        private const val KEY_DYNAMIC_ISLAND_LED_SPEED = "dynamic_island_led_speed"
        private const val KEY_DYNAMIC_ISLAND_SHOW_TOWER_WAVE = "dynamic_island_show_tower_wave"
        private const val KEY_DYNAMIC_ISLAND_SHOW_SERVER_NAME = "dynamic_island_show_server_name"
        private const val KEY_DYNAMIC_ISLAND_SHOW_SESSION_DATA = "dynamic_island_show_session_data"
        private const val KEY_DYNAMIC_ISLAND_SHOW_IP_BADGE = "dynamic_island_show_ip_badge"
        private const val KEY_DYNAMIC_ISLAND_DRAGGABLE = "dynamic_island_draggable"
        private const val KEY_DYNAMIC_ISLAND_OFFSET_X = "dynamic_island_offset_x"
        private const val KEY_DYNAMIC_ISLAND_OFFSET_Y = "dynamic_island_offset_y"
        private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
        private const val KEY_SCHEDULE_START_HOUR = "schedule_start_hour"
        private const val KEY_SCHEDULE_START_MINUTE = "schedule_start_minute"
        private const val KEY_SCHEDULE_END_HOUR = "schedule_end_hour"
        private const val KEY_SCHEDULE_END_MINUTE = "schedule_end_minute"
        private const val KEY_SERVER_LIST_AUTO_REFRESH = "server_list_auto_refresh"
        private const val KEY_LAST_SERVER_LIST_REFRESH = "last_server_list_refresh"
        private const val KEY_IDLE_DISCONNECT_ENABLED = "idle_disconnect_enabled"
        private const val KEY_IDLE_TIMEOUT_MINUTES = "idle_timeout_minutes"
        private const val KEY_SESSION_DATA_LIMIT_ENABLED = "session_data_limit_enabled"
        private const val KEY_SESSION_DATA_LIMIT_MB = "session_data_limit_mb"
        private const val KEY_SMART_PING_AUTO_SWITCH = "smart_ping_auto_switch"
        private const val KEY_AUTO_CONNECT_ON_LAUNCH = "auto_connect_on_launch"
        private const val KEY_STEALTH_NOTIFICATION_ENABLED = "stealth_notification_enabled"
        private const val KEY_KEEP_ALIVE_ENABLED = "keep_alive_enabled"
        private const val KEY_KEEP_ALIVE_INTERVAL_SEC = "keep_alive_interval_sec"
        private const val KEY_AUTO_RECONNECT_ENABLED = "auto_reconnect_enabled"
        private const val KEY_APP_COLOR_THEME = "app_color_theme"
        private const val KEY_APP_CUSTOM_ACCENT_ARGB = "app_custom_accent_argb"
        private const val KEY_APP_CUSTOM_PALETTE = "app_custom_palette"
        private const val KEY_APP_UI_DESIGN_STYLE = "app_ui_design_style"
        private const val KEY_HERO_WAVE_ANIM_STYLE = "hero_wave_anim_style"
        private const val KEY_DYNAMIC_ISLAND_SHOW_QUICK_APPS = "dynamic_island_show_quick_apps"
        private const val KEY_DYNAMIC_ISLAND_MOTION_STYLE = "dynamic_island_motion_style"
        private const val KEY_UNMETERED_VPN_MODE = "unmetered_vpn_mode_enabled"
        private const val KEY_BATTERY_SAVER_MODE = "battery_saver_mode_enabled"
        private const val KEY_CORE_SELECTION = "core_selection"
        const val DEFAULT_SOCKS_BIND_ADDRESS = "127.0.0.1"
        const val DEFAULT_SOCKS_PORT = 1080
        const val DEFAULT_HTTP_PROXY_PORT = 8080
        const val DEFAULT_UPSTREAM_PROXY_PORT = 1080

        val DEFAULT_PINNED_ISLAND_APPS = setOf(
            "com.android.chrome",
            "org.telegram.messenger",
            "com.google.android.youtube",
        )

        val SOCKS_BIND_ADDRESS_PRESETS = listOf(
            "127.0.0.1" to "Loopback only (127.0.0.1)",
            "0.0.0.0" to "All interfaces (0.0.0.0)",
        )

        fun socksBindPresetsFor(lang: AppLanguage): List<Pair<String, String>> = when (lang) {
            AppLanguage.EN -> SOCKS_BIND_ADDRESS_PRESETS
            AppLanguage.FA -> listOf(
                "127.0.0.1" to "فقط دستگاه فعلی (۱۲۷.۰.۰.۱)",
                "0.0.0.0" to "تمام کارت‌های شبکه (۰.۰.۰.۰)",
            )
            AppLanguage.AR -> listOf(
                "127.0.0.1" to "الجهاز الحالي فقط (١٢٧.٠.٠.١)",
                "0.0.0.0" to "جميع واجهات الشبكة (٠.٠.٠.٠)",
            )
        }

        fun isValidDohUrl(value: String): Boolean {
            val trimmed = value.trim()
            if (!trimmed.startsWith("https://", ignoreCase = true)) return false
            return runCatching {
                val uri = java.net.URI.create(trimmed)
                !uri.host.isNullOrBlank()
            }.getOrDefault(false)
        }

        fun isValidIpAddress(value: String): Boolean {
            return if (android.os.Build.VERSION.SDK_INT >= 29) {
                android.net.InetAddresses.isNumericAddress(value)
            } else {
                @Suppress("DEPRECATION")
                android.util.Patterns.IP_ADDRESS.matcher(value).matches()
            }
        }

        fun isValidHostname(value: String): Boolean {
            val host = value.trim().removeSuffix(".")
            if (host.isEmpty() || host.length > 253) return false

            if (host.contains(':')) return false
            if (host.all { it.isDigit() || it == '.' }) return false
            val labels = host.split('.')
            if (labels.size < 2) return false
            return labels.all { label ->
                label.isNotEmpty() &&
                    label.length <= 63 &&
                    !label.startsWith('-') &&
                    !label.endsWith('-') &&
                    label.all { char ->
                        char == '-' || (char.code < 128 && (char.isLetterOrDigit()))
                    }
            }
        }

        fun isValidEdgeHost(value: String): Boolean {
            val host = value.trim().removeSuffix(".")
            return isValidHostname(host) || isValidIpAddress(host)
        }
    }
}
