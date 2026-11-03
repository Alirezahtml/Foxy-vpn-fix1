package com.vauth.foxyvpn.ui.screens

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vauth.foxyvpn.BuildConfig
import com.vauth.foxyvpn.data.CustomAppPalette
import com.vauth.foxyvpn.data.ConnectionProfile
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.OfficialServerListWorker
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.ui.components.CustomAccentColorPickerDialog
import com.vauth.foxyvpn.ui.components.CustomAppPaletteEditorDialog
import com.vauth.foxyvpn.quicksettings.DynamicIslandOverlayService
import com.vauth.foxyvpn.ui.components.DynamicIslandAppPickerDialog
import com.vauth.foxyvpn.ui.components.DynamicIslandCustomizationModal
import com.vauth.foxyvpn.ui.theme.AppLanguage
import com.vauth.foxyvpn.ui.theme.LanguageController
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicColors
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.ThemeController
import com.vauth.foxyvpn.ui.theme.ThemeMode
import com.vauth.foxyvpn.ui.theme.formatOpaqueHexColor
import com.vauth.foxyvpn.ui.theme.parseOpaqueHexColor
import com.vauth.foxyvpn.ui.theme.currentFoxyFonts
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.formatPercentLocalized
import com.vauth.foxyvpn.ui.theme.localizeDigits
import com.vauth.foxyvpn.ui.theme.localizeNumber
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import android.app.TimePickerDialog
import android.os.PowerManager
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import com.vauth.foxyvpn.R
import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.CoreSelection
import com.vauth.foxyvpn.data.DynamicIslandHaloColorMode
import com.vauth.foxyvpn.data.DynamicIslandHaloRotationType
import com.vauth.foxyvpn.data.DynamicIslandLedStyle
import com.vauth.foxyvpn.data.DynamicIslandMotionStyle
import com.vauth.foxyvpn.data.DynamicIslandWaveStyle
import com.vauth.foxyvpn.data.FoxyColorTheme
import com.vauth.foxyvpn.data.HeroWaveAnimationStyle
import com.vauth.foxyvpn.data.TokenStore
import com.vauth.foxyvpn.data.FoxyBackup
import com.vauth.foxyvpn.vpn.CoreProtocolPolicy
import com.vauth.foxyvpn.ui.theme.StyleAtmospherePosterOverlay
import com.vauth.foxyvpn.ui.theme.formatClockTimeLocalized
import com.vauth.foxyvpn.ui.theme.formatMinutesLocalized
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.vauth.foxyvpn.data.WaveColorMode
import com.vauth.foxyvpn.ui.components.DynamicIslandLivePreviewCard
import com.vauth.foxyvpn.vpn.FoxyVpnService
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val INSTAGRAM_URL = "https://instagram.com/Aliem061"
private const val TELEGRAM_URL = "https://t.me/aliem_061"

private enum class SettingsCategory {
    APPEARANCE_WAVES,
    DYNAMIC_ISLAND,
    PROTOCOL_NETWORK,
    SPEED_AUTOMATION,
    SMART_TOOLKIT,
    ACCOUNTS_ABOUT,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    themeController: ThemeController,
    languageController: LanguageController,
    onOpenLogs: () -> Unit,
    onOpenTools: (Int) -> Unit = {},
    onSignOut: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val refreshScope = rememberCoroutineScope()
    val neu = currentNeumorphicColors
    val s = currentFoxyStrings
    val fonts = currentFoxyFonts
    val defaultAccentArgb = if (neu.isDark) themeController.colorTheme.primaryDarkHex else themeController.colorTheme.primaryLightHex
    var appAccentHexInput by remember(themeController.colorTheme, themeController.customAccentArgb, neu.isDark) {
        mutableStateOf(formatOpaqueHexColor(themeController.customAccentArgb ?: defaultAccentArgb))
    }
    var showAppAccentPicker by remember { mutableStateOf(false) }
    var showCustomPaletteEditor by remember { mutableStateOf(false) }

    if (showCustomPaletteEditor) {
        CustomAppPaletteEditorDialog(
            initialPalette = themeController.customPalette ?: CustomAppPalette.from(themeController.colorTheme),
            language = languageController.language,
            onDismiss = { showCustomPaletteEditor = false },
            onApply = { palette ->
                themeController.applyCustomPalette(palette)
                showCustomPaletteEditor = false
            },
        )
    }

