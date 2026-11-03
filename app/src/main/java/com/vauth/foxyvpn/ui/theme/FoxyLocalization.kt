package com.vauth.foxyvpn.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.vauth.foxyvpn.data.SettingsStore
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val badge: String,
    val isRtl: Boolean,
) {
    EN(code = "en", nativeName = "English", badge = "EN", isRtl = false),
    FA(code = "fa", nativeName = "فارسی", badge = "فا", isRtl = true),
    AR(code = "ar", nativeName = "العربية", badge = "عر", isRtl = true),
}

typealias FoxyLanguage = AppLanguage

class LanguageController(private val settingsStore: SettingsStore) {
    var language: AppLanguage by mutableStateOf(settingsStore.appLanguage)
        private set

    fun set(next: AppLanguage) {
        if (next == language) return
        language = next
        settingsStore.appLanguage = next
    }

    fun toggle() {
        val next = when (language) {
            AppLanguage.EN -> AppLanguage.FA
            AppLanguage.FA -> AppLanguage.AR
            AppLanguage.AR -> AppLanguage.EN
        }
        set(next)
    }
}

@Composable
fun rememberLanguageController(settingsStore: SettingsStore): LanguageController =
    remember(settingsStore) { LanguageController(settingsStore) }

/**
 * Converts Western ASCII digits (0-9) into Persian (۰-۹) or Arabic-Indic (٠-٩) digits
 * when the active language is Persian or Arabic.
 */
private val IPV4_HOST_REGEX = Regex("""^\d{1,3}(\.\d{1,3}){3}(:\d+)?$""")
private val IPV6_HEX_REGEX = Regex("""^[0-9a-fA-F:]+$""")

