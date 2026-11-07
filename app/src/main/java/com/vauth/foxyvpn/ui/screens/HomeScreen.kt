package com.vauth.foxyvpn.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.R
import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.GUARDIAN_ENDPOINT_DEFAULT
import com.vauth.foxyvpn.data.RECOMMENDED_COUNTRY_CODE
import com.vauth.foxyvpn.data.CustomAppPalette
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.ProxyCandidate
import com.vauth.foxyvpn.ui.components.DynamicIsland
import com.vauth.foxyvpn.ui.components.CountryFlagImage
import com.vauth.foxyvpn.ui.components.CustomAppPaletteEditorDialog
import com.vauth.foxyvpn.ui.theme.AnimatedOdometerText
import com.vauth.foxyvpn.ui.theme.AnimatedWorldMapBackground
import com.vauth.foxyvpn.ui.theme.FoxyLanguage
import com.vauth.foxyvpn.ui.theme.LanguageController
import com.vauth.foxyvpn.ui.theme.LiveTrafficSparkline
import com.vauth.foxyvpn.ui.theme.LocalFoxyStatusColors
import com.vauth.foxyvpn.ui.theme.NeumorphicCard
import com.vauth.foxyvpn.ui.theme.NeumorphicIconButton
import com.vauth.foxyvpn.ui.theme.NeumorphicRadarPulse
import com.vauth.foxyvpn.ui.theme.NeumorphicSlideToConnect
import com.vauth.foxyvpn.ui.theme.StyleAtmospherePosterOverlay
import com.vauth.foxyvpn.ui.theme.ThemeController
import com.vauth.foxyvpn.ui.theme.ThemeMode
import com.vauth.foxyvpn.ui.theme.currentFoxyFonts
import com.vauth.foxyvpn.ui.theme.currentFoxyStrings
import com.vauth.foxyvpn.ui.theme.currentNeumorphicColors
import com.vauth.foxyvpn.ui.theme.formatBytesLocalized
import com.vauth.foxyvpn.ui.theme.formatClockTimeLocalized
import com.vauth.foxyvpn.ui.theme.formatDurationLocalized
import com.vauth.foxyvpn.ui.theme.formatMinutesLocalized
import com.vauth.foxyvpn.ui.theme.formatPingLocalized
import com.vauth.foxyvpn.ui.theme.formatSpeedLocalized
import com.vauth.foxyvpn.ui.theme.localizeCountryName
import com.vauth.foxyvpn.ui.theme.localizeDigits
import com.vauth.foxyvpn.ui.theme.neumorphicSunken
import com.vauth.foxyvpn.ui.theme.neumorphicTouchBounce
import com.vauth.foxyvpn.ui.theme.shakeOnTrigger
import com.vauth.foxyvpn.ui.theme.staggeredEntrance
import com.vauth.foxyvpn.ui.theme.subtleFloatAnimation
import com.vauth.foxyvpn.vpn.ExitCheck
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.vpn.IpLocationInfo
import com.vauth.foxyvpn.vpn.PingUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    app: FoxyVpnApp,
    themeController: ThemeController,
    languageController: LanguageController,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenServers: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTools: (Int) -> Unit = {},
    onOpenLogin: () -> Unit = {},
) {
    val state by FoxyVpnService.state.collectAsState()
    val lastError by FoxyVpnService.lastError.collectAsState()
    val liveStats by FoxyVpnService.vpnLiveStats.collectAsState()
    val connectedAtEpochMs by FoxyVpnService.connectedAtEpochMs.collectAsState()
    val pingLatency by FoxyVpnService.pingLatencyMs.collectAsState()
    val vpnEdgeIp by FoxyVpnService.vpnEdgeIp.collectAsState()

    val selectedProxy by app.proxyStateStore.selectedProxyFlow.collectAsState()

    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val fonts = currentFoxyFonts
    val systemInDarkTheme = isSystemInDarkTheme()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAccountSwitcherDialog by remember { mutableStateOf(false) }
    val accountsRevision by app.tokenStore.accountsRevision.collectAsState()
    var showHomePaletteEditor by remember { mutableStateOf(false) }
    if (showHomePaletteEditor) {
        val appLanguage = when (s.language) {
            FoxyLanguage.FA -> com.vauth.foxyvpn.ui.theme.AppLanguage.FA
            FoxyLanguage.AR -> com.vauth.foxyvpn.ui.theme.AppLanguage.AR
            FoxyLanguage.EN -> com.vauth.foxyvpn.ui.theme.AppLanguage.EN
        }
        CustomAppPaletteEditorDialog(
            initialPalette = themeController.customPalette ?: CustomAppPalette.from(themeController.colorTheme),
            language = appLanguage,
            onDismiss = { showHomePaletteEditor = false },
            onApply = { palette ->
                themeController.applyCustomPalette(palette)
                showHomePaletteEditor = false
            },
        )
    }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var realIp by remember {
        mutableStateOf<IpLocationInfo?>(
            app.settingsStore.cachedRealIp?.let {
                IpLocationInfo(it, app.settingsStore.cachedRealCountry.orEmpty())
            },
        )
    }
    var isPinging by remember { mutableStateOf(false) }
    var disconnectedPingLatency by remember { mutableStateOf<Int?>(null) }
    var quotaRemaining by remember { mutableStateOf(app.settingsStore.cachedQuotaRemaining) }
    var quotaMax by remember { mutableStateOf(app.settingsStore.cachedQuotaMax) }
    var isUnlimited by remember { mutableStateOf(app.settingsStore.cachedIsUnlimited) }

    // Rolling 24-sample history for live traffic waveform
    val rxHistory = remember { mutableStateListOf<Long>().apply { repeat(24) { add(0L) } } }
    val txHistory = remember { mutableStateListOf<Long>().apply { repeat(24) { add(0L) } } }

    LaunchedEffect(liveStats, state) {
        if (state == ConnectionState.CONNECTED) {
            if (rxHistory.size >= 24) rxHistory.removeAt(0)
            rxHistory.add(liveStats.rxRateBytesPerSec)
            if (txHistory.size >= 24) txHistory.removeAt(0)
            txHistory.add(liveStats.txRateBytesPerSec)
        } else {
            for (i in 0 until rxHistory.size) rxHistory[i] = 0L
            for (i in 0 until txHistory.size) txHistory[i] = 0L
        }
    }

    LaunchedEffect(state) {
        if (state == ConnectionState.CONNECTED) {
            quotaRemaining = app.settingsStore.cachedQuotaRemaining
            quotaMax = app.settingsStore.cachedQuotaMax
            isUnlimited = app.settingsStore.cachedIsUnlimited
        }
    }

    LaunchedEffect(state, connectedAtEpochMs) {
        val connectedSince = connectedAtEpochMs
        if (state == ConnectionState.CONNECTED && connectedSince != null) {
            while (isActive) {
                val now = System.currentTimeMillis()
                elapsedSeconds = ((now - connectedSince) / 1000L).coerceAtLeast(0L)
                delay(1000L)
            }
        } else {
            elapsedSeconds = 0L
        }
    }

    LaunchedEffect(state) {
        if (state == ConnectionState.CONNECTED) {
            while (isActive) {
                FoxyVpnService.pingNow()
                delay(15_000L)
            }
        }
    }

    LaunchedEffect(selectedProxy, state) {
        if (state != ConnectionState.CONNECTED) {
            val target = selectedProxy ?: app.proxyStateStore.load() ?: run {
                val cached = app.serverListClient.loadCachedCountries()
                ServerListClient.candidatesForCountry(cached, RECOMMENDED_COUNTRY_CODE).firstOrNull()
                    ?: cached.firstOrNull { it.code.isNotBlank() }?.let { country ->
                        ServerListClient.candidatesForCountry(cached, country.code).firstOrNull()
                    }
            }
            if (target != null) {
                val measured = PingUtil.measureTcpLatencyMs(target.host, target.port)
                if (measured != null) {
                    disconnectedPingLatency = measured
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            ExitCheck().fetchDirectIpInfo().onSuccess { info ->
                realIp = info
                app.settingsStore.cachedRealIp = info.ip
                app.settingsStore.cachedRealCountry = info.countryCode
            }
        }
        launch {
            val token = app.authRepository.currentAccessToken()
            if (token != null) {
                runCatching { app.guardianClient.fetchUserInfo(GUARDIAN_ENDPOINT_DEFAULT, token) }
                    .onSuccess { entitlement ->
                        val unlimited = !entitlement.limitedBandwidth
                        isUnlimited = unlimited
                        app.settingsStore.cachedIsUnlimited = unlimited
                        entitlement.quotaRemaining?.let {
                            quotaRemaining = it
                            app.settingsStore.cachedQuotaRemaining = it
                        }
                        entitlement.maxBytes?.let {
                            quotaMax = it
                            app.settingsStore.cachedQuotaMax = it
                        }
                        app.tokenStore.updateActiveAccountQuota(
                            remaining = entitlement.quotaRemaining,
                            max = entitlement.maxBytes,
                            unlimited = unlimited,
                        )
                    }
            }
        }
    }

    val heroStatusColor by animateColorAsState(
        targetValue = when (state) {
            ConnectionState.CONNECTED -> statusColors.connected
            ConnectionState.CONNECTING -> statusColors.connecting
            ConnectionState.DISCONNECTED -> neu.accent
        },
        animationSpec = tween(420),
        label = "hero-accent-color",
    )

    Scaffold(
        containerColor = neu.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val scrollState = rememberScrollState()

            // 1. 3D Interactive World Globe & Map Background (tilts & rotates with scroll)
            AnimatedWorldMapBackground(
                isConnected = state == ConnectionState.CONNECTED,
                isConnecting = state == ConnectionState.CONNECTING,
                accentColor = heroStatusColor,
                isDark = neu.isDark,
                scrollOffsetPx = scrollState.value.toFloat(),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Pixel UI keeps the hero atmospheric but quiet, preserving readable tonal surfaces.
                        alpha = when (neu.designStyle) {
                            com.vauth.foxyvpn.data.FoxyUiDesignStyle.PIXEL_GOOGLE -> 0.10f
                            com.vauth.foxyvpn.data.FoxyUiDesignStyle.SAFETY_SURF -> 0.06f
                            else -> 1f
                        }
                    },
            )

            // 1B. Design-aware atmosphere: quiet tonal shapes for Pixel UI, clean surfaces for the current design.
            StyleAtmospherePosterOverlay(
                modifier = Modifier.fillMaxSize(),
            )

            var isMagazineOpen by remember { mutableStateOf(false) }
            val magazineRotation by animateFloatAsState(
                targetValue = if (isMagazineOpen) 180f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "magazine-trigger-rotation",
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 2. TOP BRAND HEADER (Moved 20% higher to the standard top position above Dynamic Island, with larger Amsterdam logo & Collapsible Magazine Action Button)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 4.dp),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // Top-Left Brand Lockup: First App Icon (44.dp), then Enlarged "Foxy vpn" Amsterdam Signature (50.dp x 168.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                NeumorphicIconButton(
                                    size = 44.dp,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    modifier = Modifier.subtleFloatAnimation(amplitudePx = 3f, durationMs = 2600),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.foxy_official_icon),
                                        contentDescription = "Foxy vpn",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(1.2.dp, neu.accent.copy(alpha = 0.48f), CircleShape),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.foxy_brand_amsterdam),
                                    contentDescription = "Foxy vpn",
                                    colorFilter = ColorFilter.tint(neu.textPrimary),
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(168.dp),
                                )
                            }

                            // Top-Right Controls: Day/Night Theme Toggle + Settings Button right next to it + Collapsible Magazine Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // 1. Day/Night Theme Toggle Button (Telegram-Style Appearance Icon)
                                NeumorphicIconButton(
                                    size = 42.dp,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        themeController.toggle(systemInDarkTheme)
                                    },
                                    onLongClick = { showHomePaletteEditor = true },
                                    onLongClickLabel = when (s.language) {
                                        FoxyLanguage.FA -> "ساخت پالت رنگ دلخواه"
                                        FoxyLanguage.AR -> "إنشاء لوحة ألوان مخصصة"
                                        FoxyLanguage.EN -> "Build a custom color palette"
                                    },
                                    modifier = Modifier.neumorphicTouchBounce(),
                                ) {
                                    val darkAppearance = themeController.resolveDark(systemInDarkTheme)
                                    Icon(
                                        painter = painterResource(id = if (darkAppearance) R.drawable.ic_theme_night else R.drawable.ic_theme_day),
                                        contentDescription = when (s.language) {
                                            FoxyLanguage.FA -> if (darkAppearance) "حالت شب" else "حالت روز"
                                            FoxyLanguage.AR -> if (darkAppearance) "الوضع الداكن" else "الوضع الفاتح"
                                            FoxyLanguage.EN -> if (darkAppearance) "Dark theme" else "Light theme"
                                        },
                                        tint = if (darkAppearance) Color(0xFFC8D3FF) else Color(0xFFFFB74D),
                                        modifier = Modifier.size(23.dp),
                                    )
                                }

                                // 2. Settings Icon Button right next to Day/Night Theme Button
                                NeumorphicIconButton(
                                    size = 42.dp,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isMagazineOpen = false
                                        onOpenSettings()
                                    },
                                    modifier = Modifier.neumorphicTouchBounce(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = s.headerSettingsButton,
                                        tint = neu.textPrimary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                // 3. Main Collapsible Magazine Action Button ("دکمه خشابی")
                                NeumorphicIconButton(
                                    size = 42.dp,
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isMagazineOpen = !isMagazineOpen
                                    },
                                    modifier = Modifier.neumorphicTouchBounce(),
                                ) {
                                    Icon(
                                        imageVector = if (isMagazineOpen) Icons.Filled.Close else Icons.Filled.Tune,
                                        contentDescription = s.headerSettingsButton,
                                        tint = if (isMagazineOpen) neu.accent else neu.textPrimary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .graphicsLayer { rotationZ = magazineRotation },
                                    )
                                }
                            }
                        }
                    }

                    // Collapsible Magazine Action Bar ("خشاب ابزار با انیمیشن نرم")
                    AnimatedVisibility(
                        visible = isMagazineOpen,
                        enter = expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = 360f,
                            ),
                        ) + fadeIn(tween(180)) + scaleIn(
                            initialScale = 0.92f,
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = 360f,
                            ),
                        ),
                        exit = shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = 0.86f,
                                stiffness = 420f,
                            ),
                        ) + fadeOut(tween(130)) + scaleOut(targetScale = 0.92f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                        ) {
                            NeumorphicCard(
                                cornerRadius = 20.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                ) {
                                    // Compact Single-Row Magazine Action Buttons Capsule ("خشاب دکمه‌های ابزار")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Control 1: Connection Wave Animation Switcher
                                        NeumorphicIconButton(
                                            size = 42.dp,
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                themeController.cycleHeroWaveStyle()
                                            },
                                            modifier = Modifier.neumorphicTouchBounce(),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Waves,
                                                contentDescription = s.heroWaveStyleTitle,
                                                tint = neu.accent,
                                                modifier = Modifier.size(19.dp),
                                            )
                                        }

                                        // Control 1B: Color Palette Switcher
                                        NeumorphicIconButton(
                                            size = 42.dp,
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                themeController.cycleColorTheme()
                                            },
                                            modifier = Modifier.neumorphicTouchBounce(),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Palette,
                                                contentDescription = s.colorPaletteTitle,
                                                tint = neu.accent,
                                                modifier = Modifier.size(19.dp),
                                            )
                                        }

                                        // Control 2: Speed Test
                                        NeumorphicIconButton(
                                            size = 42.dp,
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                isMagazineOpen = false
                                                onOpenTools(0)
                                            },
                                            modifier = Modifier.neumorphicTouchBounce(),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Speed,
                                                contentDescription = s.headerSpeedButton,
                                                tint = neu.accent,
                                                modifier = Modifier.size(19.dp),
                                            )
                                        }

                                        // Control 3: Animated Language Switcher (EN -> فا -> عر)
                                        NeumorphicIconButton(
                                            size = 42.dp,
                                            onClick = {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                languageController.toggle()
                                            },
                                            modifier = Modifier.neumorphicTouchBounce(),
                                        ) {
                                            AnimatedContent(
                                                targetState = languageController.language,
                                                transitionSpec = {
                                                    (slideInVertically { h -> h / 2 } + scaleIn(initialScale = 0.75f) + fadeIn(tween(200)))
                                                        .togetherWith(slideOutVertically { h -> -h / 2 } + scaleOut(targetScale = 0.75f) + fadeOut(tween(150)))
                                                },
                                                label = "lang-switch-micro",
                                            ) { lang ->
                                                Text(
                                                    text = lang.badge,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = fonts.headingFont,
                                                        fontWeight = FontWeight.ExtraBold,
                                                    ),
                                                    color = neu.accent,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Dynamic Island right below Top Brand Header
                DynamicIsland(
                    settingsStore = app.settingsStore,
                    selectedProxy = selectedProxy,
                    onDisconnect = onDisconnect,
                    onRequestConnect = onRequestConnect,
                    onOpenServers = onOpenServers,
                    isSystemOverlay = false,
                )

                Spacer(Modifier.height(8.dp))

                // Account Status & Quota Mini-Badge with Interactive Account Switcher ("تعویض و مدیریت اکانت‌ها")
                val accountEmail = remember(accountsRevision) { app.tokenStore.loadEmail() }
                if (!accountEmail.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(neu.surfaceElevated.copy(alpha = 0.75f))
                            .border(1.dp, neu.accent.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showAccountSwitcherDialog = true
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(statusColors.connected),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = accountEmail,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = neu.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = "Switch Account",
                                    tint = neu.accent,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            val quotaBadgeText = when {
                                isUnlimited -> when (s.language) {
                                    FoxyLanguage.FA -> "نامحدود"
                                    FoxyLanguage.AR -> "غير محدود"
                                    FoxyLanguage.EN -> "Unlimited"
                                }
                                quotaRemaining != null -> {
                                    val currentRem = if (state == ConnectionState.CONNECTED) {
                                        ((quotaRemaining ?: 0L) - liveStats.sessionBytes).coerceAtLeast(0L)
                                    } else {
                                        quotaRemaining ?: 0L
                                    }
                                    s.language.formatBytesLocalized(currentRem)
                                }
                                else -> when (s.language) {
                                    FoxyLanguage.FA -> "حساب فعال"
                                    FoxyLanguage.AR -> "حساب نشط"
                                    FoxyLanguage.EN -> "Active Account"
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(neu.accent.copy(alpha = 0.14f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = quotaBadgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = fonts.monoFont,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = neu.accent,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (showAccountSwitcherDialog) {
                    val savedAccounts = remember(accountsRevision) { app.tokenStore.loadAccounts() }
                    val currentActive = remember(accountsRevision) { app.tokenStore.activeAccountEmail }
                    AlertDialog(
                        onDismissRequest = { showAccountSwitcherDialog = false },
                        containerColor = neu.surfaceElevated,
                        shape = RoundedCornerShape(22.dp),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    tint = neu.accent,
                                    modifier = Modifier.size(26.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = when (s.language) {
                                        FoxyLanguage.FA -> "مدیریت و تعویض حساب"
                                        FoxyLanguage.AR -> "إدارة وتبديل الحساب"
                                        FoxyLanguage.EN -> "Account Switcher"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = neu.textPrimary,
                                )
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = when (s.language) {
                                        FoxyLanguage.FA -> "برای تغییر سهمیه و مشخصات تونل، روی حساب مورد نظر کلیک کنید:"
                                        FoxyLanguage.AR -> "اختر الحساب المطلوب لاستخدام الحصة والجلسة:"
                                        FoxyLanguage.EN -> "Tap an account below to switch your active session and quota:"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = neu.textSecondary,
                                )
                                Spacer(Modifier.height(4.dp))
                                if (savedAccounts.isEmpty()) {
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "هیچ حساب دیگری ذخیره نشده است."
                                            FoxyLanguage.AR -> "لا توجد حسابات أخرى محفوظة."
                                            FoxyLanguage.EN -> "No other saved accounts found."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = neu.textSecondary,
                                    )
                                } else {
                                    savedAccounts.forEach { acc ->
                                        val isCurrent = acc.email.equals(currentActive, ignoreCase = true)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isCurrent) neu.accent.copy(alpha = 0.15f) else neu.background)
                                                .border(
                                                    1.dp,
                                                    if (isCurrent) neu.accent else neu.textSecondary.copy(alpha = 0.15f),
                                                    RoundedCornerShape(12.dp),
                                                )
                                                .clickable {
                                                    if (!isCurrent) {
                                                        app.tokenStore.switchToAccount(acc.email)
                                                        Toast.makeText(
                                                            context,
                                                            when (s.language) {
                                                                FoxyLanguage.FA -> "حساب فعال به ${acc.email} تغییر یافت"
                                                                FoxyLanguage.AR -> "تم تبديل الحساب إلى ${acc.email}"
                                                                FoxyLanguage.EN -> "Switched to ${acc.email}"
                                                            },
                                                            Toast.LENGTH_SHORT,
                                                        ).show()
                                                        if (state == ConnectionState.CONNECTED) {
                                                            FoxyVpnService.reconnect(context)
                                                        }
                                                    }
                                                    showAccountSwitcherDialog = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = acc.email,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = fonts.headingFont,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    ),
                                                    color = if (isCurrent) neu.accent else neu.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                val quotaDesc = when {
                                                    acc.isUnlimited -> "سهمیه نامحدود"
                                                    acc.quotaRemaining != null -> "${s.quotaLimitPrefix} ${s.language.formatBytesLocalized(acc.quotaRemaining)}"
                                                    else -> "حساب رسمی موزیلا"
                                                }
                                                Text(
                                                    text = quotaDesc,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.monoFont),
                                                    color = neu.textSecondary,
                                                )
                                            }
                                            if (isCurrent) {
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = "Active",
                                                    tint = neu.accent,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showAccountSwitcherDialog = false
                                    onOpenLogin()
                                },
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = when (s.language) {
                                            FoxyLanguage.FA -> "ورود با حساب جدید"
                                            FoxyLanguage.AR -> "تسجيل بحساب جديد"
                                            FoxyLanguage.EN -> "Add New Account"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = neu.accent,
                                    )
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAccountSwitcherDialog = false }) {
                                Text(s.cancel, color = neu.textSecondary)
                            }
                        },
                    )
                }

                // 4. CONNECTION STATUS & TIMER HERO:
                // Enlarged 320.dp Screen-Filling Harmonic Wave Aura + Full-Width Single-Line Status Badge
                val statusText = when (state) {
                    ConnectionState.CONNECTED -> s.statusConnected
                    ConnectionState.CONNECTING -> s.statusConnecting
                    ConnectionState.DISCONNECTED -> s.statusDisconnected
                }
                val statusColor by animateColorAsState(
                    targetValue = when (state) {
                        ConnectionState.CONNECTED -> statusColors.connected
                        ConnectionState.CONNECTING -> statusColors.connecting
                        ConnectionState.DISCONNECTED -> neu.textSecondary
                    },
                    animationSpec = tween(400),
                    label = "hero-status-color",
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0),
                ) {
                    // Enlarged 320.dp harmonic wave rings behind the timer & status badge
                    val waveConfigRev by app.settingsStore.islandConfigRevision.collectAsState()
                    @Suppress("UNUSED_EXPRESSION")
                    waveConfigRev
                    NeumorphicRadarPulse(
                        color = if (state == ConnectionState.DISCONNECTED) neu.accent else statusColor,
                        isActive = state == ConnectionState.CONNECTED,
                        isConnecting = state == ConnectionState.CONNECTING,
                        size = 320.dp,
                        waveStyle = themeController.heroWaveStyle,
                        speedMultiplier = app.settingsStore.waveSpeedMultiplier,
                        amplitudeScale = app.settingsStore.waveAmplitudeScale,
                        glowEnabled = app.settingsStore.waveGlowEnabled,
                        colorMode = app.settingsStore.waveColorMode,
                    )

                    // Overlay Column takes full width so SECURE & CONNECTED never truncates or wraps
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        val formattedTimer = if (state == ConnectionState.CONNECTED) {
                            s.language.formatDurationLocalized(elapsedSeconds, alwaysShowHours = true)
                        } else {
                            s.language.formatDurationLocalized(0L, alwaysShowHours = true)
                        }

                        AnimatedOdometerText(
                            text = formattedTimer,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.ExtraBold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = neu.textPrimary,
                        )

                        Spacer(Modifier.height(10.dp))

                        // Full-Width Single-Line Status Pill Directly Below Numeric Timer
                        AnimatedContent(
                            targetState = statusText,
                            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(180)) },
                            label = "status-label-single-line",
                        ) { label ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(statusColor.copy(alpha = 0.14f))
                                    .border(
                                        width = 1.dp,
                                        color = statusColor.copy(alpha = 0.38f),
                                        shape = RoundedCornerShape(50),
                                    )
                                    .padding(horizontal = 18.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = if (s.isRtl) 0.sp else 1.0.sp,
                                    ),
                                    color = statusColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Visible,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 5. SLIDE-TO-CONNECT PRIMARY CONTROLLER
                NeumorphicSlideToConnect(
                    state = state,
                    lastError = lastError,
                    onRequestConnect = onRequestConnect,
                    onDisconnect = onDisconnect,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .staggeredEntrance(1)
                        .shakeOnTrigger(lastError),
                )

                val error = lastError
                AnimatedVisibility(
                    visible = error != null && state != ConnectionState.CONNECTING,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    val displayError = remember(error, s.language) {
                        val raw = error.orEmpty()
                        when {
                            raw.contains("vpn.mozilla.org", ignoreCase = true) || raw.contains("20000ms", ignoreCase = true) -> {
                                when (s.language) {
                                    FoxyLanguage.FA -> "اختلال موقت اینترنت در اتصال به موزیلا (فیلترینگ ISP). سیستم با موفقیت توکن شما را ذخیره کرد و از مسیرهای Anycast پشتیبان استفاده می‌کند؛ اسلایدر را مجدداً بکشید."
                                    FoxyLanguage.AR -> "تعذر الاتصال المؤقت بخوادم موزيلا بسبب حجب الشبكة. تم حفظ الجلسة وتفعيل مسارات Anycast الاحتياطية؛ اسحب للمحاولة مجدداً."
                                    FoxyLanguage.EN -> "Temporary network timeout connecting to Mozilla auth servers. Session cached securely & Anycast backups activated; slide to retry."
                                }
                            }
                            else -> raw
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = displayError,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp,
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 6. 5-TOOL QUICK NETWORK & SECURITY TOOLKIT HUB
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(2),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = s.smartToolsSection,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                            ),
                            color = neu.textSecondary,
                            maxLines = 1,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenTools(0) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = s.openAllTools,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = neu.accent,
                                maxLines = 1,
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = neu.accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        val shortcutInfinite = rememberInfiniteTransition(label = "shortcut-hub-anim")
                        val shortcutPulse by shortcutInfinite.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "shortcut-pulse",
                        )
                        val chevronShiftPx by shortcutInfinite.animateFloat(
                            initialValue = -2.5f,
                            targetValue = 2.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "shortcut-chevron-shift",
                        )

                        NetworkToolTab.entries.forEachIndexed { idx, tool ->
                            val toolTitle = tool.localizedTitle(s)
                            val toolSub = tool.localizedSubtitle(s)
                            val phaseOffset = ((shortcutPulse + idx * 0.22f) % 1f)
                            val iconScale = 0.95f + (0.09f * kotlin.math.sin(phaseOffset * Math.PI).toFloat())
                            val badgeAccent = when (idx % 3) {
                                0 -> neu.accent
                                1 -> if (state == ConnectionState.CONNECTED) statusColors.connected else neu.accent
                                else -> Color(0xFF0EA5E9)
                            }
                            NeumorphicCard(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenTools(tool.id)
                                },
                                cornerRadius = 20.dp,
                                modifier = Modifier
                                    .width(206.dp)
                                    .heightIn(min = 142.dp)
                                    .staggeredEntrance(idx + 2, delayPerItemMs = 50)
                                    .neumorphicTouchBounce(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .graphicsLayer {
                                                    scaleX = iconScale
                                                    scaleY = iconScale
                                                }
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(badgeAccent.copy(alpha = 0.14f))
                                                .border(
                                                    width = 1.dp,
                                                    color = badgeAccent.copy(alpha = 0.28f + 0.22f * shortcutPulse),
                                                    shape = RoundedCornerShape(13.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = toolTitle,
                                                tint = badgeAccent,
                                                modifier = Modifier.size(21.dp),
                                            )
                                        }

                                        // Animated quick-launch pill indicator inspired by modern minimal cards
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(neu.surfaceElevated)
                                                .border(
                                                    width = 0.8.dp,
                                                    color = neu.textSecondary.copy(alpha = 0.16f),
                                                    shape = CircleShape,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = badgeAccent,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .graphicsLayer {
                                                        translationX = if (s.isRtl) -chevronShiftPx else chevronShiftPx
                                                    },
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = toolTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = fonts.headingFont,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = neu.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = toolSub,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = fonts.bodyFont),
                                        color = neu.textSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 7. SERVER LOCATION CARD (with Animated Live Status Ring, Auto-Best Server Lightning Action & Quick Protocol Bar)
                val serverCardInfinite = rememberInfiniteTransition(label = "server-card-pulse")
                val serverRingPulse by serverCardInfinite.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "server-ring-scale",
                )
                val serverActiveColor = if (state == ConnectionState.CONNECTED) statusColors.connected else neu.accent
                var isFindingBestServer by remember { mutableStateOf(false) }
                val configRevForProto by app.settingsStore.islandConfigRevision.collectAsState()
                var activeProtocolMode by remember(configRevForProto) {
                    mutableStateOf(app.settingsStore.connectionProtocolMode)
                }

                NeumorphicCard(
                    onClick = onOpenServers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(3)
                        .neumorphicTouchBounce(),
                    cornerRadius = 22.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (selectedProxy != null) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .graphicsLayer {
                                            scaleX = serverRingPulse
                                            scaleY = serverRingPulse
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CountryFlagImage(
                                        countryCode = selectedProxy?.countryCode,
                                        modifier = Modifier.size(width = 38.dp, height = 29.dp),
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(50.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .graphicsLayer {
                                                scaleX = serverRingPulse
                                                scaleY = serverRingPulse
                                            }
                                            .clip(CircleShape)
                                            .background(serverActiveColor.copy(alpha = 0.12f)),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(neu.surfaceElevated)
                                            .border(1.2.dp, serverActiveColor.copy(alpha = 0.42f), CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        com.vauth.foxyvpn.ui.theme.FoxySpeedometerIcon(
                                            tint = neu.accent,
                                            needleTint = serverActiveColor,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.serverLocationLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = if (s.isRtl) 0.sp else 0.9.sp,
                                    ),
                                    color = neu.textSecondary,
                                    maxLines = 1,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = selectedProxy?.let {
                                        val countryLabel = s.language.localizeCountryName(it.countryName, it.countryCode)
                                        if (it.cityCode.isNotBlank()) "$countryLabel (${it.cityCode.uppercase()})" else countryLabel
                                    } ?: s.recommendedFastest,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = neu.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // Quick Auto-Best Server Lightning Action (⚡)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(neu.accent.copy(alpha = 0.14f))
                                    .border(1.dp, neu.accent.copy(alpha = 0.35f), CircleShape)
                                    .clickable(enabled = !isFindingBestServer) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            isFindingBestServer = true
                                            val countries = app.serverListClient.loadCachedCountries()
                                            val candidates = countries.flatMap { c ->
                                                ServerListClient.candidatesForCountry(
                                                    countries,
                                                    c.code,
                                                    app.settingsStore.connectionProtocolMode,
                                                ).take(1)
                                            }.take(10)
                                            var bestCandidate: ProxyCandidate? = null
                                            var bestRtt = Int.MAX_VALUE
                                            for (cand in candidates) {
                                                val rtt = PingUtil.measureTcpLatencyMs(cand.host, cand.port)
                                                if (rtt != null && rtt < bestRtt) {
                                                    bestRtt = rtt
                                                    bestCandidate = cand
                                                }
                                            }
                                            if (bestCandidate != null) {
                                                app.proxyStateStore.save(bestCandidate)
                                                disconnectedPingLatency = bestRtt
                                                if (state == ConnectionState.CONNECTED) {
                                                    FoxyVpnService.reconnect(app)
                                                }
                                                Toast.makeText(
                                                    app,
                                                    when (s.language) {
                                                        FoxyLanguage.FA -> "بهترین سرور انتخاب شد: ${s.language.localizeCountryName(bestCandidate.countryName, bestCandidate.countryCode)} (${bestRtt}ms)"
                                                        FoxyLanguage.AR -> "تم اختيار أسرع خادم: ${s.language.localizeCountryName(bestCandidate.countryName, bestCandidate.countryCode)} (${bestRtt}ms)"
                                                        FoxyLanguage.EN -> "Fastest server selected: ${bestCandidate.countryName} (${bestRtt}ms)"
                                                    },
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            isFindingBestServer = false
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isFindingBestServer) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = neu.accent,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Auto Best Server",
                                        tint = neu.accent,
                                        modifier = Modifier.size(19.dp),
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(neu.surfaceElevated),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = s.islandChangeServer,
                                    tint = serverActiveColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = neu.textSecondary.copy(alpha = 0.12f))
                        Spacer(Modifier.height(10.dp))

                        // Inline Sing-Box MASQUE v2.0 Quick Protocol Pill Bar (One-Tap Switch on Home Screen)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ConnectionProtocolMode.entries.forEach { mode ->
                                val isSelected = activeProtocolMode == mode
                                val pillShape = RoundedCornerShape(11.dp)
                                val shortTitle = when (mode) {
                                    ConnectionProtocolMode.AUTO -> when (s.language) {
                                        FoxyLanguage.FA -> "خودکار"
                                        FoxyLanguage.AR -> "تلقائي"
                                        FoxyLanguage.EN -> "Auto"
                                    }
                                    ConnectionProtocolMode.HTTP3_QUIC -> "H3 QUIC"
                                    ConnectionProtocolMode.HTTP2_TCP -> "H2 TCP"
                                    ConnectionProtocolMode.HTTP11_TLS -> "H1.1 TLS"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(pillShape)
                                        .background(
                                            if (isSelected) serverActiveColor.copy(alpha = 0.16f)
                                            else neu.surfaceElevated.copy(alpha = 0.65f),
                                        )
                                        .border(
                                            width = if (isSelected) 1.3.dp else 0.8.dp,
                                            color = if (isSelected) serverActiveColor else neu.textSecondary.copy(alpha = 0.14f),
                                            shape = pillShape,
                                        )
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            activeProtocolMode = mode
                                            app.settingsStore.connectionProtocolMode = mode
                                            FoxyVpnService.switchProtocol(app, mode)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = shortTitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = fonts.monoFont,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            fontSize = 10.5.sp,
                                        ),
                                        color = if (isSelected) serverActiveColor else neu.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 7B. ANIMATED WORLD MAP ORIGIN-TO-EXIT ROUTE CARD
                val liveProtocolBadge by FoxyVpnService.activeProtocolBadge.collectAsState()
                WorldRouteMapCard(
                    state = state,
                    realIp = realIp,
                    cachedRealIp = app.settingsStore.cachedRealIp,
                    cachedRealCountry = app.settingsStore.cachedRealCountry,
                    selectedProxy = selectedProxy,
                    vpnEdgeIp = vpnEdgeIp,
                    protocolLabel = if (state == ConnectionState.CONNECTED) liveProtocolBadge else activeProtocolMode.shortBadge,
                    adBlockEnabled = app.settingsStore.adBlockEnabled,
                    onOpenServers = onOpenServers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(3),
                )

                Spacer(Modifier.height(18.dp))

                // 8. CURRENT IP CARD:
                // - "CURRENT IP • TAP FOR LEAK TEST" on a single horizontal line
                // - Full IPv6/IPv4 address + Pahlavi/Country Flag + One-Tap Copy button
                NeumorphicCard(
                    onClick = { onOpenTools(NetworkToolTab.LEAK_INSPECTOR.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(4)
                        .neumorphicTouchBounce(),
                    cornerRadius = 22.dp,
                ) {
                    val isConnected = state == ConnectionState.CONNECTED
                    val activeIpInfo = if (isConnected) vpnEdgeIp else realIp
                    val displayCountryCode = activeIpInfo?.countryCode
                        ?.takeIf { it.matches(Regex("[A-Za-z]{2}")) }
                        ?.uppercase()
                        ?: if (!isConnected) app.settingsStore.cachedRealCountry
                            ?.takeIf { it.matches(Regex("[A-Za-z]{2}")) }
                            ?.uppercase() else null
                    val rawIpOnly = activeIpInfo?.ip?.takeIf(String::isNotBlank)
                        ?: if (!isConnected) app.settingsStore.cachedRealIp else null
                    val rawIpDisplay = activeIpInfo?.ip?.takeIf(String::isNotBlank)?.let { ip ->
                        val countrySuffix = displayCountryCode?.let {
                            " • ${s.language.localizeCountryName("", it)}"
                        }.orEmpty()
                        "\u200E$ip$countrySuffix"
                    } ?: if (isConnected) {
                        s.verifying
                    } else {
                        app.settingsStore.cachedRealIp?.let { ip ->
                            val countrySuffix = displayCountryCode?.let {
                                " • ${s.language.localizeCountryName("", it)}"
                            }.orEmpty()
                            "\u200E$ip$countrySuffix"
                        } ?: s.checking
                    }
                    val ipDisplay = rawIpDisplay
                    val badgeColor = if (isConnected) statusColors.connected else MaterialTheme.colorScheme.error

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        // Top Row: Shield Icon + Single-line "CURRENT IP • TAP FOR LEAK TEST" + Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) statusColors.connected.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Filled.Security else Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "آی‌پی فعلی • تست نشت"
                                    FoxyLanguage.AR -> "عنوان IP • فحص التسرب"
                                    FoxyLanguage.EN -> s.currentIpTapLeakTest
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = if (s.isRtl) 0.sp else 0.4.sp,
                                ),
                                color = neu.textSecondary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = if (isConnected) s.badgeProtected else s.badgeExposed,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = fonts.headingFont,
                                        fontWeight = FontWeight.ExtraBold,
                                    ),
                                    color = badgeColor,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Separate Row Below: Styled Quote Block / Framed Box for IPv6 / IPv4 Address
                        val quoteShape = RoundedCornerShape(14.dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neumorphicSunken(
                                    shape = quoteShape,
                                    cornerRadius = 14.dp,
                                    depth = 2.5.dp,
                                    surfaceColor = neu.surfaceElevated,
                                )
                                .border(
                                    width = 1.dp,
                                    color = badgeColor.copy(alpha = 0.24f),
                                    shape = quoteShape,
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Quote Block Accent Bar
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(22.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(badgeColor),
                            )
                            Spacer(Modifier.width(12.dp))
                            AnimatedContent(
                                targetState = ipDisplay,
                                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                                label = "ip-quote-box-anim",
                                modifier = Modifier.weight(1f),
                            ) { targetIp ->
                                Text(
                                    text = targetIp,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = fonts.monoFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (targetIp.length > 26) 11.5.sp else 13.5.sp,
                                    ),
                                    color = neu.textPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (displayCountryCode != null) {
                                Spacer(Modifier.width(8.dp))
                                CountryFlagImage(
                                    countryCode = displayCountryCode,
                                    modifier = Modifier.size(width = 26.dp, height = 18.dp),
                                )
                            }
                            if (!rawIpOnly.isNullOrBlank()) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(neu.accent.copy(alpha = 0.14f))
                                        .clickable {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val cm = app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            cm?.setPrimaryClip(ClipData.newPlainText("IP Address", rawIpOnly))
                                            Toast.makeText(
                                                app,
                                                when (s.language) {
                                                    FoxyLanguage.FA -> "آی‌پی کپی شد: $rawIpOnly"
                                                    FoxyLanguage.AR -> "تم نسخ عنوان IP: $rawIpOnly"
                                                    FoxyLanguage.EN -> "IP copied: $rawIpOnly"
                                                },
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy IP",
                                        tint = neu.accent,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 9. TELEMETRY, LIVE WAVEFORM SPARKLINE & QUOTA CARD
                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(5),
                    cornerRadius = 22.dp,
                ) {
                    Column(Modifier.padding(20.dp)) {
                        val isConnected = state == ConnectionState.CONNECTED
                        val remaining = quotaRemaining
                        val max = quotaMax
                        val dynamicRemaining = if (state == ConnectionState.CONNECTED && remaining != null) {
                            (remaining - liveStats.sessionBytes).coerceAtLeast(0L)
                        } else {
                            remaining
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Data Used Sunken Box
                            val tileShape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neumorphicSunken(
                                        shape = tileShape,
                                        cornerRadius = 14.dp,
                                        depth = 2.5.dp,
                                        surfaceColor = neu.surfaceElevated,
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                Column {
                                    Text(
                                        text = s.dataUsedLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = fonts.headingFont,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                                        ),
                                        color = neu.textSecondary,
                                        maxLines = 1,
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = if (isConnected) {
                                            s.language.formatBytesLocalized(liveStats.sessionBytes)
                                        } else {
                                            s.language.formatBytesLocalized(0L)
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = fonts.monoFont,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = neu.textPrimary,
                                        maxLines = 1,
                                    )
                                }
                            }

                            // Latency / Ping Sunken Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neumorphicSunken(
                                        shape = tileShape,
                                        cornerRadius = 14.dp,
                                        depth = 2.5.dp,
                                        surfaceColor = neu.surfaceElevated,
                                    )
                                    .clickable(role = Role.Button) {
                                        if (!isPinging) {
                                            scope.launch {
                                                isPinging = true
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (state == ConnectionState.CONNECTED) {
                                                    FoxyVpnService.pingNow()
                                                } else {
                                                    val target = selectedProxy ?: app.proxyStateStore.load() ?: run {
                                                        val cached = app.serverListClient.loadCachedCountries()
                                                        ServerListClient.candidatesForCountry(cached, RECOMMENDED_COUNTRY_CODE).firstOrNull()
                                                            ?: cached.firstOrNull { it.code.isNotBlank() }?.let { country ->
                                                                ServerListClient.candidatesForCountry(cached, country.code).firstOrNull()
                                                            }
                                                    }
                                                    if (target != null) {
                                                        disconnectedPingLatency = PingUtil.measureTcpLatencyMs(target.host, target.port)
                                                    }
                                                }
                                                isPinging = false
                                            }
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = s.latencyLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = fonts.headingFont,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                                            ),
                                            color = neu.textSecondary,
                                            maxLines = 1,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        com.vauth.foxyvpn.ui.theme.FoxySpeedometerIcon(
                                            tint = neu.accent,
                                            needleTint = if (isConnected) statusColors.connected else neu.textPrimary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    val currentPing = if (isConnected) pingLatency else disconnectedPingLatency
                                    Text(
                                        text = when {
                                            isPinging -> s.testing
                                            currentPing != null -> s.language.formatPingLocalized(currentPing)
                                            else -> "\u2014"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = fonts.monoFont,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = if (currentPing != null) statusColors.connected else neu.textPrimary,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Live Traffic Waveform Sparkline Graph (Header separated from Canvas to prevent overlap)
                        val waveShape = RoundedCornerShape(14.dp)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neumorphicSunken(
                                    shape = waveShape,
                                    cornerRadius = 14.dp,
                                    depth = 2.5.dp,
                                    surfaceColor = neu.surfaceElevated,
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "↓ ${s.language.formatSpeedLocalized(liveStats.rxRateBytesPerSec)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = fonts.monoFont,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (isConnected) statusColors.connected else neu.textSecondary,
                                    maxLines = 1,
                                )
                                Text(
                                    text = "↑ ${s.language.formatSpeedLocalized(liveStats.txRateBytesPerSec)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = fonts.monoFont,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = statusColors.connecting,
                                    maxLines = 1,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LiveTrafficSparkline(
                                rxHistory = rxHistory,
                                txHistory = txHistory,
                                rxColor = if (isConnected) statusColors.connected else neu.accent,
                                txColor = statusColors.connecting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Quota Details & Sunken Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = s.monthlyQuotaLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = fonts.bodyFont,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = neu.textSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            val quotaText = when {
                                isUnlimited -> s.unlimitedQuota
                                dynamicRemaining != null && max != null ->
                                    "${s.language.formatBytesLocalized(dynamicRemaining)} / ${s.language.formatBytesLocalized(max)}"
                                dynamicRemaining != null -> s.language.formatBytesLocalized(dynamicRemaining)
                                max != null -> "${s.quotaLimitPrefix} ${s.language.formatBytesLocalized(max)}"
                                else -> "\u2014"
                            }
                            Text(
                                text = quotaText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = fonts.monoFont,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = neu.textPrimary,
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        val rawQuotaFraction = when {
                            isUnlimited -> 1f
                            max != null && max > 0L && dynamicRemaining != null ->
                                (dynamicRemaining.toFloat() / max.toFloat()).coerceIn(0f, 1f)
                            else -> 0f
                        }
                        val animatedQuotaFraction by animateFloatAsState(
                            targetValue = rawQuotaFraction,
                            animationSpec = tween(600, easing = FastOutSlowInEasing),
                            label = "quota-bar-fill",
                        )

                        val progressShape = RoundedCornerShape(7.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .neumorphicSunken(
                                    shape = progressShape,
                                    cornerRadius = 7.dp,
                                    depth = 2.dp,
                                    surfaceColor = neu.surfaceElevated,
                                ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedQuotaFraction.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(progressShape)
                                    .background(
                                        if (isConnected) statusColors.connected
                                        else neu.accent.copy(alpha = 0.8f),
                                    ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 10. QUICK POWER TOGGLES GRID (2x2 COMPACT TACTILE CONTROL DECK)
                val configRev by app.settingsStore.islandConfigRevision.collectAsState()
                @Suppress("UNUSED_EXPRESSION")
                configRev
                var unmeteredBoost by remember(configRev) { mutableStateOf(app.settingsStore.unmeteredVpnModeEnabled) }
                var adBlockEnabled by remember(configRev) { mutableStateOf(app.settingsStore.adBlockEnabled) }
                var autoBestFailover by remember(configRev) { mutableStateOf(app.settingsStore.smartPingAutoSwitch) }
                var batteryEco by remember(configRev) { mutableStateOf(app.settingsStore.batterySaverModeEnabled) }

                NeumorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(6),
                    cornerRadius = 22.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = when (s.language) {
                                    FoxyLanguage.FA -> "کنترل سریع عملکرد و بهینه‌ساز"
                                    FoxyLanguage.AR -> "التحكم السريع بالأداء والتحسين"
                                    FoxyLanguage.EN -> "QUICK PERFORMANCE & SHIELD DECK"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = if (s.isRtl) 0.sp else 0.8.sp,
                                ),
                                color = neu.textSecondary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = s.headerSettingsButton,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = neu.accent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onOpenSettings() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        data class QuickPowerItem(
                            val title: String,
                            val badge: String,
                            val icon: androidx.compose.ui.graphics.vector.ImageVector,
                            val enabled: Boolean,
                            val activeColor: Color,
                            val onToggle: (Boolean) -> Unit,
                        )

                        val powerItems = listOf(
                            QuickPowerItem(
                                title = when (s.language) {
                                    FoxyLanguage.FA -> "بوستر سرعت توربو"
                                    FoxyLanguage.AR -> "معزز السرعة توربو"
                                    FoxyLanguage.EN -> "Turbo Speed Boost"
                                },
                                badge = if (unmeteredBoost) "1MB TUN" else "STD",
                                icon = Icons.Filled.Bolt,
                                enabled = unmeteredBoost,
                                activeColor = statusColors.connected,
                                onToggle = {
                                    unmeteredBoost = it
                                    app.settingsStore.unmeteredVpnModeEnabled = it
                                    FoxyVpnService.applyNetworkConfiguration(app)
                                },
                            ),
                            QuickPowerItem(
                                title = when (s.language) {
                                    FoxyLanguage.FA -> "سپر ضد تبلیغات"
                                    FoxyLanguage.AR -> "حاجب الإعلانات"
                                    FoxyLanguage.EN -> "AdBlock Shield"
                                },
                                badge = if (adBlockEnabled) "DNS ON" else "OFF",
                                icon = Icons.Filled.Security,
                                enabled = adBlockEnabled,
                                activeColor = neu.accent,
                                onToggle = {
                                    adBlockEnabled = it
                                    app.settingsStore.adBlockEnabled = it
                                    FoxyVpnService.applyRuntimeSettings(app)
                                },
                            ),
                            QuickPowerItem(
                                title = when (s.language) {
                                    FoxyLanguage.FA -> "سوییچ هوشمند پینگ"
                                    FoxyLanguage.AR -> "تبديل ذكي لأقل بينغ"
                                    FoxyLanguage.EN -> "Smart Ping Switch"
                                },
                                badge = if (autoBestFailover) "AUTO" else "MANUAL",
                                icon = Icons.Filled.Speed,
                                enabled = autoBestFailover,
                                activeColor = statusColors.connected,
                                onToggle = {
                                    autoBestFailover = it
                                    app.settingsStore.smartPingAutoSwitch = it
                                    FoxyVpnService.applyRuntimeSettings(app)
                                },
                            ),
                            QuickPowerItem(
                                title = when (s.language) {
                                    FoxyLanguage.FA -> "ذخیره هوشمند باتری"
                                    FoxyLanguage.AR -> "توفير البطارية الذكي"
                                    FoxyLanguage.EN -> "Smart Battery Eco"
                                },
                                badge = if (batteryEco) "ECO ON" else "PERF",
                                icon = Icons.Filled.Shield,
                                enabled = batteryEco,
                                activeColor = neu.accent,
                                onToggle = {
                                    batteryEco = it
                                    app.settingsStore.batterySaverModeEnabled = it
                                    FoxyVpnService.applyRuntimeSettings(app)
                                },
                            ),
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            powerItems.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowItems.forEach { item ->
                                        val tileShape = RoundedCornerShape(16.dp)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(tileShape)
                                                .background(
                                                    if (item.enabled) item.activeColor.copy(alpha = 0.13f)
                                                    else neu.surfaceElevated.copy(alpha = 0.72f),
                                                )
                                                .border(
                                                    width = if (item.enabled) 1.2.dp else 1.dp,
                                                    color = if (item.enabled) item.activeColor.copy(alpha = 0.45f)
                                                    else neu.textSecondary.copy(alpha = 0.12f),
                                                    shape = tileShape,
                                                )
                                                .clickable { item.onToggle(!item.enabled) }
                                                .padding(horizontal = 12.dp, vertical = 11.dp),
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (item.enabled) item.activeColor.copy(alpha = 0.22f)
                                                                else neu.textSecondary.copy(alpha = 0.10f),
                                                            ),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            imageVector = item.icon,
                                                            contentDescription = item.title,
                                                            tint = if (item.enabled) item.activeColor else neu.textSecondary,
                                                            modifier = Modifier.size(16.dp),
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                if (item.enabled) item.activeColor.copy(alpha = 0.20f)
                                                                else neu.textSecondary.copy(alpha = 0.10f),
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                                    ) {
                                                        Text(
                                                            text = item.badge,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontFamily = fonts.monoFont,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 9.sp,
                                                            ),
                                                            color = if (item.enabled) item.activeColor else neu.textSecondary,
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = fonts.headingFont,
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                    color = if (item.enabled) neu.textPrimary else neu.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Dedicated 2D VPN Cyber Tactical & Dot-Matrix Route Map (inspired by NordVPN, ProtonVPN, and Surfshark).
 * Renders a flat 2D midnight tactical viewport with high-density stippled Dot-Matrix continents,
 * precision vector continental outlines, global VPN PoP relay hubs, glowing encrypted Bezier tunnel arc,
 * bidirectional packet telemetry, live Haversine distance (km), and interactive 1.0x–3.4x zoom.
 */
@Composable
private fun WorldRouteMapCard(
    state: ConnectionState,
    realIp: IpLocationInfo?,
    cachedRealIp: String?,
    cachedRealCountry: String?,
    selectedProxy: ProxyCandidate?,
    vpnEdgeIp: IpLocationInfo?,
    protocolLabel: String,
    adBlockEnabled: Boolean,
    onOpenServers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neu = currentNeumorphicColors
    val statusColors = LocalFoxyStatusColors.current
    val s = currentFoxyStrings
    val fonts = currentFoxyFonts
    val isConnected = state == ConnectionState.CONNECTED
    val isConnecting = state == ConnectionState.CONNECTING
    val coreExitLocation by FoxyVpnService.vpnExitLocation.collectAsState()

    // Strictly use real detected or cached ISP country; never fallback to a fake country code
    val resolvedOriginCountryCode = (realIp?.countryCode?.takeIf { it.matches(Regex("[A-Za-z]{2}")) }
        ?: cachedRealCountry?.takeIf { it.matches(Regex("[A-Za-z]{2}")) })?.uppercase()
    val originIpRaw = realIp?.ip?.takeIf { it.isNotBlank() }
        ?: cachedRealIp?.takeIf { it.isNotBlank() }
    val originIpText = originIpRaw?.let { "\u200E$it" } ?: s.checking

    // Use the verified public egress country while connected. The core-selected
    // city is shown only when its country agrees with that verified egress.
    val liveExitCountryCode = vpnEdgeIp?.takeIf {
        it.ip.isNotBlank() && it.countryCode.matches(Regex("[A-Za-z]{2}"))
    }?.countryCode?.uppercase()
    val coreLocationCountryCode = coreExitLocation?.countryCode
        ?.takeIf { it.matches(Regex("[A-Za-z]{2}")) }
        ?.uppercase()
    val coreLocationMatchesEgress = liveExitCountryCode != null &&
        coreLocationCountryCode == liveExitCountryCode
    val resolvedExitCountryCode = if (isConnected) {
        liveExitCountryCode
    } else {
        selectedProxy?.countryCode?.takeIf { it.isNotBlank() && it != "REC" }
            ?.uppercase() ?: liveExitCountryCode
    }
    val resolvedExitCityCode = when {
        isConnected && coreLocationMatchesEgress -> coreExitLocation?.cityCode?.takeIf(::isKnownRouteMapCityCode)
        isConnected -> null
        else -> selectedProxy?.cityCode?.takeIf(::isKnownRouteMapCityCode)
    }
    val verifiedCoreCityName = coreExitLocation?.takeIf { coreLocationMatchesEgress && resolvedExitCityCode != null }
        ?.let { it.cityName.takeIf(String::isNotBlank) ?: it.cityCode.takeIf(String::isNotBlank) }

    val exitCountryName = when {
        isConnected && liveExitCountryCode != null -> {
            val country = s.language.localizeCountryName(liveExitCountryCode, liveExitCountryCode)
            verifiedCoreCityName?.let { "$country ($it)" } ?: country
        }
        isConnected -> s.checking
        selectedProxy != null && selectedProxy.countryCode.isNotBlank() && selectedProxy.countryCode != "REC" -> {
            val baseCountry = s.language.localizeCountryName(selectedProxy.countryName, selectedProxy.countryCode)
            if (!resolvedExitCityCode.isNullOrBlank()) "$baseCountry (${resolvedExitCityCode.uppercase()})" else baseCountry
        }
        resolvedExitCountryCode != null -> s.language.localizeCountryName(resolvedExitCountryCode, resolvedExitCountryCode)
        else -> s.recommendedFastest
    }

    val exitIpText = when {
        isConnected && vpnEdgeIp != null -> "\u200E${vpnEdgeIp.ip}"
        isConnected -> s.checking
        selectedProxy != null -> "\u200E${selectedProxy.host}:${selectedProxy.port}"
        else -> s.recommendedFastest
    }

    val isRouteVerifiedLive = isConnected && liveExitCountryCode != null && resolvedOriginCountryCode != null

    val infiniteTransition = rememberInfiniteTransition(label = "vpn-tactical-map-arc")
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnected) 1750 else 2900,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "route-packet-progress",
    )
    val reversePacketProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnected) 2150 else 3300,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "route-reverse-packet-progress",
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "route-node-pulse",
    )
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "route-radar-sweep",
    )

    // Interactive Zoom & Focus Controls (1.0x World View up to 3.4x Country Detail View)
    var targetZoom by remember { mutableStateOf(1.0f) }
    var focusDestination by remember { mutableStateOf(false) }
    // 0 = Standard 2D Google Maps (lyrs=m), 1 = Terrain 2D Google Maps (lyrs=p)
    var googleMapLayerIndex by remember { mutableStateOf(0) }
    val googleLayerCode = if (googleMapLayerIndex == 0) "m" else "p"
    val routeMapCropTop = remember(resolvedOriginCountryCode, resolvedExitCountryCode, resolvedExitCityCode) {
        val routeY = listOfNotNull(
            resolvedOriginCountryCode?.let { code ->
                resolveLatLonDeg(code, null)?.first?.let(::mercatorYPixel)
            },
            resolvedExitCountryCode?.let { code ->
                resolveLatLonDeg(code, resolvedExitCityCode)?.first?.let(::mercatorYPixel)
            },
        )
        if (routeY.isEmpty()) GOOGLE_WORLD_DEFAULT_CROP_Y_TOP else {
            val centerY = routeY.average()
            val maxTop = 1024 - GOOGLE_WORLD_CROP_HEIGHT
            (((centerY - GOOGLE_WORLD_CROP_HEIGHT / 2.0).roundToInt().coerceIn(0, maxTop)) / 16) * 16
        }
    }
    val mapCacheKey = "${googleLayerCode}_${s.language.code}_$routeMapCropTop"
    var googleWorldBitmap by remember(mapCacheKey) {
        mutableStateOf(GoogleMaps2DTileCache[mapCacheKey])
    }

    LaunchedEffect(mapCacheKey) {
        if (googleWorldBitmap == null) {
            val loaded = fetchStandard2DGoogleWorldMap(
                layerCode = googleLayerCode,
                langCode = s.language.code,
                cropTop = routeMapCropTop,
            )
            if (loaded != null) {
                GoogleMaps2DTileCache[mapCacheKey] = loaded
                if (GoogleMaps2DTileCache.size > 8) {
                    GoogleMaps2DTileCache.keys.filter { it != mapCacheKey }.take(2)
                        .forEach { staleKey -> GoogleMaps2DTileCache.remove(staleKey) }
                }
                googleWorldBitmap = loaded
            }
        }
    }

    val originNormState = resolvedOriginCountryCode?.let { locationToMercatorNorm(it, null, routeMapCropTop) }
    val exitNormState = resolvedExitCountryCode?.let { locationToMercatorNorm(it, resolvedExitCityCode, routeMapCropTop) }

    // Compute real Haversine distance in kilometers between Origin and Exit
    val routeDistanceKm = remember(resolvedOriginCountryCode, resolvedExitCountryCode, resolvedExitCityCode) {
        if (resolvedOriginCountryCode != null && resolvedExitCountryCode != null) {
            computeRouteDistanceKm(resolvedOriginCountryCode, resolvedExitCountryCode, resolvedExitCityCode)
        } else {
            null
        }
    }

    val animatedZoom by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "map-zoom-scale",
    )

    val focusCenterNorm = when {
        focusDestination && exitNormState != null -> exitNormState
        targetZoom > 1.05f && exitNormState != null && originNormState != null -> Offset(
            (originNormState.x + exitNormState.x) * 0.5f,
            (originNormState.y + exitNormState.y) * 0.5f,
        )
        targetZoom > 1.05f && exitNormState != null -> exitNormState
        else -> Offset(0.52f, 0.44f)
    }

    val animatedFocusX by animateFloatAsState(
        targetValue = focusCenterNorm.x,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "map-focus-x",
    )
    val animatedFocusY by animateFloatAsState(
        targetValue = focusCenterNorm.y,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "map-focus-y",
    )

    val routeColor = when (state) {
        ConnectionState.CONNECTED -> statusColors.connected
        ConnectionState.CONNECTING -> statusColors.connecting
        ConnectionState.DISCONNECTED -> neu.accent
    }
    val originNodeColor = Color(0xFF0066FF)

    NeumorphicCard(
        onClick = onOpenServers,
        cornerRadius = 22.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (s.language) {
                            FoxyLanguage.FA -> "نقشه موقعیت خروجی VPN"
                            FoxyLanguage.AR -> "خريطة موقع خروج VPN"
                            FoxyLanguage.EN -> "VPN EGRESS LOCATION MAP"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = fonts.headingFont,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = neu.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    val distanceSuffix = routeDistanceKm?.let { " • ${it} km" }.orEmpty()
                    Text(
                        text = if (isRouteVerifiedLive) {
                            when (s.language) {
                                FoxyLanguage.FA -> "کشور خروجی تأیید شد؛ مسیر شماتیک و فاصله تقریبی$distanceSuffix"
                                FoxyLanguage.AR -> "تم التحقق من بلد الخروج؛ المسار تخطيطي والمسافة تقديرية$distanceSuffix"
                                FoxyLanguage.EN -> "Egress country verified; schematic route, estimated distance$distanceSuffix"
                            }
                        } else {
                            when (s.language) {
                                FoxyLanguage.FA -> "مقصد بر اساس موقعیت انتخاب‌شده؛ مسیر شماتیک$distanceSuffix"
                                FoxyLanguage.AR -> "الوجهة حسب الموقع المحدد؛ المسار تخطيطي$distanceSuffix"
                                FoxyLanguage.EN -> "Selected location only; route is schematic$distanceSuffix"
                            }
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = fonts.bodyFont,
                            fontSize = 10.sp,
                        ),
                        color = if (isRouteVerifiedLive) statusColors.connected else neu.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (adBlockEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColors.connected.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = if (s.language == FoxyLanguage.FA) "ضد تبلیغ" else "AdBlock",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = fonts.headingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                ),
                                color = statusColors.connected,
                                maxLines = 1,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(routeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = protocolLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = routeColor,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Standard 2D Flat Google Map Viewport + Clean Professional Tunnel Animation
            val mapShape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp)
                    .neumorphicSunken(
                        shape = mapShape,
                        cornerRadius = 16.dp,
                        depth = 2.dp,
                        surfaceColor = Color(0xFFAAD3DF),
                    )
                    .border(
                        width = 1.dp,
                        color = routeColor.copy(alpha = 0.28f),
                        shape = mapShape,
                    )
                    .clip(mapShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFAAD3DF))
                        .graphicsLayer {
                            scaleX = animatedZoom
                            scaleY = animatedZoom
                            val maxShiftX = (size.width * (animatedZoom - 1f)) * 0.5f
                            val maxShiftY = (size.height * (animatedZoom - 1f)) * 0.5f
                            translationX = ((0.5f - animatedFocusX) * size.width * animatedZoom)
                                .coerceIn(-maxShiftX, maxShiftX)
                            translationY = ((0.5f - animatedFocusY) * size.height * animatedZoom)
                                .coerceIn(-maxShiftY, maxShiftY)
                        },
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val invZoom = (1f / animatedZoom).coerceIn(0.35f, 1f)

                        // 1. Standard 2D Flat Google Map (mt1.google.com/vt/lyrs=m) or Clean 2D Flat Fallback while loading
                        val bmp = googleWorldBitmap
                        if (bmp != null) {
                            drawImage(
                                image = bmp,
                                dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                dstSize = androidx.compose.ui.unit.IntSize(w.toInt().coerceAtLeast(1), h.toInt().coerceAtLeast(1)),
                            )
                        } else {
                            // Natural 2D Google Maps Ocean & Continents fallback before tiles finish loading
                            drawRect(color = Color(0xFFAAD3DF), size = size)
                            VPN_WORLD_CONTINENT_POLYGONS.forEach { poly ->
                                if (poly.size >= 3) {
                                    val path = Path().apply {
                                        moveTo(poly[0].x * w, poly[0].y * h)
                                        for (idx in 1 until poly.size) {
                                            lineTo(poly[idx].x * w, poly[idx].y * h)
                                        }
                                        close()
                                    }
                                    drawPath(path = path, color = Color(0xFFF2EFE9))
                                    drawPath(
                                        path = path,
                                        color = Color(0xFFD0C9BE),
                                        style = Stroke(width = 1.dp.toPx() * invZoom),
                                    )
                                }
                            }
                        }

                        val originNorm = originNormState
                        val exitNorm = exitNormState

                        // 2. Simple, Sleek, Professional Tunnel Animation (Origin -> Exit)
                        if (originNorm != null && exitNorm != null) {
                            val startPt = Offset(originNorm.x * w, originNorm.y * h)
                            val endPt = Offset(exitNorm.x * w, exitNorm.y * h)

                            val midX = (startPt.x + endPt.x) * 0.5f
                            val arcLift = (kotlin.math.abs(endPt.x - startPt.x) * 0.28f).coerceIn(14.dp.toPx(), 46.dp.toPx())
                            val controlPt = Offset(midX, (minOf(startPt.y, endPt.y) - arcLift).coerceAtLeast(8.dp.toPx()))

                            val arcPath = Path().apply {
                                moveTo(startPt.x, startPt.y)
                                quadraticTo(controlPt.x, controlPt.y, endPt.x, endPt.y)
                            }

                            // Minimal route renderer: one stable line and one travelling packet.
                            // Keep the telemetry animation separate from the route geometry.
                            drawPath(
                                path = arcPath,
                                color = Color.Black.copy(alpha = 0.16f),
                                style = Stroke(width = 4.8.dp.toPx() * invZoom, cap = StrokeCap.Round),
                            )
                            drawPath(
                                path = arcPath,
                                color = routeColor.copy(alpha = 0.22f),
                                style = Stroke(width = 3.6.dp.toPx() * invZoom, cap = StrokeCap.Round),
                            )
                            drawPath(
                                path = arcPath,
                                color = routeColor,
                                style = Stroke(width = 2.0.dp.toPx() * invZoom, cap = StrokeCap.Round),
                            )

                            if (isConnected || isConnecting) {
                                val phase = packetProgress * 150f
                                drawPath(
                                    path = arcPath,
                                    color = Color.White.copy(alpha = 0.80f),
                                    style = Stroke(
                                        width = 1.8.dp.toPx() * invZoom,
                                        cap = StrokeCap.Round,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(11f, 180f), phase),
                                    ),
                                )

                                fun pointOnRoute(t: Float): Offset {
                                    val clamped = t.coerceIn(0f, 1f)
                                    val inv = 1f - clamped
                                    return Offset(
                                        inv * inv * startPt.x + 2f * inv * clamped * controlPt.x + clamped * clamped * endPt.x,
                                        inv * inv * startPt.y + 2f * inv * clamped * controlPt.y + clamped * clamped * endPt.y,
                                    )
                                }
                                val head = pointOnRoute(packetProgress)
                                drawCircle(routeColor.copy(alpha = 0.28f), 6.5.dp.toPx() * invZoom, head)
                                drawCircle(Color.White, 2.4.dp.toPx() * invZoom, head)
                            }
                        }

                        // 3. Pixel-style animated origin marker with a radar sweep.
                        if (originNorm != null) {
                            val startPt = Offset(originNorm.x * w, originNorm.y * h)
                            drawCircle(
                                color = originNodeColor.copy(alpha = 0.25f * (1.5f - pulseRadius).coerceIn(0.2f, 1f)),
                                radius = 16.dp.toPx() * pulseRadius * invZoom,
                                center = startPt,
                            )
                            drawCircle(
                                color = originNodeColor.copy(alpha = 0.58f),
                                radius = 11.dp.toPx() * invZoom,
                                center = startPt,
                                style = Stroke(width = 1.4.dp.toPx() * invZoom),
                            )
                            drawArc(
                                color = Color.White.copy(alpha = 0.92f),
                                startAngle = radarSweepAngle,
                                sweepAngle = 92f,
                                useCenter = false,
                                topLeft = Offset(startPt.x - 12.dp.toPx() * invZoom, startPt.y - 12.dp.toPx() * invZoom),
                                size = Size(24.dp.toPx() * invZoom, 24.dp.toPx() * invZoom),
                                style = Stroke(width = 2.dp.toPx() * invZoom, cap = StrokeCap.Round),
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.22f),
                                radius = 6.8.dp.toPx() * invZoom,
                                center = Offset(startPt.x, startPt.y + 1.dp.toPx() * invZoom),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6.2.dp.toPx() * invZoom,
                                center = startPt,
                            )
                            drawCircle(
                                color = originNodeColor,
                                radius = 4.2.dp.toPx() * invZoom,
                                center = startPt,
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.9f),
                                start = Offset(startPt.x - 2.2.dp.toPx() * invZoom, startPt.y),
                                end = Offset(startPt.x + 2.2.dp.toPx() * invZoom, startPt.y),
                                strokeWidth = 1.2.dp.toPx() * invZoom,
                                cap = StrokeCap.Round,
                            )
                        }

                        // 4. Pixel-style animated exit marker with a protected center.
                        if (exitNorm != null) {
                            val endPt = Offset(exitNorm.x * w, exitNorm.y * h)
                            drawCircle(
                                color = routeColor.copy(alpha = 0.30f * (1.5f - pulseRadius).coerceIn(0.2f, 1f)),
                                radius = 18.dp.toPx() * pulseRadius * invZoom,
                                center = endPt,
                            )
                            drawCircle(
                                color = routeColor.copy(alpha = 0.64f),
                                radius = 12.5.dp.toPx() * invZoom,
                                center = endPt,
                                style = Stroke(width = 1.5.dp.toPx() * invZoom),
                            )
                            drawArc(
                                color = Color.White.copy(alpha = 0.94f),
                                startAngle = radarSweepAngle + 180f,
                                sweepAngle = 118f,
                                useCenter = false,
                                topLeft = Offset(endPt.x - 13.5.dp.toPx() * invZoom, endPt.y - 13.5.dp.toPx() * invZoom),
                                size = Size(27.dp.toPx() * invZoom, 27.dp.toPx() * invZoom),
                                style = Stroke(width = 2.2.dp.toPx() * invZoom, cap = StrokeCap.Round),
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.24f),
                                radius = 7.6.dp.toPx() * invZoom,
                                center = Offset(endPt.x, endPt.y + 1.dp.toPx() * invZoom),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 7.0.dp.toPx() * invZoom,
                                center = endPt,
                            )
                            drawCircle(
                                color = routeColor,
                                radius = 4.8.dp.toPx() * invZoom,
                                center = endPt,
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.92f),
                                start = Offset(endPt.x - 2.3.dp.toPx() * invZoom, endPt.y),
                                end = Offset(endPt.x + 2.3.dp.toPx() * invZoom, endPt.y),
                                strokeWidth = 1.4.dp.toPx() * invZoom,
                                cap = StrokeCap.Round,
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.92f),
                                start = Offset(endPt.x, endPt.y - 2.3.dp.toPx() * invZoom),
                                end = Offset(endPt.x, endPt.y + 2.3.dp.toPx() * invZoom),
                                strokeWidth = 1.4.dp.toPx() * invZoom,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }

                // Top-Start Live Tunnel Telemetry Pill (Origin Flag -> Exit Flag)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF111114).copy(alpha = 0.84f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) statusColors.connected else routeColor),
                    )
                    if (!resolvedOriginCountryCode.isNullOrBlank()) {
                        CountryFlagImage(
                            countryCode = resolvedOriginCountryCode,
                            modifier = Modifier.size(width = 15.dp, height = 11.dp),
                        )
                    }
                    Text(
                        text = "\u200E${resolvedOriginCountryCode ?: "ISP"} → ${resolvedExitCountryCode ?: "VPN"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = fonts.monoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                        ),
                        color = Color.White,
                        maxLines = 1,
                    )
                    if (!resolvedExitCountryCode.isNullOrBlank()) {
                        CountryFlagImage(
                            countryCode = resolvedExitCountryCode,
                            modifier = Modifier.size(width = 15.dp, height = 11.dp),
                        )
                    }
                }

                // Top-End Interactive Zoom Controls (Zoom Level, Country Focus, Zoom Out -, Zoom In +)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Zoom Level Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF111114).copy(alpha = 0.84f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", animatedZoom)}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                            ),
                            color = Color.White,
                        )
                    }

                    // Focus Destination Country Button (No circular flag container)
                    val exitFlagForFocus = resolvedExitCountryCode
                    if (!exitFlagForFocus.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (focusDestination) routeColor.copy(alpha = 0.30f)
                                    else Color(0xFF111114).copy(alpha = 0.78f)
                                )
                                .border(
                                    1.dp,
                                    if (focusDestination) routeColor else Color.White.copy(alpha = 0.24f),
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable {
                                    focusDestination = !focusDestination
                                    targetZoom = if (focusDestination) 2.6f else 1.0f
                                }
                                .padding(horizontal = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CountryFlagImage(
                                countryCode = exitFlagForFocus,
                                modifier = Modifier.size(width = 20.dp, height = 14.dp),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (focusDestination) routeColor
                                    else Color(0xFF111114).copy(alpha = 0.84f)
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                                .clickable {
                                    focusDestination = !focusDestination
                                    targetZoom = if (focusDestination) 2.6f else 1.0f
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Public,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }

                    // Zoom Out (-) Circular Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF111114).copy(alpha = 0.84f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable {
                                val next = (targetZoom - 0.6f).coerceAtLeast(1.0f)
                                targetZoom = next
                                if (next <= 1.05f) focusDestination = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "−",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = Color.White,
                        )
                    }

                    // Zoom In (+) Circular Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF111114).copy(alpha = 0.84f))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                            .clickable {
                                targetZoom = (targetZoom + 0.6f).coerceAtMost(3.4f)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                            color = Color.White,
                        )
                    }
                }

                // Bottom Row: Google Maps 2D Badge & Layer Toggle
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomBetweenRowAlignment)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF111114).copy(alpha = 0.82f))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "Google Maps 2D",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.monoFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                            ),
                            color = Color.White,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF111114).copy(alpha = 0.84f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                            .clickable { googleMapLayerIndex = (googleMapLayerIndex + 1) % 2 }
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        val styleLabel = when (googleMapLayerIndex) {
                            0 -> if (s.language == FoxyLanguage.FA) "نقشه استاندارد گوگل" else "Standard Map"
                            else -> if (s.language == FoxyLanguage.FA) "نقشه طبیعی گوگل" else "Terrain Map"
                        }
                        Text(
                            text = styleLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Two-Column Origin vs Exit Server Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Origin Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(neu.surfaceElevated.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    val originCountryLocalized = resolvedOriginCountryCode?.let {
                        s.language.localizeCountryName(it, it)
                    } ?: s.checking
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = when (s.language) {
                                FoxyLanguage.FA -> "مبدأ"
                                FoxyLanguage.AR -> "المصدر"
                                FoxyLanguage.EN -> "ORIGIN"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = neu.textSecondary,
                            maxLines = 1,
                        )
                        if (resolvedOriginCountryCode != null) {
                            CountryFlagImage(resolvedOriginCountryCode, Modifier.size(width = 18.dp, height = 12.dp))
                        }
                        Text(
                            text = resolvedOriginCountryCode?.let { "${it} · $originCountryLocalized" } ?: originCountryLocalized,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.headingFont, fontWeight = FontWeight.Bold),
                            color = neu.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = formatCompactIpForColumn(originIpText),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = fonts.monoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (originIpText.length > 18) 10.5.sp else 12.sp,
                        ),
                        color = neu.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Exit Server Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(routeColor.copy(alpha = 0.10f))
                        .border(1.dp, routeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable {
                            focusDestination = true
                            targetZoom = 2.6f
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = when (s.language) {
                                FoxyLanguage.FA -> "خروجی"
                                FoxyLanguage.AR -> "الخروج"
                                FoxyLanguage.EN -> "EXIT"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = fonts.headingFont,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = routeColor,
                            maxLines = 1,
                        )
                        if (resolvedExitCountryCode != null) {
                            CountryFlagImage(resolvedExitCountryCode, Modifier.size(width = 18.dp, height = 12.dp))
                        }
                        Text(
                            text = resolvedExitCountryCode?.let { "$it · $exitCountryName" } ?: exitCountryName,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = fonts.headingFont, fontWeight = FontWeight.Bold),
                            color = routeColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = formatCompactIpForColumn(exitIpText),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = fonts.monoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (exitIpText.length > 18) 10.5.sp else 12.sp,
                        ),
                        color = neu.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun formatCompactIpForColumn(ipText: String): String {
    val clean = ipText.removePrefix("\u200E").trim()
    val groups = clean.split(':')
    if (groups.size >= 5 && clean.any { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
        val nonEmpty = groups.filter { it.isNotEmpty() }
        if (nonEmpty.size >= 4) {
            return "\u200E${nonEmpty[0]}:${nonEmpty[1]}::${nonEmpty[nonEmpty.size - 2]}:${nonEmpty.last()}"
        }
    }
    return ipText
}

private val Alignment.Companion.BottomBetweenRowAlignment: Alignment
    get() = Alignment.BottomCenter

private const val GOOGLE_WORLD_DEFAULT_CROP_Y_TOP = 210
private const val GOOGLE_WORLD_CROP_HEIGHT = 548

private val VPN_MAP_CITY_CODES = setOf(
    "fra", "ber", "muc", "dus", "ams", "lon", "man", "mnc", "par", "mrs", "zrh", "vie", "mil", "rom",
    "mad", "bcn", "sto", "mma", "osl", "hel", "waw", "prg", "otp", "buh", "nyc", "ewr", "was", "iad",
    "chi", "ord", "mia", "dal", "dfw", "lax", "sjc", "sfo", "sea", "atl", "den", "tor", "yyz", "van",
    "yvr", "mtl", "yul", "sao", "gru", "sin", "tyo", "nrt", "osa", "hkg", "syd", "mel", "akl",
)

internal fun isKnownRouteMapCityCode(cityCode: String): Boolean = cityCode.trim().lowercase() in VPN_MAP_CITY_CODES

private val GoogleMaps2DTileCache = java.util.concurrent.ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

private suspend fun fetchStandard2DGoogleWorldMap(
    layerCode: String,
    langCode: String,
    cropTop: Int,
): androidx.compose.ui.graphics.ImageBitmap? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    runCatching {
        val fullWorld = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(fullWorld)
        var loadedCount = 0
        for (ty in 0..3) {
            for (tx in 0..3) {
                val urlStr = "https://mt1.google.com/vt/lyrs=$layerCode&hl=$langCode&x=$tx&y=$ty&z=2"
                val tileBmp = runCatching {
                    val conn = (java.net.URL(urlStr).openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 4500
                        readTimeout = 4500
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                    }
                    conn.inputStream.use { stream -> BitmapFactory.decodeStream(stream) }
                }.getOrNull()
                if (tileBmp != null) {
                    canvas.drawBitmap(tileBmp, (tx * 256).toFloat(), (ty * 256).toFloat(), null)
                    tileBmp.recycle()
                    loadedCount++
                }
            }
        }
        if (loadedCount >= 8) {
            val cropped = Bitmap.createBitmap(
                fullWorld,
                0,
                cropTop.coerceIn(0, 1024 - GOOGLE_WORLD_CROP_HEIGHT),
                1024,
                GOOGLE_WORLD_CROP_HEIGHT,
            )
            fullWorld.recycle()
            cropped.asImageBitmap()
        } else {
            fullWorld.recycle()
            null
        }
    }.getOrNull()
}

// Global VPN PoP Backbone Hubs in Mercator normalized coordinates (NY, London, Amsterdam, Frankfurt, Paris, Zurich, Singapore, Tokyo)
private val VPN_BACKBONE_HUBS = listOf(
    Offset(0.294f, 0.344f), // 0: New York (US)
    Offset(0.499f, 0.246f), // 1: London (UK)
    Offset(0.514f, 0.237f), // 2: Amsterdam (NL)
    Offset(0.524f, 0.260f), // 3: Frankfurt (DE)
    Offset(0.506f, 0.273f), // 4: Paris (FR)
    Offset(0.523f, 0.287f), // 5: Zurich (CH)
    Offset(0.643f, 0.402f), // 6: Middle East / Gulf Gateway
    Offset(0.788f, 0.610f), // 7: Singapore (SG)
    Offset(0.888f, 0.388f), // 8: Tokyo (JP)
)

private val VPN_BACKBONE_HUB_LINKS = listOf(
    0 to 1,
    0 to 4,
    1 to 2,
    2 to 3,
    3 to 4,
    3 to 5,
    5 to 6,
    6 to 7,
    7 to 8,
)

// 2D Continental Vector Polygons in normalized Web-Mercator viewport coordinates
private val VPN_WORLD_CONTINENT_POLYGONS: List<List<Offset>> = listOf(
    // North America
    listOf(
        Offset(0.06f, 0.14f), Offset(0.16f, 0.12f), Offset(0.27f, 0.14f),
        Offset(0.34f, 0.22f), Offset(0.32f, 0.34f), Offset(0.27f, 0.42f),
        Offset(0.23f, 0.48f), Offset(0.18f, 0.44f), Offset(0.14f, 0.34f),
        Offset(0.08f, 0.24f),
    ),
    // Greenland
    listOf(
        Offset(0.33f, 0.07f), Offset(0.42f, 0.06f), Offset(0.44f, 0.14f),
        Offset(0.36f, 0.16f),
    ),
    // South America
    listOf(
        Offset(0.26f, 0.50f), Offset(0.33f, 0.52f), Offset(0.38f, 0.60f),
        Offset(0.36f, 0.74f), Offset(0.31f, 0.89f), Offset(0.28f, 0.86f),
        Offset(0.26f, 0.68f),
    ),
    // Europe (Continental + Scandinavia)
    listOf(
        Offset(0.47f, 0.14f), Offset(0.56f, 0.12f), Offset(0.62f, 0.16f),
        Offset(0.61f, 0.31f), Offset(0.55f, 0.35f), Offset(0.49f, 0.34f),
        Offset(0.46f, 0.28f), Offset(0.48f, 0.21f),
    ),
    // UK & Ireland
    listOf(
        Offset(0.475f, 0.21f), Offset(0.503f, 0.21f), Offset(0.505f, 0.26f),
        Offset(0.476f, 0.26f),
    ),
    // Africa
    listOf(
        Offset(0.45f, 0.38f), Offset(0.56f, 0.37f), Offset(0.62f, 0.46f),
        Offset(0.63f, 0.58f), Offset(0.58f, 0.78f), Offset(0.53f, 0.81f),
        Offset(0.49f, 0.64f), Offset(0.44f, 0.50f),
    ),
    // Eurasia & Middle East & Asia
    listOf(
        Offset(0.58f, 0.14f), Offset(0.76f, 0.12f), Offset(0.92f, 0.16f),
        Offset(0.90f, 0.34f), Offset(0.85f, 0.48f), Offset(0.79f, 0.58f),
        Offset(0.72f, 0.54f), Offset(0.67f, 0.46f), Offset(0.60f, 0.44f),
        Offset(0.58f, 0.34f),
    ),
    // Japan
    listOf(
        Offset(0.875f, 0.33f), Offset(0.902f, 0.34f), Offset(0.895f, 0.41f),
        Offset(0.870f, 0.40f),
    ),
    // Southeast Asia & Indonesia
    listOf(
        Offset(0.77f, 0.58f), Offset(0.86f, 0.59f), Offset(0.88f, 0.65f),
        Offset(0.78f, 0.65f),
    ),
    // Australia
    listOf(
        Offset(0.79f, 0.69f), Offset(0.89f, 0.68f), Offset(0.91f, 0.81f),
        Offset(0.81f, 0.83f),
    ),
)

private fun isNormalizedPointOnLand(nx: Float, ny: Float): Boolean {
    for (poly in VPN_WORLD_CONTINENT_POLYGONS) {
        if (isPointInPolygon(nx, ny, poly)) return true
    }
    return false
}

private fun isPointInPolygon(x: Float, y: Float, polygon: List<Offset>): Boolean {
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y
        val intersect = ((yi > y) != (yj > y)) &&
            (x < (xj - xi) * (y - yi) / ((yj - yi).takeIf { kotlin.math.abs(it) > 1e-6f } ?: 1e-6f) + xi)
        if (intersect) inside = !inside
        j = i
    }
    return inside
}

private fun computeRouteDistanceKm(
    originCountryCode: String,
    exitCountryCode: String,
    exitCityCode: String?,
): Int? {
    val (lat1, lon1) = resolveLatLonDeg(originCountryCode, null) ?: return null
    val (lat2, lon2) = resolveLatLonDeg(exitCountryCode, exitCityCode) ?: return null
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return (r * c).toInt().coerceAtLeast(1)
}

internal fun resolveLatLonDeg(countryCode: String, cityCode: String?): Pair<Double, Double>? {
    val cityCoords: Pair<Double, Double>? = when (cityCode?.lowercase()?.trim()) {
        "fra" -> 50.1109 to 8.6821      // Frankfurt
        "ber" -> 52.5200 to 13.4050     // Berlin
        "muc" -> 48.1351 to 11.5820     // Munich
        "dus" -> 51.2277 to 6.7735      // Dusseldorf
        "ams" -> 52.3676 to 4.9041      // Amsterdam
        "lon" -> 51.5074 to -0.1278     // London
        "man", "mnc" -> 53.4808 to -2.2426 // Manchester
        "par" -> 48.8566 to 2.3522      // Paris
        "mrs" -> 43.2965 to 5.3698      // Marseille
        "zrh" -> 47.3769 to 8.5417      // Zurich
        "vie" -> 48.2082 to 16.3738     // Vienna
        "mil" -> 45.4642 to 9.1900      // Milan
        "rom" -> 41.9028 to 12.4964     // Rome
        "mad" -> 40.4168 to -3.7038     // Madrid
        "bcn" -> 41.3874 to 2.1686      // Barcelona
        "sto" -> 59.3293 to 18.0686     // Stockholm
        "mma" -> 55.6050 to 13.0038     // Malmo
        "osl" -> 59.9139 to 10.7522     // Oslo
        "hel" -> 60.1699 to 24.9384     // Helsinki
        "waw" -> 52.2297 to 21.0122     // Warsaw
        "prg" -> 50.0755 to 14.4378     // Prague
        "otp", "buh" -> 44.4268 to 26.1025 // Bucharest
        "nyc", "ewr" -> 40.7128 to -74.0060 // New York
        "was", "iad" -> 38.9072 to -77.0369 // Washington DC
        "chi", "ord" -> 41.8781 to -87.6298 // Chicago
        "mia" -> 25.7617 to -80.1918    // Miami
        "dal", "dfw" -> 32.7767 to -96.7970 // Dallas
        "lax" -> 34.0522 to -118.2437   // Los Angeles
        "sjc", "sfo" -> 37.3382 to -121.8863 // San Jose / SF
        "sea" -> 47.6062 to -122.3321   // Seattle
        "atl" -> 33.7490 to -84.3880    // Atlanta
        "den" -> 39.7392 to -104.9903   // Denver
        "tor", "yyz" -> 43.6532 to -79.3832 // Toronto
        "van", "yvr" -> 49.2827 to -123.1207 // Vancouver
        "mtl", "yul" -> 45.5017 to -73.5673 // Montreal
        "sao", "gru" -> -23.5505 to -46.6333 // Sao Paulo
        "sin" -> 1.3521 to 103.8198     // Singapore
        "tyo", "nrt" -> 35.6762 to 139.6503 // Tokyo
        "osa" -> 34.6937 to 135.5023    // Osaka
        "hkg" -> 22.3193 to 114.1694    // Hong Kong
        "syd" -> -33.8688 to 151.2093   // Sydney
        "mel" -> -37.8136 to 144.9631   // Melbourne
        "akl" -> -36.8485 to 174.7633   // Auckland
        else -> null
    }

    if (!cityCode.isNullOrBlank() && cityCoords == null) return null
    return cityCoords ?: when (countryCode.uppercase()) {
        "US" -> 38.9072 to -77.0369
        "CA" -> 56.1304 to -106.3468
        "MX" -> 23.6345 to -102.5528
        "BR" -> -15.7975 to -47.8919
        "AR" -> -34.6037 to -58.3816
        "CL" -> -33.4489 to -70.6693
        "CO" -> 4.7110 to -74.0721
        "GB", "UK" -> 51.5074 to -0.1278
        "IE" -> 53.3498 to -6.2603
        "FR" -> 48.8566 to 2.3522
        "BE" -> 50.8503 to 4.3517
        "NL" -> 52.3676 to 4.9041
        "DE" -> 50.1109 to 8.6821
        "CH" -> 46.8182 to 8.2275
        "AT" -> 47.5162 to 14.5501
        "IT" -> 41.8719 to 12.5674
        "ES" -> 40.4637 to -3.7492
        "PT" -> 38.7223 to -9.1393
        "SE" -> 59.3293 to 18.0686
        "NO" -> 59.9139 to 10.7522
        "DK" -> 55.6761 to 12.5683
        "FI" -> 60.1699 to 24.9384
        "PL" -> 52.2297 to 21.0122
        "CZ" -> 50.0755 to 14.4378
        "HU" -> 47.4979 to 19.0402
        "RO" -> 44.4268 to 26.1025
        "BG" -> 42.6977 to 23.3219
        "GR" -> 37.9838 to 23.7275
        "UA" -> 50.4501 to 30.5234
        "TR" -> 39.9334 to 32.8597
        "IR" -> 35.6892 to 51.3890
        "IQ" -> 33.3152 to 44.3661
        "SA" -> 24.7136 to 46.6753
        "AE" -> 25.2048 to 55.2708
        "IL" -> 32.0853 to 34.7818
        "EG" -> 30.0444 to 31.2357
        "ZA" -> -26.2041 to 28.0473
        "IN" -> 20.5937 to 78.9629
        "PK" -> 30.3753 to 69.3451
        "SG" -> 1.3521 to 103.8198
        "MY" -> 3.1390 to 101.6869
        "TH" -> 13.7563 to 100.5018
        "ID" -> -6.2088 to 106.8456
        "JP" -> 35.6762 to 139.6503
        "HK" -> 22.3193 to 114.1694
        "TW" -> 25.0330 to 121.5654
        "KR" -> 37.5665 to 126.9780
        "AU" -> -33.8688 to 151.2093
        "NZ" -> -36.8485 to 174.7633
        "RU" -> 55.7558 to 37.6173
        else -> null
    }
}

private fun mercatorYPixel(latDeg: Double): Double {
    val latRad = Math.toRadians(latDeg.coerceIn(-85.0, 85.0))
    return ((1.0 - kotlin.math.ln(kotlin.math.tan(latRad) + 1.0 / kotlin.math.cos(latRad)) / Math.PI) / 2.0) * 1024.0
}

private fun locationToMercatorNorm(countryCode: String, cityCode: String?, cropTop: Int): Offset? {
    val (latDeg, lonDeg) = resolveLatLonDeg(countryCode, cityCode) ?: return null
    val mercX = ((lonDeg + 180.0) / 360.0) * 1024.0
    val mercY = mercatorYPixel(latDeg)
    val normX = (mercX / 1024.0).toFloat().coerceIn(0.03f, 0.97f)
    val normY = ((mercY - cropTop) / GOOGLE_WORLD_CROP_HEIGHT.toDouble()).toFloat().coerceIn(0.02f, 0.98f)
    return Offset(normX, normY)
}