    if (showAppAccentPicker) {
        CustomAccentColorPickerDialog(
            initialArgb = themeController.customAccentArgb ?: defaultAccentArgb,
            title = when (languageController.language) {
                AppLanguage.FA -> "ساخت رنگ دلخواه"
                AppLanguage.AR -> "إنشاء لون مخصص"
                AppLanguage.EN -> "Create a custom color"
            },
            description = when (languageController.language) {
                AppLanguage.FA -> "رنگ اصلی را انتخاب کنید؛ کنتراست برای حالت روشن و تاریک تنظیم می‌شود."
                AppLanguage.AR -> "اختر اللون الأساسي؛ يتم ضبط التباين للوضعين الفاتح والداكن."
                AppLanguage.EN -> "Choose an accent; contrast is adjusted for light and dark themes."
            },
            applyLabel = when (languageController.language) {
                AppLanguage.FA -> "اعمال"
                AppLanguage.AR -> "تطبيق"
                AppLanguage.EN -> "Apply"
            },
            cancelLabel = when (languageController.language) {
                AppLanguage.FA -> "لغو"
                AppLanguage.AR -> "إلغاء"
                AppLanguage.EN -> "Cancel"
            },
            onDismiss = { showAppAccentPicker = false },
            onApply = { argb ->
                themeController.applyCustomAccentArgb(argb)
                appAccentHexInput = formatOpaqueHexColor(argb)
                showAppAccentPicker = false
            },
        )
    }
    var showHaloColorPicker by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    var selectedSubPage by remember { mutableIntStateOf(0) }

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
        selectedSubPage = 0
    }

    var exitCheckEnabled by remember { mutableStateOf(settingsStore.exitCheckEnabled) }
    var dnsProvider by remember { mutableStateOf(settingsStore.dnsProvider) }
    var customDohUrl by remember { mutableStateOf(settingsStore.customDohUrl) }
    var bootstrapDnsProvider by remember { mutableStateOf(settingsStore.bootstrapDnsProvider) }
    var customBootstrapIp by remember { mutableStateOf(settingsStore.customBootstrapIp) }
    var socksBindAddress by remember { mutableStateOf(settingsStore.socksBindAddress) }
    var socksPort by remember { mutableStateOf(settingsStore.socksPort) }
    var httpProxyEnabled by remember { mutableStateOf(settingsStore.httpProxyEnabled) }
    var httpProxyPort by remember { mutableStateOf(settingsStore.httpProxyPort) }
    var proxyOnlyMode by remember { mutableStateOf(settingsStore.proxyOnlyMode) }
    var customEdgeAddress by remember { mutableStateOf(settingsStore.customEdgeAddress) }
    var upstreamProxyEnabled by remember { mutableStateOf(settingsStore.upstreamProxyEnabled) }
    var upstreamProxyType by remember { mutableStateOf(settingsStore.upstreamProxyType) }
    var upstreamProxyHost by remember { mutableStateOf(settingsStore.upstreamProxyHost) }
    var upstreamProxyPort by remember { mutableStateOf(settingsStore.upstreamProxyPort) }
    var upstreamProxyUsername by remember { mutableStateOf(settingsStore.upstreamProxyUsername) }
    var upstreamProxyPassword by remember { mutableStateOf(settingsStore.upstreamProxyPassword) }
    var excludedApps by remember { mutableStateOf(settingsStore.excludedApps) }
    var mtu by remember { mutableStateOf(settingsStore.mtu) }
    var tlsSplitMode by remember { mutableStateOf(settingsStore.tlsSplitMode) }
    var connectionProtocolMode by remember { mutableStateOf(settingsStore.connectionProtocolMode) }
    var connectionProfile by remember { mutableStateOf(settingsStore.connectionProfile) }
    var coreSelection by remember { mutableStateOf(settingsStore.coreSelection) }
    val activeProtocolBadge by FoxyVpnService.activeProtocolBadge.collectAsState()
    var adBlockEnabled by remember { mutableStateOf(settingsStore.adBlockEnabled) }
    var autoSwitchAccountByQuotaEnabled by remember { mutableStateOf(settingsStore.autoSwitchAccountByQuotaEnabled) }
    var autoSwitchQuotaThresholdPercent by remember { mutableStateOf(settingsStore.autoSwitchQuotaThresholdPercent) }
    val tokenStore = remember(context) { TokenStore(context) }
    val accountsRevision by TokenStore.accountsRevision.collectAsState()
    val savedAccounts = remember(accountsRevision) { tokenStore.loadAccounts() }
    val activeAccountEmail = remember(accountsRevision) { tokenStore.activeAccountEmail }
    val createBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(FoxyBackup.create(settingsStore, tokenStore).toByteArray(Charsets.UTF_8))
                } ?: error("Cannot open backup destination")
                Toast.makeText(context, when (s.language) {
                    AppLanguage.FA -> "نسخه پشتیبان ذخیره شد"
                    AppLanguage.AR -> "تم حفظ النسخة الاحتياطية"
                    AppLanguage.EN -> "Backup saved"
                }, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, when (s.language) {
                    AppLanguage.FA -> "ذخیره نسخه پشتیبان ناموفق بود"
                    AppLanguage.AR -> "فشل حفظ النسخة الاحتياطية"
                    AppLanguage.EN -> "Backup failed"
                }, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.use { it.reader().readText() }
                    ?: error("Cannot read backup")
                val result = FoxyBackup.restore(json, settingsStore, tokenStore)
                Toast.makeText(context, when (s.language) {
                    AppLanguage.FA -> "${result.importedAccounts} اکانت بازیابی شد؛ برای اعمال همه تنظیمات برنامه را دوباره باز کنید."
                    AppLanguage.AR -> "تمت استعادة ${result.importedAccounts} حساب؛ أعد فتح التطبيق لتطبيق جميع الإعدادات."
                    AppLanguage.EN -> "Restored ${result.importedAccounts} account(s). Reopen the app to apply all settings."
                }, Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, when (s.language) {
                    AppLanguage.FA -> "فایل پشتیبان نامعتبر یا ناسازگار است"
                    AppLanguage.AR -> "النسخة الاحتياطية غير صالحة أو غير متوافقة"
                    AppLanguage.EN -> "Invalid or incompatible backup"
                }, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Pro Wave Studio State
    var waveSpeedMultiplier by remember { mutableFloatStateOf(settingsStore.waveSpeedMultiplier) }
    var waveAmplitudeScale by remember { mutableFloatStateOf(settingsStore.waveAmplitudeScale) }
    var waveGlowEnabled by remember { mutableStateOf(settingsStore.waveGlowEnabled) }
    var waveColorMode by remember { mutableStateOf(settingsStore.waveColorMode) }

    // Dynamic Island State
    var dynamicIslandEnabled by remember { mutableStateOf(settingsStore.dynamicIslandEnabled) }
    var dynamicIslandOverlayEnabled by remember { mutableStateOf(settingsStore.dynamicIslandOverlayEnabled) }
    var dynamicIslandSize by remember { mutableStateOf(settingsStore.dynamicIslandSize) }
    var dynamicIslandScalePercent by remember { mutableFloatStateOf(settingsStore.dynamicIslandScalePercent.toFloat()) }
    var dynamicIslandCornerRadius by remember { mutableStateOf(settingsStore.dynamicIslandCornerRadius) }
    var dynamicIslandPosition by remember { mutableStateOf(settingsStore.dynamicIslandPosition) }
    var dynamicIslandTheme by remember { mutableStateOf(settingsStore.dynamicIslandTheme) }
    var dynamicIslandShowSpeed by remember { mutableStateOf(settingsStore.dynamicIslandShowSpeed) }
    var dynamicIslandShowPing by remember { mutableStateOf(settingsStore.dynamicIslandShowPing) }
    var dynamicIslandShowFlag by remember { mutableStateOf(settingsStore.dynamicIslandShowFlag) }
    var dynamicIslandShowTimer by remember { mutableStateOf(settingsStore.dynamicIslandShowTimer) }
    var dynamicIslandAnimation by remember { mutableStateOf(settingsStore.dynamicIslandAnimation) }
    var dynamicIslandPinnedApps by remember { mutableStateOf(settingsStore.dynamicIslandPinnedApps) }
    var dynamicIslandLedBorderEnabled by remember { mutableStateOf(settingsStore.dynamicIslandLedBorderEnabled) }
    var dynamicIslandLedStyle by remember { mutableStateOf(settingsStore.dynamicIslandLedStyle) }
    var dynamicIslandMotionStyle by remember { mutableStateOf(settingsStore.dynamicIslandMotionStyle) }
    var dynamicIslandLedSpeed by remember { mutableFloatStateOf(settingsStore.dynamicIslandLedSpeed) }
    var dynamicIslandShowTowerWave by remember { mutableStateOf(settingsStore.dynamicIslandShowTowerWave) }
    var dynamicIslandShowServerName by remember { mutableStateOf(settingsStore.dynamicIslandShowServerName) }
    var dynamicIslandShowSessionData by remember { mutableStateOf(settingsStore.dynamicIslandShowSessionData) }
    var dynamicIslandShowIpBadge by remember { mutableStateOf(settingsStore.dynamicIslandShowIpBadge) }
    var dynamicIslandDraggable by remember { mutableStateOf(settingsStore.dynamicIslandDraggable) }
    var dynamicIslandShowQuickApps by remember { mutableStateOf(settingsStore.dynamicIslandShowQuickApps) }
    var dynamicIslandLedCustomColorHex by remember { mutableStateOf(settingsStore.dynamicIslandLedCustomColorHex.takeIf { it != 0L } ?: 0xFF1E88FFL) }
    var dynamicIslandHaloHexInput by remember(dynamicIslandLedCustomColorHex) {
        mutableStateOf(formatOpaqueHexColor(dynamicIslandLedCustomColorHex))
    }
    var dynamicIslandHaloColorMode by remember { mutableStateOf(settingsStore.dynamicIslandHaloColorMode) }
    var dynamicIslandHaloRotationType by remember { mutableStateOf(settingsStore.dynamicIslandHaloRotationType) }
    var dynamicIslandHaloShadowDistanceDp by remember { mutableFloatStateOf(settingsStore.dynamicIslandHaloShadowDistanceDp) }
    var dynamicIslandWaveStyle by remember { mutableStateOf(settingsStore.dynamicIslandWaveStyle) }
    var dynamicIslandBgAlphaPercent by remember { mutableFloatStateOf(settingsStore.dynamicIslandBgAlphaPercent.toFloat()) }
    var dynamicIslandAutoCollapseSec by remember { mutableIntStateOf(settingsStore.dynamicIslandAutoCollapseSec) }
    var dynamicIslandCompactTelemetry by remember { mutableStateOf(settingsStore.dynamicIslandCompactTelemetry) }
    var dynamicIslandShowProtocolBadge by remember { mutableStateOf(settingsStore.dynamicIslandShowProtocolBadge) }
    var dynamicIslandHapticEnabled by remember { mutableStateOf(settingsStore.dynamicIslandHapticEnabled) }

    if (showHaloColorPicker) {
        CustomAccentColorPickerDialog(
            initialArgb = dynamicIslandLedCustomColorHex,
            title = when (languageController.language) {
                AppLanguage.FA -> "انتخاب رنگ دلخواه هاله"
                AppLanguage.AR -> "اختيار لون الهالة"
                AppLanguage.EN -> "Choose a halo color"
            },
            description = when (languageController.language) {
                AppLanguage.FA -> "رنگ را با کنترل‌های RGB تنظیم کنید؛ پیش‌نمایش قبل از اعمال نمایش داده می‌شود."
                AppLanguage.AR -> "اضبط اللون عبر قنوات RGB وشاهد المعاينة قبل التطبيق."
                AppLanguage.EN -> "Adjust the RGB channels and preview the color before applying it."
            },
            applyLabel = when (languageController.language) {
                AppLanguage.FA -> "اعمال"
                AppLanguage.AR -> "تطبيق"
                AppLanguage.EN -> "Apply"
            },
            cancelLabel = when (languageController.language) {
                AppLanguage.FA -> "لغو"
                AppLanguage.AR -> "إلغاء"
                AppLanguage.EN -> "Cancel"
            },
            onDismiss = { showHaloColorPicker = false },
            onApply = { argb ->
                dynamicIslandLedCustomColorHex = 0xFF000000L or (argb and 0x00FFFFFFL)
                dynamicIslandHaloHexInput = formatOpaqueHexColor(dynamicIslandLedCustomColorHex)
                settingsStore.dynamicIslandLedCustomColorHex = dynamicIslandLedCustomColorHex
                dynamicIslandHaloColorMode = DynamicIslandHaloColorMode.SINGLE_COLOR
                settingsStore.dynamicIslandHaloColorMode = DynamicIslandHaloColorMode.SINGLE_COLOR
                showHaloColorPicker = false
            },
        )
    }

    // Speed Booster, Smart Battery Saver & Automation State
    var unmeteredVpnModeEnabled by remember { mutableStateOf(settingsStore.unmeteredVpnModeEnabled) }
    var batterySaverModeEnabled by remember { mutableStateOf(settingsStore.batterySaverModeEnabled) }
    var scheduleEnabled by remember { mutableStateOf(settingsStore.scheduleEnabled) }
    var scheduleStartHour by remember { mutableStateOf(settingsStore.scheduleStartHour) }
    var scheduleStartMinute by remember { mutableStateOf(settingsStore.scheduleStartMinute) }
    var scheduleEndHour by remember { mutableStateOf(settingsStore.scheduleEndHour) }
    var scheduleEndMinute by remember { mutableStateOf(settingsStore.scheduleEndMinute) }
    var serverListAutoRefresh by remember { mutableStateOf(settingsStore.serverListAutoRefresh) }
    var serverListRefreshing by remember { mutableStateOf(false) }
    var serverListStatus by remember { mutableStateOf<String?>(null) }
    var idleDisconnectEnabled by remember { mutableStateOf(settingsStore.idleDisconnectEnabled) }
    var idleTimeoutMinutes by remember { mutableStateOf(settingsStore.idleTimeoutMinutes) }
    var sessionDataLimitEnabled by remember { mutableStateOf(settingsStore.sessionDataLimitEnabled) }
    var sessionDataLimitMb by remember { mutableStateOf(settingsStore.sessionDataLimitMb) }
    var smartPingAutoSwitch by remember { mutableStateOf(settingsStore.smartPingAutoSwitch) }
    var autoConnectOnLaunch by remember { mutableStateOf(settingsStore.autoConnectOnLaunch) }
    var stealthNotificationEnabled by remember { mutableStateOf(settingsStore.stealthNotificationEnabled) }

    // Deep Dynamic Island Micro-Detail states
    var islandCollapsedHeightDp by remember { mutableFloatStateOf(settingsStore.dynamicIslandCollapsedHeightDp.toFloat()) }
    var islandExpandedWidthPct by remember { mutableFloatStateOf(settingsStore.dynamicIslandExpandedWidthPercent.toFloat()) }
    var islandFontScalePct by remember { mutableFloatStateOf(settingsStore.dynamicIslandFontScalePercent.toFloat()) }
    var islandBorderWidthDp by remember { mutableFloatStateOf(settingsStore.dynamicIslandBorderWidthDp) }
    var islandShadowDepthDp by remember { mutableFloatStateOf(settingsStore.dynamicIslandShadowDepthDp.toFloat()) }
    var islandSpringDampingPct by remember { mutableFloatStateOf(settingsStore.dynamicIslandSpringDampingPercent.toFloat()) }
    var islandSpringStiffness by remember { mutableFloatStateOf(settingsStore.dynamicIslandSpringStiffness.toFloat()) }
    var islandQuickAppIconDp by remember { mutableFloatStateOf(settingsStore.dynamicIslandQuickAppIconSizeDp.toFloat()) }
    var islandWidthScalePct by remember { mutableFloatStateOf(settingsStore.dynamicIslandWidthScalePercent.toFloat()) }
    var islandHeightScalePct by remember { mutableFloatStateOf(settingsStore.dynamicIslandHeightScalePercent.toFloat()) }
    var islandAnimationSpeedPct by remember { mutableFloatStateOf(settingsStore.dynamicIslandAnimationSpeedPercent.toFloat()) }
    var islandShowAppNames by remember { mutableStateOf(settingsStore.dynamicIslandShowAppNames) }
    var islandDoubleTapAction by remember { mutableStateOf(settingsStore.dynamicIslandDoubleTapAction) }
    var islandIndicatorStyle by remember { mutableStateOf(settingsStore.dynamicIslandIndicatorStyle) }
    var islandShowJitterBadge by remember { mutableStateOf(settingsStore.dynamicIslandShowJitterBadge) }
    var islandShowSecurityBadge by remember { mutableStateOf(settingsStore.dynamicIslandShowSecurityBadge) }
    var islandShowNetworkActions by remember { mutableStateOf(settingsStore.dynamicIslandShowNetworkActions) }
    var islandShowTelegramGamingActions by remember { mutableStateOf(settingsStore.dynamicIslandShowTelegramGamingActions) }
    var islandSurfaceMode by remember { mutableStateOf(settingsStore.dynamicIslandSurfaceMode) }
    var islandShowQuickStyleDeck by remember { mutableStateOf(settingsStore.dynamicIslandShowQuickStyleDeck) }
    var mtuModeAuto by remember { mutableStateOf(settingsStore.mtuModeAuto) }

    // Dialog toggles
    var showDnsProviderDialog by remember { mutableStateOf(false) }
    var showBootstrapDnsDialog by remember { mutableStateOf(false) }
    var showMtuDialog by remember { mutableStateOf(false) }
    var showSocksBindDialog by remember { mutableStateOf(false) }
    var showSocksPortDialog by remember { mutableStateOf(false) }
    var showHttpPortDialog by remember { mutableStateOf(false) }
    var showEdgeAddressDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyTypeDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyAddressDialog by remember { mutableStateOf(false) }
    var showUpstreamProxyCredentialsDialog by remember { mutableStateOf(false) }
    var showSplitTunnelDialog by remember { mutableStateOf(false) }
    var showTlsSplitDialog by remember { mutableStateOf(false) }
    var showConnectionProfileDialog by remember { mutableStateOf(false) }
    var showIslandSizeDialog by remember { mutableStateOf(false) }
    var showIslandCornerRadiusDialog by remember { mutableStateOf(false) }
    var showIslandPositionDialog by remember { mutableStateOf(false) }
    var showIslandThemeDialog by remember { mutableStateOf(false) }
    var showIslandPinnedAppsDialog by remember { mutableStateOf(false) }
    var showMozillaWebPortal by remember { mutableStateOf(false) }
    var showIdleTimeoutDialog by remember { mutableStateOf(false) }
    var showDataCapDialog by remember { mutableStateOf(false) }
    var utlsFingerprint by remember { mutableStateOf(settingsStore.utlsFingerprint) }
    var tcpFastOpen by remember { mutableStateOf(settingsStore.tcpFastOpen) }
    var ipv6Enabled by remember { mutableStateOf(settingsStore.ipv6Enabled) }
    var bypassLocalNetwork by remember { mutableStateOf(settingsStore.bypassLocalNetwork) }
    var showUtlsDialog by remember { mutableStateOf(false) }
    var showCoreGuideDialog by remember { mutableStateOf(false) }

    if (showUtlsDialog) {
        AlertDialog(
            onDismissRequest = { showUtlsDialog = false },
            title = {
                Text(
                    when (s.language) {
                        AppLanguage.FA -> "اثر انگشت TLS مرورگر (uTLS Fingerprint)"
                        AppLanguage.AR -> "بصمة متصفح TLS (uTLS Fingerprint)"
                        AppLanguage.EN -> "uTLS ClientHello Fingerprint"
                    },
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsStore.UtlsFingerprint.entries.forEach { fp ->
                        val selected = utlsFingerprint == fp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) neu.accent.copy(alpha = 0.15f) else neu.surfaceElevated)
                                .border(1.dp, if (selected) neu.accent else neu.textSecondary.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                .clickable {
                                    utlsFingerprint = fp
                                    settingsStore.utlsFingerprint = fp
                                    showUtlsDialog = false
                                    FoxyVpnService.reconnect(context)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = fp.localizedLabel(s.language),
                                modifier = Modifier.weight(1f),
                                color = if (selected) neu.accent else neu.textPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                            if (selected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = neu.accent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUtlsDialog = false }) {
                    Text(s.cancel)
                }
            },
        )
    }

    if (showCoreGuideDialog) {
        AlertDialog(
            onDismissRequest = { showCoreGuideDialog = false },
            title = {
                Text(
                    when (s.language) {
                        AppLanguage.FA -> "راهنمای هسته Sing-Box MASQUE v2.0 و پروتکل‌ها"
                        AppLanguage.AR -> "دليل محرك Sing-Box MASQUE v2.0 والبروتوكولات"
                        AppLanguage.EN -> "Sing-Box MASQUE v2.0 Core & Protocols"
                    },
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        when (s.language) {
                            AppLanguage.FA -> "هسته یکپارچه Sing-Box MASQUE v2.0 (v1.15.0-foxy-masque.2): تمام هسته‌های قدیمی حذف شده و با هسته رسمی و بومی Sing-Box مجهز به پشتیبانی کامل MASQUE (RFC 9298 connect-udp) و چرخش خودکار توکن جایگزین شده است."
                            AppLanguage.AR -> "محرك Sing-Box MASQUE v2.0 الموحد: تم استبدال جميع المحركات القديمة بمحرك Sing-Box الرسمي مع دعم كامل لـ MASQUE (RFC 9298) وتدوير الرمز التلقائي."
                            AppLanguage.EN -> "Unified Sing-Box MASQUE v2.0 Core (v1.15.0-foxy-masque.2): All legacy cores have been replaced by the official native Sing-Box MASQUE engine with RFC 9298 connect-udp and live bearer token rotation."
                        },
                        lineHeight = 20.sp,
                    )
                    Text(
                        when (s.language) {
                            AppLanguage.FA -> "پروتکل‌های فعال در هسته:\n• خودکار هوشمند (H3/H2): ابتدا HTTP/3 QUIC را تست کرده و در صورت محدودیت به HTTP/2 + MASQUE سوییچ می‌کند.\n• HTTP/2 (TCP/TLS + MASQUE): اتصال پایدار چندجریانی با پشتیبانی UDP.\n• HTTP/3 (QUIC + MASQUE): کمترین تاخیر و سرعت بالا روی UDP/QUIC.\n• HTTP/1.1 (TLS + فرگمنت): تانل مستقیم CONNECT با تکه‌تکه‌سازی بسته برای شبکه‌های بسیار محدود."
                            AppLanguage.AR -> "البروتوكولات المدعومة:\n• تلقائي ذكي (H3/H2)\n• HTTP/2 (TCP/TLS + MASQUE)\n• HTTP/3 (QUIC + MASQUE)\n• HTTP/1.1 (TLS + تجزئة الحزم)"
                            AppLanguage.EN -> "Supported Protocols:\n• Smart Auto (H3/H2): Races HTTP/3 QUIC with automatic fallback to HTTP/2 + MASQUE.\n• HTTP/2 (TCP/TLS + MASQUE): Multiplexed TLS/TCP with RFC 9298 UDP.\n• HTTP/3 (QUIC + MASQUE): Low-latency QUIC transport.\n• HTTP/1.1 (TLS + Fragment): Direct HTTP/1.1 CONNECT tunnel with TLS record/packet fragmentation."
                        },
                        lineHeight = 20.sp,
                        color = neu.textSecondary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCoreGuideDialog = false }) {
                    Text(when (s.language) {
                        AppLanguage.FA -> "متوجه شدم"
                        AppLanguage.AR -> "فهمت"
                        AppLanguage.EN -> "Got it"
                    })
                }
            },
        )
    }

    if (showMozillaWebPortal) {
        MozillaInAppWebPortal(
            onCapturedEmail = {
                showMozillaWebPortal = false
                onSignOut()
            },
            onCloseAndSwitchToLogin = {
                showMozillaWebPortal = false
                onSignOut()
            },
            onClose = { showMozillaWebPortal = false },
        )
        return
    }

    if (showIslandPinnedAppsDialog) {
        DynamicIslandAppPickerDialog(
            current = dynamicIslandPinnedApps,
            onDismiss = { showIslandPinnedAppsDialog = false },
            onConfirm = {
                dynamicIslandPinnedApps = it
                settingsStore.dynamicIslandPinnedApps = it
                showIslandPinnedAppsDialog = false
            },
        )
    }

    if (showIslandCornerRadiusDialog) {
        DynamicIslandCornerRadiusDialog(
            current = dynamicIslandCornerRadius,
            onDismiss = { showIslandCornerRadiusDialog = false },
            onConfirm = {
                dynamicIslandCornerRadius = it
                settingsStore.dynamicIslandCornerRadius = it
                showIslandCornerRadiusDialog = false
            },
        )
    }

    if (showIslandSizeDialog) {
        DynamicIslandSizeDialog(
            current = dynamicIslandSize,
            onDismiss = { showIslandSizeDialog = false },
            onConfirm = {
                dynamicIslandSize = it
                settingsStore.dynamicIslandSize = it
                showIslandSizeDialog = false
            },
        )
    }

    if (showIslandPositionDialog) {
        DynamicIslandPositionDialog(
            current = dynamicIslandPosition,
            onDismiss = { showIslandPositionDialog = false },
            onConfirm = {
                dynamicIslandPosition = it
                settingsStore.dynamicIslandPosition = it
                showIslandPositionDialog = false
            },
        )
    }

    if (showIslandThemeDialog) {
        DynamicIslandThemeDialog(
            current = dynamicIslandTheme,
            onDismiss = { showIslandThemeDialog = false },
            onConfirm = {
                dynamicIslandTheme = it
                settingsStore.dynamicIslandTheme = it
                showIslandThemeDialog = false
            },
        )
    }

    if (showMtuDialog) {
        MtuPickerDialog(
            current = mtu,
            onDismiss = { showMtuDialog = false },
            onConfirm = {
                mtu = it
                settingsStore.mtu = it
                mtuModeAuto = false
                settingsStore.mtuModeAuto = false
                FoxyVpnService.applyNetworkConfiguration(context)
                showMtuDialog = false
            },
        )
    }

    if (showDnsProviderDialog) {
        DnsProviderPickerDialog(
            currentProvider = dnsProvider,
            currentCustomUrl = customDohUrl,
            onDismiss = { showDnsProviderDialog = false },
            onConfirm = { provider, url ->
                dnsProvider = provider
                settingsStore.dnsProvider = provider
                customDohUrl = url
                settingsStore.customDohUrl = url
                showDnsProviderDialog = false
            },
        )
    }
    if (showBootstrapDnsDialog) {
        BootstrapDnsPickerDialog(
            currentProvider = bootstrapDnsProvider,
            currentCustomIp = customBootstrapIp,
            onDismiss = { showBootstrapDnsDialog = false },
            onConfirm = { provider, ip ->
                bootstrapDnsProvider = provider
                settingsStore.bootstrapDnsProvider = provider
                customBootstrapIp = ip
                settingsStore.customBootstrapIp = ip
                showBootstrapDnsDialog = false
            },
        )
    }
    if (showSocksBindDialog) {
        SocksBindAddressPickerDialog(
            current = socksBindAddress,
            onDismiss = { showSocksBindDialog = false },
            onConfirm = {
                socksBindAddress = it
                settingsStore.socksBindAddress = it
                showSocksBindDialog = false
            },
        )
    }
    if (showSocksPortDialog) {
        SocksPortPickerDialog(
            current = socksPort,
            onDismiss = { showSocksPortDialog = false },
            onConfirm = {
                socksPort = it
                settingsStore.socksPort = it
                showSocksPortDialog = false
            },
        )
    }
    if (showHttpPortDialog) {
        HttpPortPickerDialog(
            current = httpProxyPort,
            onDismiss = { showHttpPortDialog = false },
            onConfirm = {
                httpProxyPort = it
                settingsStore.httpProxyPort = it
                showHttpPortDialog = false
            },
        )
    }
    if (showEdgeAddressDialog) {
        EdgeAddressPickerDialog(
            current = customEdgeAddress,
            onDismiss = { showEdgeAddressDialog = false },
            onConfirm = {
                customEdgeAddress = it
                settingsStore.customEdgeAddress = it
                showEdgeAddressDialog = false
            },
        )
    }
    if (showUpstreamProxyTypeDialog) {
        UpstreamProxyTypeDialog(
            current = upstreamProxyType,
            onDismiss = { showUpstreamProxyTypeDialog = false },
            onConfirm = {
                upstreamProxyType = it
                settingsStore.upstreamProxyType = it
                showUpstreamProxyTypeDialog = false
            },
        )
    }
    if (showUpstreamProxyAddressDialog) {
        UpstreamProxyAddressDialog(
            currentHost = upstreamProxyHost,
            currentPort = upstreamProxyPort,
            onDismiss = { showUpstreamProxyAddressDialog = false },
            onConfirm = { host, port ->
                upstreamProxyHost = host
                upstreamProxyPort = port
                settingsStore.upstreamProxyHost = host
                settingsStore.upstreamProxyPort = port
                showUpstreamProxyAddressDialog = false
            },
        )
    }
    if (showUpstreamProxyCredentialsDialog) {
        UpstreamProxyCredentialsDialog(
            currentUsername = upstreamProxyUsername,
            currentPassword = upstreamProxyPassword,
            onDismiss = { showUpstreamProxyCredentialsDialog = false },
            onConfirm = { username, password ->
                upstreamProxyUsername = username
                upstreamProxyPassword = password
                settingsStore.upstreamProxyUsername = username
                settingsStore.upstreamProxyPassword = password
                showUpstreamProxyCredentialsDialog = false
            },
        )
    }
    if (showSplitTunnelDialog) {
        SplitTunnelDialog(
            current = excludedApps,
            onDismiss = { showSplitTunnelDialog = false },
            onConfirm = {
                excludedApps = it
                settingsStore.excludedApps = it
                showSplitTunnelDialog = false
            },
        )
    }
    if (showTlsSplitDialog) {
        TlsSplitModePickerDialog(
            current = tlsSplitMode,
            onDismiss = { showTlsSplitDialog = false },
            onConfirm = {
                tlsSplitMode = it
                settingsStore.tlsSplitMode = it
                showTlsSplitDialog = false
            },
        )
    }
    if (showConnectionProfileDialog) {
        AlertDialog(
            onDismissRequest = { showConnectionProfileDialog = false },
            title = {
                Text(
                    when (s.language) {
                        AppLanguage.FA -> "پروفایل اتصال"
                        AppLanguage.AR -> "ملف الاتصال"
                        AppLanguage.EN -> "Connection profile"
                    },
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConnectionProfile.entries.forEach { profile ->
                        val selected = profile == connectionProfile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) neu.accent.copy(alpha = 0.14f) else Color.Transparent)
                                .clickable {
                                    connectionProfile = profile
                                    settingsStore.connectionProfile = profile
                                    showConnectionProfileDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(profile.localizedLabel(s.language), fontWeight = FontWeight.SemiBold)
                                Text(profile.localizedDescription(s.language), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConnectionProfileDialog = false }) {
                    Text(
                        when (s.language) {
                            AppLanguage.FA -> "تأیید"
                            AppLanguage.AR -> "تم"
                            AppLanguage.EN -> "Done"
                        },
                    )
                }
            },
        )
    }

    if (showIdleTimeoutDialog) {
        val options = listOf(5, 10, 15, 30, 60)
        AlertDialog(
            onDismissRequest = { showIdleTimeoutDialog = false },
            title = { Text(s.idleTimeoutTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { mins ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    idleTimeoutMinutes = mins
                                    settingsStore.idleTimeoutMinutes = mins
                                    showIdleTimeoutDialog = false
                                }
                                .padding(vertical = 8.dp),
                        ) {
                            RadioButton(
                                selected = idleTimeoutMinutes == mins,
                                onClick = {
                                    idleTimeoutMinutes = mins
                                    settingsStore.idleTimeoutMinutes = mins
                                    showIdleTimeoutDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(s.language.formatMinutesLocalized(mins))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIdleTimeoutDialog = false }) {
                    Text(s.cancel)
                }
            },
        )
    }

    if (showDataCapDialog) {
        val options = listOf(250, 500, 1024, 2048, 5120)
        AlertDialog(
            onDismissRequest = { showDataCapDialog = false },
            title = { Text(s.sessionDataLimitAmountTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { mb ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sessionDataLimitMb = mb
                                    settingsStore.sessionDataLimitMb = mb
                                    showDataCapDialog = false
                                }
                                .padding(vertical = 8.dp),
                        ) {
                            RadioButton(
                                selected = sessionDataLimitMb == mb,
                                onClick = {
                                    sessionDataLimitMb = mb
                                    settingsStore.sessionDataLimitMb = mb
                                    showDataCapDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${s.language.localizeNumber(mb)} MB")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataCapDialog = false }) {
                    Text(s.cancel)
                }
            },
        )
    }

    val categoryTitle: (SettingsCategory) -> String = { cat ->
        when (cat) {
            SettingsCategory.APPEARANCE_WAVES -> when (s.language) {
                AppLanguage.FA -> "زبان، تم و استودیو امواج"
                AppLanguage.AR -> "اللغة والمظهر والموجات"
                AppLanguage.EN -> "Theme & Wave Studio"
            }
            SettingsCategory.DYNAMIC_ISLAND -> when (s.language) {
                AppLanguage.FA -> "شخصی‌سازی داینامیک آیلند"
                AppLanguage.AR -> "تخصيص الجزيرة التفاعلية"
                AppLanguage.EN -> "Dynamic Island Studio"
            }
            SettingsCategory.PROTOCOL_NETWORK -> when (s.language) {
                AppLanguage.FA -> "پروتکل، امنیت و شبکه"
                AppLanguage.AR -> "البروتوكول والأمان والشبكة"
                AppLanguage.EN -> "Protocol, Security & Network"
            }
            SettingsCategory.SPEED_AUTOMATION -> when (s.language) {
                AppLanguage.FA -> "بوستر سرعت و پایداری"
                AppLanguage.AR -> "معزز السرعة والاستقرار"
                AppLanguage.EN -> "Speed Booster & Stability"
            }
            SettingsCategory.SMART_TOOLKIT -> when (s.language) {
                AppLanguage.FA -> "ابزارهای شبکه و امنیت"
                AppLanguage.AR -> "أدوات الشبكة والأمان"
                AppLanguage.EN -> "Network & Security Toolkit"
            }
            SettingsCategory.ACCOUNTS_ABOUT -> when (s.language) {
                AppLanguage.FA -> "مدیریت حساب‌ها و درباره"
                AppLanguage.AR -> "إدارة الحسابات وحول التطبيق"
                AppLanguage.EN -> "Accounts & About"
            }
        }
    }

    val subPageTitles: List<String> = when (selectedCategory) {
        SettingsCategory.APPEARANCE_WAVES -> listOf(
            when (s.language) {
                AppLanguage.FA -> "زبان و سبک موج"
                AppLanguage.AR -> "اللغة والموجة"
                AppLanguage.EN -> "Theme & Wave"
            },
            when (s.language) {
                AppLanguage.FA -> "استودیو امواج"
                AppLanguage.AR -> "استوديو الموجات"
                AppLanguage.EN -> "Wave Studio"
            },
        )
        SettingsCategory.DYNAMIC_ISLAND -> listOf(
            when (s.language) {
                AppLanguage.FA -> "پایه"
                AppLanguage.AR -> "الحجم"
                AppLanguage.EN -> "Core"
            },
            when (s.language) {
                AppLanguage.FA -> "حرکت"
                AppLanguage.AR -> "الحركة"
                AppLanguage.EN -> "Motion"
            },
            when (s.language) {
                AppLanguage.FA -> "نشانگر"
                AppLanguage.AR -> "المؤشر"
                AppLanguage.EN -> "Indicator"
            },
            when (s.language) {
                AppLanguage.FA -> "شبکه"
                AppLanguage.AR -> "الشبكة"
                AppLanguage.EN -> "Network"
            },
            when (s.language) {
                AppLanguage.FA -> "استودیو"
                AppLanguage.AR -> "استوديو"
                AppLanguage.EN -> "Studio"
            },
        )
        SettingsCategory.PROTOCOL_NETWORK -> listOf(
            when (s.language) {
                AppLanguage.FA -> "پروتکل و امنیت"
                AppLanguage.AR -> "البروتوكول"
                AppLanguage.EN -> "Protocol"
            },
            when (s.language) {
                AppLanguage.FA -> "DNS و تفکیک"
                AppLanguage.AR -> "DNS والنفق"
                AppLanguage.EN -> "DNS & Split"
            },
            when (s.language) {
                AppLanguage.FA -> "پروکسی"
                AppLanguage.AR -> "الوكيل"
                AppLanguage.EN -> "Proxy"
            },
        )
        SettingsCategory.SPEED_AUTOMATION -> listOf(
            when (s.language) {
                AppLanguage.FA -> "سرعت و رام‌ها"
                AppLanguage.AR -> "السرعة والنظام"
                AppLanguage.EN -> "Speed & ROM"
            },
            when (s.language) {
                AppLanguage.FA -> "اتوماسیون"
                AppLanguage.AR -> "الأتمتة"
                AppLanguage.EN -> "Automation"
            },
        )
        SettingsCategory.SMART_TOOLKIT -> emptyList()
        SettingsCategory.ACCOUNTS_ABOUT -> listOf(
            when (s.language) {
                AppLanguage.FA -> "مدیریت اکانت"
                AppLanguage.AR -> "الحسابات"
                AppLanguage.EN -> "Accounts"
            },
            when (s.language) {
                AppLanguage.FA -> "گزارش و ارتباط"
                AppLanguage.AR -> "السجلات"
                AppLanguage.EN -> "Logs & Info"
            },
        )
        null -> emptyList()
    }

    Scaffold(
        containerColor = neu.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = selectedCategory?.let { categoryTitle(it) } ?: s.settingsTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = neu.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (selectedCategory == null) {
                                when (s.language) {
                                    AppLanguage.FA -> "دسته‌بندی صفحه‌ای منظم"
                                    AppLanguage.AR -> "تصميم صفحات منظمة"
                                    AppLanguage.EN -> "Paginated Category Hub"
                                }
                            } else {
                                when (s.language) {
                                    AppLanguage.FA -> "بازگشت به همه دسته‌ها با دکمه برگشت"
                                    AppLanguage.AR -> "اضغط رجوع للعودة إلى الفئات"
                                    AppLanguage.EN -> "Tap back for all categories"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.bodyFont),
                            color = neu.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    NeumorphicIconButton(
                        size = 42.dp,
                        onClick = {
                            if (selectedCategory != null) {
                                selectedCategory = null
                                selectedSubPage = 0
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = s.back,
                            tint = neu.textPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = neu.background),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(neu.background)
                .padding(padding),
        ) {
            StyleAtmospherePosterOverlay(modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    AnimatedContent(
                        targetState = Pair(selectedCategory, selectedSubPage),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier.weight(1f),
                        label = "settings_paginated_content",
                    ) { (cat, subPage) ->
                        if (cat == null) {
                            // =========================================================================
                            // PAGINATED CATEGORY HUB (2x3 NON-SCROLLING GRID)
                            // =========================================================================
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val hubItems = listOf(
                                    Triple(
                                        SettingsCategory.APPEARANCE_WAVES,
                                        Icons.Filled.GraphicEq,
                                        when (s.language) {
                                            AppLanguage.FA -> "۱۶ موج + ۶ پالت رنگی زنده چیدمان"
                                            AppLanguage.AR -> "١٦ موجة + ٦ لوحات ألوان حية"
                                            AppLanguage.EN -> "16 Waves + 6 Live Layout Palettes"
                                        },
                                    ),
                                    Triple(
                                        SettingsCategory.DYNAMIC_ISLAND,
                                        Icons.Filled.ViewCarousel,
                                        when (s.language) {
                                            AppLanguage.FA -> "پیش‌نمایش زنده + ۱۶ نشانگر و ۱۸ حرکت"
                                            AppLanguage.AR -> "معاينة حية + ١٦ مؤشر و١٨ حركة"
                                            AppLanguage.EN -> "Live Preview + 16 Indicators & 18 Motions"
                                        },
                                    ),
                                    Triple(
                                        SettingsCategory.PROTOCOL_NETWORK,
                                        Icons.Filled.Bolt,
                                        when (s.language) {
                                            AppLanguage.FA -> "اتصال واقعی پروتکل (${activeProtocolBadge}) و DNS"
                                            AppLanguage.AR -> "تبديل البروتوكول الحقيقي (${activeProtocolBadge})"
                                            AppLanguage.EN -> "Real Protocol Switch (${activeProtocolBadge}) & DNS"
                                        },
                                    ),
                                    Triple(
                                        SettingsCategory.SPEED_AUTOMATION,
                                        Icons.Filled.Speed,
                                        when (s.language) {
                                            AppLanguage.FA -> "بوستر سرعت، باتری و زمان‌بندی"
                                            AppLanguage.AR -> "معزز السرعة والبطارية والجدولة"
                                            AppLanguage.EN -> "Speed Booster, Battery & Schedule"
                                        },
                                    ),
                                    Triple(
                                        SettingsCategory.SMART_TOOLKIT,
                                        Icons.Filled.Security,
                                        when (s.language) {
                                            AppLanguage.FA -> "تست سرعت، نشت، پینگ و واچ‌داگ"
                                            AppLanguage.AR -> "اختبار السرعة والتسرب والبينج"
                                            AppLanguage.EN -> "Speed Test, Leak Check & Scanner"
                                        },
                                    ),
                                    Triple(
                                        SettingsCategory.ACCOUNTS_ABOUT,
                                        Icons.Filled.AccountCircle,
                                        when (s.language) {
                                            AppLanguage.FA -> "چند اکانت (${s.language.localizeNumber(savedAccounts.size)}) و سایت موزیلا"
                                            AppLanguage.AR -> "حسابات متعددة (${s.language.localizeNumber(savedAccounts.size)}) وموزيلا"
                                            AppLanguage.EN -> "Multi-Account (${savedAccounts.size}) & Mozilla"
                                        },
                                    ),
                                )

                                hubItems.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        rowItems.forEach { (category, icon, subtitle) ->
                                            val accentColor = when (category) {
                                                SettingsCategory.APPEARANCE_WAVES -> Color(0xFF00B4FF)
                                                SettingsCategory.DYNAMIC_ISLAND -> Color(0xFF3B82F6)
                                                SettingsCategory.PROTOCOL_NETWORK -> Color(0xFF10B981)
                                                SettingsCategory.SPEED_AUTOMATION -> Color(0xFF06B6D4)
                                                SettingsCategory.SMART_TOOLKIT -> Color(0xFF8B5CF6)
                                                SettingsCategory.ACCOUNTS_ABOUT -> Color(0xFF0EA5E9)
                                            }
                                            NeumorphicCard(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                                    .neumorphicTouchBounce(),
                                                cornerRadius = 22.dp,
                                                onClick = {
                                                    selectedCategory = category
                                                    selectedSubPage = 0
                                                },
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(14.dp),
                                                    verticalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically,
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(CircleShape)
                                                                .background(accentColor.copy(alpha = 0.14f))
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = accentColor.copy(alpha = 0.35f),
                                                                    shape = CircleShape,
                                                                ),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Icon(
                                                                imageVector = icon,
                                                                contentDescription = null,
                                                                tint = accentColor,
                                                                modifier = Modifier.size(22.dp),
                                                            )
                                                        }
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                            contentDescription = null,
                                                            tint = neu.textSecondary,
                                                            modifier = Modifier.size(18.dp),
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = categoryTitle(category),
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = subtitle,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontFamily = fonts.bodyFont,
                                                            ),
                                                            color = neu.textSecondary,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // =========================================================================
                            // DEDICATED NON-SCROLLING CATEGORY PAGE WITH SUB-PAGE TABS
                            // =========================================================================
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (subPageTitles.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        subPageTitles.forEachIndexed { idx, tabLabel ->
                                            val isSelectedTab = subPage == idx
                                            val tabShape = RoundedCornerShape(12.dp)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .neumorphicTouchBounce()
                                                    .let { mod ->
                                                        if (isSelectedTab) {
                                                            mod.clip(tabShape).background(neu.accent)
                                                        } else {
                                                            mod.neumorphicSunken(
                                                                shape = tabShape,
                                                                cornerRadius = 12.dp,
                                                                depth = 2.dp,
                                                                surfaceColor = neu.surfaceElevated,
                                                            )
                                                        }
                                                    }
                                                    .clickable { selectedSubPage = idx }
                                                    .padding(vertical = 8.dp, horizontal = 3.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                    Text(
                                                        text = tabLabel,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = fonts.headingFont,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = if (subPageTitles.size >= 5) 10.5.sp else 12.sp,
                                                    ),
                                                    color = if (isSelectedTab) Color.White else neu.textPrimary,
                                                    maxLines = 2,
                                                    softWrap = true,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 14.sp,
                                                )
                                            }
                                        }
                                    }
                                }

                                // Page Body Card (Responsive container that never clips bottom rows on compact displays)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                ) {
                                    when (cat) {
                                        // -----------------------------------------------------------------
                                        // CATEGORY 1: APPEARANCE, LANGUAGE & PRO WAVE STUDIO
                                        // -----------------------------------------------------------------
                                        SettingsCategory.APPEARANCE_WAVES -> {
                                            if (subPage == 0) {
                                                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(16.dp)) {
                                                        Text(
                                                            s.appLanguageTitle,
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        ) {
                                                            AppLanguage.entries.forEach { lang ->
                                                                val isSelected = languageController.language == lang
                                                                val pillShape = RoundedCornerShape(12.dp)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .neumorphicTouchBounce()
                                                                        .let { mod ->
                                                                            if (isSelected) mod.clip(pillShape).background(neu.accent)
                                                                            else mod.neumorphicSunken(pillShape, 12.dp, 2.dp, neu.surfaceElevated)
                                                                        }
                                                                        .clickable { languageController.set(lang) }
                                                                        .padding(vertical = 10.dp),
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Text(
                                                                        text = lang.nativeName,
                                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                                        color = if (isSelected) Color.White else neu.textPrimary,
                                                                        maxLines = 1,
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                                        Text(
                                                            s.appThemeTitle,
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        ) {
                                                            val themeOptions = listOf(
                                                                ThemeMode.SYSTEM to s.themeSystem,
                                                                ThemeMode.LIGHT to s.themeLight,
                                                                ThemeMode.DARK to s.themeDark,
                                                            )
                                                            themeOptions.forEach { (mode, label) ->
                                                                val isSelected = themeController.mode == mode
                                                                val pillShape = RoundedCornerShape(12.dp)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .neumorphicTouchBounce()
                                                                        .let { mod ->
                                                                            if (isSelected) mod.clip(pillShape).background(neu.accent)
                                                                            else mod.neumorphicSunken(pillShape, 12.dp, 2.dp, neu.surfaceElevated)
                                                                        }
                                                                        .clickable { themeController.set(mode) }
                                                                        .padding(vertical = 10.dp),
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Text(
                                                                        text = label,
                                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                                            fontFamily = fonts.headingFont,
                                                                            fontWeight = FontWeight.Bold,
                                                                        ),
                                                                        color = if (isSelected) Color.White else neu.textPrimary,
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                                        // One neutral base plus a fully user-defined accent avoids a crowded preset gallery.
                                                        Text(
                                                            text = when (languageController.language) {
                                                                AppLanguage.FA -> "رنگ‌بندی دلخواه برنامه"
                                                                AppLanguage.AR -> "ألوان التطبيق المخصصة"
                                                                AppLanguage.EN -> "Custom app colors"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            text = when (languageController.language) {
                                                                AppLanguage.FA -> "یک رنگ شاخص بسازید؛ سطح‌های خنثی و خوانا در شب و روز حفظ می‌شوند."
                                                                AppLanguage.AR -> "أنشئ لونًا مميزًا مع الحفاظ على أسطح محايدة وواضحة في الوضعين."
                                                                AppLanguage.EN -> "Create your own accent while keeping clean, readable light and dark surfaces."
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = neu.textSecondary,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        TextButton(onClick = { showCustomPaletteEditor = true }) {
                                                            Text(
                                                                when (languageController.language) {
                                                                    AppLanguage.FA -> if (themeController.customPalette == null) "ساخت پالت رنگ" else "ویرایش پالت رنگ"
                                                                    AppLanguage.AR -> if (themeController.customPalette == null) "إنشاء لوحة ألوان" else "تعديل لوحة الألوان"
                                                                    AppLanguage.EN -> if (themeController.customPalette == null) "Build a color palette" else "Edit color palette"
                                                                },
                                                            )
                                                        }
                                                        Spacer(Modifier.height(10.dp))
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                                        ) {
                                                            listOf(FoxyColorTheme.CYBER_FOX).forEach { palette ->
                                                                val isSelected = themeController.colorTheme == palette
                                                                val itemShape = RoundedCornerShape(14.dp)
                                                                val primarySwatch = Color(if (neu.isDark) palette.primaryDarkHex else palette.primaryLightHex)
                                                                val darkSurfaceSwatch = Color(palette.darkSurfaceHex)
                                                                val lightSurfaceSwatch = Color(palette.lightBgHex)
                                                                val connectedSwatch = Color(palette.secondaryAccentHex)
                                                                Row(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .neumorphicTouchBounce()
                                                                        .clip(itemShape)
                                                                        .background(
                                                                            if (isSelected) primarySwatch.copy(alpha = if (neu.isDark) 0.18f else 0.11f)
                                                                            else neu.surfaceElevated.copy(alpha = 0.72f),
                                                                        )
                                                                        .border(
                                                                            width = if (isSelected) 1.6.dp else 1.dp,
                                                                            color = if (isSelected) primarySwatch else neu.textSecondary.copy(alpha = 0.14f),
                                                                            shape = itemShape,
                                                                        )
                                                                        .clickable { themeController.applyColorTheme(palette) }
                                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.weight(1f),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                    ) {
                                                                        // 4-Swatch Live Palette Preview Badge (Dark Surface, Light Surface, Primary Blue, Connected Green)
                                                                        Row(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(10.dp))
                                                                                .background(if (neu.isDark) Color(0xFF09090D) else Color(0xFFE4E4EA))
                                                                                .padding(4.dp),
                                                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                        ) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(14.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(darkSurfaceSwatch)
                                                                                    .border(0.8.dp, Color.White.copy(alpha = 0.30f), CircleShape),
                                                                            )
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(14.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(lightSurfaceSwatch)
                                                                                    .border(0.8.dp, Color.Black.copy(alpha = 0.18f), CircleShape),
                                                                            )
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(14.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(primarySwatch),
                                                                            )
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(14.dp)
                                                                                    .clip(CircleShape)
                                                                                    .background(connectedSwatch),
                                                                            )
                                                                        }

                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(
                                                                                text = palette.localizedLabel(languageController.language),
                                                                                style = MaterialTheme.typography.labelLarge.copy(
                                                                                    fontFamily = fonts.headingFont,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                ),
                                                                                color = if (isSelected) primarySwatch else neu.textPrimary,
                                                                                maxLines = 1,
                                                                            )
                                                                            Text(
                                                                                text = palette.localizedSubtitle(languageController.language),
                                                                                style = MaterialTheme.typography.labelSmall,
                                                                                color = neu.textSecondary,
                                                                                maxLines = 1,
                                                                            )
                                                                        }
                                                                    }

                                                                    Spacer(Modifier.width(8.dp))

                                                                    // Active Check Pill inspired by Screenshots 4 & 5
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(CircleShape)
                                                                            .background(
                                                                                if (isSelected) {
                                                                                    if (neu.isDark) Color.White else Color(0xFF111114)
                                                                                } else {
                                                                                    neu.textSecondary.copy(alpha = 0.12f)
                                                                                },
                                                                            )
                                                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Text(
                                                                            text = if (isSelected) {
                                                                                when (languageController.language) {
                                                                                    AppLanguage.FA -> "فعال"
                                                                                    AppLanguage.AR -> "نشط"
                                                                                    AppLanguage.EN -> "Active"
                                                                                }
                                                                            } else {
                                                                                when (languageController.language) {
                                                                                    AppLanguage.FA -> "انتخاب"
                                                                                    AppLanguage.AR -> "اختيار"
                                                                                    AppLanguage.EN -> "Select"
                                                                                }
                                                                            },
                                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                                fontFamily = fonts.headingFont,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 10.sp,
                                                                            ),
                                                                            color = if (isSelected) {
                                                                                if (neu.isDark) Color(0xFF09090B) else Color.White
                                                                            } else {
                                                                                neu.textSecondary
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(Modifier.height(10.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            OutlinedTextField(
                                                                value = appAccentHexInput,
                                                                onValueChange = { raw ->
                                                                    val digits = raw.uppercase().filter {
                                                                        it in '0'..'9' || it in 'A'..'F'
                                                                    }.take(6)
                                                                    val normalized = "#$digits"
                                                                    appAccentHexInput = normalized
                                                                    parseOpaqueHexColor(normalized)?.let(themeController::applyCustomAccentArgb)
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                label = {
                                                                    Text(
                                                                        when (languageController.language) {
                                                                            AppLanguage.FA -> "رنگ شاخص (HEX)"
                                                                            AppLanguage.AR -> "اللون المميز (HEX)"
                                                                            AppLanguage.EN -> "Accent color (HEX)"
                                                                        },
                                                                    )
                                                                },
                                                                leadingIcon = {
                                                                    Box(
                                                                        Modifier
                                                                            .size(18.dp)
                                                                            .clip(CircleShape)
                                                                            .background(MaterialTheme.colorScheme.primary),
                                                                    )
                                                                },
                                                                singleLine = true,
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                                                shape = RoundedCornerShape(13.dp),
                                                            )
                                                            TextButton(onClick = { showAppAccentPicker = true }) {
                                                                Text(
                                                                    when (languageController.language) {
                                                                        AppLanguage.FA -> "انتخاب رنگ"
                                                                        AppLanguage.AR -> "اختيار اللون"
                                                                        AppLanguage.EN -> "Choose"
                                                                    },
                                                                )
                                                            }
                                                        }
                                                        TextButton(
                                                            onClick = {
                                                                themeController.resetCustomAccentArgb()
                                                                appAccentHexInput = formatOpaqueHexColor(defaultAccentArgb)
                                                            },
                                                        ) {
                                                            Text(
                                                                when (languageController.language) {
                                                                    AppLanguage.FA -> "بازگشت به رنگ پایه"
                                                                    AppLanguage.AR -> "استعادة اللون الأساسي"
                                                                    AppLanguage.EN -> "Reset to base color"
                                                                },
                                                            )
                                                        }

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

                                                        Text(
                                                            text = s.heroWaveStyleTitle,
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        SettingsDropdownRow(
                                                            title = when (s.language) {
                                                                AppLanguage.FA -> "سبک انیمیشن امواج اتصال"
                                                                AppLanguage.AR -> "نمط حركة موجات الاتصال"
                                                                AppLanguage.EN -> "Connection Wave Animation Style"
                                                            },
                                                            selected = themeController.heroWaveStyle.localizedLabel(s.language),
                                                            options = HeroWaveAnimationStyle.entries.map { it.localizedLabel(s.language) },
                                                            icon = Icons.Filled.Waves,
                                                            onSelected = { label ->
                                                                HeroWaveAnimationStyle.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let(themeController::applyHeroWaveStyle)
                                                            },
                                                        )
                                                    }
                                                }
                                            } else {
                                                // Sub-page 1: Pro Wave Studio (Wave Color Mode, Speed, Amplitude, Glow)
                                                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(16.dp)) {
                                                        Text(
                                                            text = when (s.language) {
                                                                AppLanguage.FA -> "تم رنگی امواج اتصال (صفحه اصلی و داینامیک)"
                                                                AppLanguage.AR -> "نمط ألوان موجات الاتصال"
                                                                AppLanguage.EN -> "Wave Color Palette (Hero & Dynamic Island)"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontFamily = fonts.headingFont,
                                                                fontWeight = FontWeight.Bold,
                                                            ),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        SettingsDropdownRow(
                                                            title = when (s.language) {
                                                                AppLanguage.FA -> "پالت رنگ امواج اتصال"
                                                                AppLanguage.AR -> "لوحة ألوان موجات الاتصال"
                                                                AppLanguage.EN -> "Connection Wave Color Palette"
                                                            },
                                                            selected = waveColorMode.localizedLabel(s.language),
                                                            options = WaveColorMode.entries.map { it.localizedLabel(s.language) },
                                                            icon = Icons.Filled.Palette,
                                                            onSelected = { label ->
                                                                WaveColorMode.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let {
                                                                    waveColorMode = it
                                                                    settingsStore.waveColorMode = it
                                                                }
                                                            },
                                                        )

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                        ) {
                                                            Text(
                                                                text = when (s.language) {
                                                                    AppLanguage.FA -> "سرعت حرکت امواج"
                                                                    AppLanguage.AR -> "سرعة حركة الموجات"
                                                                    AppLanguage.EN -> "Wave Animation Speed"
                                                                },
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                color = neu.textPrimary,
                                                            )
                                                            Text(
                                                                text = s.language.localizeDigits(String.format("%.1fx", waveSpeedMultiplier)),
                                                                style = MaterialTheme.typography.labelMedium.copy(
                                                                    fontFamily = fonts.monoFont,
                                                                    fontWeight = FontWeight.Bold,
                                                                ),
                                                                color = neu.accent,
                                                            )
                                                        }
                                                        Slider(
                                                            value = waveSpeedMultiplier,
                                                            onValueChange = {
                                                                waveSpeedMultiplier = it
                                                                settingsStore.waveSpeedMultiplier = it
                                                            },
                                                            valueRange = 0.4f..2.5f,
                                                            colors = SliderDefaults.colors(
                                                                thumbColor = neu.accent,
                                                                activeTrackColor = neu.accent,
                                                            ),
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                        ) {
                                                            Text(
                                                                text = when (s.language) {
                                                                    AppLanguage.FA -> "دامنه و بلندی امواج"
                                                                    AppLanguage.AR -> "سعة وارتفاع الموجات"
                                                                    AppLanguage.EN -> "Wave Amplitude & Height"
                                                                },
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                color = neu.textPrimary,
                                                            )
                                                            Text(
                                                                text = s.language.localizeDigits(String.format("%.1fx", waveAmplitudeScale)),
                                                                style = MaterialTheme.typography.labelMedium.copy(
                                                                    fontFamily = fonts.monoFont,
                                                                    fontWeight = FontWeight.Bold,
                                                                ),
                                                                color = neu.accent,
                                                            )
                                                        }
                                                        Slider(
                                                            value = waveAmplitudeScale,
                                                            onValueChange = {
                                                                waveAmplitudeScale = it
                                                                settingsStore.waveAmplitudeScale = it
                                                            },
                                                            valueRange = 0.5f..1.8f,
                                                            colors = SliderDefaults.colors(
                                                                thumbColor = neu.accent,
                                                                activeTrackColor = neu.accent,
                                                            ),
                                                        )

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                                        SettingToggleRow(
                                                            title = when (s.language) {
                                                                AppLanguage.FA -> "هاله نوری و درخشش سه‌بعدی امواج"
                                                                AppLanguage.AR -> "توهج ثلاثي الأبعاد للموجات"
                                                                AppLanguage.EN -> "3D Luminous Wave Glow Halo"
                                                            },
                                                            subtitle = when (s.language) {
                                                                AppLanguage.FA -> "افزودن لایه درخشش نرم در پس‌زمینه امواج رادار و داینامیک آیلند"
                                                                AppLanguage.AR -> "إضافة طبقة توهج ناعمة خلف الموجات"
                                                                AppLanguage.EN -> "Render soft ambient glow behind radar and Dynamic Island waves"
                                                            },
                                                            checked = waveGlowEnabled,
                                                            onCheckedChange = {
                                                                waveGlowEnabled = it
                                                                settingsStore.waveGlowEnabled = it
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // -----------------------------------------------------------------
                                        // CATEGORY 2: DYNAMIC ISLAND + LIVE INTERACTIVE PREVIEW
                                        // -----------------------------------------------------------------
                                        SettingsCategory.DYNAMIC_ISLAND -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Live In-App Preview Always Visible at Top of Dynamic Island Customization!
                                                DynamicIslandLivePreviewCard(
                                                    settingsStore = settingsStore,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )

                                                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(14.dp)) {
                                                        when (subPage) {
                                                            0 -> {
                                                                SettingToggleRow(
                                                                    title = s.enableDynamicIslandTitle,
                                                                    subtitle = s.enableDynamicIslandSub,
                                                                    checked = dynamicIslandEnabled,
                                                                    onCheckedChange = {
                                                                        dynamicIslandEnabled = it
                                                                        settingsStore.dynamicIslandEnabled = it
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                                                val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
                                                                SettingToggleRow(
                                                                    title = s.overlayIslandTitle,
                                                                    subtitle = if (hasOverlayPermission) s.overlayIslandSubGranted else s.overlayIslandSubNeedsPerm,
                                                                    checked = dynamicIslandOverlayEnabled && hasOverlayPermission,
                                                                    onCheckedChange = { enable ->
                                                                        if (enable) {
                                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                                                val intent = Intent(
                                                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                                                    Uri.parse("package:${context.packageName}"),
                                                                                )
                                                                                context.startActivity(intent)
                                                                            } else {
                                                                                dynamicIslandOverlayEnabled = true
                                                                                settingsStore.dynamicIslandOverlayEnabled = true
                                                                                DynamicIslandOverlayService.start(context)
                                                                            }
                                                                        } else {
                                                                            dynamicIslandOverlayEnabled = false
                                                                            settingsStore.dynamicIslandOverlayEnabled = false
                                                                            DynamicIslandOverlayService.stop(context)
                                                                        }
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                ) {
                                                                    Text(
                                                                        text = s.islandManualScaleTitle,
                                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = s.language.formatPercentLocalized(dynamicIslandScalePercent.roundToInt()),
                                                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Slider(
                                                                    value = dynamicIslandScalePercent,
                                                                    onValueChange = {
                                                                        dynamicIslandScalePercent = it
                                                                        settingsStore.dynamicIslandScalePercent = it.roundToInt()
                                                                    },
                                                                    valueRange = 0f..100f,
                                                                    colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                ) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "شفافیت و غلظت پس‌زمینه کپسول"
                                                                            AppLanguage.AR -> "شفافية خلفية الكبسولة"
                                                                            AppLanguage.EN -> "Capsule Background Opacity"
                                                                        },
                                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = s.language.formatPercentLocalized(dynamicIslandBgAlphaPercent.roundToInt()),
                                                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Slider(
                                                                    value = dynamicIslandBgAlphaPercent,
                                                                    onValueChange = {
                                                                        dynamicIslandBgAlphaPercent = it
                                                                        settingsStore.dynamicIslandBgAlphaPercent = it.roundToInt()
                                                                    },
                                                                    valueRange = 65f..100f,
                                                                    colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                )

                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                                                // Dedicated On/Off Switch for Country Flag (فقط کلید خاموش/روشن پرچم کشور)
                                                                SettingToggleRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "پرچم کشور"
                                                                        AppLanguage.AR -> "علم الدولة"
                                                                        AppLanguage.EN -> "Country Flag"
                                                                    },
                                                                    subtitle = when (s.language) {
                                                                        AppLanguage.FA -> "نمایش پرچم کشور سرور متصل در کپسول و پنل باز داینامیک آیلند"
                                                                        AppLanguage.AR -> "عرض علم دولة الخادم المتصل في الجزيرة التفاعلية"
                                                                        AppLanguage.EN -> "Show connected server country flag in Dynamic Island"
                                                                    },
                                                                    checked = dynamicIslandShowFlag,
                                                                    onCheckedChange = {
                                                                        dynamicIslandShowFlag = it
                                                                        settingsStore.dynamicIslandShowFlag = it
                                                                    },
                                                                )

                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))

                                                                // Luminous Halo effect toggle.
                                                                SettingToggleRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "فعال‌سازی هالهٔ نوری"
                                                                        AppLanguage.AR -> "تفعيل الهالة الضوئية"
                                                                        AppLanguage.EN -> "Enable luminous halo"
                                                                    },
                                                                    subtitle = when (s.language) {
                                                                        AppLanguage.FA -> "حاشیهٔ نورانی متحرک با سایهٔ پویا"
                                                                        AppLanguage.AR -> "حافة مضيئة متحركة مع ظل ديناميكي"
                                                                        AppLanguage.EN -> "Animated luminous border with a dynamic shadow"
                                                                    },
                                                                    checked = dynamicIslandLedBorderEnabled,
                                                                    onCheckedChange = {
                                                                        dynamicIslandLedBorderEnabled = it
                                                                        settingsStore.dynamicIslandLedBorderEnabled = it
                                                                    },
                                                                )

                                                                if (dynamicIslandLedBorderEnabled) {
                                                                    Spacer(Modifier.height(10.dp))

                                                                    // A. Color Mode: Single Color vs Multi-Color
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "انتخاب نوع رنگ (تک رنگی یا چند رنگ مختلف)"
                                                                            AppLanguage.AR -> "نوع لون الهالة (لون واحد أو متعدد الألوان)"
                                                                            AppLanguage.EN -> "Halo Color Mode (Single or Multi-Color)"
                                                                        },
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            fontFamily = fonts.headingFont,
                                                                            fontWeight = FontWeight.Bold,
                                                                        ),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Spacer(Modifier.height(6.dp))
                                                                    SettingsDropdownRow(
                                                                        title = when (s.language) {
                                                                            AppLanguage.FA -> "نوع رنگ هاله"
                                                                            AppLanguage.AR -> "نوع لون الهالة"
                                                                            AppLanguage.EN -> "Halo Color Mode"
                                                                        },
                                                                        selected = dynamicIslandHaloColorMode.localizedLabel(s.language),
                                                                        options = DynamicIslandHaloColorMode.entries.map { it.localizedLabel(s.language) },
                                                                        icon = Icons.Filled.Palette,
                                                                        onSelected = { label ->
                                                                            DynamicIslandHaloColorMode.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let { mode ->
                                                                                dynamicIslandHaloColorMode = mode
                                                                                settingsStore.dynamicIslandHaloColorMode = mode
                                                                                if (mode == DynamicIslandHaloColorMode.SINGLE_COLOR && settingsStore.dynamicIslandLedCustomColorHex == 0L) {
                                                                                    settingsStore.dynamicIslandLedCustomColorHex = dynamicIslandLedCustomColorHex
                                                                                }
                                                                            }
                                                                        },
                                                                    )

                                                                    Spacer(Modifier.height(10.dp))

                                                                    if (dynamicIslandHaloColorMode == DynamicIslandHaloColorMode.MULTI_COLOR) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "سبک حرکت چندرنگ هاله"
                                                                                AppLanguage.AR -> "طيف الألوان المتعددة"
                                                                                AppLanguage.EN -> "Multi-color animation"
                                                                            },
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.textSecondary,
                                                                        )
                                                                        Spacer(Modifier.height(6.dp))
                                                                        val multiPresets = listOf(
                                                                            DynamicIslandLedStyle.CYBER_RAINBOW,
                                                                            DynamicIslandLedStyle.FOXY_FIRE,
                                                                            DynamicIslandLedStyle.EMERALD_PULSE,
                                                                            DynamicIslandLedStyle.AURORA_BOREALIS,
                                                                        )
                                                                        SettingsDropdownRow(
                                                                            title = when (s.language) {
                                                                                AppLanguage.FA -> "سبک انیمیشن چندرنگ هاله"
                                                                                AppLanguage.AR -> "نمط حركة ألوان الهالة"
                                                                                AppLanguage.EN -> "Multi-Color Halo Animation"
                                                                            },
                                                                            selected = dynamicIslandLedStyle.localizedLabel(s.language),
                                                                            options = multiPresets.map { it.localizedLabel(s.language) },
                                                                            icon = Icons.Filled.AutoAwesome,
                                                                            onSelected = { label ->
                                                                                multiPresets.firstOrNull { it.localizedLabel(s.language) == label }?.let {
                                                                                    dynamicIslandLedStyle = it
                                                                                    settingsStore.dynamicIslandLedStyle = it
                                                                                }
                                                                            },
                                                                        )
                                                                    } else {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "انتخاب رنگ تک‌رنگی هاله"
                                                                                AppLanguage.AR -> "اختيار اللون الثابت للهالة"
                                                                                AppLanguage.EN -> "Select Single Halo Color"
                                                                            },
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.textSecondary,
                                                                        )
                                                                        Spacer(Modifier.height(6.dp))
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                        ) {
                                                                            OutlinedTextField(
                                                                                value = dynamicIslandHaloHexInput,
                                                                                onValueChange = { raw ->
                                                                                    val digits = raw.uppercase().filter {
                                                                                        it in '0'..'9' || it in 'A'..'F'
                                                                                    }.take(6)
                                                                                    val normalized = "#$digits"
                                                                                    dynamicIslandHaloHexInput = normalized
                                                                                    parseOpaqueHexColor(normalized)?.let { argb ->
                                                                                        dynamicIslandLedCustomColorHex = argb
                                                                                        settingsStore.dynamicIslandLedCustomColorHex = argb
                                                                                    }
                                                                                },
                                                                                modifier = Modifier.weight(1f),
                                                                                label = {
                                                                                    Text(
                                                                                        when (s.language) {
                                                                                            AppLanguage.FA -> "رنگ دلخواه (HEX)"
                                                                                            AppLanguage.AR -> "لون مخصص (HEX)"
                                                                                            AppLanguage.EN -> "Custom color (HEX)"
                                                                                        },
                                                                                    )
                                                                                },
                                                                                leadingIcon = {
                                                                                    Box(
                                                                                        Modifier
                                                                                            .size(18.dp)
                                                                                            .clip(CircleShape)
                                                                                            .background(Color(dynamicIslandLedCustomColorHex)),
                                                                                    )
                                                                                },
                                                                                singleLine = true,
                                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                                                                shape = RoundedCornerShape(13.dp),
                                                                            )
                                                                            TextButton(onClick = { showHaloColorPicker = true }) {
                                                                                Text(
                                                                                    when (s.language) {
                                                                                        AppLanguage.FA -> "انتخاب"
                                                                                        AppLanguage.AR -> "اختيار"
                                                                                        AppLanguage.EN -> "Pick"
                                                                                    },
                                                                                )
                                                                            }
                                                                        }
                                                                        Spacer(Modifier.height(6.dp))
                                                                        val singleSwatches = listOf(
                                                                            0xFF1E88FFL to when (s.language) {
                                                                                AppLanguage.FA -> "آبی"
                                                                                AppLanguage.AR -> "أزرق"
                                                                                AppLanguage.EN -> "Blue"
                                                                            },
                                                                            0xFF00B8D4L to when (s.language) {
                                                                                AppLanguage.FA -> "فیروزه‌ای"
                                                                                AppLanguage.AR -> "فيروزي"
                                                                                AppLanguage.EN -> "Cyan"
                                                                            },
                                                                            0xFF10B981L to when (s.language) {
                                                                                AppLanguage.FA -> "زمردی"
                                                                                AppLanguage.AR -> "زمردي"
                                                                                AppLanguage.EN -> "Emerald"
                                                                            },
                                                                            0xFF8B5CF6L to when (s.language) {
                                                                                AppLanguage.FA -> "بنفش"
                                                                                AppLanguage.AR -> "بنفسجي"
                                                                                AppLanguage.EN -> "Violet"
                                                                            },
                                                                        )
                                                                        FlowRow(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                                                        ) {
                                                                            singleSwatches.forEach { (hex, lbl) ->
                                                                                val isSwatchSel = dynamicIslandLedCustomColorHex == hex
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .clip(RoundedCornerShape(10.dp))
                                                                                        .background(if (isSwatchSel) neu.accent.copy(alpha = 0.22f) else neu.surfaceElevated)
                                                                                        .border(
                                                                                            width = if (isSwatchSel) 1.5.dp else 1.dp,
                                                                                            color = if (isSwatchSel) Color(hex) else Color.Transparent,
                                                                                            shape = RoundedCornerShape(10.dp),
                                                                                        )
                                                                                        .clickable {
                                                                                            dynamicIslandLedCustomColorHex = hex
                                                                                            dynamicIslandHaloHexInput = formatOpaqueHexColor(hex)
                                                                                            settingsStore.dynamicIslandLedCustomColorHex = hex
                                                                                        }
                                                                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                ) {
                                                                                    Box(
                                                                                        modifier = Modifier
                                                                                            .size(14.dp)
                                                                                            .clip(CircleShape)
                                                                                            .background(Color(hex))
                                                                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                                                                    )
                                                                                    Spacer(Modifier.width(6.dp))
                                                                                    Text(
                                                                                        text = lbl,
                                                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                                                            fontFamily = fonts.headingFont,
                                                                                            fontWeight = FontWeight.Bold,
                                                                                        ),
                                                                                        color = neu.textPrimary,
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Spacer(Modifier.height(10.dp))

                                                                    // B. Rotation / Animation Type Selector
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "نوع انیمیشن چرخیدن هاله"
                                                                            AppLanguage.AR -> "نوع حركة دوران الهالة"
                                                                            AppLanguage.EN -> "Halo Rotation Animation Style"
                                                                        },
                                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                                            fontFamily = fonts.headingFont,
                                                                            fontWeight = FontWeight.Bold,
                                                                        ),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Spacer(Modifier.height(6.dp))
                                                                    SettingsDropdownRow(
                                                                        title = when (s.language) {
                                                                            AppLanguage.FA -> "نوع انیمیشن چرخش هاله"
                                                                            AppLanguage.AR -> "نوع حركة دوران الهالة"
                                                                            AppLanguage.EN -> "Halo Rotation Animation Style"
                                                                        },
                                                                        selected = dynamicIslandHaloRotationType.localizedLabel(s.language),
                                                                        options = DynamicIslandHaloRotationType.entries.map { it.localizedLabel(s.language) },
                                                                        icon = Icons.Filled.AutoAwesome,
                                                                        onSelected = { label ->
                                                                            DynamicIslandHaloRotationType.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let {
                                                                                dynamicIslandHaloRotationType = it
                                                                                settingsStore.dynamicIslandHaloRotationType = it
                                                                            }
                                                                        },
                                                                    )

                                                                    Spacer(Modifier.height(10.dp))

                                                                    // C. Speed Slider
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "سرعت چرخش هاله نورانی"
                                                                                AppLanguage.AR -> "سرعة دوران الهالة"
                                                                                AppLanguage.EN -> "Halo Rotation Speed"
                                                                            },
                                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                            color = neu.textPrimary,
                                                                        )
                                                                        Text(
                                                                            text = String.format(java.util.Locale.US, "%.1fx", dynamicIslandLedSpeed),
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                            color = neu.accent,
                                                                        )
                                                                    }
                                                                    Slider(
                                                                        value = dynamicIslandLedSpeed,
                                                                        onValueChange = {
                                                                            dynamicIslandLedSpeed = it
                                                                            settingsStore.dynamicIslandLedSpeed = it
                                                                        },
                                                                        valueRange = 0.3f..3.0f,
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )

                                                                    // D. Dynamic Shadow Distance Slider ("دوری داینامیک سایه‌ای")
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "دوری داینامیک سایه‌ای (پخش هاله نوری)"
                                                                                AppLanguage.AR -> "مدى انتشار الهالة الظلية حول الجزيرة"
                                                                                AppLanguage.EN -> "Dynamic Shadow Halo Spread"
                                                                            },
                                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                            color = neu.textPrimary,
                                                                        )
                                                                        Text(
                                                                            text = "${dynamicIslandHaloShadowDistanceDp.roundToInt()} dp",
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                            color = neu.accent,
                                                                        )
                                                                    }
                                                                    Slider(
                                                                        value = dynamicIslandHaloShadowDistanceDp,
                                                                        onValueChange = {
                                                                            dynamicIslandHaloShadowDistanceDp = it
                                                                            settingsStore.dynamicIslandHaloShadowDistanceDp = it
                                                                        },
                                                                        valueRange = 2f..28f,
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )

                                                                    // E. Halo Rim Thickness
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "ضخامت خط نوری هاله"
                                                                                AppLanguage.AR -> "سُمك الخط المضيء حول الجزيرة"
                                                                                AppLanguage.EN -> "Halo Rim Thickness"
                                                                            },
                                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                            color = neu.textPrimary,
                                                                        )
                                                                        Text(
                                                                            text = String.format(java.util.Locale.US, "%.1f dp", islandBorderWidthDp),
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                            color = neu.accent,
                                                                        )
                                                                    }
                                                                    Slider(
                                                                        value = islandBorderWidthDp,
                                                                        onValueChange = {
                                                                            islandBorderWidthDp = it
                                                                            settingsStore.dynamicIslandBorderWidthDp = it
                                                                        },
                                                                        valueRange = 0.8f..3.0f,
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                }
                                                            }
                                                            1 -> {
                                                                SettingClickableRow(
                                                                    title = s.islandSizeTitle,
                                                                    value = dynamicIslandSize.localizedLabel(s.language),
                                                                    onClick = { showIslandSizeDialog = true },
                                                                )
                                                                Spacer(Modifier.height(12.dp))
                                                                NeumorphicCard(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.padding(14.dp),
                                                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "اندازه دقیق و حرکت"
                                                                                AppLanguage.AR -> "الحجم الدقيق والحركة"
                                                                                AppLanguage.EN -> "Precision Size & Motion"
                                                                            },
                                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.textPrimary,
                                                                        )
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "عرض و ارتفاع را مستقل تنظیم کنید؛ تغییرات با انیمیشن نرم روی پیش‌نمایش اعمال می‌شوند."
                                                                                AppLanguage.AR -> "اضبط العرض والارتفاع بشكل مستقل مع تطبيق التغييرات بسلاسة على المعاينة."
                                                                                AppLanguage.EN -> "Tune width and height independently; changes animate smoothly in the live preview."
                                                                            },
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = neu.textSecondary,
                                                                            lineHeight = 18.sp,
                                                                        )
                                                                        IslandTuningSlider(
                                                                            title = when (s.language) {
                                                                                AppLanguage.FA -> "اندازه افقی"
                                                                                AppLanguage.AR -> "الحجم الأفقي"
                                                                                AppLanguage.EN -> "Horizontal size"
                                                                            },
                                                                            valueText = "${islandWidthScalePct.roundToInt()}%",
                                                                            value = islandWidthScalePct,
                                                                            range = 75f..135f,
                                                                            neu = neu,
                                                                            onValueChange = {
                                                                                islandWidthScalePct = it
                                                                                settingsStore.dynamicIslandWidthScalePercent = it.roundToInt()
                                                                            },
                                                                        )
                                                                        IslandTuningSlider(
                                                                            title = when (s.language) {
                                                                                AppLanguage.FA -> "اندازه عمودی"
                                                                                AppLanguage.AR -> "الحجم العمودي"
                                                                                AppLanguage.EN -> "Vertical size"
                                                                            },
                                                                            valueText = "${islandHeightScalePct.roundToInt()}%",
                                                                            value = islandHeightScalePct,
                                                                            range = 75f..135f,
                                                                            neu = neu,
                                                                            onValueChange = {
                                                                                islandHeightScalePct = it
                                                                                settingsStore.dynamicIslandHeightScalePercent = it.roundToInt()
                                                                            },
                                                                        )
                                                                        IslandTuningSlider(
                                                                            title = when (s.language) {
                                                                                AppLanguage.FA -> "سرعت باز و بسته شدن"
                                                                                AppLanguage.AR -> "سرعة الفتح والإغلاق"
                                                                                AppLanguage.EN -> "Open/close speed"
                                                                            },
                                                                            valueText = "${islandAnimationSpeedPct.roundToInt()}%",
                                                                            value = islandAnimationSpeedPct,
                                                                            range = 70f..140f,
                                                                            neu = neu,
                                                                            onValueChange = {
                                                                                islandAnimationSpeedPct = it
                                                                                settingsStore.dynamicIslandAnimationSpeedPercent = it.roundToInt()
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                SettingClickableRow(
                                                                    title = s.islandCornerRadiusTitle,
                                                                    value = s.language.localizeNumber(dynamicIslandCornerRadius),
                                                                    onClick = { showIslandCornerRadiusDialog = true },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                SettingClickableRow(
                                                                    title = s.islandPositionTitle,
                                                                    value = dynamicIslandPosition.localizedLabel(s.language),
                                                                    onClick = { showIslandPositionDialog = true },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                SettingClickableRow(
                                                                    title = s.islandThemeTitle,
                                                                    value = dynamicIslandTheme.localizedLabel(s.language),
                                                                    onClick = { showIslandThemeDialog = true },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "بافت و پوسته پس‌زمینه کپسول (۴ حالت):"
                                                                        AppLanguage.AR -> "مظهر وسطح الكبسولة (٤ أنماط):"
                                                                        AppLanguage.EN -> "Capsule Surface Material (4 Modes):"
                                                                    },
                                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                    color = neu.textSecondary,
                                                                )
                                                                Spacer(Modifier.height(6.dp))
                                                                SettingsDropdownRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "پوسته سطح کپسول"
                                                                        AppLanguage.AR -> "مظهر سطح الكبسولة"
                                                                        AppLanguage.EN -> "Capsule Surface Material"
                                                                    },
                                                                    selected = when (s.language) {
                                                                        AppLanguage.FA -> islandSurfaceMode.labelFa
                                                                        AppLanguage.AR -> islandSurfaceMode.labelAr
                                                                        AppLanguage.EN -> islandSurfaceMode.labelEn
                                                                    },
                                                                    options = com.vauth.foxyvpn.data.DynamicIslandSurfaceMode.entries.map {
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> it.labelFa
                                                                            AppLanguage.AR -> it.labelAr
                                                                            AppLanguage.EN -> it.labelEn
                                                                        }
                                                                    },
                                                                    onSelected = { label ->
                                                                        com.vauth.foxyvpn.data.DynamicIslandSurfaceMode.entries.firstOrNull { preset ->
                                                                            label == when (s.language) {
                                                                                AppLanguage.FA -> preset.labelFa
                                                                                AppLanguage.AR -> preset.labelAr
                                                                                AppLanguage.EN -> preset.labelEn
                                                                            }
                                                                        }?.let { preset ->
                                                                            islandSurfaceMode = preset
                                                                            settingsStore.dynamicIslandSurfaceMode = preset
                                                                        }
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                Text(
                                                                    text = s.islandMotionStyleTitle,
                                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                    color = neu.textSecondary,
                                                                )
                                                                Spacer(Modifier.height(6.dp))
                                                                SettingsDropdownRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "سبک انیمیشن حرکت"
                                                                        AppLanguage.AR -> "نمط حركة الأنيميشن"
                                                                        AppLanguage.EN -> "Motion Animation Style"
                                                                    },
                                                                    selected = dynamicIslandMotionStyle.localizedLabel(s.language),
                                                                    options = DynamicIslandMotionStyle.entries.map { it.localizedLabel(s.language) },
                                                                    onSelected = { label ->
                                                                        DynamicIslandMotionStyle.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let { preset ->
                                                                            dynamicIslandMotionStyle = preset
                                                                            settingsStore.dynamicIslandMotionStyle = preset
                                                                        }
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "بسته شدن خودکار:"
                                                                            AppLanguage.AR -> "الإغلاق التلقائي:"
                                                                            AppLanguage.EN -> "Auto-Collapse Timer:"
                                                                        },
                                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                        listOf(0, 5, 10, 15).forEach { sec ->
                                                                            val isSel = dynamicIslandAutoCollapseSec == sec
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .clip(RoundedCornerShape(8.dp))
                                                                                    .background(if (isSel) neu.accent else neu.accent.copy(alpha = 0.12f))
                                                                                    .clickable {
                                                                                        dynamicIslandAutoCollapseSec = sec
                                                                                        settingsStore.dynamicIslandAutoCollapseSec = sec
                                                                                    }
                                                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                                            ) {
                                                                                Text(
                                                                                    text = if (sec == 0) s.none else "${s.language.localizeNumber(sec)}s",
                                                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                                    color = if (isSel) Color.White else neu.textPrimary,
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            2 -> {
                                                                SettingToggleRow(
                                                                    title = s.islandShowTowerWaveTitle,
                                                                    subtitle = s.islandShowTowerWaveSub,
                                                                    checked = dynamicIslandShowTowerWave,
                                                                    onCheckedChange = {
                                                                        dynamicIslandShowTowerWave = it
                                                                        settingsStore.dynamicIslandShowTowerWave = it
                                                                    },
                                                                )
                                                                if (dynamicIslandShowTowerWave) {
                                                                    Spacer(Modifier.height(6.dp))
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "تغییر نشانگر وضعیت و شبکه (۷ سبک زنده):"
                                                                            AppLanguage.AR -> "تغيير مؤشر الحالة والشبكة:"
                                                                            AppLanguage.EN -> "Status & Network Indicator Style (7 Modes):"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textSecondary,
                                                                    )
                                                                    Spacer(Modifier.height(6.dp))
                                                                    SettingsDropdownRow(
                                                                        title = when (s.language) {
                                                                            AppLanguage.FA -> "سبک نشانگر وضعیت و شبکه"
                                                                            AppLanguage.AR -> "نمط مؤشر الحالة والشبكة"
                                                                            AppLanguage.EN -> "Status & Network Indicator Style"
                                                                        },
                                                                        selected = when (s.language) {
                                                                            AppLanguage.FA -> islandIndicatorStyle.labelFa
                                                                            AppLanguage.AR -> islandIndicatorStyle.labelAr
                                                                            AppLanguage.EN -> islandIndicatorStyle.labelEn
                                                                        },
                                                                        options = com.vauth.foxyvpn.data.DynamicIslandIndicatorStyle.entries.map {
                                                                            when (s.language) {
                                                                                AppLanguage.FA -> it.labelFa
                                                                                AppLanguage.AR -> it.labelAr
                                                                                AppLanguage.EN -> it.labelEn
                                                                            }
                                                                        },
                                                                        icon = Icons.Filled.GraphicEq,
                                                                        onSelected = { label ->
                                                                            com.vauth.foxyvpn.data.DynamicIslandIndicatorStyle.entries.firstOrNull {
                                                                                label == when (s.language) {
                                                                                    AppLanguage.FA -> it.labelFa
                                                                                    AppLanguage.AR -> it.labelAr
                                                                                    AppLanguage.EN -> it.labelEn
                                                                                }
                                                                            }?.let {
                                                                                islandIndicatorStyle = it
                                                                                settingsStore.dynamicIslandIndicatorStyle = it
                                                                            }
                                                                        },
                                                                    )
                                                                    if (islandIndicatorStyle == com.vauth.foxyvpn.data.DynamicIslandIndicatorStyle.TOWER_WAVE) {
                                                                        Spacer(Modifier.height(8.dp))
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "سبک موج آنتن:"
                                                                                AppLanguage.AR -> "نمط الموجة:"
                                                                                AppLanguage.EN -> "Tower Wave Preset:"
                                                                            },
                                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.textSecondary,
                                                                        )
                                                                        Spacer(Modifier.height(4.dp))
                                                                        SettingsDropdownRow(
                                                                            title = when (s.language) {
                                                                                AppLanguage.FA -> "سبک موج آنتن"
                                                                                AppLanguage.AR -> "نمط موجة البرج"
                                                                                AppLanguage.EN -> "Tower Wave Style"
                                                                            },
                                                                            selected = dynamicIslandWaveStyle.localizedLabel(s.language),
                                                                            options = DynamicIslandWaveStyle.entries.map { it.localizedLabel(s.language) },
                                                                            icon = Icons.Filled.Waves,
                                                                            onSelected = { label ->
                                                                                DynamicIslandWaveStyle.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let {
                                                                                    dynamicIslandWaveStyle = it
                                                                                    settingsStore.dynamicIslandWaveStyle = it
                                                                                }
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                SettingToggleRow(
                                                                    title = s.islandShowQuickAppsTitle,
                                                                    subtitle = s.islandShowQuickAppsSub,
                                                                    checked = dynamicIslandShowQuickApps,
                                                                    onCheckedChange = {
                                                                        dynamicIslandShowQuickApps = it
                                                                        settingsStore.dynamicIslandShowQuickApps = it
                                                                    },
                                                                )
                                                                if (dynamicIslandShowQuickApps) {
                                                                    Spacer(Modifier.height(6.dp))
                                                                    SettingClickableRow(
                                                                        title = s.islandPinnedAppsTitle,
                                                                        value = when (s.language) {
                                                                            AppLanguage.FA -> "${s.language.localizeDigits(dynamicIslandPinnedApps.size.toString())} برنامه در نوار دسترسی سریع داینامیک آیلند (ضربه برای افزودن/حذف)"
                                                                            AppLanguage.AR -> "${s.language.localizeDigits(dynamicIslandPinnedApps.size.toString())} تطبيقات في شريط الوصول السريع بالجزيرة"
                                                                            AppLanguage.EN -> "${dynamicIslandPinnedApps.size} app(s) in Dynamic Island quick-launch bar (tap to edit)"
                                                                        },
                                                                        onClick = { showIslandPinnedAppsDialog = true },
                                                                    )
                                                                    Spacer(Modifier.height(6.dp))
                                                                    SettingToggleRow(
                                                                        title = when (s.language) {
                                                                            AppLanguage.FA -> "نمایش نام برنامه‌ها کنار آیکون"
                                                                            AppLanguage.AR -> "إظهار أسماء التطبيقات"
                                                                            AppLanguage.EN -> "Show App Names Next to Icons"
                                                                        },
                                                                        subtitle = when (s.language) {
                                                                            AppLanguage.FA -> "در صورت غیرفعال بودن، فقط آیکون برنامه‌ها به‌صورت فشرده نمایش داده می‌شود"
                                                                            AppLanguage.AR -> "عرض الأيقونات فقط عند التعطيل"
                                                                            AppLanguage.EN -> "Show compact app icons only when disabled"
                                                                        },
                                                                        checked = islandShowAppNames,
                                                                        onCheckedChange = {
                                                                            islandShowAppNames = it
                                                                            settingsStore.dynamicIslandShowAppNames = it
                                                                        },
                                                                    )
                                                                }
                                                            }
                                                            3 -> {
                                                                // Sub-page 3: Network Actions & Telemetry Badges Grid
                                                                SettingToggleRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "نوار تست پینگ و پروتکل در داینامیک آیلند"
                                                                        AppLanguage.AR -> "شريط فحص البينج والبروتوكول"
                                                                        AppLanguage.EN -> "Ping Test & Protocol Bar in Island"
                                                                    },
                                                                    subtitle = when (s.language) {
                                                                        AppLanguage.FA -> "نمایش تست پینگ واقعی و انتخاب پروتکل تونل"
                                                                        AppLanguage.AR -> "عرض اختبار البينج الحقيقي واختيار بروتوكول النفق"
                                                                        AppLanguage.EN -> "Show real ping testing and tunnel protocol selection"
                                                                    },
                                                                    checked = islandShowNetworkActions,
                                                                    onCheckedChange = {
                                                                        islandShowNetworkActions = it
                                                                        settingsStore.dynamicIslandShowNetworkActions = it
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 6.dp))
                                                                SettingToggleRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "دکمه‌های تلگرام و گیمینگ در داینامیک آیلند"
                                                                        AppLanguage.AR -> "أزرار تيليجرام والألعاب في الجزيرة التفاعلية"
                                                                        AppLanguage.EN -> "Telegram & Gaming Buttons in Island"
                                                                    },
                                                                    subtitle = when (s.language) {
                                                                        AppLanguage.FA -> "نمایش دکمه‌های پروکسی تلگرام و حالت گیمینگ داخل کارت داینامیک آیلند"
                                                                        AppLanguage.AR -> "إظهار أزرار بروكسي تيليجرام ووضع الألعاب داخل الجزيرة"
                                                                        AppLanguage.EN -> "Show Telegram proxy & Gaming mode buttons inside Dynamic Island"
                                                                    },
                                                                    checked = islandShowTelegramGamingActions,
                                                                    onCheckedChange = {
                                                                        islandShowTelegramGamingActions = it
                                                                        settingsStore.dynamicIslandShowTelegramGamingActions = it
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 6.dp))
                                                                SettingToggleRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "نوار تغییر سریع نشانگر، حرکت و پوسته داخل آیلند"
                                                                        AppLanguage.AR -> "شريط تبديل المؤشر والحركة والسطح داخل الجزيرة"
                                                                        AppLanguage.EN -> "Quick Style Deck Inside Expanded Island"
                                                                    },
                                                                    subtitle = when (s.language) {
                                                                        AppLanguage.FA -> "تغییر آنی نشانگر، فیزیک حرکت و پوسته با یک لمس داخل کارت داینامیک"
                                                                        AppLanguage.AR -> "تبديل المؤشر والحركة والسطح بلمسة واحدة"
                                                                        AppLanguage.EN -> "Cycle indicators, motions & surfaces right inside the island"
                                                                    },
                                                                    checked = islandShowQuickStyleDeck,
                                                                    onCheckedChange = {
                                                                        islandShowQuickStyleDeck = it
                                                                        settingsStore.dynamicIslandShowQuickStyleDeck = it
                                                                    },
                                                                )
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                                                                val badgeItems = listOf(
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "سرعت لحظه‌ای"
                                                                            AppLanguage.AR -> "السرعة الحية"
                                                                            AppLanguage.EN -> "Live Speed"
                                                                        },
                                                                        dynamicIslandShowSpeed,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowSpeed = v
                                                                            settingsStore.dynamicIslandShowSpeed = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "پینگ سرور"
                                                                            AppLanguage.AR -> "بينج الخادم"
                                                                            AppLanguage.EN -> "Server Ping"
                                                                        },
                                                                        dynamicIslandShowPing,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowPing = v
                                                                            settingsStore.dynamicIslandShowPing = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "نوسان پینگ (Jitter)"
                                                                            AppLanguage.AR -> "تذبذب البينج (Jitter)"
                                                                            AppLanguage.EN -> "Live Jitter (±ms)"
                                                                        },
                                                                        islandShowJitterBadge,
                                                                        { v: Boolean ->
                                                                            islandShowJitterBadge = v
                                                                            settingsStore.dynamicIslandShowJitterBadge = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "سپر امنیت تونل"
                                                                            AppLanguage.AR -> "درع أمان النفق"
                                                                            AppLanguage.EN -> "TLS Guard Badge"
                                                                        },
                                                                        islandShowSecurityBadge,
                                                                        { v: Boolean ->
                                                                            islandShowSecurityBadge = v
                                                                            settingsStore.dynamicIslandShowSecurityBadge = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "پرچم کشور"
                                                                            AppLanguage.AR -> "علم الدولة"
                                                                            AppLanguage.EN -> "Country Flag"
                                                                        },
                                                                        dynamicIslandShowFlag,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowFlag = v
                                                                            settingsStore.dynamicIslandShowFlag = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "نام سرور"
                                                                            AppLanguage.AR -> "اسم الخادم"
                                                                            AppLanguage.EN -> "Server Name"
                                                                        },
                                                                        dynamicIslandShowServerName,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowServerName = v
                                                                            settingsStore.dynamicIslandShowServerName = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "حجم مصرفی"
                                                                            AppLanguage.AR -> "حجم البيانات"
                                                                            AppLanguage.EN -> "Data Used"
                                                                        },
                                                                        dynamicIslandShowSessionData,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowSessionData = v
                                                                            settingsStore.dynamicIslandShowSessionData = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "نشان پورت/IP"
                                                                            AppLanguage.AR -> "شارة المنفذ"
                                                                            AppLanguage.EN -> "Port/IP Tag"
                                                                        },
                                                                        dynamicIslandShowIpBadge,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowIpBadge = v
                                                                            settingsStore.dynamicIslandShowIpBadge = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "تایمر اتصال"
                                                                            AppLanguage.AR -> "مؤقت الاتصال"
                                                                            AppLanguage.EN -> "Timer"
                                                                        },
                                                                        dynamicIslandShowTimer,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowTimer = v
                                                                            settingsStore.dynamicIslandShowTimer = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "نشان پروتکل"
                                                                            AppLanguage.AR -> "شارة البروتوكول"
                                                                            AppLanguage.EN -> "Protocol Tag"
                                                                        },
                                                                        dynamicIslandShowProtocolBadge,
                                                                        { v: Boolean ->
                                                                            dynamicIslandShowProtocolBadge = v
                                                                            settingsStore.dynamicIslandShowProtocolBadge = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "حالت فشرده"
                                                                            AppLanguage.AR -> "وضع مضغوط"
                                                                            AppLanguage.EN -> "Compact Mode"
                                                                        },
                                                                        dynamicIslandCompactTelemetry,
                                                                        { v: Boolean ->
                                                                            dynamicIslandCompactTelemetry = v
                                                                            settingsStore.dynamicIslandCompactTelemetry = v
                                                                        },
                                                                    ),
                                                                    Triple(
                                                                        when (s.language) {
                                                                            AppLanguage.FA -> "لرزش لمسی"
                                                                            AppLanguage.AR -> "اهتزاز لمسي"
                                                                            AppLanguage.EN -> "Haptic Touch"
                                                                        },
                                                                        dynamicIslandHapticEnabled,
                                                                        { v: Boolean ->
                                                                            dynamicIslandHapticEnabled = v
                                                                            settingsStore.dynamicIslandHapticEnabled = v
                                                                        },
                                                                    ),
                                                                )
                                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                    badgeItems.chunked(2).forEach { pair ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                        ) {
                                                                            pair.forEach { (label, checked, onToggle) ->
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .weight(1f)
                                                                                        .heightIn(min = 64.dp)
                                                                                        .clip(RoundedCornerShape(12.dp))
                                                                                        .background(neu.surfaceElevated.copy(alpha = 0.65f))
                                                                                        .clickable { onToggle(!checked) }
                                                                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                                ) {
                                                                                    Text(
                                                                                        text = label,
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                                        color = neu.textPrimary,
                                                                                        maxLines = 2,
                                                                                        softWrap = true,
                                                                                        lineHeight = 14.sp,
                                                                                        modifier = Modifier.weight(1f),
                                                                                    )
                                                                                    Switch(
                                                                                        checked = checked,
                                                                                        onCheckedChange = onToggle,
                                                                                        modifier = Modifier.scale(0.76f),
                                                                                        colors = SwitchDefaults.colors(
                                                                                            checkedThumbColor = Color.White,
                                                                                            checkedTrackColor = neu.accent,
                                                                                        ),
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            else -> {
                                                                // Sub-page 4: Deep Micro-Detail Geometry, Typography, Physics & Double-Tap Studio
                                                                // Paired 2-column responsive layout so all controls fit cleanly without clipping
                                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "ارتفاع بسته (dp) / پهنای باز (%)"
                                                                            AppLanguage.AR -> "الارتفاع (dp) / العرض الموسع (%)"
                                                                            AppLanguage.EN -> "Collapsed Height / Expanded Width"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = "${islandCollapsedHeightDp.roundToInt()}dp • ${islandExpandedWidthPct.roundToInt()}%",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Slider(
                                                                        value = islandCollapsedHeightDp,
                                                                        onValueChange = {
                                                                            islandCollapsedHeightDp = it
                                                                            settingsStore.dynamicIslandCollapsedHeightDp = it.roundToInt()
                                                                        },
                                                                        valueRange = 32f..56f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                    Slider(
                                                                        value = islandExpandedWidthPct,
                                                                        onValueChange = {
                                                                            islandExpandedWidthPct = it
                                                                            settingsStore.dynamicIslandExpandedWidthPercent = it.roundToInt()
                                                                        },
                                                                        valueRange = 82f..100f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                }

                                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "مقیاس فونت (%) / آیکون اپ (dp)"
                                                                            AppLanguage.AR -> "حجم الخط (%) / حجم الأيقونة (dp)"
                                                                            AppLanguage.EN -> "Font Scale (%) / App Icon (dp)"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = "${islandFontScalePct.roundToInt()}% • ${islandQuickAppIconDp.roundToInt()}dp",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Slider(
                                                                        value = islandFontScalePct,
                                                                        onValueChange = {
                                                                            islandFontScalePct = it
                                                                            settingsStore.dynamicIslandFontScalePercent = it.roundToInt()
                                                                        },
                                                                        valueRange = 80f..125f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                    Slider(
                                                                        value = islandQuickAppIconDp,
                                                                        onValueChange = {
                                                                            islandQuickAppIconDp = it
                                                                            settingsStore.dynamicIslandQuickAppIconSizeDp = it.roundToInt()
                                                                        },
                                                                        valueRange = 28f..48f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                }

                                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "ضخامت کادر / عمق سایه سه‌بعدی"
                                                                            AppLanguage.AR -> "سمك الإطار / عمق الظل"
                                                                            AppLanguage.EN -> "Border Width / 3D Shadow"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = "${String.format(java.util.Locale.US, "%.1f", islandBorderWidthDp)}dp • ${islandShadowDepthDp.roundToInt()}dp",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Slider(
                                                                        value = islandBorderWidthDp,
                                                                        onValueChange = {
                                                                            islandBorderWidthDp = it
                                                                            settingsStore.dynamicIslandBorderWidthDp = it
                                                                        },
                                                                        valueRange = 0.5f..3.0f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                    Slider(
                                                                        value = islandShadowDepthDp,
                                                                        onValueChange = {
                                                                            islandShadowDepthDp = it
                                                                            settingsStore.dynamicIslandShadowDepthDp = it.roundToInt()
                                                                        },
                                                                        valueRange = 0f..20f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                }

                                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "فیزیک فنر (نرمی / سرعت بازشدن)"
                                                                            AppLanguage.AR -> "فيزياء الزنبرك (النعومة / السرعة)"
                                                                            AppLanguage.EN -> "Spring Physics (Damping / Stiffness)"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textPrimary,
                                                                    )
                                                                    Text(
                                                                        text = "${islandSpringDampingPct.roundToInt()}% • ${islandSpringStiffness.roundToInt()}",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                        color = neu.accent,
                                                                    )
                                                                }
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Slider(
                                                                        value = islandSpringDampingPct,
                                                                        onValueChange = {
                                                                            islandSpringDampingPct = it
                                                                            settingsStore.dynamicIslandSpringDampingPercent = it.roundToInt()
                                                                        },
                                                                        valueRange = 55f..95f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                    Slider(
                                                                        value = islandSpringStiffness,
                                                                        onValueChange = {
                                                                            islandSpringStiffness = it
                                                                            settingsStore.dynamicIslandSpringStiffness = it.roundToInt()
                                                                        },
                                                                        valueRange = 180f..600f,
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = SliderDefaults.colors(thumbColor = neu.accent, activeTrackColor = neu.accent),
                                                                    )
                                                                }

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                ) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "عملیات دو بار ضربه (Double-Tap):"
                                                                            AppLanguage.AR -> "إجراء النقر المزدوج:"
                                                                            AppLanguage.EN -> "Double-Tap Action:"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = neu.textSecondary,
                                                                    )
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .background(neu.accent.copy(alpha = 0.12f))
                                                                            .clickable {
                                                                                islandCollapsedHeightDp = 40f
                                                                                settingsStore.dynamicIslandCollapsedHeightDp = 40
                                                                                islandExpandedWidthPct = 96f
                                                                                settingsStore.dynamicIslandExpandedWidthPercent = 96
                                                                                islandFontScalePct = 100f
                                                                                settingsStore.dynamicIslandFontScalePercent = 100
                                                                                islandQuickAppIconDp = 36f
                                                                                settingsStore.dynamicIslandQuickAppIconSizeDp = 36
                                                                                islandBorderWidthDp = 1.2f
                                                                                settingsStore.dynamicIslandBorderWidthDp = 1.2f
                                                                                islandShadowDepthDp = 8f
                                                                                settingsStore.dynamicIslandShadowDepthDp = 8
                                                                                islandSpringDampingPct = 82f
                                                                                settingsStore.dynamicIslandSpringDampingPercent = 82
                                                                                islandSpringStiffness = 380f
                                                                                settingsStore.dynamicIslandSpringStiffness = 380
                                                                            }
                                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "بازنشانی پیش‌فرض"
                                                                                AppLanguage.AR -> "إعادة ضبط"
                                                                                AppLanguage.EN -> "Reset Defaults"
                                                                            },
                                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.accent,
                                                                        )
                                                                    }
                                                                }
                                                                Spacer(Modifier.height(6.dp))
                                                                SettingsDropdownRow(
                                                                    title = when (s.language) {
                                                                        AppLanguage.FA -> "عملکرد ضربه دوگانه"
                                                                        AppLanguage.AR -> "إجراء النقر المزدوج"
                                                                        AppLanguage.EN -> "Double-Tap Action"
                                                                    },
                                                                    selected = islandDoubleTapAction.localizedLabel(s.language),
                                                                    options = com.vauth.foxyvpn.data.DynamicIslandDoubleTapAction.entries.map { it.localizedLabel(s.language) },
                                                                    icon = Icons.Filled.SwapHoriz,
                                                                    onSelected = { label ->
                                                                        com.vauth.foxyvpn.data.DynamicIslandDoubleTapAction.entries.firstOrNull { it.localizedLabel(s.language) == label }?.let {
                                                                            islandDoubleTapAction = it
                                                                            settingsStore.dynamicIslandDoubleTapAction = it
                                                                        }
                                                                    },
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(Modifier.height(12.dp))
                                            }
                                        }

                                        // -----------------------------------------------------------------
                                        // CATEGORY 3: REAL PROTOCOL SWITCHING, DNS & NETWORK
                                        // -----------------------------------------------------------------
                                        SettingsCategory.PROTOCOL_NETWORK -> {
                                            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                Column(Modifier.padding(16.dp)) {
                                                    when (subPage) {
                                                        0 -> {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "پروتکل اتصال تونل"
                                                                        AppLanguage.AR -> "بروتوكول اتصال النفق"
                                                                        AppLanguage.EN -> "Tunnel connection protocol"
                                                                    },
                                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                                        fontFamily = fonts.headingFont,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 15.sp,
                                                                        lineHeight = 21.sp,
                                                                        letterSpacing = 0.sp,
                                                                    ),
                                                                    color = neu.textPrimary,
                                                                    modifier = Modifier.weight(1f),
                                                                    maxLines = 2,
                                                                    softWrap = true,
                                                                )
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(Color(0xFF080C14))
                                                                        .border(1.dp, Color(0xFF0088FF).copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                ) {
                                                                    Text(
                                                                        text = activeProtocolBadge,
                                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                                            fontFamily = fonts.monoFont,
                                                                            fontWeight = FontWeight.Bold,
                                                                        ),
                                                                        color = Color.White,
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.height(4.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "هسته شبکه"
                                                                        AppLanguage.AR -> "محرك الشبكة"
                                                                        AppLanguage.EN -> "Networking Core"
                                                                    },
                                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                    color = neu.textPrimary,
                                                                )
                                                                IconButton(onClick = { showCoreGuideDialog = true }) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Info,
                                                                        contentDescription = when (s.language) {
                                                                            AppLanguage.FA -> "راهنمای تفاوت هسته‌ها"
                                                                            AppLanguage.AR -> "دليل الفرق بين المحركات"
                                                                            AppLanguage.EN -> "Explain core differences"
                                                                        },
                                                                        tint = neu.accent,
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.height(6.dp))
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(12.dp))
                                                                    .background(neu.accent.copy(alpha = 0.14f))
                                                                    .border(1.2.dp, neu.accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = when (s.language) {
                                                                            AppLanguage.FA -> "هسته یکپارچه Sing-Box MASQUE v2.0"
                                                                            AppLanguage.AR -> "محرك Sing-Box MASQUE v2.0 الموحد"
                                                                            AppLanguage.EN -> "Unified Sing-Box MASQUE v2.0 Core"
                                                                        },
                                                                        color = neu.accent,
                                                                        fontWeight = FontWeight.Bold,
                                                                    )
                                                                    Spacer(Modifier.height(2.dp))
                                                                    Text(
                                                                        text = "v1.15.0-foxy-masque.2 • H3/QUIC • H2/MASQUE • H1.1/TLS • uTLS",
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont),
                                                                        color = neu.textSecondary,
                                                                    )
                                                                }
                                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = neu.accent)
                                                            }
                                                            Spacer(Modifier.height(10.dp))
                                                            Text(
                                                                text = when (s.language) {
                                                                    AppLanguage.FA -> "با انتخاب هر پروتکل، تونل فعال بلافاصله با همان پروتکل بازتنظیم و متصل می‌شود"
                                                                    AppLanguage.AR -> "عند اختيار أي بروتوكول يتم إعادة ربط النفق فوراً بهذا البروتوكول"
                                                                    AppLanguage.EN -> "Selecting a protocol immediately re-handshakes the active tunnel with that transport"
                                                                },
                                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = fonts.bodyFont),
                                                                color = neu.textSecondary,
                                                            )
                                                            Spacer(Modifier.height(10.dp))
                                                            SettingClickableRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "پروفایل اتصال"
                                                                    AppLanguage.AR -> "ملف الاتصال"
                                                                    AppLanguage.EN -> "Connection profile"
                                                                },
                                                                value = connectionProfile.localizedLabel(s.language),
                                                                onClick = { showConnectionProfileDialog = true },
                                                            )
                                                            Spacer(Modifier.height(8.dp))
                                                            Column(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                            ) {
                                                                ConnectionProtocolMode.entries.forEach { mode ->
                                                                    val isSupported = mode.isSupported
                                                                    val isSelected = isSupported && connectionProtocolMode == mode
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .then(if (isSupported) Modifier.neumorphicTouchBounce() else Modifier)
                                                                            .fillMaxWidth()
                                                                            .clip(RoundedCornerShape(12.dp))
                                                                            .background(
                                                                                if (!isSupported) neu.surfaceElevated.copy(alpha = 0.45f)
                                                                                else if (isSelected) neu.accent.copy(alpha = 0.16f)
                                                                                else neu.surfaceElevated,
                                                                            )
                                                                            .border(
                                                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                                                color = if (isSelected) neu.accent else neu.textSecondary.copy(alpha = 0.15f),
                                                                                shape = RoundedCornerShape(12.dp),
                                                                            )
                                                                            .clickable(enabled = isSupported) {
                                                                                connectionProtocolMode = mode
                                                                                settingsStore.connectionProtocolMode = mode
                                                                                FoxyVpnService.switchProtocol(context, mode)
                                                                            }
                                                                            .padding(horizontal = 12.dp, vertical = 11.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                    ) {
                                                                        Column(
                                                                            modifier = Modifier.weight(1f),
                                                                        ) {
                                                                            Text(
                                                                                text = mode.localizedLabel(s.language),
                                                                                style = MaterialTheme.typography.titleSmall.copy(
                                                                                    fontFamily = fonts.headingFont,
                                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                                    fontSize = 15.sp,
                                                                                    lineHeight = 21.sp,
                                                                                    letterSpacing = 0.sp,
                                                                                ),
                                                                                color = if (!isSupported) neu.textSecondary.copy(alpha = 0.58f)
                                                                                else if (isSelected) neu.accent else neu.textPrimary,
                                                                                maxLines = 2,
                                                                                softWrap = true,
                                                                            )
                                                                            Spacer(Modifier.height(3.dp))
                                                                            Text(
                                                                                text = mode.localizedDescription(s.language),
                                                                                style = MaterialTheme.typography.bodySmall.copy(
                                                                                    fontFamily = fonts.bodyFont,
                                                                                    fontSize = 13.sp,
                                                                                    lineHeight = 19.sp,
                                                                                    letterSpacing = 0.sp,
                                                                                ),
                                                                                color = neu.textSecondary,
                                                                                maxLines = 3,
                                                                                softWrap = true,
                                                                            )
                                                                        }
                                                                        if (isSelected) {
                                                                            Icon(
                                                                                imageVector = Icons.Filled.CheckCircle,
                                                                                contentDescription = null,
                                                                                tint = neu.accent,
                                                                                modifier = Modifier.size(20.dp),
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))

                                                            SettingToggleRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "مسدودساز تبلیغات و ردیاب‌ها"
                                                                    AppLanguage.AR -> "حظر الإعلانات والمتتبعات"
                                                                    AppLanguage.EN -> "Ad & Tracker Blocker (AdBlock DNS)"
                                                                },
                                                                subtitle = when (s.language) {
                                                                    AppLanguage.FA -> "مسدودسازی خودکار دامنه‌های تبلیغاتی و ردیاب‌ها در سطح تونل"
                                                                    AppLanguage.AR -> "حظر تلقائي لنطاقات الإعلانات عبر النفق"
                                                                    AppLanguage.EN -> "Automatically block ad networks and trackers inside the VPN tunnel"
                                                                },
                                                                checked = adBlockEnabled,
                                                                onCheckedChange = {
                                                                    adBlockEnabled = it
                                                                    settingsStore.adBlockEnabled = it
                                                                    if (it && dnsProvider != SettingsStore.DnsProvider.ADGUARD) {
                                                                        dnsProvider = SettingsStore.DnsProvider.ADGUARD
                                                                        settingsStore.dnsProvider = SettingsStore.DnsProvider.ADGUARD
                                                                    }
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingToggleRow(
                                                                title = s.verifyExitTitle,
                                                                subtitle = s.verifyExitSub,
                                                                checked = exitCheckEnabled,
                                                                onCheckedChange = {
                                                                    exitCheckEnabled = it
                                                                    settingsStore.exitCheckEnabled = it
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = s.tunnelMtuTitle,
                                                                value = if (mtuModeAuto) "Auto (${s.mtuBytesValue(1400)})" else s.mtuBytesValue(mtu),
                                                                onClick = { showMtuDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "بازگردانی تنظیمات شبکه به مقادیر امن پیش‌فرض"
                                                                    AppLanguage.AR -> "استعادة إعدادات الشبكة الآمنة الافتراضية"
                                                                    AppLanguage.EN -> "Reset Network Tuning to Safe Defaults"
                                                                },
                                                                value = "Auto Safe",
                                                                onClick = {
                                                                    settingsStore.resetNetworkTuningToSafeDefaults()
                                                                    mtuModeAuto = true
                                                                    mtu = 1400
                                                                    FoxyVpnService.applyNetworkConfiguration(context)
                                                                    dnsProvider = settingsStore.dnsProvider
                                                                    bootstrapDnsProvider = settingsStore.bootstrapDnsProvider
                                                                    tlsSplitMode = settingsStore.tlsSplitMode
                                                                    connectionProtocolMode = settingsStore.connectionProtocolMode
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "کیل‌سوئیچ و اتصال دائمی اندروید (Kill-Switch)"
                                                                    AppLanguage.AR -> "مفتاح الإيقاف والاتصال الدائم (Kill-Switch)"
                                                                    AppLanguage.EN -> "System Kill-Switch & Always-On VPN"
                                                                },
                                                                value = when (s.language) {
                                                                    AppLanguage.FA -> "تنظیمات VPN سیستم"
                                                                    AppLanguage.AR -> "إعدادات VPN للنظام"
                                                                    AppLanguage.EN -> "Android VPN Settings"
                                                                },
                                                                onClick = {
                                                                    runCatching {
                                                                        context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                                                                    }
                                                                },
                                                            )
                                                        }
                                                        1 -> {
                                                            SectionHeading(s.sectionDns)
                                                            SettingClickableRow(
                                                                title = s.tunnelDnsTitle,
                                                                value = when (dnsProvider) {
                                                                    SettingsStore.DnsProvider.CUSTOM -> customDohUrl.ifBlank { dnsProvider.localizedLabel(s.language) }
                                                                    else -> dnsProvider.localizedLabel(s.language)
                                                                },
                                                                onClick = { showDnsProviderDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingClickableRow(
                                                                title = s.resolverDnsTitle,
                                                                value = when (bootstrapDnsProvider) {
                                                                    SettingsStore.BootstrapDnsProvider.CUSTOM -> s.language.localizeDigits(customBootstrapIp.ifBlank { bootstrapDnsProvider.localizedLabel(s.language) })
                                                                    else -> bootstrapDnsProvider.localizedLabel(s.language)
                                                                },
                                                                onClick = { showBootstrapDnsDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                                            SectionHeading(s.sectionSplitTunneling)
                                                            SettingClickableRow(
                                                                title = s.excludedAppsTitle,
                                                                value = if (excludedApps.isEmpty()) s.none else s.excludedAppsCount(excludedApps.size),
                                                                onClick = { showSplitTunnelDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))
                                                            SettingClickableRow(
                                                                title = s.tlsFragmentationTitle,
                                                                value = tlsSplitMode.localizedLabel(s.language),
                                                                onClick = { showTlsSplitDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingClickableRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "اثر انگشت TLS مرورگر (uTLS Fingerprint)"
                                                                    AppLanguage.AR -> "بصمة متصفح TLS (uTLS)"
                                                                    AppLanguage.EN -> "uTLS ClientHello Fingerprint"
                                                                },
                                                                value = utlsFingerprint.localizedLabel(s.language),
                                                                onClick = { showUtlsDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingToggleRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "شتاب‌دهنده TCP Fast Open"
                                                                    AppLanguage.AR -> "تسريع TCP Fast Open"
                                                                    AppLanguage.EN -> "TCP Fast Open (TFO)"
                                                                },
                                                                subtitle = when (s.language) {
                                                                    AppLanguage.FA -> "کاهش تاخیر دست‌دهی اولیه روی پروتکل‌های مبتنی بر TCP"
                                                                    AppLanguage.AR -> "تقليل زمن المصافحة الأولية لاتصالات TCP"
                                                                    AppLanguage.EN -> "Reduces initial TCP handshake latency when supported by the network"
                                                                },
                                                                checked = tcpFastOpen,
                                                                onCheckedChange = {
                                                                    tcpFastOpen = it
                                                                    settingsStore.tcpFastOpen = it
                                                                    FoxyVpnService.reconnect(context)
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingToggleRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "مسیریابی IPv6 در تونل"
                                                                    AppLanguage.AR -> "توجيه IPv6 داخل النفق"
                                                                    AppLanguage.EN -> "IPv6 Tunnel Routing"
                                                                },
                                                                subtitle = when (s.language) {
                                                                    AppLanguage.FA -> "هدایت ترافیک IPv6 در کنار IPv4 از داخل تونل"
                                                                    AppLanguage.AR -> "توجيه حركة مرور IPv6 بجانب IPv4 عبر النفق"
                                                                    AppLanguage.EN -> "Route IPv6 traffic alongside IPv4 inside the Sing-Box tunnel"
                                                                },
                                                                checked = ipv6Enabled,
                                                                onCheckedChange = {
                                                                    ipv6Enabled = it
                                                                    settingsStore.ipv6Enabled = it
                                                                    FoxyVpnService.reconnect(context)
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingToggleRow(
                                                                title = when (s.language) {
                                                                    AppLanguage.FA -> "عبور مستقیم شبکه محلی (Bypass LAN)"
                                                                    AppLanguage.AR -> "تجاوز الشبكة المحلية (Bypass LAN)"
                                                                    AppLanguage.EN -> "Bypass Local Network (LAN)"
                                                                },
                                                                subtitle = when (s.language) {
                                                                    AppLanguage.FA -> "دسترسی مستقیم به مودم و دستگاه‌های شبکه خانگی بدون عبور از VPN"
                                                                    AppLanguage.AR -> "الوصول المباشر لأجهزة الشبكة المحلية دون المرور عبر VPN"
                                                                    AppLanguage.EN -> "Allow local LAN devices and routers to connect directly outside the VPN"
                                                                },
                                                                checked = bypassLocalNetwork,
                                                                onCheckedChange = {
                                                                    bypassLocalNetwork = it
                                                                    settingsStore.bypassLocalNetwork = it
                                                                    FoxyVpnService.reconnect(context)
                                                                },
                                                            )
                                                        }
                                                        else -> {
                                                            SettingToggleRow(
                                                                title = s.proxyOnlyModeTitle,
                                                                subtitle = s.proxyOnlyModeSub,
                                                                checked = proxyOnlyMode,
                                                                onCheckedChange = {
                                                                    proxyOnlyMode = it
                                                                    settingsStore.proxyOnlyMode = it
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = s.localBindAddressTitle,
                                                                value = s.language.localizeDigits(socksBindAddress),
                                                                onClick = { showSocksBindDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = s.socksPortTitle,
                                                                value = s.language.localizeNumber(socksPort),
                                                                onClick = { showSocksPortDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingToggleRow(
                                                                title = s.enableHttpProxyTitle,
                                                                subtitle = s.enableHttpProxySub,
                                                                checked = httpProxyEnabled,
                                                                onCheckedChange = {
                                                                    httpProxyEnabled = it
                                                                    settingsStore.httpProxyEnabled = it
                                                                },
                                                            )
                                                            if (httpProxyEnabled) {
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                                SettingClickableRow(
                                                                    title = s.httpProxyPortTitle,
                                                                    value = s.language.localizeNumber(httpProxyPort),
                                                                    onClick = { showHttpPortDialog = true },
                                                                )
                                                            }
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingClickableRow(
                                                                title = s.customEdgeAddressTitle,
                                                                value = if (customEdgeAddress.isBlank()) s.notSet else s.language.localizeDigits(customEdgeAddress),
                                                                onClick = { showEdgeAddressDialog = true },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            SettingToggleRow(
                                                                title = s.chainUpstreamProxyTitle,
                                                                subtitle = s.chainUpstreamProxySub,
                                                                checked = upstreamProxyEnabled,
                                                                onCheckedChange = {
                                                                    upstreamProxyEnabled = it
                                                                    settingsStore.upstreamProxyEnabled = it
                                                                },
                                                            )
                                                            if (upstreamProxyEnabled) {
                                                                HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 6.dp))
                                                                SettingClickableRow(
                                                                    title = s.upstreamProxyTypeTitle,
                                                                    value = upstreamProxyType.localizedLabel(s.language),
                                                                    onClick = { showUpstreamProxyTypeDialog = true },
                                                                )
                                                                SettingClickableRow(
                                                                    title = s.upstreamProxyAddressTitle,
                                                                    value = if (upstreamProxyHost.isBlank()) s.notSet else s.language.localizeDigits("$upstreamProxyHost:$upstreamProxyPort"),
                                                                    onClick = { showUpstreamProxyAddressDialog = true },
                                                                )
                                                                SettingClickableRow(
                                                                    title = s.upstreamProxyCredentialsTitle,
                                                                    value = if (upstreamProxyUsername.isBlank()) s.none else upstreamProxyUsername,
                                                                    onClick = { showUpstreamProxyCredentialsDialog = true },
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // -----------------------------------------------------------------
                                        // CATEGORY 4: SPEED BOOSTER, BATTERY SAVER & AUTOMATION
                                        // -----------------------------------------------------------------
                                        SettingsCategory.SPEED_AUTOMATION -> {
                                            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                Column(Modifier.padding(16.dp)) {
                                                    if (subPage == 0) {
                                                        SettingToggleRow(
                                                            title = s.unmeteredSpeedBoostTitle,
                                                            subtitle = s.unmeteredSpeedBoostSub,
                                                            checked = unmeteredVpnModeEnabled,
                                                            onCheckedChange = {
                                                                unmeteredVpnModeEnabled = it
                                                                settingsStore.unmeteredVpnModeEnabled = it
                                                                FoxyVpnService.applyNetworkConfiguration(context)
                                                            },
                                                        )
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                        SettingToggleRow(
                                                            title = s.batterySaverModeTitle,
                                                            subtitle = s.batterySaverModeSub,
                                                            checked = batterySaverModeEnabled,
                                                            onCheckedChange = {
                                                                batterySaverModeEnabled = it
                                                                settingsStore.batterySaverModeEnabled = it
                                                                FoxyVpnService.applyRuntimeSettings(context)
                                                            },
                                                        )
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                        val powerManager = context.getSystemService(PowerManager::class.java)
                                                        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            runCatching { powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true }.getOrDefault(false)
                                                        } else {
                                                            true
                                                        }
                                                        SettingClickableRow(
                                                            title = s.batteryExemptionTitle,
                                                            value = if (isIgnoringBatteryOptimizations) s.batteryExemptionGrantedSub else s.batteryExemptionRequestSub,
                                                            onClick = {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                                    runCatching {
                                                                        @Suppress("BatteryLife")
                                                                        val intent = Intent(
                                                                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                                                            Uri.parse("package:${context.packageName}"),
                                                                        )
                                                                        context.startActivity(intent)
                                                                    }.onFailure {
                                                                        runCatching {
                                                                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                        )
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingClickableRow(
                                                            title = when (s.language) {
                                                                AppLanguage.FA -> "محافظت پس‌زمینه رام‌ها (MIUI / OneUI / Android 10+)"
                                                                AppLanguage.AR -> "حماية الخلفية للأنظمة (MIUI / OneUI)"
                                                                AppLanguage.EN -> "OEM ROM Background Protection (Android 10+)"
                                                            },
                                                            value = when (s.language) {
                                                                AppLanguage.FA -> "تنظیمات اجرای خودکار"
                                                                AppLanguage.AR -> "إعدادات التشغيل التلقائي"
                                                                AppLanguage.EN -> "App Info & Autostart"
                                                            },
                                                            onClick = {
                                                                runCatching {
                                                                    val intent = Intent(
                                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                                        Uri.parse("package:${context.packageName}"),
                                                                    )
                                                                    context.startActivity(intent)
                                                                }
                                                            },
                                                        )
                                                    } else {
                                                        SettingToggleRow(
                                                            title = s.scheduleVpnTitle,
                                                            subtitle = s.scheduleVpnSub,
                                                            checked = scheduleEnabled,
                                                            onCheckedChange = {
                                                                scheduleEnabled = it
                                                                settingsStore.scheduleEnabled = it
                                                            },
                                                        )
                                                        if (scheduleEnabled) {
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                            ) {
                                                                Box(modifier = Modifier.weight(1f)) {
                                                                    SettingClickableRow(
                                                                        title = s.scheduleFromLabel,
                                                                        value = s.language.formatClockTimeLocalized(scheduleStartHour, scheduleStartMinute),
                                                                        onClick = {
                                                                            TimePickerDialog(
                                                                                context,
                                                                                { _, h, m ->
                                                                                    scheduleStartHour = h
                                                                                    scheduleStartMinute = m
                                                                                    settingsStore.scheduleStartHour = h
                                                                                    settingsStore.scheduleStartMinute = m
                                                                                },
                                                                                scheduleStartHour,
                                                                                scheduleStartMinute,
                                                                                true,
                                                                            ).show()
                                                                        },
                                                                    )
                                                                }
                                                                Box(modifier = Modifier.weight(1f)) {
                                                                    SettingClickableRow(
                                                                        title = s.scheduleToLabel,
                                                                        value = s.language.formatClockTimeLocalized(scheduleEndHour, scheduleEndMinute),
                                                                        onClick = {
                                                                            TimePickerDialog(
                                                                                context,
                                                                                { _, h, m ->
                                                                                    scheduleEndHour = h
                                                                                    scheduleEndMinute = m
                                                                                    settingsStore.scheduleEndHour = h
                                                                                    settingsStore.scheduleEndMinute = m
                                                                                },
                                                                                scheduleEndHour,
                                                                                scheduleEndMinute,
                                                                                true,
                                                                            ).show()
                                                                        },
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingToggleRow(
                                                            title = s.idleDisconnectTitle,
                                                            subtitle = s.idleDisconnectSub,
                                                            checked = idleDisconnectEnabled,
                                                            onCheckedChange = {
                                                                idleDisconnectEnabled = it
                                                                settingsStore.idleDisconnectEnabled = it
                                                            },
                                                        )
                                                        if (idleDisconnectEnabled) {
                                                            SettingClickableRow(
                                                                title = s.idleTimeoutTitle,
                                                                value = s.language.formatMinutesLocalized(idleTimeoutMinutes),
                                                                onClick = { showIdleTimeoutDialog = true },
                                                            )
                                                        }
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingToggleRow(
                                                            title = s.sessionDataLimitTitle,
                                                            subtitle = s.sessionDataLimitSub,
                                                            checked = sessionDataLimitEnabled,
                                                            onCheckedChange = {
                                                                sessionDataLimitEnabled = it
                                                                settingsStore.sessionDataLimitEnabled = it
                                                            },
                                                        )
                                                        if (sessionDataLimitEnabled) {
                                                            SettingClickableRow(
                                                                title = s.sessionDataLimitAmountTitle,
                                                                value = "${s.language.localizeNumber(sessionDataLimitMb)} MB",
                                                                onClick = { showDataCapDialog = true },
                                                            )
                                                        }
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingToggleRow(
                                                            title = s.smartPingSwitchTitle,
                                                            subtitle = s.smartPingSwitchSub,
                                                            checked = smartPingAutoSwitch,
                                                            onCheckedChange = {
                                                                smartPingAutoSwitch = it
                                                                settingsStore.smartPingAutoSwitch = it
                                                            },
                                                        )
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingToggleRow(
                                                            title = s.autoConnectLaunchTitle,
                                                            subtitle = s.autoConnectLaunchSub,
                                                            checked = autoConnectOnLaunch,
                                                            onCheckedChange = {
                                                                autoConnectOnLaunch = it
                                                                settingsStore.autoConnectOnLaunch = it
                                                            },
                                                        )
                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                                                        SettingToggleRow(
                                                            title = s.stealthNotificationTitle,
                                                            subtitle = s.stealthNotificationSub,
                                                            checked = stealthNotificationEnabled,
                                                            onCheckedChange = {
                                                                stealthNotificationEnabled = it
                                                                settingsStore.stealthNotificationEnabled = it
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // -----------------------------------------------------------------
                                        // CATEGORY 5: SMART NETWORK & SECURITY TOOLKIT
                                        // -----------------------------------------------------------------
                                        SettingsCategory.SMART_TOOLKIT -> {
                                            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                Column(Modifier.padding(18.dp)) {
                                                    SettingToggleRow(
                                                        title = when (s.language) {
                                                            AppLanguage.FA -> "به‌روزرسانی خودکار سرورهای رسمی Mozilla"
                                                            AppLanguage.AR -> "التحديث التلقائي لخوادم Mozilla الرسمية"
                                                            AppLanguage.EN -> "Automatic official Mozilla server updates"
                                                        },
                                                        subtitle = when (s.language) {
                                                            AppLanguage.FA -> "هر ۱۲ ساعت با اینترنت فعال؛ پاسخ نامعتبر cache سالم را جایگزین نمی‌کند"
                                                            AppLanguage.AR -> "كل 12 ساعة عند توفر الإنترنت؛ لا يستبدل التخزين المؤقت السليم برد غير صالح"
                                                            AppLanguage.EN -> "Every 12 hours when online; invalid responses never replace a valid cache"
                                                        },
                                                        checked = serverListAutoRefresh,
                                                        onCheckedChange = {
                                                            serverListAutoRefresh = it
                                                            settingsStore.serverListAutoRefresh = it
                                                            OfficialServerListWorker.schedule(context)
                                                        },
                                                    )
                                                    Spacer(Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        Text(
                                                            text = serverListStatus ?: when (s.language) {
                                                                AppLanguage.FA -> "منبع: Mozilla Remote Settings رسمی"
                                                                AppLanguage.AR -> "المصدر: Mozilla Remote Settings الرسمي"
                                                                AppLanguage.EN -> "Source: official Mozilla Remote Settings"
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            color = neu.textSecondary,
                                                            style = MaterialTheme.typography.bodySmall,
                                                        )
                                                        TextButton(
                                                            enabled = !serverListRefreshing,
                                                            onClick = {
                                                                serverListRefreshing = true
                                                                serverListStatus = null
                                                                refreshScope.launch {
                                                                    val result = ServerListClient(context).refreshFromOfficial()
                                                                    result.onSuccess { count ->
                                                                        settingsStore.lastServerListRefreshEpochMs = System.currentTimeMillis()
                                                                        serverListStatus = "Updated: $count countries"
                                                                    }.onFailure { error ->
                                                                        serverListStatus = "Update failed: ${error.message ?: "network error"}"
                                                                    }
                                                                    serverListRefreshing = false
                                                                }
                                                            },
                                                        ) {
                                                            if (serverListRefreshing) {
                                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                            } else {
                                                                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                                                            }
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(when (s.language) {
                                                                AppLanguage.FA -> "اکنون"
                                                                AppLanguage.AR -> "الآن"
                                                                AppLanguage.EN -> "Update now"
                                                            })
                                                        }
                                                    }
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = when (s.language) {
                                                            AppLanguage.FA -> "پروکسی مستقل تلگرام (SOCKS5)"
                                                            AppLanguage.AR -> "بروكسي تيليجرام المستقل (SOCKS5)"
                                                            AppLanguage.EN -> "Standalone Telegram SOCKS5 Proxy"
                                                        },
                                                        value = "127.0.0.1:${settingsStore.socksPort}",
                                                        leadingIconRes = R.drawable.ic_telegram_proxy,
                                                        onClick = { onOpenTools(5) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = when (s.language) {
                                                            AppLanguage.FA -> "حالت گیمینگ و آنالیز پینگ/جیتر"
                                                            AppLanguage.AR -> "وضع الألعاب وتحليل البينج والجيتر"
                                                            AppLanguage.EN -> "Gaming Mode & Jitter/Loss Analyzer"
                                                        },
                                                        value = if (settingsStore.gamingModeEnabled) "Active" else "Ready",
                                                        onClick = { onOpenTools(6) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = s.toolSpeedTitle,
                                                        value = s.toolSpeedSubtitle,
                                                        onClick = { onOpenTools(0) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = s.toolLeakTitle,
                                                        value = s.toolLeakSubtitle,
                                                        onClick = { onOpenTools(1) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = s.toolScannerTitle,
                                                        value = s.toolScannerSubtitle,
                                                        onClick = { onOpenTools(2) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = s.toolWatchdogTitle,
                                                        value = s.toolWatchdogSubtitle,
                                                        onClick = { onOpenTools(3) },
                                                    )
                                                    HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                    SettingClickableRow(
                                                        title = s.toolMtuTitle,
                                                        value = s.toolMtuSubtitle,
                                                        onClick = { onOpenTools(4) },
                                                    )
                                                }
                                            }
                                        }

                                        // -----------------------------------------------------------------
                                        // CATEGORY 6: MULTI-ACCOUNT MANAGER, MOZILLA & ABOUT
                                        // -----------------------------------------------------------------
                                        SettingsCategory.ACCOUNTS_ABOUT -> {
                                            if (subPage == 0) {
                                                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(16.dp)) {
                                                        SettingToggleRow(
                                                            title = when (s.language) {
                                                                AppLanguage.FA -> "تعویض خودکار اکانت بر اساس درصد مصرف"
                                                                AppLanguage.AR -> "تبديل الحساب تلقائياً حسب نسبة الاستهلاك"
                                                                AppLanguage.EN -> "Auto-Switch Account on Quota Threshold"
                                                            },
                                                            subtitle = when (s.language) {
                                                                AppLanguage.FA -> "انتقال خودکار اتصال به اکانت بعدی هنگام رسیدن مصرف به سقف"
                                                                AppLanguage.AR -> "الانتقال تلقائياً لحساب آخر عند وصول الاستهلاك للحد"
                                                                AppLanguage.EN -> "Automatically switch to another saved account when quota exceeds threshold"
                                                            },
                                                            checked = autoSwitchAccountByQuotaEnabled,
                                                            onCheckedChange = {
                                                                autoSwitchAccountByQuotaEnabled = it
                                                                settingsStore.autoSwitchAccountByQuotaEnabled = it
                                                            },
                                                        )
                                                        if (autoSwitchAccountByQuotaEnabled) {
                                                            Spacer(Modifier.height(8.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            ) {
                                                                listOf(75, 85, 90, 95).forEach { pct ->
                                                                    val isSelected = autoSwitchQuotaThresholdPercent == pct
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .clip(RoundedCornerShape(10.dp))
                                                                            .background(if (isSelected) neu.accent.copy(alpha = 0.16f) else neu.surfaceElevated)
                                                                            .border(
                                                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                                                color = if (isSelected) neu.accent else neu.textSecondary.copy(alpha = 0.15f),
                                                                                shape = RoundedCornerShape(10.dp),
                                                                            )
                                                                            .clickable {
                                                                                autoSwitchQuotaThresholdPercent = pct
                                                                                settingsStore.autoSwitchQuotaThresholdPercent = pct
                                                                            }
                                                                            .padding(vertical = 7.dp),
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Text(
                                                                            text = s.language.formatPercentLocalized(pct),
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.monoFont, fontWeight = FontWeight.Bold),
                                                                            color = if (isSelected) neu.accent else neu.textPrimary,
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))

                                                        Text(
                                                            text = when (s.language) {
                                                                AppLanguage.FA -> "اکانت‌های ذخیره‌شده (${s.language.localizeNumber(savedAccounts.size)})"
                                                                AppLanguage.AR -> "الحسابات المحفوظة (${s.language.localizeNumber(savedAccounts.size)})"
                                                                AppLanguage.EN -> "Saved Accounts (${savedAccounts.size})"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = fonts.headingFont, fontWeight = FontWeight.Bold),
                                                            color = neu.textPrimary,
                                                        )
                                                        Spacer(Modifier.height(6.dp))
                                                        if (savedAccounts.isEmpty()) {
                                                            Text(
                                                                text = activeAccountEmail ?: when (s.language) {
                                                                    AppLanguage.FA -> "اکانت فعلی متصل است"
                                                                    AppLanguage.AR -> "الحساب الحالي متصل"
                                                                    AppLanguage.EN -> "Active session connected"
                                                                },
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = neu.textSecondary,
                                                            )
                                                        } else {
                                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                savedAccounts.take(3).forEach { acc ->
                                                                    val isCurrent = acc.email.equals(activeAccountEmail, ignoreCase = true)
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clip(RoundedCornerShape(12.dp))
                                                                            .background(if (isCurrent) neu.accent.copy(alpha = 0.12f) else neu.surfaceElevated.copy(alpha = 0.7f))
                                                                            .border(1.dp, if (isCurrent) neu.accent.copy(alpha = 0.45f) else neu.textSecondary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                                            .clickable {
                                                                                if (!isCurrent) tokenStore.switchToAccount(acc.email)
                                                                            }
                                                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                    ) {
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(
                                                                                text = acc.email,
                                                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                                                color = if (isCurrent) neu.accent else neu.textPrimary,
                                                                                maxLines = 1,
                                                                                overflow = TextOverflow.Ellipsis,
                                                                            )
                                                                            val quotaLabel = when {
                                                                                acc.isUnlimited -> when (s.language) {
                                                                                    AppLanguage.FA -> "حجم نامحدود"
                                                                                    AppLanguage.AR -> "غير محدود"
                                                                                    AppLanguage.EN -> "Unlimited Quota"
                                                                                }
                                                                                acc.quotaMax != null && acc.quotaMax > 0L -> {
                                                                                    val pct = s.language.formatPercentLocalized(acc.usedPercent)
                                                                                    when (s.language) {
                                                                                        AppLanguage.FA -> "مصرف شده: $pct"
                                                                                        AppLanguage.AR -> "المستهلك: $pct"
                                                                                        AppLanguage.EN -> "Used: $pct"
                                                                                    }
                                                                                }
                                                                                else -> when (s.language) {
                                                                                    AppLanguage.FA -> "آماده جایگزینی سریع"
                                                                                    AppLanguage.AR -> "جاهز للتبديل السريع"
                                                                                    AppLanguage.EN -> "Ready for instant switch"
                                                                                }
                                                                            }
                                                                            Text(
                                                                                text = quotaLabel,
                                                                                style = MaterialTheme.typography.labelSmall,
                                                                                color = neu.textSecondary,
                                                                            )
                                                                        }
                                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                            Icon(
                                                                                imageVector = if (isCurrent) Icons.Filled.CheckCircle else Icons.Filled.SwapHoriz,
                                                                                contentDescription = null,
                                                                                tint = neu.accent,
                                                                                modifier = Modifier.size(18.dp),
                                                                            )
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(26.dp)
                                                                                    .clip(CircleShape)
                                                                                    .clickable { tokenStore.removeAccount(acc.email) },
                                                                                contentAlignment = Alignment.Center,
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Filled.DeleteOutline,
                                                                                    contentDescription = null,
                                                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                                                    modifier = Modifier.size(16.dp),
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(Modifier.height(10.dp))
                                                        NeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
                                                            Column(Modifier.padding(12.dp)) {
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> if (savedAccounts.isEmpty()) "بازیابی اطلاعات و اکانت‌ها" else "پشتیبان‌گیری و بازیابی"
                                                                        AppLanguage.AR -> if (savedAccounts.isEmpty()) "استعادة البيانات والحسابات" else "النسخ الاحتياطي والاستعادة"
                                                                        AppLanguage.EN -> if (savedAccounts.isEmpty()) "Restore accounts and settings" else "Backup & Restore"
                                                                    },
                                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                    color = neu.textPrimary,
                                                                )
                                                                Spacer(Modifier.height(4.dp))
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "تنظیمات، ظاهر برنامه و اکانت‌های ذخیره‌شده را در یک فایل JSON نگه دارید."
                                                                        AppLanguage.AR -> "احفظ الإعدادات والمظهر والحسابات المحفوظة في ملف JSON."
                                                                        AppLanguage.EN -> "Keep settings, appearance and saved accounts in one JSON backup file."
                                                                    },
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = neu.textSecondary,
                                                                    lineHeight = 18.sp,
                                                                )
                                                                Spacer(Modifier.height(10.dp))
                                                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .clip(RoundedCornerShape(12.dp))
                                                                            .background(neu.accent.copy(alpha = 0.14f))
                                                                            .clickable { createBackupLauncher.launch("foxyvpn-backup.json") }
                                                                            .padding(vertical = 11.dp, horizontal = 8.dp),
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "ذخیره پشتیبان"
                                                                                AppLanguage.AR -> "حفظ نسخة"
                                                                                AppLanguage.EN -> "Create backup"
                                                                            },
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.accent,
                                                                        )
                                                                    }
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .clip(RoundedCornerShape(12.dp))
                                                                            .background(neu.surfaceElevated)
                                                                            .clickable { restoreBackupLauncher.launch(arrayOf("application/json", "text/plain")) }
                                                                            .padding(vertical = 11.dp, horizontal = 8.dp),
                                                                        contentAlignment = Alignment.Center,
                                                                    ) {
                                                                        Text(
                                                                            text = when (s.language) {
                                                                                AppLanguage.FA -> "بازیابی اطلاعات"
                                                                                AppLanguage.AR -> "استعادة البيانات"
                                                                                AppLanguage.EN -> "Restore backup"
                                                                            },
                                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                                            color = neu.textPrimary,
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(Modifier.height(10.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .neumorphicTouchBounce()
                                                                .clip(RoundedCornerShape(14.dp))
                                                                .background(neu.accent.copy(alpha = 0.14f))
                                                                .border(1.dp, neu.accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                                                .clickable(onClick = onSignOut)
                                                                .padding(vertical = 10.dp, horizontal = 12.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = neu.accent, modifier = Modifier.size(18.dp))
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "افزودن یا ثبت‌نام اکانت جدید"
                                                                        AppLanguage.AR -> "إضافة أو تسجيل حساب جديد"
                                                                        AppLanguage.EN -> "Add or Sign Up New Account"
                                                                    },
                                                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = fonts.headingFont, fontWeight = FontWeight.Bold),
                                                                    color = neu.accent,
                                                                )
                                                            }
                                                        }

                                                        Spacer(Modifier.height(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .neumorphicTouchBounce()
                                                                .clip(RoundedCornerShape(14.dp))
                                                                .background(neu.surfaceElevated)
                                                                .border(1.dp, neu.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                                                .clickable { showMozillaWebPortal = true }
                                                                .padding(vertical = 10.dp, horizontal = 12.dp),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Filled.Language, contentDescription = null, tint = neu.accent, modifier = Modifier.size(18.dp))
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(
                                                                    text = when (s.language) {
                                                                        AppLanguage.FA -> "ساخت اکانت در سایت رسمی موزیلا (داخل برنامه)"
                                                                        AppLanguage.AR -> "إنشاء حساب في موقع موزيلا الرسمي (داخل التطبيق)"
                                                                        AppLanguage.EN -> "Create Account on Official Mozilla Site (In-App)"
                                                                    },
                                                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = fonts.headingFont, fontWeight = FontWeight.Bold),
                                                                    color = neu.textPrimary,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                                                        Column(Modifier.padding(16.dp)) {
                                                            SettingClickableRow(
                                                                title = s.viewConnectionLogsTitle,
                                                                value = s.viewConnectionLogsSub,
                                                                onClick = onOpenLogs,
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingClickableRow(
                                                                title = s.versionTitle,
                                                                value = s.language.localizeDigits(BuildConfig.VERSION_NAME),
                                                                onClick = {},
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingSocialLinkRow(
                                                                title = s.instagramTitle,
                                                                value = s.instagramSubtitle,
                                                                isInstagram = true,
                                                                onClick = {
                                                                    runCatching {
                                                                        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL)).apply {
                                                                            setPackage("com.instagram.android")
                                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                        }
                                                                        context.startActivity(appIntent)
                                                                    }.onFailure {
                                                                        runCatching {
                                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_URL)).apply {
                                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                            })
                                                                        }
                                                                    }
                                                                },
                                                            )
                                                            HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 10.dp))
                                                            SettingSocialLinkRow(
                                                                title = s.telegramTitle,
                                                                value = s.telegramSubtitle,
                                                                isInstagram = false,
                                                                onClick = {
                                                                    runCatching {
                                                                        val tgDeepLink = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=aliem_061")).apply {
                                                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                        }
                                                                        context.startActivity(tgDeepLink)
                                                                    }.onFailure {
                                                                        runCatching {
                                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL)).apply {
                                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                            })
                                                                        }
                                                                    }
                                                                },
                                                            )
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(18.dp))
                                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                                            .border(
                                                                width = 1.dp,
                                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f),
                                                                shape = RoundedCornerShape(18.dp),
                                                            )
                                                            .clickable(onClick = onSignOut)
                                                            .padding(vertical = 14.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                Icons.AutoMirrored.Filled.Logout,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(20.dp),
                                                            )
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                s.signOutButton,
                                                                style = MaterialTheme.typography.titleMedium.copy(
                                                                    fontFamily = fonts.headingFont,
                                                                    fontWeight = FontWeight.Bold,
                                                                ),
                                                                color = MaterialTheme.colorScheme.error,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Bottom Non-Scrolling Page Stepper Bar
                                if (subPageTitles.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (subPage > 0) neu.surfaceElevated else neu.surfaceElevated.copy(alpha = 0.35f),
                                                )
                                                .clickable(enabled = subPage > 0) {
                                                    if (subPage > 0) selectedSubPage = subPage - 1
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                        ) {
                                            Text(
                                                text = when (s.language) {
                                                    AppLanguage.FA -> "صفحه قبلی"
                                                    AppLanguage.AR -> "الصفحة السابقة"
                                                    AppLanguage.EN -> "Previous Page"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (subPage > 0) neu.textPrimary else neu.textSecondary.copy(alpha = 0.4f),
                                            )
                                        }

                                        Text(
                                            text = "${s.language.localizeNumber(subPage + 1)} / ${s.language.localizeNumber(subPageTitles.size)}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = fonts.monoFont,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = neu.accent,
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (subPage < subPageTitles.lastIndex) neu.accent.copy(alpha = 0.16f)
                                                    else neu.surfaceElevated.copy(alpha = 0.35f),
                                                )
                                                .clickable(enabled = subPage < subPageTitles.lastIndex) {
                                                    if (subPage < subPageTitles.lastIndex) selectedSubPage = subPage + 1
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                        ) {
                                            Text(
                                                text = when (s.language) {
                                                    AppLanguage.FA -> "صفحه بعدی"
                                                    AppLanguage.AR -> "الصفحة التالية"
                                                    AppLanguage.EN -> "Next Page"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (subPage < subPageTitles.lastIndex) neu.accent else neu.textSecondary.copy(alpha = 0.4f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IslandTuningSlider(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    neu: NeumorphicColors,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = neu.textPrimary,
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = neu.accent,
        )
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        colors = SliderDefaults.colors(
            thumbColor = neu.accent,
            activeTrackColor = neu.accent,
            inactiveTrackColor = neu.textSecondary.copy(alpha = 0.18f),
        ),
    )
}

@Composable
private fun SettingsDropdownRow(
    title: String,
    selected: String,
    options: List<String>,
    icon: ImageVector = Icons.Filled.Tune,
    onSelected: (String) -> Unit,
) {
    val neu = currentNeumorphicColors
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(neu.surfaceElevated.copy(alpha = 0.72f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(icon, contentDescription = null, tint = neu.accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = neu.textSecondary)
                Spacer(Modifier.height(3.dp))
                Text(selected, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = neu.textPrimary, maxLines = 2)
            }
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = neu.accent, modifier = Modifier.size(20.dp))
        }
        if (expanded) {
            AlertDialog(
                onDismissRequest = { expanded = false },
                title = { Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (option == selected) neu.accent.copy(alpha = 0.16f) else neu.surfaceElevated)
                                    .clickable {
                                        expanded = false
                                        onSelected(option)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = option,
                                    modifier = Modifier.weight(1f),
                                    color = if (option == selected) neu.accent else neu.textPrimary,
                                    fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (option == selected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = neu.accent, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { expanded = false }) { Text(currentFoxyStrings.cancel) }
                },
            )
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    val neu = currentNeumorphicColors
    val fonts = currentFoxyFonts
    val s = currentFoxyStrings
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = fonts.headingFont,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (s.isRtl) 0.sp else 1.1.sp,
        ),
        color = neu.textSecondary,
        modifier = Modifier.padding(start = 6.dp, bottom = 10.dp, top = 4.dp),
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val neu = currentNeumorphicColors
    val fonts = currentFoxyFonts
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = fonts.headingFont,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = fonts.bodyFont),
                color = neu.textSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = neu.accent,
                uncheckedThumbColor = neu.textSecondary,
                uncheckedTrackColor = neu.surfaceElevated,
            ),
        )
    }
}

@Composable
private fun SettingClickableRow(
    title: String,
    value: String,
    leadingIconRes: Int? = null,
    onClick: () -> Unit,
) {
    val neu = currentNeumorphicColors
    val fonts = currentFoxyFonts
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (leadingIconRes != null) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(neu.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(leadingIconRes),
                    contentDescription = title,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = fonts.headingFont,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = neu.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = fonts.bodyFont),
                color = neu.textSecondary,
                lineHeight = 18.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = neu.textSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SettingSocialLinkRow(
    title: String,
    value: String,
    isInstagram: Boolean,
    onClick: () -> Unit,
) {
    val neu = currentNeumorphicColors
    val fonts = currentFoxyFonts
    val brandColor = if (isInstagram) Color(0xFFE1306C) else Color(0xFF229ED9)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandColor.copy(alpha = 0.14f))
                    .border(1.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(
                        id = if (isInstagram) R.drawable.foxy_instagram_official else R.drawable.foxy_telegram_official,
                    ),
                    contentDescription = if (isInstagram) "Instagram" else "Telegram",
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = fonts.headingFont,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = neu.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = fonts.bodyFont),
                    color = brandColor,
                    lineHeight = 18.sp,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = neu.textSecondary,
            modifier = Modifier.size(22.dp),
        )
    }
}

// Dialogs
@Composable
private fun DynamicIslandSizeDialog(
    current: SettingsStore.DynamicIslandSize,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandSize) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandSizeTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandSize.entries.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == size, onClick = { selected = size })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == size, onClick = { selected = size })
                        Spacer(Modifier.width(10.dp))
                        Text(size.localizedLabel(s.language), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandCornerRadiusDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    val presets = when (s.language) {
        AppLanguage.FA -> listOf(
            18 to "نرم و طبیعی (۱۸)",
            22 to "متعادل (۲۲)",
            26 to "کپسول استاندارد (۲۶)",
            30 to "کپسول عمیق (۳۰)",
            36 to "کاملاً گرد (۳۶)",
        )
        AppLanguage.AR -> listOf(
            18 to "ناعم وطبيعي (١٨)",
            22 to "متوازن (٢٢)",
            26 to "كبسولة قياسية (٢٦)",
            30 to "كبسولة عميقة (٣٠)",
            36 to "دائري بالكامل (٣٦)",
        )
        AppLanguage.EN -> listOf(
            18 to "Soft Native (18)",
            22 to "Balanced Pill (22)",
            26 to "Standard Capsule (26)",
            30 to "Deep Capsule (30)",
            36 to "Full Stadium (36)",
        )
    }
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandCornerRadiusTitle) },
        text = {
            Column {
                presets.forEach { (radius, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == radius, onClick = { selected = radius })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == radius, onClick = { selected = radius })
                        Spacer(Modifier.width(10.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandPositionDialog(
    current: SettingsStore.DynamicIslandPosition,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandPosition) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandPositionTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandPosition.entries.forEach { pos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == pos, onClick = { selected = pos })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == pos, onClick = { selected = pos })
                        Spacer(Modifier.width(10.dp))
                        Text(pos.localizedLabel(s.language), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DynamicIslandThemeDialog(
    current: SettingsStore.DynamicIslandTheme,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DynamicIslandTheme) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.islandThemeTitle) },
        text = {
            Column {
                SettingsStore.DynamicIslandTheme.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == theme, onClick = { selected = theme })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected == theme, onClick = { selected = theme })
                        Spacer(Modifier.width(10.dp))
                        Text(theme.localizedLabel(s.language), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun DnsProviderPickerDialog(
    currentProvider: SettingsStore.DnsProvider,
    currentCustomUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.DnsProvider, String) -> Unit,
) {
    val s = currentFoxyStrings
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var urlText by remember { mutableStateOf(currentCustomUrl) }
    val isCustom = selectedProvider == SettingsStore.DnsProvider.CUSTOM
    val isValidCustomUrl = SettingsStore.isValidDohUrl(urlText)
    val canSave = !isCustom || isValidCustomUrl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tunnelDnsTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsStore.DnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.localizedLabel(s.language))
                            val detail = provider.dohUrl ?: ""
                            if (detail.isNotEmpty()) {
                                Text(
                                    s.language.localizeDigits(detail),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (isCustom) {
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it.trim() },
                        label = { Text(s.dialogDohUrlLabel) },
                        singleLine = true,
                        isError = urlText.isNotBlank() && !isValidCustomUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedProvider, urlText) },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun BootstrapDnsPickerDialog(
    currentProvider: SettingsStore.BootstrapDnsProvider,
    currentCustomIp: String,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.BootstrapDnsProvider, String) -> Unit,
) {
    val s = currentFoxyStrings
    var selectedProvider by remember { mutableStateOf(currentProvider) }
    var ipText by remember { mutableStateOf(currentCustomIp) }
    val isCustom = selectedProvider == SettingsStore.BootstrapDnsProvider.CUSTOM
    val isValidCustomIp = SettingsStore.isValidIpAddress(ipText.trim())
    val canSave = !isCustom || isValidCustomIp

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.resolverDnsTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsStore.BootstrapDnsProvider.entries.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(provider.localizedLabel(s.language))
                            Text(
                                provider.localizedDescription(s.language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (isCustom) {
                    OutlinedTextField(
                        value = ipText,
                        onValueChange = { ipText = it.trim() },
                        label = { Text(s.dialogIpAddressLabel) },
                        singleLine = true,
                        isError = ipText.isNotBlank() && !isValidCustomIp,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedProvider, ipText) },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun SocksBindAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.localBindAddressTitle) },
        text = {
            Column {
                SettingsStore.socksBindPresetsFor(s.language).forEach { (address, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == address,
                                onClick = { selected = address },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(
                            selected = selected == address,
                            onClick = { selected = address },
                        )
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun SocksPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.socksPortTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text(s.dialogPortRangeLabel) },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_SOCKS_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_SOCKS_PORT.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun HttpPortPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current.toString()) }
    val port = text.toIntOrNull()
    val isValid = port != null && port in 1..65_535

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.httpProxyPortTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(5) },
                    label = { Text(s.dialogPortRangeLabel) },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { text = SettingsStore.DEFAULT_HTTP_PROXY_PORT.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_HTTP_PROXY_PORT.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { port?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun EdgeAddressPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val s = currentFoxyStrings
    var text by remember { mutableStateOf(current) }
    val trimmed = text.trim()
    val isValid = trimmed.isEmpty() || SettingsStore.isValidEdgeHost(trimmed)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.customEdgeAddressTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.trim() },
                    label = { Text(s.dialogHostOrIpLabel) },
                    singleLine = true,
                    isError = text.isNotBlank() && !isValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trimmed) }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyTypeDialog(
    current: SettingsStore.UpstreamProxyType,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.UpstreamProxyType) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyTypeTitle) },
        text = {
            Column {
                SettingsStore.UpstreamProxyType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == type,
                                onClick = { selected = type },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(selected = selected == type, onClick = { selected = type })
                        Text(type.localizedLabel(s.language), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyAddressDialog(
    currentHost: String,
    currentPort: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
) {
    val s = currentFoxyStrings
    var hostText by remember { mutableStateOf(currentHost) }
    var portText by remember { mutableStateOf(currentPort.toString()) }
    val port = portText.toIntOrNull()
    val isValidHost = hostText.isBlank() || SettingsStore.isValidEdgeHost(hostText)
    val isValidPort = port != null && port in 1..65_535
    val canSave = isValidHost && isValidPort

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyAddressTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = hostText,
                    onValueChange = { hostText = it.trim() },
                    label = { Text(s.dialogHostOrIpLabel) },
                    singleLine = true,
                    isError = hostText.isNotBlank() && !isValidHost,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter(Char::isDigit).take(5) },
                    label = { Text(s.dialogPortRangeLabel) },
                    singleLine = true,
                    isError = portText.isNotBlank() && !isValidPort,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { port?.let { onConfirm(hostText, it) } },
                enabled = canSave,
            ) {
                Text(s.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun UpstreamProxyCredentialsDialog(
    currentUsername: String,
    currentPassword: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    val s = currentFoxyStrings
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf(currentPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.upstreamProxyCredentialsTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(s.dialogUsernameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(s.loginPasswordLabel) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(username, password) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun TlsSplitModePickerDialog(
    current: SettingsStore.TlsSplitMode,
    onDismiss: () -> Unit,
    onConfirm: (SettingsStore.TlsSplitMode) -> Unit,
) {
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tlsFragmentationTitle) },
        text = {
            Column {
                SettingsStore.TlsSplitMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == mode,
                                onClick = { selected = mode },
                            )
                            .padding(vertical = 6.dp),
                    ) {
                        RadioButton(selected = selected == mode, onClick = { selected = mode })
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(mode.localizedLabel(s.language))
                            Text(
                                mode.localizedDescription(s.language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

private data class AppItem(
    val packageName: String,
    val label: String,
)

@Composable
private fun SplitTunnelDialog(
    current: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
) {
    val context = LocalContext.current
    val s = currentFoxyStrings
    var selected by remember { mutableStateOf(current) }
    var searchQuery by remember { mutableStateOf("") }

    val installedApps by produceState<List<AppItem>>(initialValue = emptyList()) {
        value = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            packages
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.packageName == "com.android.chrome" }
                .map { AppItem(it.packageName, pm.getApplicationLabel(it).toString()) }
                .sortedBy { it.label.lowercase() }
        }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.excludedAppsTitle) },
        text = {
            Column(Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(s.dialogSearchAppsPlaceholder) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
                if (installedApps.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isChecked = selected.contains(app.packageName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = if (isChecked) selected - app.packageName
                                        else selected + app.packageName
                                    }
                                    .padding(vertical = 4.dp),
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { check ->
                                        selected = if (check) selected + app.packageName
                                        else selected - app.packageName
                                    },
                                )
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(app.label, maxLines = 1)
                                    Text(
                                        app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("${s.save} (${s.language.localizeNumber(selected.size)})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}

@Composable
private fun MtuPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val s = currentFoxyStrings
    var mtuText by remember { mutableStateOf(current.toString()) }
    val parsedMtu = mtuText.toIntOrNull()
    val isValid = parsedMtu != null && parsedMtu in SettingsStore.MIN_MTU..SettingsStore.MAX_MTU

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.tunnelMtuTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = mtuText,
                    onValueChange = { mtuText = it.filter(Char::isDigit).take(4) },
                    label = { Text(s.language.localizeDigits("${SettingsStore.MIN_MTU}\u2013${SettingsStore.MAX_MTU}")) },
                    singleLine = true,
                    isError = mtuText.isNotBlank() && !isValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { mtuText = SettingsStore.DEFAULT_MTU.toString() },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(s.resetToDefault(SettingsStore.DEFAULT_MTU.toString()))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedMtu?.let { onConfirm(it) } }, enabled = isValid) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}