fun AppLanguage.localizeDigits(input: String): String {
    if (this == AppLanguage.EN) return input
    val trimmed = input.trim()
    // Never localize IPv4 addresses or IPv6 hex strings
    if (IPV4_HOST_REGEX.matches(trimmed) || (trimmed.contains(':') && trimmed.any { it in 'a'..'f' || it in 'A'..'F' } && IPV6_HEX_REGEX.matches(trimmed))) {
        return input
    }
    val zeroChar = if (this == AppLanguage.FA) '۰' else '٠'
    val sb = StringBuilder(input.length)
    for (ch in input) {
        if (ch in '0'..'9') {
            sb.append((zeroChar.code + (ch - '0')).toChar())
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun AppLanguage.localizeNumber(value: Int): String = localizeDigits(value.toString())
fun AppLanguage.localizeNumber(value: Long): String = localizeDigits(value.toString())

/**
 * Formats byte counts with 100% localized digits and units for EN, FA, and AR.
 */
fun AppLanguage.formatBytesLocalized(bytes: Long): String {
    if (bytes < 0) return "\u2014"
    val units = when (this) {
        AppLanguage.EN -> listOf("B", "KB", "MB", "GB", "TB")
        AppLanguage.FA -> listOf("بایت", "کیلوبایت", "مگابایت", "گیگابایت", "ترابایت")
        AppLanguage.AR -> listOf("بايت", "ك.ب", "م.ب", "ج.ب", "ت.ب")
    }
    if (bytes < 1024L) {
        return "${localizeNumber(bytes)} ${units[0]}"
    }
    var value = bytes / 1024.0
    var unitIdx = 1
    while (value >= 1024.0 && unitIdx < units.lastIndex) {
        value /= 1024.0
        unitIdx++
    }
    val pattern = if (value >= 100.0) "%.0f" else "%.1f"
    val formattedNum = localizeDigits(String.format(Locale.US, pattern, value))
    return "$formattedNum ${units[unitIdx]}"
}

/**
 * Formats transfer rate (bytes/sec) with 100% localized digits and units.
 */
fun AppLanguage.formatSpeedLocalized(bytesPerSecond: Long): String {
    val safe = bytesPerSecond.coerceAtLeast(0L)
    val base = formatBytesLocalized(safe)
    val perSecSuffix = when (this) {
        AppLanguage.EN -> "/s"
        AppLanguage.FA -> "/ث"
        AppLanguage.AR -> "/ث"
    }
    return "$base$perSecSuffix"
}

/**
 * Formats latency in milliseconds with 100% localized digits and unit.
 */
fun AppLanguage.formatPingLocalized(ms: Int): String {
    val num = localizeNumber(ms)
    return when (this) {
        AppLanguage.EN -> "$num ms"
        AppLanguage.FA -> "$num م‌ث"
        AppLanguage.AR -> "$num م.ث"
    }
}

/**
 * Formats Mbps throughput with 100% localized digits and unit.
 */
fun AppLanguage.formatMbpsLocalized(mbps: Float, includeUnit: Boolean = true): String {
    val num = localizeDigits(String.format(Locale.US, "%.1f", mbps))
    if (!includeUnit) return num
    val unit = when (this) {
        AppLanguage.EN -> "Mbps"
        AppLanguage.FA -> "مگابیت/ث"
        AppLanguage.AR -> "ميجابت/ث"
    }
    return "$num $unit"
}

/**
 * Formats percentage (0..100%) with localized digits and percent sign.
 */
fun AppLanguage.formatPercentLocalized(percent: Int): String {
    val num = localizeNumber(percent)
    return when (this) {
        AppLanguage.EN -> "$num%"
        AppLanguage.FA -> "٪$num"
        AppLanguage.AR -> "٪$num"
    }
}

/**
 * Formats HH:MM:SS or MM:SS timer strings with localized digits.
 */
fun AppLanguage.formatDurationLocalized(seconds: Long, alwaysShowHours: Boolean = true): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    val hStr = localizeDigits(String.format(Locale.US, "%02d", h))
    val mStr = localizeDigits(String.format(Locale.US, "%02d", m))
    val sStr = localizeDigits(String.format(Locale.US, "%02d", s))
    return if (alwaysShowHours || h > 0) {
        "\u200E$hStr\u200E:\u200E$mStr\u200E:\u200E$sStr\u200E"
    } else {
        "\u200E$mStr\u200E:\u200E$sStr\u200E"
    }
}

/**
 * Formats 24-hour HH:MM clock time with localized digits.
 */
fun AppLanguage.formatClockTimeLocalized(hour: Int, minute: Int): String {
    val hStr = localizeDigits(String.format(Locale.US, "%02d", hour.coerceIn(0, 23)))
    val mStr = localizeDigits(String.format(Locale.US, "%02d", minute.coerceIn(0, 59)))
    return "\u200E$hStr\u200E:\u200E$mStr\u200E"
}

/**
 * Formats minute duration with localized digits and unit.
 */
fun AppLanguage.formatMinutesLocalized(minutes: Int): String {
    val num = localizeNumber(minutes)
    return when (this) {
        AppLanguage.EN -> "$num min"
        AppLanguage.FA -> "$num دقیقه"
        AppLanguage.AR -> "$num دقيقة"
    }
}

/**
 * Translates common country names or codes into Persian/Arabic when FA/AR is active.
 */
fun AppLanguage.localizeCountryName(rawName: String, countryCode: String = ""): String {
    if (this == AppLanguage.EN) return rawName.ifBlank { countryCode }
    val code = countryCode.uppercase(Locale.US).trim()
    val norm = rawName.trim().lowercase(Locale.US)
    if (this == AppLanguage.FA) {
        return when {
            code == "US" || norm.contains("united states") || norm == "usa" -> "ایالات متحده"
            code == "DE" || norm.contains("germany") -> "آلمان"
            code == "NL" || norm.contains("netherlands") -> "هلند"
            code == "GB" || code == "UK" || norm.contains("united kingdom") -> "بریتانیا"
            code == "FR" || norm.contains("france") -> "فرانسه"
            code == "FI" || norm.contains("finland") -> "فنلاند"
            code == "SE" || norm.contains("sweden") -> "سوئد"
            code == "CH" || norm.contains("switzerland") -> "سوئیس"
            code == "BE" || norm.contains("belgium") -> "بلژیک"
            code == "AT" || norm.contains("austria") -> "اتریش"
            code == "CA" || norm.contains("canada") -> "کانادا"
            code == "SG" || norm.contains("singapore") -> "سنگاپور"
            code == "JP" || norm.contains("japan") -> "ژاپن"
            code == "ES" || norm.contains("spain") -> "اسپانیا"
            code == "IT" || norm.contains("italy") -> "ایتالیا"
            code == "IE" || norm.contains("ireland") -> "ایرلند"
            code == "PL" || norm.contains("poland") -> "لهستان"
            code == "NO" || norm.contains("norway") -> "نروژ"
            code == "DK" || norm.contains("denmark") -> "دانمارک"
            code == "CZ" || norm.contains("czech") -> "جمهوری چک"
            code == "RO" || norm.contains("romania") -> "رومانی"
            code == "BG" || norm.contains("bulgaria") -> "بلغارستان"
            code == "AU" || norm.contains("australia") -> "استرالیا"
            code == "NZ" || norm.contains("new zealand") -> "نیوزیلند"
            code == "BR" || norm.contains("brazil") -> "برزیل"
            code == "TR" || norm.contains("turkey") -> "ترکیه"
            code == "AE" || norm.contains("emirates") -> "امارات"
            code == "IN" || norm.contains("india") -> "هند"
            code == "HK" || norm.contains("hong kong") -> "هنگ‌کنگ"
            code == "KR" || norm.contains("korea") -> "کره جنوبی"
            code == "REC" || norm.contains("recommended") -> "پیشنهادی (سریع‌ترین)"
            else -> rawName.ifBlank { countryCode }
        }
    } else {
        return when {
            code == "US" || norm.contains("united states") || norm == "usa" -> "الولايات المتحدة"
            code == "DE" || norm.contains("germany") -> "ألمانيا"
            code == "NL" || norm.contains("netherlands") -> "هولندا"
            code == "GB" || code == "UK" || norm.contains("united kingdom") -> "المملكة المتحدة"
            code == "FR" || norm.contains("france") -> "فرنسا"
            code == "FI" || norm.contains("finland") -> "فنلندا"
            code == "SE" || norm.contains("sweden") -> "السويد"
            code == "CH" || norm.contains("switzerland") -> "سويسرا"
            code == "BE" || norm.contains("belgium") -> "بلجيكا"
            code == "AT" || norm.contains("austria") -> "النمسا"
            code == "CA" || norm.contains("canada") -> "كندا"
            code == "SG" || norm.contains("singapore") -> "سنغافورة"
            code == "JP" || norm.contains("japan") -> "اليابان"
            code == "ES" || norm.contains("spain") -> "إسبانيا"
            code == "IT" || norm.contains("italy") -> "إيطاليا"
            code == "IE" || norm.contains("ireland") -> "أيرلندا"
            code == "PL" || norm.contains("poland") -> "بولندا"
            code == "NO" || norm.contains("norway") -> "النرويج"
            code == "DK" || norm.contains("denmark") -> "الدنمارك"
            code == "CZ" || norm.contains("czech") -> "التشيك"
            code == "RO" || norm.contains("romania") -> "رومانيا"
            code == "BG" || norm.contains("bulgaria") -> "بلغاريا"
            code == "AU" || norm.contains("australia") -> "أستراليا"
            code == "NZ" || norm.contains("new zealand") -> "نيوزيلندا"
            code == "BR" || norm.contains("brazil") -> "البرازيل"
            code == "TR" || norm.contains("turkey") -> "تركيا"
            code == "AE" || norm.contains("emirates") -> "الإمارات"
            code == "IN" || norm.contains("india") -> "الهند"
            code == "HK" || norm.contains("hong kong") -> "هونغ كونغ"
            code == "KR" || norm.contains("korea") -> "كوريا الجنوبية"
            code == "REC" || norm.contains("recommended") -> "الموصى به (الأسرع)"
            else -> rawName.ifBlank { countryCode }
        }
    }
}

class FoxyStrings(
    val language: AppLanguage,
    val isRtl: Boolean,
) {
    // Common
    var appName: String = "FoxyVpn"
    var back: String = ""
    var save: String = ""
    var cancel: String = ""
    var resetToDefault: (String) -> String = { it }
    var enabled: String = ""
    var disabled: String = ""
    var none: String = ""
    var notSet: String = ""
    var checking: String = ""
    var verifying: String = ""
    var testing: String = ""
    // Header Action Tooltips / Labels
    var headerThemeButton: String = ""
    var headerSpeedButton: String = ""
    var headerLanguageButton: String = ""
    var headerSettingsButton: String = ""
    // Home Screen
    var statusConnected: String = ""
    var statusConnecting: String = ""
    var statusDisconnected: String = ""
    var slideToConnect: String = ""
    var slideToRetry: String = ""
    var connectingTapToCancel: String = ""
    var disconnecting: String = ""
    var slideToDisconnect: String = ""
    var smartToolsSection: String = ""
    var openAllTools: String = ""
    var serverLocationLabel: String = ""
    var recommendedFastest: String = ""
    var recommendedSubtitle: String = ""
    var currentIpTapLeakTest: String = ""
    var badgeProtected: String = ""
    var badgeExposed: String = ""
    var dataUsedLabel: String = ""
    var latencyLabel: String = ""
    var monthlyQuotaLabel: String = ""
    var unlimitedQuota: String = ""
    var quotaLimitPrefix: String = ""
    // Dynamic Island & Full Customization
    var islandConnecting: String = ""
    var islandReady: String = ""
    var islandServerDefault: String = ""
    var islandActiveEncrypted: String = ""
    var islandSecuringConnection: String = ""
    var islandProtectedReady: String = ""
    var islandLatency: String = ""
    var islandDuration: String = ""
    var islandLiveSpeed: String = ""
    var islandChangeServer: String = ""
    var islandConnect: String = ""
    var islandDisconnect: String = ""
    var islandCancel: String = ""
    var islandCustomizeButton: String = ""
    var islandCustomizeModalTitle: String = ""
    var islandCustomizeModalSub: String = ""
    var islandManualScaleTitle: String = ""
    var islandManualScaleSub: String = ""
    var islandPinnedAppsTitle: String = ""
    var islandPinnedAppsSub: String = ""
    var islandAddAppsButton: String = ""
    var islandNoPinnedApps: String = ""
    var islandContentSectionTitle: String = ""
    // Network Tools Screen & 5 Tools
    var toolkitScreenTitle: String = ""
    var toolSpeedTitle: String = ""
    var toolSpeedSubtitle: String = ""
    var toolLeakTitle: String = ""
    var toolLeakSubtitle: String = ""
    var toolScannerTitle: String = ""
    var toolScannerSubtitle: String = ""
    var toolWatchdogTitle: String = ""
    var toolWatchdogSubtitle: String = ""
    var toolMtuTitle: String = ""
    var toolMtuSubtitle: String = ""
    // Tool 1: Speed Test
    var speedReadyStage: String = ""
    var speedMeasuringPingStage: String = ""
    var speedTestingTunnelDlStage: String = ""
    var speedTestingDirectDlStage: String = ""
    var speedTestingTunnelUlStage: String = ""
    var speedTestingDirectUlStage: String = ""
    var speedDoneTunnelStage: String = ""
    var speedDoneDirectStage: String = ""
    var speedModeTunnel: String = ""
    var speedModeDirect: String = ""
    var speedDownloadLabel: String = ""
    var speedUploadLabel: String = ""
    var speedPingLabel: String = ""
    var speedJitterLabel: String = ""
    var speedRunningButton: String = ""
    var speedStartButton: String = ""
    var speedUnitMbps: String = ""
    // Tool 2: Leak Inspector
    var leakScanningTitle: String = ""
    var leakProtectedTitle: String = ""
    var leakVerifyingTitle: String = ""
    var leakExposedTitle: String = ""
    var leakDirectIpLabel: String = ""
    var leakVpnIpLabel: String = ""
    var leakNotConnected: String = ""
    var leakProbing: String = ""
    var leakColoLabel: String = ""
    var leakProtocolLabel: String = ""
    var leakSniLabel: String = ""
    var leakDohLabel: String = ""
    var leakDohVerified: (Int, String) -> String = { _, _ -> "" }
    var leakIpv6Label: String = ""
    var leakIpv6Protected: String = ""
    var leakIpv6Inactive: String = ""
    var leakScanningButton: String = ""
    var leakRescanButton: String = ""
    // Tool 3: Smart Server Scanner
    var scannerHeaderTitle: String = ""
    var scannerInitialSubtitle: String = ""
    var scannerLoadingCatalog: String = ""
    var scannerEmptyCatalog: String = ""
    var scannerProbingServer: (String, String) -> String = { _, _ -> "" }
    var scannerOptimalFound: (String, Int) -> String = { _, _ -> "" }
    var scannerTimeoutMessage: String = ""
    var scannerRunningButton: String = ""
    var scannerStartButton: String = ""
    var scannerConnectBestButton: (String, Int) -> String = { _, _ -> "" }
    // Tool 4: Watchdog & Keep-Alive
    var watchdogHeaderTitle: String = ""
    var watchdogPulsesLabel: String = ""
    var watchdogPulsesValue: (Int) -> String = { "" }
    var watchdogLastPulseLabel: String = ""
    var watchdogAutoReconnectTitle: String = ""
    var watchdogAutoReconnectSub: String = ""
    var watchdogKeepAliveTitle: String = ""
    var watchdogKeepAliveSub: String = ""
    var watchdogIntervalLabel: String = ""
    var watchdogSecondsUnit: (Int) -> String = { "" }
    var watchdogConnectFirstButton: String = ""
    var watchdogSendingPulseButton: String = ""
    var watchdogSendNowButton: (String) -> String = { "" }
    // Tool 5: MTU & Anti-DPI Optimizer
    var mtuInitialStatus: String = ""
    var mtuProbingStatus: (Int, String) -> String = { _, _ -> "" }
    var mtuOptimalFoundStatus: (Int, String, Int) -> String = { _, _, _ -> "" }
    var mtuTimeoutStatus: String = ""
    var mtuAppliedStatus: (Int, String) -> String = { _, _ -> "" }
    var mtuActiveLabel: String = ""
    var mtuBytesValue: (Int) -> String = { "" }
    var mtuFragmentLabel: String = ""
    var mtuProbingButton: String = ""
    var mtuFindBestButton: String = ""
    var mtuApplyBestButton: (Int, String) -> String = { _, _ -> "" }
    // Locations Screen
    var locationsTitle: String = ""
    var searchLocationsPlaceholder: String = ""
    var noActiveServers: String = ""
    var extraEdgesSuffix: (Int) -> String = { "" }
    // Logs Screen
    var logsTitle: (Int, Int) -> String = { _, _ -> "" }
    var logFilterAll: String = ""
    var logFilterInfo: String = ""
    var logFilterProblems: String = ""
    var logsEmpty: String = ""
    var logsNoProblems: String = ""
    var logsNothingMatches: String = ""
    var logsCopiedToast: String = ""
    // Login Screen
    var loginTitle: String = ""
    var loginSubtitle: String = ""
    var loginEmailLabel: String = ""
    var loginPasswordLabel: String = ""
    var loginContinueButton: String = ""
    var loginTwoFactorPrompt: String = ""
    var loginTwoFactorCodeLabel: String = ""
    var loginVerifyButton: String = ""
    // Settings Screen
    var settingsTitle: String = ""
    var sectionLanguageAndTheme: String = ""
    var appLanguageTitle: String = ""
    var appLanguageSubtitle: String = ""
    var appThemeTitle: String = ""
    var themeSystem: String = ""
    var themeLight: String = ""
    var themeDark: String = ""
    var colorPaletteTitle: String = ""
    var colorPaletteSub: String = ""
    var heroWaveStyleTitle: String = ""
    var heroWaveStyleSub: String = ""
    var islandShowQuickAppsTitle: String = ""
    var islandShowQuickAppsSub: String = ""
    var sectionSmartToolkit: String = ""
    var sectionDynamicIsland: String = ""
    var enableDynamicIslandTitle: String = ""
    var enableDynamicIslandSub: String = ""
    var overlayIslandTitle: String = ""
    var overlayIslandSubGranted: String = ""
    var overlayIslandSubNeedsPerm: String = ""
    var islandSizeTitle: String = ""
    var islandCornerRadiusTitle: String = ""
    var islandPositionTitle: String = ""
    var islandThemeTitle: String = ""
    var islandShowSpeedTitle: String = ""
    var islandShowSpeedSub: String = ""
    var islandShowPingTitle: String = ""
    var islandShowPingSub: String = ""
    var islandShowFlagTitle: String = ""
    var islandShowFlagSub: String = ""
    var islandShowTimerTitle: String = ""
    var islandShowTimerSub: String = ""
    var islandAnimationTitle: String = ""
    var islandAnimationSub: String = ""
    var islandMotionStyleTitle: String = ""
    var islandMotionStyleSub: String = ""
    var islandLedBorderTitle: String = ""
    var islandLedBorderSub: String = ""
    var islandLedStyleTitle: String = ""
    var islandLedSpeedTitle: String = ""
    var islandShowTowerWaveTitle: String = ""
    var islandShowTowerWaveSub: String = ""
    var islandShowServerNameTitle: String = ""
    var islandShowServerNameSub: String = ""
    var islandShowSessionDataTitle: String = ""
    var islandShowSessionDataSub: String = ""
    var islandShowIpBadgeTitle: String = ""
    var islandShowIpBadgeSub: String = ""
    var islandDraggableTitle: String = ""
    var islandDraggableSub: String = ""
    var islandResetPositionButton: String = ""
    // Smart Automation, Schedule & Idle Battery/Data Saver
    var sectionSmartAutomation: String = ""
    var unmeteredSpeedBoostTitle: String = ""
    var unmeteredSpeedBoostSub: String = ""
    var tcpBufferBoostTitle: String = ""
    var tcpBufferBoostSub: String = ""
    var batterySaverModeTitle: String = ""
    var batterySaverModeSub: String = ""
    var batteryExemptionTitle: String = ""
    var batteryExemptionGrantedSub: String = ""
    var batteryExemptionRequestSub: String = ""
    var scheduleVpnTitle: String = ""
    var scheduleVpnSub: String = ""
    var scheduleTimeWindowTitle: String = ""
    var scheduleFromLabel: String = ""
    var scheduleToLabel: String = ""
    var idleDisconnectTitle: String = ""
    var idleDisconnectSub: String = ""
    var idleTimeoutTitle: String = ""
    var sessionDataLimitTitle: String = ""
    var sessionDataLimitSub: String = ""
    var sessionDataLimitAmountTitle: String = ""
    var smartPingSwitchTitle: String = ""
    var smartPingSwitchSub: String = ""
    var autoConnectLaunchTitle: String = ""
    var autoConnectLaunchSub: String = ""
    var stealthNotificationTitle: String = ""
    var stealthNotificationSub: String = ""
    // Permissions & Package Installer
    var sectionPermissions: String = ""
    var installPermissionTitle: String = ""
    var installPermissionGrantedSub: String = ""
    var installPermissionRequestSub: String = ""
    var notificationPermissionTitle: String = ""
    var notificationPermissionGrantedSub: String = ""
    var notificationPermissionRequestSub: String = ""
    var sectionConnection: String = ""
    var verifyExitTitle: String = ""
    var verifyExitSub: String = ""
    var tunnelMtuTitle: String = ""
    var sectionDns: String = ""
    var tunnelDnsTitle: String = ""
    var resolverDnsTitle: String = ""
    var sectionLocalProxy: String = ""
    var proxyOnlyModeTitle: String = ""
    var proxyOnlyModeSub: String = ""
    var localBindAddressTitle: String = ""
    var socksPortTitle: String = ""
    var enableHttpProxyTitle: String = ""
    var enableHttpProxySub: String = ""
    var httpProxyPortTitle: String = ""
    var sectionSplitTunneling: String = ""
    var excludedAppsTitle: String = ""
    var excludedAppsCount: (Int) -> String = { "" }
    var sectionAdvancedNetworking: String = ""
    var customEdgeAddressTitle: String = ""
    var tlsFragmentationTitle: String = ""
    var chainUpstreamProxyTitle: String = ""
    var chainUpstreamProxySub: String = ""
    var upstreamProxyTypeTitle: String = ""
    var upstreamProxyAddressTitle: String = ""
    var upstreamProxyCredentialsTitle: String = ""
    var sectionAboutAndLogs: String = ""
    var viewConnectionLogsTitle: String = ""
    var viewConnectionLogsSub: String = ""
    var versionTitle: String = ""
    var instagramTitle: String = ""
    var instagramSubtitle: String = ""
    var telegramTitle: String = ""
    var telegramSubtitle: String = ""
    var signOutButton: String = ""
    // Dialog helper labels
    var dialogDohUrlLabel: String = ""
    var dialogIpAddressLabel: String = ""
    var dialogHostOrIpLabel: String = ""
    var dialogPortRangeLabel: String = ""
    var dialogUsernameLabel: String = ""
    var dialogSearchAppsPlaceholder: String = ""
}

private fun createEnglishFoxyStrings(): FoxyStrings = FoxyStrings(
    language = AppLanguage.EN,
    isRtl = false,
).apply {
    appName = "FoxyVpn"
    back = "Back"
    save = "Save"
    cancel = "Cancel"
    resetToDefault = { def -> "Reset to default ($def)" }
    enabled = "Enabled"
    disabled = "Disabled"
    none = "None"
    notSet = "Not set"
    checking = "Checking…"
    verifying = "Verifying…"
    testing = "Testing…"
    headerThemeButton = "Theme"
    headerSpeedButton = "Speed Test"
    headerLanguageButton = "Language"
    headerSettingsButton = "Settings"
    statusConnected = "SECURE & CONNECTED"
    statusConnecting = "ESTABLISHING TUNNEL…"
    statusDisconnected = "NOT PROTECTED"
    slideToConnect = "Slide to Connect"
    slideToRetry = "Slide to Retry Connection"
    connectingTapToCancel = "Connecting… (Tap to Cancel)"
    disconnecting = "Disconnecting…"
    slideToDisconnect = "Slide to Disconnect"
    smartToolsSection = "SMART NETWORK TOOLS"
    openAllTools = "Open All"
    serverLocationLabel = "SERVER LOCATION"
    recommendedFastest = "Recommended (Fastest)"
    recommendedSubtitle = "Automatically selects the lowest-latency edge server"
    currentIpTapLeakTest = "CURRENT IP • TAP FOR LEAK TEST"
    badgeProtected = "Protected"
    badgeExposed = "Exposed"
    dataUsedLabel = "DATA USED"
    latencyLabel = "LATENCY"
    monthlyQuotaLabel = "Monthly Quota"
    unlimitedQuota = "Unlimited"
    quotaLimitPrefix = "Limit:"
    islandConnecting = "Connecting…"
    islandReady = "Ready"
    islandServerDefault = "FoxyVpn Server"
    islandActiveEncrypted = "Active & Encrypted"
    islandSecuringConnection = "Securing Connection…"
    islandProtectedReady = "Protected Mode Ready"
    islandLatency = "Latency"
    islandDuration = "Duration"
    islandLiveSpeed = "Live Speed"
    islandChangeServer = "Change Server"
    islandConnect = "Connect"
    islandDisconnect = "Disconnect"
    islandCancel = "Cancel"
    islandCustomizeButton = "Customize Dynamic Island"
    islandCustomizeModalTitle = "Dynamic Island Studio"
    islandCustomizeModalSub = "Fine-tune size (0–100%), live telemetry badges, and quick-launch apps"
    islandManualScaleTitle = "Manual Island Size Scale"
    islandManualScaleSub = "Linear size control from 0% (Compact Mini) to 100% (Full Width)"
    islandPinnedAppsTitle = "Select Dynamic Island Quick-Launch Apps"
    islandPinnedAppsSub = "Add apps to the quick-launch bar inside Dynamic Island (for fast app opening; unrelated to VPN split tunneling)"
    islandAddAppsButton = "Manage Island Apps"
    islandNoPinnedApps = "No apps pinned yet — tap Manage to add shortcuts"
    islandContentSectionTitle = "DISPLAYED ISLAND CONTENT"
    toolkitScreenTitle = "Network & Security Toolkit"
    toolSpeedTitle = "Speed Test"
    toolSpeedSubtitle = "Download, Upload, Ping & Jitter"
    toolLeakTitle = "IP & Leak Test"
    toolLeakSubtitle = "ISP vs VPN IP, DoH & IPv6 Audit"
    toolScannerTitle = "Smart Scanner"
    toolScannerSubtitle = "Find Lowest-Latency Server"
    toolWatchdogTitle = "Keep-Alive Guard"
    toolWatchdogSubtitle = "Anti-Drop Heartbeat & Redial"
    toolMtuTitle = "MTU & DPI Lab"
    toolMtuSubtitle = "Optimal MTU & TLS Fragmentation"
    speedReadyStage = "Ready to measure live network throughput"
    speedMeasuringPingStage = "Measuring Latency & Jitter (5 samples)…"
    speedTestingTunnelDlStage = "Testing Encrypted Tunnel Download…"
    speedTestingDirectDlStage = "Testing Direct Download…"
    speedTestingTunnelUlStage = "Testing Encrypted Tunnel Upload…"
    speedTestingDirectUlStage = "Testing Direct Upload…"
    speedDoneTunnelStage = "Benchmark completed through Encrypted VPN Tunnel"
    speedDoneDirectStage = "Benchmark completed on Direct Network"
    speedModeTunnel = "TUNNEL MODE (ROUTED VIA SOCKS5)"
    speedModeDirect = "DIRECT ISP MODE (VPN DISCONNECTED)"
    speedDownloadLabel = "DOWNLOAD"
    speedUploadLabel = "UPLOAD"
    speedPingLabel = "PING RTT"
    speedJitterLabel = "JITTER"
    speedRunningButton = "Running Live Speed Test…"
    speedStartButton = "Start Live Speed Test"
    speedUnitMbps = "Mbps"
    leakScanningTitle = "INSPECTING IP & DNS TUNNEL…"
    leakProtectedTitle = "ZERO LEAKS DETECTED • 100% MASKED"
    leakVerifyingTitle = "TUNNEL ACTIVE • VERIFYING EXIT"
    leakExposedTitle = "VPN DISCONNECTED • REAL IP EXPOSED"
    leakDirectIpLabel = "ISP DIRECT IP"
    leakVpnIpLabel = "VPN EXIT IP"
    leakNotConnected = "Not Connected"
    leakProbing = "Probing…"
    leakColoLabel = "Cloudflare Edge Datacenter"
    leakProtocolLabel = "TLS / HTTP Protocol"
    leakSniLabel = "Encrypted SNI Status"
    leakDohLabel = "DoH DNS Query Check"
    leakDohVerified = { ms, ip -> "Verified in ${ms}ms$ip" }
    leakIpv6Label = "IPv6 Leak Protection"
    leakIpv6Protected = "Protected (Sinkholed via fd00::/128)"
    leakIpv6Inactive = "Inactive (Direct Network)"
    leakScanningButton = "Scanning IP & DNS…"
    leakRescanButton = "Re-Scan IP & Leak Status"
    scannerHeaderTitle = "SMART MULTI-EDGE LATENCY SCANNER"
    scannerInitialSubtitle = "Scan all available servers to find the lowest-latency edge"
    scannerLoadingCatalog = "Loading server catalog…"
    scannerEmptyCatalog = "No servers found in cache. Open Locations first to refresh."
    scannerProbingServer = { country, city -> "Probing $country ($city)…" }
    scannerOptimalFound = { country, ms -> "Optimal server found: $country ($ms ms)" }
    scannerTimeoutMessage = "Scan finished, but TCP probes timed out on current network."
    scannerRunningButton = "Scanning Edge Servers…"
    scannerStartButton = "Scan All Locations"
    scannerConnectBestButton = { code, ms -> "Connect to #1 Fastest ($code • ${ms}ms)" }
    watchdogHeaderTitle = "ANTI-DROP WATCHDOG & HEARTBEAT"
    watchdogPulsesLabel = "HEARTBEAT PULSES"
    watchdogPulsesValue = { count -> "$count sent" }
    watchdogLastPulseLabel = "LAST PULSE"
    watchdogAutoReconnectTitle = "Auto-Reconnect Watchdog"
    watchdogAutoReconnectSub = "Automatically rotates edges and redials H2 tunnel on drop"
    watchdogKeepAliveTitle = "Active Keep-Alive Heartbeat"
    watchdogKeepAliveSub = "Prevents NAT/CGNAT idle timeouts on mobile & Wi-Fi"
    watchdogIntervalLabel = "Heartbeat Probe Interval"
    watchdogSecondsUnit = { sec -> "${sec}s" }
    watchdogConnectFirstButton = "Connect VPN to Send Heartbeat"
    watchdogSendingPulseButton = "Sending Keep-Alive Pulse…"
    watchdogSendNowButton = { ping -> "Send Immediate Heartbeat ($ping)" }
    mtuInitialStatus = "Test MTU packet sizes & TLS SNI fragmentation on your ISP"
    mtuProbingStatus = { mtu, mode -> "Probing TLS Handshake: MTU $mtu + $mode…" }
    mtuOptimalFoundStatus = { mtu, mode, ms -> "Optimal profile verified: MTU $mtu + $mode ($ms ms TLS RTT)" }
    mtuTimeoutStatus = "TLS probe timed out on current network"
    mtuAppliedStatus = { mtu, mode -> "Applied MTU $mtu & TLS $mode!" }
    mtuActiveLabel = "ACTIVE MTU"
    mtuBytesValue = { mtu -> "$mtu bytes" }
    mtuFragmentLabel = "TLS FRAGMENT"
    mtuProbingButton = "Probing MTU & TLS Profiles…"
    mtuFindBestButton = "Find Best MTU & Anti-DPI Profile"
    mtuApplyBestButton = { mtu, mode -> "Apply Optimal (MTU $mtu • $mode)" }
    locationsTitle = "Server Locations"
    searchLocationsPlaceholder = "Search countries or cities…"
    noActiveServers = "No active servers"
    extraEdgesSuffix = { count -> "+$count edges" }
    logsTitle = { vis, total -> if (vis == total) "Logs ($total)" else "Logs ($vis/$total)" }
    logFilterAll = "All"
    logFilterInfo = "Info"
    logFilterProblems = "Problems"
    logsEmpty = "No log entries yet."
    logsNoProblems = "No warnings or errors logged."
    logsNothingMatches = "Nothing matches this filter."
    logsCopiedToast = "Logs copied to clipboard"
    loginTitle = "Sign in to FoxyVpn"
    loginSubtitle = "Connect with your official account to utilize 50 GB of fast, encrypted monthly VPN traffic."
    loginEmailLabel = "Account Email"
    loginPasswordLabel = "Password"
    loginContinueButton = "Continue"
    loginTwoFactorPrompt = "Enter the confirmation code sent to your email"
    loginTwoFactorCodeLabel = "6-Digit Code"
    loginVerifyButton = "Verify & Sign In"
    settingsTitle = "Settings"
    sectionLanguageAndTheme = "LANGUAGE & APPEARANCE"
    appLanguageTitle = "App Language"
    appLanguageSubtitle = "Switch interface language and layout direction (LTR / RTL)"
    appThemeTitle = "Appearance Mode"
    themeSystem = "System"
    themeLight = "Light"
    themeDark = "Dark"
    colorPaletteTitle = "Accent Color Palette"
    colorPaletteSub = "Choose a harmonious, eye-friendly color combination across all cards & animations"
    heroWaveStyleTitle = "Connection Wave Animation"
    heroWaveStyleSub = "Select the screen-spanning wave & radar pulse animation style"
    islandShowQuickAppsTitle = "Show Quick-Launch Apps Row"
    islandShowQuickAppsSub = "Enable or disable the YouTube, Telegram, Chrome & custom app shortcuts inside Dynamic Island"
    sectionSmartToolkit = "SMART NETWORK & SECURITY TOOLKIT"
    sectionDynamicIsland = "DYNAMIC ISLAND"
    enableDynamicIslandTitle = "Enable Dynamic Island"
    enableDynamicIslandSub = "Shows a native translucent morphing island at the top"
    overlayIslandTitle = "Display Over Other Apps"
    overlayIslandSubGranted = "Floating island stays visible across all apps"
    overlayIslandSubNeedsPerm = "Requires Android overlay permission. Tap to grant."
    islandSizeTitle = "Island Size Preset"
    islandCornerRadiusTitle = "Corner Roundness"
    islandPositionTitle = "Alignment & Position"
    islandThemeTitle = "Visual Style"
    islandShowSpeedTitle = "Show Live Transfer Speed"
    islandShowSpeedSub = "Displays live upload and download rates on the island"
    islandShowPingTitle = "Show Ping Latency"
    islandShowPingSub = "Displays measured server latency on the island"
    islandShowFlagTitle = "Show Country Badge"
    islandShowFlagSub = "Displays ISO code badge of active server"
    islandShowTimerTitle = "Show Elapsed Timer"
    islandShowTimerSub = "Displays active connection duration on the island"
    islandAnimationTitle = "Glow & Wave Animations"
    islandAnimationSub = "Smooth native drop-shadow glow and signal tower waves"
    islandMotionStyleTitle = "Smooth Motion & Physics Preset"
    islandMotionStyleSub = "Select 120Hz fluid spring physics, levitation, or shimmer effects for Dynamic Island"
    islandLedBorderTitle = "Automotive RGB Chasing LED Border"
    islandLedBorderSub = "Dynamic multi-color sequential LED strip around the island card"
    islandLedStyleTitle = "LED Strip Color Spectrum (14 Presets)"
    islandLedSpeedTitle = "LED Chase Animation Speed"
    islandShowTowerWaveTitle = "Show Signal Tower Broadcast Wave"
    islandShowTowerWaveSub = "Radiating antenna mast signal waves inside Dynamic Island"
    islandShowServerNameTitle = "Show Server Country Name"
    islandShowServerNameSub = "Displays active server location name on the island"
    islandShowSessionDataTitle = "Show Session Data Used"
    islandShowSessionDataSub = "Displays total session traffic inside expanded island"
    islandShowIpBadgeTitle = "Show Exit IP & Protocol Badge"
    islandShowIpBadgeSub = "Displays verified exit IP and tunnel encryption badge"
    islandDraggableTitle = "Touch Drag & Free Move"
    islandDraggableSub = "Drag the Dynamic Island anywhere on screen by touch"
    islandResetPositionButton = "Reset Island Drag Position"
    sectionSmartAutomation = "NETWORK POLICY, BATTERY & AUTOMATION"
    unmeteredSpeedBoostTitle = "Mark VPN as unmetered"
    unmeteredSpeedBoostSub = "Lets Android treat the tunnel as unmetered for app policies; it does not change carrier billing."
    tcpBufferBoostTitle = "Turbo TCP/UDP Socket Buffer (256KB / 1MB)"
    tcpBufferBoostSub = "Expands native tun2socks socket buffers for higher peak throughput & lower packet loss"
    batterySaverModeTitle = "Smart Battery Saver (Eco Mode)"
    batterySaverModeSub = "Releases the high-performance Wi-Fi lock and updates speed stats every 5 seconds; tunnel health checks stay active"
    batteryExemptionTitle = "Unrestricted Background Battery"
    batteryExemptionGrantedSub = "Exempt from Android Doze battery restrictions (prevents background disconnects)"
    batteryExemptionRequestSub = "Tap to exempt FoxyVpn from Android battery restrictions for stable background connections"
    scheduleVpnTitle = "Scheduled Auto-On / Auto-Off"
    scheduleVpnSub = "Automatically turn VPN on and off at your scheduled hours"
    scheduleTimeWindowTitle = "Active Schedule Window"
    scheduleFromLabel = "Turn On At"
    scheduleToLabel = "Turn Off At"
    idleDisconnectTitle = "Smart Idle Auto-Disconnect"
    idleDisconnectSub = "Automatically turns off VPN when no traffic is used to save battery & data"
    idleTimeoutTitle = "Inactivity Timeout Before Disconnect"
    sessionDataLimitTitle = "Session Data Cap Auto-Cutoff"
    sessionDataLimitSub = "Automatically disconnect when session traffic reaches your limit"
    sessionDataLimitAmountTitle = "Session Data Limit"
    smartPingSwitchTitle = "Smart Ping Auto-Select"
    smartPingSwitchSub = "After a manual scan, saves the most stable server as preferred; tap Connect to switch"
    autoConnectLaunchTitle = "Auto-Connect on App Launch"
    autoConnectLaunchSub = "Immediately establish encrypted tunnel when FoxyVpn opens"
    stealthNotificationTitle = "Stealth Lockscreen Notification"
    stealthNotificationSub = "Hide server country and IP details on lockscreen notifications"
    sectionPermissions = "SYSTEM PERMISSIONS & INSTALLER"
    installPermissionTitle = "Install Unknown Apps Permission"
    installPermissionGrantedSub = "Authorized to install direct APK updates seamlessly"
    installPermissionRequestSub = "Required for direct package installation. Tap to grant permission."
    notificationPermissionTitle = "Live Status Notifications"
    notificationPermissionGrantedSub = "Active for real-time VPN tunnel status"
    notificationPermissionRequestSub = "Tap to allow status bar tunnel notifications"
    sectionConnection = "CONNECTION"
    verifyExitTitle = "Verify Exit Location"
    verifyExitSub = "Verify that tunnel traffic exits in the selected country"
    tunnelMtuTitle = "Tunnel MTU"
    sectionDns = "DNS CONFIGURATION"
    tunnelDnsTitle = "Tunnel DNS"
    resolverDnsTitle = "Bootstrap Resolver DNS"
    sectionLocalProxy = "LOCAL PROXY"
    proxyOnlyModeTitle = "Proxy-Only Mode"
    proxyOnlyModeSub = "Run local SOCKS5/HTTP proxy servers without a TUN VPN interface"
    localBindAddressTitle = "Local Bind Address"
    socksPortTitle = "SOCKS5 Port"
    enableHttpProxyTitle = "Enable HTTP Proxy"
    enableHttpProxySub = "Run local HTTP proxy alongside SOCKS5"
    httpProxyPortTitle = "HTTP Port"
    sectionSplitTunneling = "SPLIT TUNNELING"
    excludedAppsTitle = "Excluded Apps"
    excludedAppsCount = { count -> "$count app(s) bypass VPN" }
    sectionAdvancedNetworking = "ADVANCED NETWORKING"
    customEdgeAddressTitle = "Custom Edge Address"
    tlsFragmentationTitle = "TLS Fragmentation"
    chainUpstreamProxyTitle = "Chain Through Upstream Proxy"
    chainUpstreamProxySub = "Connect to the VPN server through another proxy first"
    upstreamProxyTypeTitle = "Proxy Type"
    upstreamProxyAddressTitle = "Proxy Address"
    upstreamProxyCredentialsTitle = "Proxy Credentials"
    sectionAboutAndLogs = "DIAGNOSTICS & COMMUNITY"
    viewConnectionLogsTitle = "Connection Logs"
    viewConnectionLogsSub = "Live diagnostics, events, and protocol trace"
    versionTitle = "App Version"
    instagramTitle = "Instagram Page"
    instagramSubtitle = "@Aliem061"
    telegramTitle = "Telegram Channel"
    telegramSubtitle = "t.me/aliem_061"
    signOutButton = "Sign Out"
    dialogDohUrlLabel = "DoH URL (https://...)"
    dialogIpAddressLabel = "IP Address"
    dialogHostOrIpLabel = "Server Host or IP"
    dialogPortRangeLabel = "Port (1–65535)"
    dialogUsernameLabel = "Username"
    dialogSearchAppsPlaceholder = "Search installed applications…"
}

private fun createPersianFoxyStrings(): FoxyStrings = FoxyStrings(
    language = AppLanguage.FA,
    isRtl = true,
).apply {
    appName = "FoxyVpn"
    back = "بازگشت"
    save = "ذخیره"
    cancel = "انصراف"
    resetToDefault = { def -> "بازنشانی به پیش‌فرض (${AppLanguage.FA.localizeDigits(def)})" }
    enabled = "فعال"
    disabled = "غیرفعال"
    none = "هیچ‌کدام"
    notSet = "تنظیم نشده"
    checking = "در حال بررسی…"
    verifying = "در حال تایید…"
    testing = "در حال تست…"
    headerThemeButton = "پوسته"
    headerSpeedButton = "تست سرعت"
    headerLanguageButton = "زبان"
    headerSettingsButton = "تنظیمات"
    statusConnected = "متصل و ایمن"
    statusConnecting = "در حال برقراری تونل…"
    statusDisconnected = "بدون محافظت"
    slideToConnect = "برای اتصال بکشید"
    slideToRetry = "برای تلاش مجدد بکشید"
    connectingTapToCancel = "در حال اتصال… (لغو با لمس)"
    disconnecting = "در حال قطع اتصال…"
    slideToDisconnect = "برای قطع اتصال بکشید"
    smartToolsSection = "ابزارهای هوشمند شبکه"
    openAllTools = "مشاهده همه"
    serverLocationLabel = "موقعیت سرور"
    recommendedFastest = "پیشنهادی (سریع‌ترین)"
    recommendedSubtitle = "انتخاب هوشمند سرور با کمترین پینگ"
    currentIpTapLeakTest = "آی‌پی فعلی • تست نشتی"
    badgeProtected = "محافظت‌شده"
    badgeExposed = "بدون محافظت"
    dataUsedLabel = "مصرف نشست"
    latencyLabel = "پینگ سرور"
    monthlyQuotaLabel = "حجم ماهانه حساب"
    unlimitedQuota = "نامحدود"
    quotaLimitPrefix = "سقف:"
    islandConnecting = "در حال اتصال…"
    islandReady = "آماده"
    islandServerDefault = "سرور FoxyVpn"
    islandActiveEncrypted = "متصل و رمزنگاری‌شده"
    islandSecuringConnection = "در حال ایمن‌سازی…"
    islandProtectedReady = "آماده اتصال امن"
    islandLatency = "پینگ"
    islandDuration = "مدت اتصال"
    islandLiveSpeed = "سرعت لحظه‌ای"
    islandChangeServer = "تغییر سرور"
    islandConnect = "اتصال"
    islandDisconnect = "قطع اتصال"
    islandCancel = "لغو"
    islandCustomizeButton = "شخصی‌سازی آیلند"
    islandCustomizeModalTitle = "شخصی‌سازی داینامیک آیلند"
    islandCustomizeModalSub = "تنظیم اندازه، محتوا و میانبر برنامه‌ها"
    islandManualScaleTitle = "اندازه داینامیک آیلند"
    islandManualScaleSub = "تغییر اندازه کپسول از مینی تا کامل"
    islandPinnedAppsTitle = "انتخاب برنامه‌های دسترسی سریع در داینامیک آیلند"
    islandPinnedAppsSub = "افزودن برنامه‌ها به نوار دسترسی سریع داخل داینامیک آیلند جهت باز کردن سریع (مرتبط با تفکیک تانل نیست)"
    islandAddAppsButton = "مدیریت برنامه‌ها"
    islandNoPinnedApps = "برنامه‌ای اضافه نشده — لمس برای افزودن"
    islandContentSectionTitle = "محتوای داینامیک آیلند"
    toolkitScreenTitle = "ابزارهای شبکه و امنیت"
    toolSpeedTitle = "تست سرعت"
    toolSpeedSubtitle = "سنجش دانلود، آپلود و پینگ"
    toolLeakTitle = "تست نشتی"
    toolLeakSubtitle = "بررسی آی‌پی و دی‌ان‌اس امن"
    toolScannerTitle = "اسکنر سرور"
    toolScannerSubtitle = "یافتن سریع‌ترین سرور فعال"
    toolWatchdogTitle = "نگهبان اتصال"
    toolWatchdogSubtitle = "تثبیت پینگ و اتصال خودکار"
    toolMtuTitle = "بهینه‌ساز بسته"
    toolMtuSubtitle = "تنظیم هوشمند MTU و فرگمنت"
    speedReadyStage = "آماده سنجش واقعی سرعت شبکه و تونل"
    speedMeasuringPingStage = "در حال اندازه‌گیری پینگ و نوسان (۵ نمونه)…"
    speedTestingTunnelDlStage = "در حال تست سرعت دانلود از داخل تونل امن…"
    speedTestingDirectDlStage = "در حال تست سرعت دانلود مستقیم…"
    speedTestingTunnelUlStage = "در حال تست سرعت آپلود از داخل تونل امن…"
    speedTestingDirectUlStage = "در حال تست سرعت آپلود مستقیم…"
    speedDoneTunnelStage = "تست سرعت با موفقیت از مسیر تونل انجام شد"
    speedDoneDirectStage = "تست سرعت روی اینترنت مستقیم انجام شد"
    speedModeTunnel = "حالت تونل امن (عبور از پراکسی داخلی)"
    speedModeDirect = "حالت مستقیم (وی‌پی‌ان خاموش است)"
    speedDownloadLabel = "سرعت دانلود"
    speedUploadLabel = "سرعت آپلود"
    speedPingLabel = "پینگ"
    speedJitterLabel = "نوسان پینگ"
    speedRunningButton = "در حال انجام تست سرعت…"
    speedStartButton = "شروع تست سرعت واقعی"
    speedUnitMbps = "مگابیت/ث"
    leakScanningTitle = "در حال بررسی امنیت آی‌پی و دی‌ان‌اس…"
    leakProtectedTitle = "بدون نشتی • مخفی‌سازی کامل آی‌پی"
    leakVerifyingTitle = "تونل فعال • در حال بررسی خروجی"
    leakExposedTitle = "وی‌پی‌ان قطع است • آی‌پی اصلی نمایان است"
    leakDirectIpLabel = "آی‌پی مستقیم اینترنت"
    leakVpnIpLabel = "آی‌پی خروجی تونل"
    leakNotConnected = "متصل نیست"
    leakProbing = "در حال بررسی…"
    leakColoLabel = "دیتاسنتر کلودفلر"
    leakProtocolLabel = "پروتکل ارتباطی"
    leakSniLabel = "وضعیت رمزنگاری نام دامنه"
    leakDohLabel = "تست دی‌ان‌اس امن"
    leakDohVerified = { ms, ip -> "تایید شده در ${AppLanguage.FA.localizeNumber(ms)} میلی‌ثانیه${AppLanguage.FA.localizeDigits(ip)}" }
    leakIpv6Label = "محافظت در برابر نشتی نسل شش"
    leakIpv6Protected = "فعال (مسدودسازی کامل نشتی)"
    leakIpv6Inactive = "غیرفعال (اینترنت مستقیم)"
    leakScanningButton = "در حال اسکن نشتی…"
    leakRescanButton = "بررسی مجدد آی‌پی و نشتی"
    scannerHeaderTitle = "اسکنر هوشمند کمترین پینگ سرورها"
    scannerInitialSubtitle = "تمام سرورها را برای یافتن سریع‌ترین موقعیت اسکن کنید"
    scannerLoadingCatalog = "در حال بارگذاری فهرست سرورها…"
    scannerEmptyCatalog = "سروری در حافظه یافت نشد. ابتدا بخش سرورها را باز کنید."
    scannerProbingServer = { country, city -> "در حال تست سرور ${AppLanguage.FA.localizeCountryName(country)} (${AppLanguage.FA.localizeDigits(city)})…" }
    scannerOptimalFound = { country, ms -> "بهترین سرور یافت شد: ${AppLanguage.FA.localizeCountryName(country)} (${AppLanguage.FA.localizeNumber(ms)} میلی‌ثانیه)" }
    scannerTimeoutMessage = "اسکن پایان یافت اما پاسخ شبکه دریافت نشد."
    scannerRunningButton = "در حال اسکن سرورها…"
    scannerStartButton = "شروع اسکن تمام سرورها"
    scannerConnectBestButton = { code, ms -> "اتصال به سریع‌ترین سرور (${AppLanguage.FA.localizeCountryName(code, code)} • ${AppLanguage.FA.localizeNumber(ms)} م‌ث)" }
    watchdogHeaderTitle = "نگهبان پایداری اتصال و تثبیت پینگ"
    watchdogPulsesLabel = "پالس‌های ارسالی"
    watchdogPulsesValue = { count -> "${AppLanguage.FA.localizeNumber(count)} پالس" }
    watchdogLastPulseLabel = "آخرین پالس"
    watchdogAutoReconnectTitle = "اتصال مجدد خودکار هنگام قطعی"
    watchdogAutoReconnectSub = "در صورت افت شبکه، سرور جایگزین را بدون قطعی متصل می‌کند"
    watchdogKeepAliveTitle = "ارسال پالس تثبیت‌کننده پینگ"
    watchdogKeepAliveSub = "جلوگیری از به خواب رفتن تونل روی اینترنت همراه و وای‌فای"
    watchdogIntervalLabel = "فاصله زمانی ارسال پالس"
    watchdogSecondsUnit = { sec -> "${AppLanguage.FA.localizeNumber(sec)} ثانیه" }
    watchdogConnectFirstButton = "ابتدا وی‌پی‌ان را متصل کنید"
    watchdogSendingPulseButton = "در حال ارسال پالس…"
    watchdogSendNowButton = { ping -> "ارسال فوری پالس تثبیت (${AppLanguage.FA.localizeDigits(ping)})" }
    mtuInitialStatus = "بهترین اندازه بسته و حالت فرگمنت را روی اینترنت خود بسنجید"
    mtuProbingStatus = { mtu, mode -> "در حال تست اندازه ${AppLanguage.FA.localizeNumber(mtu)} با فرگمنت $mode…" }
    mtuOptimalFoundStatus = { mtu, mode, ms -> "بهترین ترکیب تایید شد: اندازه ${AppLanguage.FA.localizeNumber(mtu)} + $mode (${AppLanguage.FA.localizeNumber(ms)} م‌ث)" }
    mtuTimeoutStatus = "پاسخی از سرور روی شبکه فعلی دریافت نشد"
    mtuAppliedStatus = { mtu, mode -> "ترکیب بهینه ${AppLanguage.FA.localizeNumber(mtu)} و $mode با موفقیت اعمال شد!" }
    mtuActiveLabel = "اندازه بسته فعلی"
    mtuBytesValue = { mtu -> "${AppLanguage.FA.localizeNumber(mtu)} بایت" }
    mtuFragmentLabel = "حالت فرگمنت"
    mtuProbingButton = "در حال تست ترکیب‌های شبکه…"
    mtuFindBestButton = "یافتن هوشمند بهترین تنظیمات"
    mtuApplyBestButton = { mtu, mode -> "اعمال تنظیمات بهینه (${AppLanguage.FA.localizeNumber(mtu)} • $mode)" }
    locationsTitle = "انتخاب سرور"
    searchLocationsPlaceholder = "جستجوی کشور یا شهر…"
    noActiveServers = "سرور فعالی موجود نیست"
    extraEdgesSuffix = { count -> "+${AppLanguage.FA.localizeNumber(count)} سرور دیگر" }
    logsTitle = { vis, total ->
        if (vis == total) "گزارش‌ها (${AppLanguage.FA.localizeNumber(total)})"
        else "گزارش‌ها (${AppLanguage.FA.localizeNumber(vis)} از ${AppLanguage.FA.localizeNumber(total)})"
    }
    logFilterAll = "همه"
    logFilterInfo = "اطلاعات"
    logFilterProblems = "خطاها"
    logsEmpty = "هنوز گزارشی ثبت نشده است."
    logsNoProblems = "هیچ هشدار یا خطایی ثبت نشده است."
    logsNothingMatches = "موردی با این فیلتر مطابقت ندارد."
    logsCopiedToast = "گزارش‌ها در حافظه کپی شد"
    loginTitle = "ورود به حساب FoxyVpn"
    loginSubtitle = "با حساب خود وارد شوید تا از ۵۰ گیگابایت ترافیک امن ماهانه استفاده کنید."
    loginEmailLabel = "آدرس ایمیل حساب"
    loginPasswordLabel = "رمز عبور"
    loginContinueButton = "ادامه و ورود"
    loginTwoFactorPrompt = "کد تایید ارسال‌شده به ایمیل خود را وارد کنید"
    loginTwoFactorCodeLabel = "کد ۶ رقمی تایید"
    loginVerifyButton = "تایید و ورود"
    settingsTitle = "تنظیمات"
    sectionLanguageAndTheme = "زبان و ظاهر برنامه"
    appLanguageTitle = "زبان برنامه"
    appLanguageSubtitle = "تغییر زبان و جهت چیدمان رابط کاربری (راست‌چین / چپ‌چین)"
    appThemeTitle = "پوسته ظاهری"
    themeSystem = "سیستم"
    themeLight = "روشن"
    themeDark = "تاریک"
    colorPaletteTitle = "ترکیب رنگ و تم برنامه"
    colorPaletteSub = "انتخاب پالت رنگ چشم‌نواز و کاربرپسند برای تمام کارت‌ها، دکمه‌ها و انیمیشن‌ها"
    heroWaveStyleTitle = "سبک انیمیشن امواج اتصال"
    heroWaveStyleSub = "انتخاب مدل امواج اتصال و رادار صفحه اصلی (۵ انیمیشن متنوع)"
    islandShowQuickAppsTitle = "نمایش نوار دسترسی سریع برنامه‌ها (یوتیوب، تلگرام و کروم)"
    islandShowQuickAppsSub = "فعال یا غیرفعال کردن میانبرهای سریع برنامه‌ها و امکان افزودن برنامه جدید در داینامیک آیلند"
    sectionSmartToolkit = "جعبه ابزار هوشمند شبکه و امنیت"
    sectionDynamicIsland = "داینامیک آیلند (جزیره پویا)"
    enableDynamicIslandTitle = "فعال‌سازی داینامیک آیلند"
    enableDynamicIslandSub = "نمایش نوار تعاملی با سایه و شفافیت نیتیو در بالای صفحه"
    overlayIslandTitle = "نمایش شناور بیرون از برنامه"
    overlayIslandSubGranted = "وقتی از برنامه خارج می‌شوید، داینامیک آیلند بالای سایر برنامه‌ها نمایش داده می‌شود"
    overlayIslandSubNeedsPerm = "نیازمند مجوز نمایش روی برنامه‌ها در اندروید است. برای فعال‌سازی لمس کنید."
    islandSizeTitle = "قالب اندازه داینامیک آیلند"
    islandCornerRadiusTitle = "میزان نرمی و گردی گوشه‌ها"
    islandPositionTitle = "موقعیت قرارگیری"
    islandThemeTitle = "سبک ظاهری و شفافیت"
    islandShowSpeedTitle = "نمایش سرعت لحظه‌ای"
    islandShowSpeedSub = "نمایش نرخ دانلود و آپلود زنده روی داینامیک آیلند"
    islandShowPingTitle = "نمایش پینگ سرور"
    islandShowPingSub = "نمایش تاخیر سرور روی داینامیک آیلند"
    islandShowFlagTitle = "نمایش نشان کشور"
    islandShowFlagSub = "نمایش کد کشور سرور فعال"
    islandShowTimerTitle = "نمایش زمان‌سنج اتصال"
    islandShowTimerSub = "نمایش مدت زمان برقراری اتصال"
    islandAnimationTitle = "جلوه‌های نوری و سایه نیتیو"
    islandAnimationSub = "انیمیشن امواج اتصال و درخشش نرم اطراف داینامیک آیلند"
    islandMotionStyleTitle = "سبک انیمیشن و حرکت روان داینامیک آیلند"
    islandMotionStyleSub = "انتخاب فیزیک حرکتی ۱۲۰ هرتز، شناوری بی‌وزنی، تپش هماهنگ با ترافیک یا درخشش منشوری"
    islandLedBorderTitle = "نوار ال‌ای‌دی متحرک"
    islandLedBorderSub = "نوار نوری رنگی و متحرک دور تا دور کپسول داینامیک آیلند"
    islandLedStyleTitle = "سبک انیمیشن نوار ال‌ای‌دی (۱۴ مدل متنوع)"
    islandLedSpeedTitle = "سرعت چرخش نوار ال‌ای‌دی"
    islandShowTowerWaveTitle = "نمایش امواج اتصال در داینامیک آیلند"
    islandShowTowerWaveSub = "نمایش انیمیشن زنده امواج اتصال در کنار وضعیت سرور"
    islandShowServerNameTitle = "نمایش نام کشور سرور"
    islandShowServerNameSub = "نمایش نام موقعیت سرور فعال در داینامیک آیلند"
    islandShowSessionDataTitle = "نمایش حجم مصرف‌شده نشست"
    islandShowSessionDataSub = "نمایش میزان ترافیک مصرفی در نمای باز داینامیک آیلند"
    islandShowIpBadgeTitle = "نمایش نشان آی‌پی و پروتکل"
    islandShowIpBadgeSub = "نمایش آی‌پی خروجی و وضعیت رمزنگاری در داینامیک آیلند"
    islandDraggableTitle = "جابجایی آزادانه با لمس"
    islandDraggableSub = "امکان تغییر مکان داینامیک آیلند در صفحه و خارج از برنامه با کشیدن انگشت"
    islandResetPositionButton = "بازنشانی موقعیت داینامیک آیلند"
    sectionSmartAutomation = "سیاست شبکه، باتری و خودکارسازی"
    unmeteredSpeedBoostTitle = "اعلام تونل به‌عنوان اینترنت نامحدود به Android"
    unmeteredSpeedBoostSub = "سیاست مصرف برنامه‌ها در Android را تغییر می‌دهد؛ هزینه یا محدودیت اپراتور را تغییر نمی‌دهد."
    tcpBufferBoostTitle = "تقویت بافر شبکه توربو (۲۵۶ کیلوبایت / ۱ مگابایت)"
    tcpBufferBoostSub = "افزایش ۴ برابری ظرفیت بافر هسته تونل برای حداکثر سرعت دانلود و کاهش افت پکت"
    batterySaverModeTitle = "بهینه‌سازی هوشمند مصرف باتری (حالت اکو)"
    batterySaverModeSub = "قفل پرمصرف وای‌فای آزاد می‌شود و آمار سرعت هر ۵ ثانیه به‌روز می‌شود؛ بررسی سلامت تونل فعال می‌ماند"
    batteryExemptionTitle = "فعالیت پایدار در پس‌زمینه (بدون محدودیت باتری)"
    batteryExemptionGrantedSub = "فعال — سیستم اندروید اتصال وی‌پی‌ان را در پس‌زمینه قطع نمی‌کند"
    batteryExemptionRequestSub = "باز کردن تنظیمات استاندارد بهینه‌سازی باتری اندروید برای جلوگیری از قطع خودکار اتصال"
    scheduleVpnTitle = "زمان‌بندی خودکار روشن و خاموش شدن"
    scheduleVpnSub = "تنظیم ساعت مشخص برای اتصال و قطع خودکار فیلترشکن"
    scheduleTimeWindowTitle = "بازه زمانی فعالیت خودکار"
    scheduleFromLabel = "ساعت روشن شدن"
    scheduleToLabel = "ساعت خاموش شدن"
    idleDisconnectTitle = "خاموشی خودکار هنگام عدم استفاده"
    idleDisconnectSub = "وقتی از فیلترشکن استفاده نمی‌کنید، برای صرفه‌جویی در باتری و حجم خودکار خاموش شود"
    idleTimeoutTitle = "مدت زمان بیکاری قبل از خاموشی"
    sessionDataLimitTitle = "قطع خودکار در سقف مصرف حجم"
    sessionDataLimitSub = "قطع خودکار اتصال پس از رسیدن مصرف نشست به سقف تعیین‌شده"
    sessionDataLimitAmountTitle = "سقف مجاز مصرف نشست"
    smartPingSwitchTitle = "انتخاب خودکار سرور با پینگ"
    smartPingSwitchSub = "پس از اسکن دستی، پایدارترین سرور به‌عنوان گزینهٔ منتخب ذخیره می‌شود؛ اتصال با لمس دکمه انجام می‌شود"
    autoConnectLaunchTitle = "اتصال خودکار به محض ورود به برنامه"
    autoConnectLaunchSub = "برقراری فوری تونل امن به محض باز کردن FoxyVpn"
    stealthNotificationTitle = "اعلان مخفی و حریم خصوصی قفل صفحه"
    stealthNotificationSub = "مخفی‌سازی نام کشور و آی‌پی در نوار اعلان قفل صفحه"
    sectionPermissions = "مجوزهای استاندارد اندروید"
    installPermissionTitle = "مجوز نصب به‌روزرسانی‌های برنامه"
    installPermissionGrantedSub = "مجوز استاندارد اندروید برای نصب مستقیم بسته‌های به‌روزرسانی فعال است"
    installPermissionRequestSub = "باز کردن صفحه مجوز استاندارد اندروید برای امکان نصب مستقیم نسخه‌های جدید"
    notificationPermissionTitle = "مجوز نمایش وضعیت در نوار اعلان"
    notificationPermissionGrantedSub = "فعال برای نمایش لحظه‌ای وضعیت اتصال در نوار اعلان اندروید"
    notificationPermissionRequestSub = "درخواست مجوز استاندارد اعلان اندروید برای نمایش وضعیت اتصال"
    sectionConnection = "تنظیمات اتصال"
    verifyExitTitle = "بررسی صحت آی‌پی خروجی"
    verifyExitSub = "اطمینان از تطابق کشور خروجی تونل با سرور انتخاب‌شده"
    tunnelMtuTitle = "اندازه بسته تونل"
    sectionDns = "تنظیمات دی‌ان‌اس"
    tunnelDnsTitle = "دی‌ان‌اس داخل تونل"
    resolverDnsTitle = "دی‌ان‌اس اولیه"
    sectionLocalProxy = "پراکسی محلی"
    proxyOnlyModeTitle = "حالت فقط پراکسی"
    proxyOnlyModeSub = "اجرای پراکسی محلی بدون ایجاد کارت شبکه مجازی"
    localBindAddressTitle = "آدرس اتصال محلی"
    socksPortTitle = "پورت پراکسی ساکس"
    enableHttpProxyTitle = "فعال‌سازی پراکسی اچ‌تی‌تی‌پی"
    enableHttpProxySub = "اجرای همزمان پراکسی وب در کنار ساکس"
    httpProxyPortTitle = "پورت پراکسی وب"
    sectionSplitTunneling = "تونل‌سازی تفکیکی برنامه‌ها"
    excludedAppsTitle = "برنامه‌های مستثنی‌شده"
    excludedAppsCount = { count -> "${AppLanguage.FA.localizeNumber(count)} برنامه خارج از تونل" }
    sectionAdvancedNetworking = "شبکه پیشرفته و ضد فیلترینگ"
    customEdgeAddressTitle = "آدرس سرور جایگزین"
    tlsFragmentationTitle = "تکه‌تکه‌سازی بسته‌های امنیتی"
    chainUpstreamProxyTitle = "زنجیره‌سازی با پراکسی بالادستی"
    chainUpstreamProxySub = "اتصال به سرور از طریق یک پراکسی واسط"
    upstreamProxyTypeTitle = "نوع پراکسی واسط"
    upstreamProxyAddressTitle = "آدرس پراکسی واسط"
    upstreamProxyCredentialsTitle = "اطلاعات ورود پراکسی"
    sectionAboutAndLogs = "گزارش‌ها و شبکه‌های اجتماعی"
    viewConnectionLogsTitle = "مشاهده گزارش‌های اتصال"
    viewConnectionLogsSub = "جزئیات فنی، رویدادها و خطایابی زنده"
    versionTitle = "نسخه برنامه"
    instagramTitle = "صفحه اینستاگرام"
    instagramSubtitle = "Aliem061"
    telegramTitle = "کانال تلگرام"
    telegramSubtitle = "t.me/aliem_061"
    signOutButton = "خروج از حساب کاربری"
    dialogDohUrlLabel = "آدرس دی‌ان‌اس امن"
    dialogIpAddressLabel = "آدرس آی‌پی"
    dialogHostOrIpLabel = "آدرس میزبان یا آی‌پی"
    dialogPortRangeLabel = "شماره پورت (۱ تا ۶۵۵۳۵)"
    dialogUsernameLabel = "نام کاربری"
    dialogSearchAppsPlaceholder = "جستجوی برنامه‌های نصب‌شده…"
}

private fun createArabicFoxyStrings(): FoxyStrings = FoxyStrings(
    language = AppLanguage.AR,
    isRtl = true,
).apply {
    appName = "FoxyVpn"
    back = "رجوع"
    save = "حفظ"
    cancel = "إلغاء"
    resetToDefault = { def -> "إعادة إلى الافتراضي (${AppLanguage.AR.localizeDigits(def)})" }
    enabled = "مفعّل"
    disabled = "معطّل"
    none = "لا يوجد"
    notSet = "غير محدد"
    checking = "جارٍ الفحص…"
    verifying = "جارٍ التحقق…"
    testing = "جارٍ الاختبار…"
    headerThemeButton = "المظهر"
    headerSpeedButton = "اختبار السرعة"
    headerLanguageButton = "اللغة"
    headerSettingsButton = "الإعدادات"
    statusConnected = "متصل وآمن"
    statusConnecting = "جارٍ إنشاء النفق الآمن…"
    statusDisconnected = "غير محمي"
    slideToConnect = "اسحب للاتصال"
    slideToRetry = "اسحب لإعادة المحاولة"
    connectingTapToCancel = "جارٍ الاتصال… (اضغط للإلغاء)"
    disconnecting = "جارٍ قطع الاتصال…"
    slideToDisconnect = "اسحب لقطع الاتصال"
    smartToolsSection = "أدوات الشبكة الذكية"
    openAllTools = "عرض الكل"
    serverLocationLabel = "موقع الخادم"
    recommendedFastest = "الموصى به (الأسرع)"
    recommendedSubtitle = "اختيار تلقائي لأسرع خادم"
    currentIpTapLeakTest = "الآي بي الحالي • فحص التسرب"
    badgeProtected = "محمي"
    badgeExposed = "مكشوف"
    dataUsedLabel = "البيانات المستهلكة"
    latencyLabel = "زمن الاستجابة"
    monthlyQuotaLabel = "الحصة الشهرية"
    unlimitedQuota = "غير محدود"
    quotaLimitPrefix = "الحد:"
    islandConnecting = "جارٍ الاتصال…"
    islandReady = "جاهز"
    islandServerDefault = "خادم FoxyVpn"
    islandActiveEncrypted = "متصل ومشفّر"
    islandSecuringConnection = "جارٍ التأمين…"
    islandProtectedReady = "جاهز للاتصال"
    islandLatency = "الاستجابة"
    islandDuration = "المدة"
    islandLiveSpeed = "السرعة الحية"
    islandChangeServer = "تغيير الخادم"
    islandConnect = "اتصال"
    islandDisconnect = "قطع الاتصال"
    islandCancel = "إلغاء"
    islandCustomizeButton = "تخصيص الجزيرة"
    islandCustomizeModalTitle = "تخصيص الجزيرة التفاعلية"
    islandCustomizeModalSub = "تحكم في الحجم والمحتوى والتطبيقات"
    islandManualScaleTitle = "حجم الجزيرة التفاعلية"
    islandManualScaleSub = "تغيير الحجم من مصغر إلى كامل"
    islandPinnedAppsTitle = "اختيار تطبيقات الوصول السريع في الجزيرة التفاعلية"
    islandPinnedAppsSub = "إضافة تطبيقات لشريط التشغيل السريع داخل الجزيرة (منفصل عن تقسيم النفق)"
    islandAddAppsButton = "إدارة التطبيقات"
    islandNoPinnedApps = "لا توجد تطبيقات — اضغط للإضافة"
    islandContentSectionTitle = "محتوى الجزيرة التفاعلية"
    toolkitScreenTitle = "أدوات الشبكة والأمان"
    toolSpeedTitle = "اختبار السرعة"
    toolSpeedSubtitle = "قياس التنزيل والرفع والاستجابة"
    toolLeakTitle = "فحص التسرب"
    toolLeakSubtitle = "فحص الآي بي ونظام الأسماء"
    toolScannerTitle = "الماسح الذكي"
    toolScannerSubtitle = "البحث عن أسرع خادم متاح"
    toolWatchdogTitle = "حارس الثبات"
    toolWatchdogSubtitle = "تثبيت الاتصال وإعادة الربط"
    toolMtuTitle = "محسّن الحزم"
    toolMtuSubtitle = "ضبط MTU وتجزئة التشفير"
    speedReadyStage = "جاهز لقياس سرعة الشبكة الحقيقية"
    speedMeasuringPingStage = "جارٍ قياس الاستجابة والتذبذب (٥ عينات)…"
    speedTestingTunnelDlStage = "جارٍ اختبار سرعة التنزيل عبر النفق المشفّر…"
    speedTestingDirectDlStage = "جارٍ اختبار سرعة التنزيل المباشر…"
    speedTestingTunnelUlStage = "جارٍ اختبار سرعة الرفع عبر النفق المشفّر…"
    speedTestingDirectUlStage = "جارٍ اختبار سرعة الرفع المباشر…"
    speedDoneTunnelStage = "اكتمل اختبار السرعة عبر النفق المشفّر بنجاح"
    speedDoneDirectStage = "اكتمل اختبار السرعة على الشبكة المباشرة"
    speedModeTunnel = "وضع النفق المشفّر (عبر الوكيل الداخلي)"
    speedModeDirect = "الوضع المباشر (الشبكة الافتراضية متوقفة)"
    speedDownloadLabel = "التنزيل"
    speedUploadLabel = "الرفع"
    speedPingLabel = "الاستجابة"
    speedJitterLabel = "التذبذب"
    speedRunningButton = "جارٍ إجراء اختبار السرعة…"
    speedStartButton = "بدء اختبار السرعة الحية"
    speedUnitMbps = "ميجابت/ث"
    leakScanningTitle = "جارٍ فحص أمان الآي بي ونظام الأسماء…"
    leakProtectedTitle = "لا يوجد تسرب • إخفاء تام بنسبة ١٠٠٪"
    leakVerifyingTitle = "النفق نشط • جارٍ التحقق من المخرج"
    leakExposedTitle = "الاتصال متوقف • الآي بي الحقيقي مكشوف"
    leakDirectIpLabel = "آي بي المزود المباشر"
    leakVpnIpLabel = "آي بي مخرج النفق"
    leakNotConnected = "غير متصل"
    leakProbing = "جارٍ الفحص…"
    leakColoLabel = "مركز بيانات الحافة"
    leakProtocolLabel = "بروتوكول الاتصال"
    leakSniLabel = "حالة تشفير اسم النطاق"
    leakDohLabel = "فحص نظام الأسماء المشفّر"
    leakDohVerified = { ms, ip -> "تم التحقق خلال ${AppLanguage.AR.localizeNumber(ms)} م.ث${AppLanguage.AR.localizeDigits(ip)}" }
    leakIpv6Label = "الحماية من تسرب الجيل السادس"
    leakIpv6Protected = "محمي بالكامل (حجب التسرب)"
    leakIpv6Inactive = "غير نشط (شبكة مباشرة)"
    leakScanningButton = "جارٍ فحص التسرب…"
    leakRescanButton = "إعادة فحص الآي بي والتسرب"
    scannerHeaderTitle = "الماسح الذكي لأقل زمن استجابة"
    scannerInitialSubtitle = "افحص جميع الخوادم المتاحة للعثور على الأسرع"
    scannerLoadingCatalog = "جارٍ تحميل قائمة الخوادم…"
    scannerEmptyCatalog = "لا توجد خوادم محفوظة. افتح قائمة المواقع أولاً."
    scannerProbingServer = { country, city -> "جارٍ فحص خادم ${AppLanguage.AR.localizeCountryName(country)} (${AppLanguage.AR.localizeDigits(city)})…" }
    scannerOptimalFound = { country, ms -> "تم العثور على أفضل خادم: ${AppLanguage.AR.localizeCountryName(country)} (${AppLanguage.AR.localizeNumber(ms)} م.ث)" }
    scannerTimeoutMessage = "انتهى الفحص ولكن انتهت مهلة الاستجابة على الشبكة الحالية."
    scannerRunningButton = "جارٍ فحص الخوادم…"
    scannerStartButton = "فحص جميع المواقع"
    scannerConnectBestButton = { code, ms -> "الاتصال بالأسرع (${AppLanguage.AR.localizeCountryName(code, code)} • ${AppLanguage.AR.localizeNumber(ms)} م.ث)" }
    watchdogHeaderTitle = "حارس استقرار الاتصال والنبضات"
    watchdogPulsesLabel = "النبضات المرسلة"
    watchdogPulsesValue = { count -> "${AppLanguage.AR.localizeNumber(count)} نبضة" }
    watchdogLastPulseLabel = "آخر نبضة"
    watchdogAutoReconnectTitle = "إعادة الاتصال التلقائي عند الانقطاع"
    watchdogAutoReconnectSub = "يبدل الخوادم ويعيد الاتصال تلقائياً عند ضعف الشبكة"
    watchdogKeepAliveTitle = "نبضات الحفاظ على نشاط النفق"
    watchdogKeepAliveSub = "يمنع خمول الاتصال على بيانات الهاتف والشبكة اللاسلكية"
    watchdogIntervalLabel = "الفاصل الزمني للنبضات"
    watchdogSecondsUnit = { sec -> "${AppLanguage.AR.localizeNumber(sec)} ثانية" }
    watchdogConnectFirstButton = "اتصل أولاً لإرسال نبضة"
    watchdogSendingPulseButton = "جارٍ إرسال النبضة…"
    watchdogSendNowButton = { ping -> "إرسال نبضة فورية (${AppLanguage.AR.localizeDigits(ping)})" }
    mtuInitialStatus = "اختبر أفضل حجم للحزم وتجزئة التشفير على شبكتك"
    mtuProbingStatus = { mtu, mode -> "جارٍ اختبار الحجم ${AppLanguage.AR.localizeNumber(mtu)} مع التجزئة $mode…" }
    mtuOptimalFoundStatus = { mtu, mode, ms -> "أفضل إعداد: الحجم ${AppLanguage.AR.localizeNumber(mtu)} + $mode (${AppLanguage.AR.localizeNumber(ms)} م.ث)" }
    mtuTimeoutStatus = "انتهت مهلة الاختبار على الشبكة الحالية"
    mtuAppliedStatus = { mtu, mode -> "تم تطبيق الحجم ${AppLanguage.AR.localizeNumber(mtu)} والوضع $mode بنجاح!" }
    mtuActiveLabel = "حجم الحزمة الحالي"
    mtuBytesValue = { mtu -> "${AppLanguage.AR.localizeNumber(mtu)} بايت" }
    mtuFragmentLabel = "وضع التجزئة"
    mtuProbingButton = "جارٍ فحص الإعدادات…"
    mtuFindBestButton = "البحث الذكي عن أفضل إعداد"
    mtuApplyBestButton = { mtu, mode -> "تطبيق الأفضل (${AppLanguage.AR.localizeNumber(mtu)} • $mode)" }
    locationsTitle = "مواقع الخوادم"
    searchLocationsPlaceholder = "ابحث عن دولة أو مدينة…"
    noActiveServers = "لا توجد خوادم نشطة"
    extraEdgesSuffix = { count -> "+${AppLanguage.AR.localizeNumber(count)} خادم إضافي" }
    logsTitle = { vis, total ->
        if (vis == total) "السجلات (${AppLanguage.AR.localizeNumber(total)})"
        else "السجلات (${AppLanguage.AR.localizeNumber(vis)} من ${AppLanguage.AR.localizeNumber(total)})"
    }
    logFilterAll = "الكل"
    logFilterInfo = "معلومات"
    logFilterProblems = "الأخطاء"
    logsEmpty = "لا توجد سجلات حتى الآن."
    logsNoProblems = "لم يتم تسجيل أي تحذيرات أو أخطاء."
    logsNothingMatches = "لا توجد نتائج مطابقة لهذا الفلتر."
    logsCopiedToast = "تم نسخ السجلات إلى الحافظة"
    loginTitle = "تسجيل الدخول إلى FoxyVpn"
    loginSubtitle = "سجّل الدخول بحسابك للاستفادة من ٥٠ جيجابايت شهرياً من حركة المرور المشفّرة."
    loginEmailLabel = "البريد الإلكتروني للحساب"
    loginPasswordLabel = "كلمة المرور"
    loginContinueButton = "متابعة وتسجيل الدخول"
    loginTwoFactorPrompt = "أدخل رمز التأكيد المرسل إلى بريدك الإلكتروني"
    loginTwoFactorCodeLabel = "رمز التحقق من ٦ أرقام"
    loginVerifyButton = "تحقق وتسجيل الدخول"
    settingsTitle = "الإعدادات"
    sectionLanguageAndTheme = "اللغة والمظهر"
    appLanguageTitle = "لغة التطبيق"
    appLanguageSubtitle = "تغيير لغة الواجهة واتجاه العرض (يمين لليسار / يسار لليمين)"
    appThemeTitle = "وضع المظهر"
    themeSystem = "النظام"
    themeLight = "فاتح"
    themeDark = "داكن"
    colorPaletteTitle = "لوحة ألوان التطبيق"
    colorPaletteSub = "اختر تركيبة ألوان متناسقة ومريحة للعين لجميع البطاقات والرسوم المتحركة"
    heroWaveStyleTitle = "نمط حركة موجات الاتصال"
    heroWaveStyleSub = "اختر شكل موجات الرادار والاتصال في الشاشة الرئيسية"
    islandShowQuickAppsTitle = "إظهار شريط تطبيقات الوصول السريع"
    islandShowQuickAppsSub = "تفعيل أو تعطيل اختصارات يوتيوب وتيليجرام وكروم وإضافة تطبيقات جديدة في الجزيرة التفاعلية"
    sectionSmartToolkit = "مجموعة أدوات الشبكة والأمان الذكية"
    sectionDynamicIsland = "الجزيرة التفاعلية"
    enableDynamicIslandTitle = "تفعيل الجزيرة التفاعلية"
    enableDynamicIslandSub = "إظهار شريط حالة تفاعلي بظلال وشفافية أصلية أعلى الشاشة"
    overlayIslandTitle = "العرض فوق التطبيقات الأخرى"
    overlayIslandSubGranted = "تظل الجزيرة العائمة مرئية أثناء التنقل بين التطبيقات"
    overlayIslandSubNeedsPerm = "يتطلب إذن العرض فوق التطبيقات. اضغط لمنح الإذن."
    islandSizeTitle = "نمط حجم الجزيرة"
    islandCornerRadiusTitle = "نعومة واستدارة الحواف"
    islandPositionTitle = "الموضع والمحاذاة"
    islandThemeTitle = "التصميم والشفافية"
    islandShowSpeedTitle = "إظهار السرعة الحية"
    islandShowSpeedSub = "عرض معدلات التنزيل والرفع المباشرة على الجزيرة"
    islandShowPingTitle = "إظهار زمن الاستجابة"
    islandShowPingSub = "عرض استجابة الخادم المقاسة على الجزيرة"
    islandShowFlagTitle = "إظهار رمز الدولة"
    islandShowFlagSub = "عرض رمز دولة الخادم النشط"
    islandShowTimerTitle = "إظهار مؤقت الاتصال"
    islandShowTimerSub = "عرض مدة الجلسة النشطة على الجزيرة"
    islandAnimationTitle = "تأثيرات التوهج والموجات"
    islandAnimationSub = "ظلال أصلية ناعمة وموجات برج الإشارة أثناء الاتصال"
    islandMotionStyleTitle = "نمط الحركة الفيزيائية للجزيرة"
    islandMotionStyleSub = "اختر حركة انسيابية بتردد ١٢٠ هرتز أو طفو انعدام الجاذبية أو التزامن مع السرعة"
    islandLedBorderTitle = "شريط إضاءة سيارات متحرك (RGB)"
    islandLedBorderSub = "إضاءة ملونة متحركة تحيط ببطاقة الجزيرة التفاعلية"
    islandLedStyleTitle = "نمط ألوان شريط الإضاءة (١٤ نمطاً)"
    islandLedSpeedTitle = "سرعة حركة شريط الإضاءة"
    islandShowTowerWaveTitle = "إظهار موجات برج الإشارة"
    islandShowTowerWaveSub = "عرض برج يبث موجات لاسلكية متتالية داخل الجزيرة"
    islandShowServerNameTitle = "إظهار اسم دولة الخادم"
    islandShowServerNameSub = "عرض اسم موقع الخادم النشط داخل الجزيرة"
    islandShowSessionDataTitle = "إظهار البيانات المستهلكة"
    islandShowSessionDataSub = "عرض إجمالي استهلاك الجلسة عند توسيع الجزيرة"
    islandShowIpBadgeTitle = "إظهار شارة الآي بي والبروتوكول"
    islandShowIpBadgeSub = "عرض عنوان الخروج وحالة التشفير داخل الجزيرة"
    islandDraggableTitle = "السحب والتحريك الحر باللمس"
    islandDraggableSub = "تحريك الجزيرة التفاعلية بحرية في أي مكان على الشاشة"
    islandResetPositionButton = "إعادة ضبط موضع الجزيرة"
    sectionSmartAutomation = "سياسة الشبكة والبطارية والأتمتة"
    unmeteredSpeedBoostTitle = "تعريف VPN كشبكة غير محدودة في Android"
    unmeteredSpeedBoostSub = "يغيّر سياسات التطبيقات في Android فقط، ولا يغيّر رسوم أو قيود شركة الاتصالات."
    tcpBufferBoostTitle = "مضاعفة ذاكرة الشبكة المؤقتة (٢٥٦ ك.ب / ١ م.ب)"
    tcpBufferBoostSub = "توسيع مخازن حزم النفق لتحقيق أقصى سرعة تنزيل وتقليل فقدان الحزم"
    batterySaverModeTitle = "تحسين استهلاك البطارية الذكي (الوضع الاقتصادي)"
    batterySaverModeSub = "يحرر قفل Wi-Fi عالي الأداء ويحدّث إحصاءات السرعة كل ٥ ثوانٍ مع استمرار فحوصات صحة النفق"
    batteryExemptionTitle = "العمل في الخلفية بدون قيود البطارية"
    batteryExemptionGrantedSub = "مستثنى من قيود خمول البطارية لضمان ثبات الاتصال"
    batteryExemptionRequestSub = "اضغط لاستثناء التطبيق من قيود توفير البطارية في الخلفية"
    scheduleVpnTitle = "الجدولة التلقائية للتشغيل والإيقاف"
    scheduleVpnSub = "تحديد وقت تلقائي لتشغيل وإيقاف الاتصال المشفّر"
    scheduleTimeWindowTitle = "فترة الجدولة النشطة"
    scheduleFromLabel = "وقت التشغيل"
    scheduleToLabel = "وقت الإيقاف"
    idleDisconnectTitle = "الإيقاف التلقائي عند عدم الاستخدام"
    idleDisconnectSub = "إيقاف الاتصال تلقائياً عند خمول حركة البيانات لتوفير البطارية والباقة"
    idleTimeoutTitle = "مهلة الخمول قبل الإيقاف"
    sessionDataLimitTitle = "الإيقاف التلقائي عند حد الاستهلاك"
    sessionDataLimitSub = "قطع الاتصال تلقائياً عند وصول استهلاك الجلسة إلى الحد المعين"
    sessionDataLimitAmountTitle = "حد بيانات الجلسة"
    smartPingSwitchTitle = "اختيار تلقائي عبر اختبار الاستجابة"
    smartPingSwitchSub = "بعد الفحص اليدوي يُحفظ الخادم الأكثر استقراراً كمفضل؛ اضغط اتصال للتبديل"
    autoConnectLaunchTitle = "الاتصال التلقائي عند فتح التطبيق"
    autoConnectLaunchSub = "بدء الاتصال المشفّر فوراً عند تشغيل FoxyVpn"
    stealthNotificationTitle = "إشعار الخصوصية على شاشة القفل"
    stealthNotificationSub = "إخفاء تفاصيل الدولة والآي بي من إشعار شاشة القفل"
    sectionPermissions = "أذونات النظام والتثبيت"
    installPermissionTitle = "إذن تثبيت الحزم والتحديثات"
    installPermissionGrantedSub = "مسموح بتثبيت ملفات الحزم مباشرة"
    installPermissionRequestSub = "مطلوب لتثبيت الحزم مباشرة. اضغط لمنح الإذن."
    notificationPermissionTitle = "إذن إشعارات الحالة الحية"
    notificationPermissionGrantedSub = "مفعّل لعرض حالة النفق المشفّر"
    notificationPermissionRequestSub = "اضغط للسماح بإشعارات حالة الاتصال"
    sectionConnection = "إعدادات الاتصال"
    verifyExitTitle = "التحقق من موقع الخروج"
    verifyExitSub = "التأكد من تطابق بلد الخروج مع الخادم المحدد"
    tunnelMtuTitle = "حجم حزمة النفق"
    sectionDns = "إعدادات نظام الأسماء"
    tunnelDnsTitle = "نظام الأسماء داخل النفق"
    resolverDnsTitle = "نظام الأسماء التمهيدي"
    sectionLocalProxy = "الوكيل المحلي"
    proxyOnlyModeTitle = "وضع الوكيل فقط"
    proxyOnlyModeSub = "تشغيل خادم وكيل محلي بدون إنشاء واجهة شبكة افتراضية"
    localBindAddressTitle = "عنوان الربط المحلي"
    socksPortTitle = "منفذ وكيل سوكس"
    enableHttpProxyTitle = "تفعيل وكيل الويب"
    enableHttpProxySub = "تشغيل وكيل ويب محلي بجانب وكيل سوكس"
    httpProxyPortTitle = "منفذ وكيل الويب"
    sectionSplitTunneling = "الانقسام الانتقائي للتطبيقات"
    excludedAppsTitle = "التطبيقات المستثناة"
    excludedAppsCount = { count -> "${AppLanguage.AR.localizeNumber(count)} تطبيق خارج النفق" }
    sectionAdvancedNetworking = "الشبكة المتقدمة وتجاوز الحجب"
    customEdgeAddressTitle = "عنوان الخادم البديل"
    tlsFragmentationTitle = "تجزئة حزم التشفير"
    chainUpstreamProxyTitle = "الربط عبر وكيل خارجي"
    chainUpstreamProxySub = "الاتصال بخادم الشبكة عبر وكيل وسيط أولاً"
    upstreamProxyTypeTitle = "نوع الوكيل الوسيط"
    upstreamProxyAddressTitle = "عنوان الوكيل الوسيط"
    upstreamProxyCredentialsTitle = "بيانات اعتماد الوكيل"
    sectionAboutAndLogs = "السجلات والتواصل الاجتماعي"
    viewConnectionLogsTitle = "سجلات الاتصال"
    viewConnectionLogsSub = "التشخيص المباشر والأحداث وتتبع البروتوكول"
    versionTitle = "إصدار التطبيق"
    instagramTitle = "صفحة إنستغرام"
    instagramSubtitle = "Aliem061"
    telegramTitle = "قناة تيليجرام"
    telegramSubtitle = "t.me/aliem_061"
    signOutButton = "تسجيل الخروج"
    dialogDohUrlLabel = "رابط نظام الأسماء المشفّر"
    dialogIpAddressLabel = "عنوان الآي بي"
    dialogHostOrIpLabel = "عنوان المضيف أو الآي بي"
    dialogPortRangeLabel = "المنفذ (١ إلى ٦٥۵٣۵)"
    dialogUsernameLabel = "اسم المستخدم"
    dialogSearchAppsPlaceholder = "ابحث في التطبيقات المثبتة…"
}

val EnglishFoxyStrings: FoxyStrings by lazy(LazyThreadSafetyMode.PUBLICATION) { createEnglishFoxyStrings() }
val PersianFoxyStrings: FoxyStrings by lazy(LazyThreadSafetyMode.PUBLICATION) { createPersianFoxyStrings() }
val ArabicFoxyStrings: FoxyStrings by lazy(LazyThreadSafetyMode.PUBLICATION) { createArabicFoxyStrings() }

fun foxyStringsFor(language: AppLanguage): FoxyStrings = when (language) {
    AppLanguage.EN -> EnglishFoxyStrings
    AppLanguage.FA -> PersianFoxyStrings
    AppLanguage.AR -> ArabicFoxyStrings
}

val LocalFoxyStrings = staticCompositionLocalOf { EnglishFoxyStrings }

val currentFoxyStrings: FoxyStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalFoxyStrings.current
