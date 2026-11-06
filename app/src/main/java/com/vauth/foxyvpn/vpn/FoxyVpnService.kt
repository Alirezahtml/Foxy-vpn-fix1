package com.vauth.foxyvpn.vpn

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.MainActivity
import com.vauth.foxyvpn.R
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.ControlPlaneHttp
import com.vauth.foxyvpn.data.CoreSelection
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.data.GuardianClient
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.formatBytesPerSecond
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.data.model.ProxyCandidate
import com.vauth.foxyvpn.data.model.ProxyPass
import com.vauth.foxyvpn.data.model.TrafficSnapshot
import com.vauth.foxyvpn.quicksettings.DynamicIslandOverlayService
import com.vauth.foxyvpn.quicksettings.QuickConnectTileService
import com.vauth.foxyvpn.widget.QuickConnectWidgetProvider
import java.io.IOException
import java.net.Socket
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val TAG = "FoxyVpnService"
private const val NOTIFICATION_ID = 1001
private const val RENEW_LEAD_SECONDS = 300L
private const val MIN_RENEW_DELAY_SECONDS = 60L
private const val CREDENTIAL_WATCHDOG_PERIOD_MS = 15_000L

data class VpnLiveStats(
    val rxRateBytesPerSec: Long = 0L,
    val txRateBytesPerSec: Long = 0L,
    val totalRxBytes: Long = 0L,
    val totalTxBytes: Long = 0L,
) {
    val sessionBytes: Long
        get() = (totalRxBytes + totalTxBytes).coerceAtLeast(0L)
}

data class CoreExitLocationInfo(
    val countryCode: String = "",
    val cityCode: String = "",
    val cityName: String = "",
)

class FoxyVpnService : VpnService() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            AppLogger.e(TAG, "unhandled coroutine error in VPN service", throwable)
        }
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    private val lifecycleMutex = Mutex()
    private val recoveryBudget = RecoveryBudget()

    private var connectJob: Job? = null
    private var renewJob: Job? = null
    private var credentialWatchdogJob: Job? = null
    private var quotaWatchdogJob: Job? = null
    private var idleWatchdogJob: Job? = null

    private var boxRuntime: CoreABoxRuntime? = null
    private var trafficMonitor: NativeTrafficMonitor? = null
    private var credentialDeadline: CredentialDeadline? = null
    private var activeCandidate: ProxyCandidate? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var registeredNetworkCallback: ConnectivityManager.NetworkCallback? = null

    @Volatile private var currentPass: ProxyPass? = null
    @Volatile private var coreATunnelEstablished = false
    @Volatile private var stoppingIntentionally = false

    @Volatile
    var nativeProtectionRequired: Boolean = true
        private set

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        installControlPlaneHooks()
        registerUnderlyingNetworkMonitor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                serviceScope.launch { disconnectAndStop() }
                return START_NOT_STICKY
            }
            ACTION_RECONNECT -> {
                startForegroundCompat(buildNotification("Reconnecting..."))
                connectJob?.cancel()
                connectJob = serviceScope.launch { reconnectInternal() }
                return START_STICKY
            }
            ACTION_APPLY_NETWORK_CONFIG -> {
                serviceScope.launch { applyNetworkConfigLive() }
                return START_STICKY
            }
            ACTION_SWITCH_PROTOCOL -> {
                serviceScope.launch { switchProtocolLive() }
                return START_STICKY
            }
            ACTION_SWITCH_CORE -> {
                startForegroundCompat(buildNotification("Switching core..."))
                connectJob?.cancel()
                connectJob = serviceScope.launch { reconnectInternal() }
                return START_STICKY
            }
            else -> {
                startForegroundCompat(buildNotification("Connecting..."))
                if (_state.value == ConnectionState.CONNECTED ||
                    _state.value == ConnectionState.CONNECTING
                ) {
                    return START_STICKY
                }
                connectJob?.cancel()
                connectJob = serviceScope.launch { connect() }
                return START_STICKY
            }
        }
    }

    private fun installControlPlaneHooks() {
        ControlPlaneHttp.networkProvider = { firstUsableNetwork() }
        ControlPlaneHttp.socketProtector = { socket: Socket ->
            if (nativeProtectionRequired) {
                if (!protect(socket)) {
                    throw IOException("VpnService.protect(socket) failed")
                }
            }
        }
    }

    private fun registerUnderlyingNetworkMonitor() {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val req =
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                .build()
        val callback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    boxRuntime?.notifyDefaultInterface()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    boxRuntime?.notifyDefaultInterface()
                }

                override fun onLost(network: Network) {
                    boxRuntime?.notifyDefaultInterface()
                }
            }
        runCatching {
            cm.registerNetworkCallback(req, callback)
            registeredNetworkCallback = callback
        }.onFailure {
            AppLogger.w(TAG, "Failed to register network callback", it)
        }
    }

    private fun unregisterUnderlyingNetworkMonitor() {
        val cb = registeredNetworkCallback ?: return
        registeredNetworkCallback = null
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        runCatching { cm.unregisterNetworkCallback(cb) }
    }

    internal fun firstUsableNetwork(): Network? {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return null
        val active = cm.activeNetwork
        if (active != null) {
            val caps = cm.getNetworkCapabilities(active)
            if (caps != null &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                !caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            ) {
                return active
            }
        }
        val all = runCatching { cm.allNetworks }.getOrNull() ?: return null
        var fallbackCellular: Network? = null
        for (net in all) {
            val caps = cm.getNetworkCapabilities(net) ?: continue
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) continue
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) continue
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            ) {
                return net
            }
            if (fallbackCellular == null) {
                fallbackCellular = net
            }
        }
        return fallbackCellular
    }

    internal fun markCoreATunnelActive() {
        coreATunnelEstablished = true
    }

    internal fun stopFromCore(source: FoxyPlatformInterface) {
        if (stoppingIntentionally) return
        AppLogger.w(TAG, "sing-box requested serviceStop ($source)")
        val app = application as? FoxyVpnApp
        if (app?.settingsStore?.autoReconnectEnabled == true && recoveryBudget.tryAcquire()) {
            AppLogger.i(TAG, "Auto-recovering tunnel after core stop...")
            serviceScope.launch { reconnectInternal() }
        } else {
            serviceScope.launch { disconnectAndStop() }
        }
    }

    private suspend fun applyNetworkConfigLive() =
        lifecycleMutex.withLock {
            if (_state.value != ConnectionState.CONNECTED) return@withLock
            AppLogger.i(TAG, "Applying network configuration live...")
            updateNotification("Applying network settings...")
        }

    private suspend fun switchProtocolLive() {
        val (runtimeSnapshot, candidateSnapshot, passSnapshot) =
            lifecycleMutex.withLock {
                if (_state.value != ConnectionState.CONNECTED &&
                    _state.value != ConnectionState.CONNECTING
                ) {
                    return
                }
                Triple(boxRuntime, activeCandidate, currentPass)
            }
        val app = application as FoxyVpnApp
        val settings = app.settingsStore
        val newMode =
            CoreProtocolPolicy.safeMode(settings.coreSelection, settings.connectionProtocolMode)

        if (runtimeSnapshot != null &&
            candidateSnapshot != null &&
            passSnapshot != null &&
            passSnapshot.token.isNotBlank()
        ) {
            AppLogger.i(
                TAG,
                "Hot-switching Sing-Box MASQUE core to ${newMode.shortBadge} (${newMode.coreTransport})...",
            )
            updateNotification("Switching to ${newMode.shortBadge}...")
            val switched =
                runCatching {
                    trafficMonitor?.stop()
                    runtimeSnapshot.close()
                    val freshRuntime = CoreABoxRuntime(this)
                    val effective =
                        freshRuntime.start(
                            candidate = candidateSnapshot,
                            bearerToken = passSnapshot.token,
                            settings = settings,
                            requested = newMode,
                        )
                    lifecycleMutex.withLock {
                        boxRuntime = freshRuntime
                    }
                    _activeProtocol.value = effective.shortBadge
                    _activeProtocolBadge.value = effective.shortBadge
                    startNativeTrafficMonitor()
                    updateNotification(
                        buildStatusText(_connectedCountry.value, _traffic.value),
                    )
                    true
                }.getOrElse { err ->
                    AppLogger.w(
                        TAG,
                        "Hot protocol switch failed (${err.message}); performing full reconnect...",
                    )
                    false
                }
            if (switched) return
        }

        connectJob?.cancel()
        connectJob = serviceScope.launch { reconnectInternal() }
    }

    private suspend fun reconnectInternal() {
        lifecycleMutex.withLock {
            stoppingIntentionally = true
            val detached = detachSessionLocked()
            withContext(NonCancellable) { closeResources(detached) }
            stoppingIntentionally = false
        }
        connect()
    }

    private suspend fun mintProxyPass(app: FoxyVpnApp, forceRefresh: Boolean = false): ProxyPass {
        val now = System.currentTimeMillis() / 1000L
        if (!forceRefresh) {
            val cached = app.tokenStore.loadProxyPass()
            if (cached != null && cached.token.isNotBlank() && cached.expiresEpochSeconds > (now + 120L)) {
                AppLogger.i(TAG, "Reusing valid cached ProxyPass (expires in ${cached.expiresEpochSeconds - now}s)")
                return cached
            }
        }

        var accessToken =
            app.authRepository
                .ensureFreshAccessToken(forceRefresh = false)
                .getOrElse { err ->
                    if (err is FxaAuthRepository.AuthInvalidException) {
                        throw err
                    }
                    val fallback = app.tokenStore.loadProxyPass()
                    if (fallback != null && fallback.token.isNotBlank() && fallback.expiresEpochSeconds > now) {
                        AppLogger.w(TAG, "Access token refresh failed, falling back to cached ProxyPass")
                        return fallback
                    }
                    throw IOException(err.message ?: "Failed to obtain Mozilla access token", err)
                }

        var passResult = app.guardianClient.fetchProxyPass(accessToken)
        if (passResult.isFailure) {
            val firstErr = passResult.exceptionOrNull()
            if (firstErr is GuardianClient.TokenInvalidError) {
                AppLogger.i(
                    TAG,
                    "Guardian rejected cached access token (${firstErr.code}); forcing OAuth token refresh...",
                )
                val refreshedToken =
                    app.authRepository.ensureFreshAccessToken(forceRefresh = true).getOrNull()
                if (!refreshedToken.isNullOrBlank()) {
                    accessToken = refreshedToken
                    runCatching { app.guardianClient.activateGuardian(accessToken) }
                    passResult = app.guardianClient.fetchProxyPass(accessToken)
                }
            }
        }

        val pass = passResult.getOrElse { err ->
            val fallback = app.tokenStore.loadProxyPass()
            if (fallback != null && fallback.token.isNotBlank() && fallback.expiresEpochSeconds > now) {
                AppLogger.w(TAG, "Guardian token fetch failed (${err.message}); utilizing cached ProxyPass fallback")
                return fallback
            }
            if (accessToken.isNotBlank()) {
                AppLogger.w(TAG, "Guardian endpoint unreachable (${err.message}); utilizing direct Mozilla bearer token for MASQUE core")
                val syntheticPass = ProxyPass(
                    token = accessToken,
                    expiresAt = java.time.Instant.ofEpochSecond(now + 86400L).toString(),
                    expiresEpochSeconds = now + 86400L,
                    quotaLimitBytes = null,
                    quotaRemainingBytes = null,
                    quotaResetEpochSeconds = null,
                )
                app.tokenStore.saveProxyPass(syntheticPass)
                return syntheticPass
            }
            throw IOException(err.message ?: "Failed to fetch Guardian proxy pass", err)
        }
        app.tokenStore.saveProxyPass(pass)
        return pass
    }

    private suspend fun connect() =
        lifecycleMutex.withLock {
            if (_state.value == ConnectionState.CONNECTED) return@withLock
            _lastError.value = null
            _exitIp.value = null
            _vpnEdgeIp.value = null
            _vpnExitLocation.value = null
            _traffic.value = TrafficSnapshot()
            _vpnLiveStats.value = VpnLiveStats()
            _connectedEndpoint.value = null
            _connectedSinceElapsedMs.value = null
            _connectedAtEpochMs.value = null
            _pingLatencyMs.value = null
            _idleTimerSeconds.value = 0L

            val appSettings = (application as FoxyVpnApp).settingsStore
            val initialRequestedMode =
                CoreProtocolPolicy.safeMode(
                    appSettings.coreSelection,
                    appSettings.connectionProtocolMode,
                )
            _activeProtocol.value = initialRequestedMode.shortBadge
            _activeProtocolBadge.value = initialRequestedMode.shortBadge
            setState(ConnectionState.CONNECTING)

            val app = application as FoxyVpnApp
            var pendingBoxRuntime: CoreABoxRuntime? = null

            try {
                installControlPlaneHooks()
                val proxyOnlyMode = app.settingsStore.proxyOnlyMode
                nativeProtectionRequired = !proxyOnlyMode

                if (!proxyOnlyMode && prepare(this) != null) {
                    error("VPN permission is not granted. Open FoxyVPN to approve the connection.")
                }

                val bindAddress =
                    if (app.settingsStore.telegramProxyModeEnabled) {
                        SettingsStore.DEFAULT_SOCKS_BIND_ADDRESS
                    } else {
                        app.settingsStore.socksBindAddress.ifBlank {
                            SettingsStore.DEFAULT_SOCKS_BIND_ADDRESS
                        }
                    }
                val socksPort = app.settingsStore.socksPort
                ListenerAvailability.ensurePortBindable(bindAddress, socksPort)
                if (app.settingsStore.httpProxyEnabled &&
                    app.settingsStore.httpProxyPort != socksPort
                ) {
                    ListenerAvailability.ensurePortBindable(
                        bindAddress,
                        app.settingsStore.httpProxyPort,
                    )
                }

                // 1. Pre-tunnel direct IP snapshot (non-blocking best effort)
                if (app.settingsStore.cachedRealIp.isNullOrBlank()) {
                    runCatching {
                        ExitCheck().fetchDirectIpInfo().getOrNull()?.let { directInfo ->
                            app.settingsStore.cachedRealIp = directInfo.ip
                            if (directInfo.countryCode.isNotBlank()) {
                                app.settingsStore.cachedRealCountry = directInfo.countryCode
                            }
                        }
                    }
                }

                // 2. Mint Guardian ProxyPass (with automatic Fastly challenge solver + OAuth refresh + Guardian activation)
                val pass = mintProxyPass(app)
                currentPass = pass
                credentialDeadline = CredentialDeadline(pass.expiresEpochSeconds)

                // 3. Load and select server candidates
                val allProxies = app.serverListClient.loadProxies().getOrThrow()
                val selectedCode = app.proxyStateStore.selectedCountryCode
                val selectedProxyHost = app.proxyStateStore.selectedProxyHost
                val candidates =
                    selectConnectionCandidates(
                        allCandidates = allProxies,
                        selectedCountryCode = selectedCode,
                        selectedProxyHost = selectedProxyHost,
                    )
                if (candidates.isEmpty()) {
                    error("No VPN proxy servers are available in the server list.")
                }

                // 4. Try candidates in order using the unified Sing-Box/MASQUE core
                var connectedCandidate: ProxyCandidate? = null
                var effectiveMode = initialRequestedMode
                var lastCandidateError: Throwable? = null

                for ((index, candidate) in candidates.withIndex()) {
                    coreATunnelEstablished = proxyOnlyMode
                    val runtime = CoreABoxRuntime(this)
                    pendingBoxRuntime = runtime
                    val attemptResult =
                        runCatching {
                            val mode =
                                runtime.start(
                                    candidate = candidate,
                                    bearerToken = pass.token,
                                    settings = app.settingsStore,
                                    requested = initialRequestedMode,
                                )
                            if (!proxyOnlyMode && !coreATunnelEstablished) {
                                error("Sing-Box core did not establish the Android VPN interface")
                            }
                            val listenerReady =
                                ListenerAvailability.awaitPortReady(
                                    host = bindAddress,
                                    port = socksPort,
                                    timeoutMs = 4_500L,
                                )
                            if (!listenerReady) {
                                AppLogger.w(
                                    TAG,
                                    "Mixed SOCKS5 listener on $bindAddress:$socksPort slow to bind; continuing",
                                )
                            }
                            mode
                        }

                    if (attemptResult.isSuccess) {
                        connectedCandidate = candidate
                        effectiveMode = attemptResult.getOrThrow()
                        boxRuntime = runtime
                        pendingBoxRuntime = null
                        break
                    } else {
                        lastCandidateError = attemptResult.exceptionOrNull()
                        AppLogger.w(
                            TAG,
                            "Candidate ${index + 1}/${candidates.size} (${candidate.host}:${candidate.port}) failed: ${lastCandidateError?.message}",
                        )
                        runCatching { runtime.close() }
                        pendingBoxRuntime = null
                    }
                }

                val chosenCandidate =
                    connectedCandidate
                        ?: throw (lastCandidateError
                            ?: IOException("Could not establish tunnel to any candidate server"))

                activeCandidate = chosenCandidate
                activeSocksPort = socksPort
                _activeProtocol.value = effectiveMode.shortBadge
                _activeProtocolBadge.value = effectiveMode.shortBadge
                _connectedEndpoint.value = chosenCandidate.host to chosenCandidate.port

                val initialCountry =
                    chosenCandidate.countryCode.ifBlank { selectedCode ?: "AUTO" }
                _connectedCountry.value = initialCountry
                _vpnExitLocation.value =
                    CoreExitLocationInfo(
                        countryCode = chosenCandidate.countryCode,
                        cityCode = chosenCandidate.cityCode,
                        cityName = chosenCandidate.countryName,
                    )
                _connectedSinceElapsedMs.value = SystemClock.elapsedRealtime()
                _connectedAtEpochMs.value = System.currentTimeMillis()
                recoveryBudget.reset()

                // 5. Start live telemetry & token renewal watchdogs
                startNativeTrafficMonitor()
                startProxyPassRenewal()
                startCredentialExpiryWatchdog()
                startQuotaAndIdleWatchdogs()

                setState(ConnectionState.CONNECTED)
                updateNotification(buildStatusText(_connectedCountry.value, _traffic.value))

                // 6. Background exit IP & country verification + initial latency probe over local SOCKS5 listener
                serviceScope.launch {
                    runCatching {
                        delay(600L)
                        val info = ExitCheck().fetchProxiedIpInfo(socksPort).getOrNull()
                        if (info != null) {
                            _exitIp.value = info.ip
                            _vpnEdgeIp.value = info
                            if (info.countryCode.isNotBlank()) {
                                _connectedCountry.value = info.countryCode
                            }
                            updateNotification(
                                buildStatusText(_connectedCountry.value, _traffic.value),
                            )
                        }
                        pingNow()
                    }
                }
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    runCatching { pendingBoxRuntime?.close() }
                    val detached = detachSessionLocked()
                    closeResources(detached)
                    setState(ConnectionState.DISCONNECTED)
                }
                throw cancelled
            } catch (e: Exception) {
                val message = e.message ?: e.javaClass.simpleName
                AppLogger.e(TAG, "connect failed: $message", e)
                _lastError.value = message
                withContext(NonCancellable) {
                    runCatching { pendingBoxRuntime?.close() }
                    val detached = detachSessionLocked()
                    closeResources(detached)
                    setState(ConnectionState.DISCONNECTED)
                    stopForegroundCompat()
                    stopSelf()
                }
            }
        }

    private fun startNativeTrafficMonitor() {
        trafficMonitor?.stop()
        val monitor =
            NativeTrafficMonitor(
                scope = serviceScope,
                onConnectionCounts = { activeIn, totalOut ->
                    _activeConnectionsCount.value = activeIn
                    _totalConnectionsCount.value = totalOut
                },
            ) { snapshot ->
                _traffic.value = snapshot
                _vpnLiveStats.value =
                    VpnLiveStats(
                        rxRateBytesPerSec = snapshot.downlinkBytesPerSecond,
                        txRateBytesPerSec = snapshot.uplinkBytesPerSecond,
                        totalRxBytes = snapshot.totalDownlinkBytes,
                        totalTxBytes = snapshot.totalUplinkBytes,
                    )
                if (_state.value == ConnectionState.CONNECTED) {
                    updateNotification(
                        buildStatusText(_connectedCountry.value, snapshot),
                    )
                }
            }
        trafficMonitor = monitor
        monitor.start()
    }

    private fun startProxyPassRenewal() {
        renewJob?.cancel()
        val app = application as FoxyVpnApp
        renewJob =
            serviceScope.launch {
                while (isActive) {
                    val pass = currentPass ?: break
                    val now = Instant.now().epochSecond
                    val delaySec =
                        (pass.expiresEpochSeconds - now - RENEW_LEAD_SECONDS).coerceAtLeast(
                            MIN_RENEW_DELAY_SECONDS,
                        )
                    delay(delaySec * 1_000L)

                    var renewed: ProxyPass? = null
                    var backoffMs = 3_000L
                    for (attempt in 1..4) {
                        if (!isActive) break
                        val res = runCatching { mintProxyPass(app, forceRefresh = true) }
                        if (res.isSuccess) {
                            renewed = res.getOrThrow()
                            break
                        }
                        AppLogger.w(
                            TAG,
                            "ProxyPass renewal attempt $attempt/4 failed: ${res.exceptionOrNull()?.message}",
                        )
                        delay(backoffMs)
                        backoffMs = (backoffMs * 2).coerceAtMost(30_000L)
                    }

                    if (renewed != null) {
                        currentPass = renewed
                        credentialDeadline?.update(renewed.expiresEpochSeconds)
                        boxRuntime?.updateBearerToken(renewed.token, app.settingsStore)
                        app.tokenStore.saveProxyPass(renewed)
                        AppLogger.i(TAG, "Rotated proxy bearer token live in Sing-Box core")
                    }
                }
            }
    }

    private fun startCredentialExpiryWatchdog() {
        credentialWatchdogJob?.cancel()
        val app = application as FoxyVpnApp
        credentialWatchdogJob =
            serviceScope.launch {
                while (isActive) {
                    delay(CREDENTIAL_WATCHDOG_PERIOD_MS)
                    val deadline = credentialDeadline ?: continue
                    if (deadline.isExpired()) {
                        AppLogger.w(TAG, "Proxy credential deadline reached; attempting background rotation...")
                        val rotated = runCatching { mintProxyPass(app, forceRefresh = true) }.getOrNull()
                        if (rotated != null) {
                            currentPass = rotated
                            deadline.update(rotated.expiresEpochSeconds)
                            boxRuntime?.updateBearerToken(rotated.token, app.settingsStore)
                            app.tokenStore.saveProxyPass(rotated)
                            AppLogger.i(TAG, "Refreshed credential in background successfully")
                        } else {
                            // Do NOT disconnect! The live tunnel is still open and running on the proxy server.
                            // Defer the next check by 90s to give the network time to resolve without dropping the user.
                            AppLogger.w(TAG, "Background token rotation temporarily unreachable; keeping active tunnel alive")
                            deadline.update(System.currentTimeMillis() / 1000L + 90L)
                        }
                    }
                }
            }
    }

    private fun startQuotaAndIdleWatchdogs() {
        quotaWatchdogJob?.cancel()
        idleWatchdogJob?.cancel()
        val app = application as FoxyVpnApp
        val settings = app.settingsStore

        quotaWatchdogJob =
            serviceScope.launch {
                while (isActive) {
                    delay(10_000L)
                    if (settings.sessionDataLimitEnabled) {
                        val limitBytes = settings.sessionDataLimitMb.toLong() * 1024L * 1024L
                        val snap = _traffic.value
                        val usedBytes = snap.totalUplinkBytes + snap.totalDownlinkBytes
                        if (usedBytes >= limitBytes) {
                            AppLogger.i(
                                TAG,
                                "Session data limit reached (${settings.sessionDataLimitMb} MB); disconnecting",
                            )
                            disconnectAndStop()
                            break
                        }
                    }
                }
            }

        idleWatchdogJob =
            serviceScope.launch {
                var idleSeconds = 0L
                while (isActive) {
                    delay(5_000L)
                    if (!settings.idleDisconnectEnabled) {
                        idleSeconds = 0L
                        _idleTimerSeconds.value = 0L
                        continue
                    }
                    val snap = _traffic.value
                    val totalRate = snap.uplinkBytesPerSecond + snap.downlinkBytesPerSecond
                    if (totalRate < 512L) {
                        idleSeconds += 5L
                        _idleTimerSeconds.value = idleSeconds
                        if (idleSeconds >= settings.idleTimeoutMinutes * 60L) {
                            AppLogger.i(TAG, "Idle timeout reached; disconnecting")
                            disconnectAndStop()
                            break
                        }
                    } else {
                        idleSeconds = 0L
                        _idleTimerSeconds.value = 0L
                    }
                }
            }
    }

    private suspend fun disconnectAndStop() {
        val detached =
            lifecycleMutex.withLock {
                stoppingIntentionally = true
                connectJob?.cancel()
                connectJob = null
                detachSessionLocked()
            }
        withContext(NonCancellable) {
            closeResources(detached)
            setState(ConnectionState.DISCONNECTED)
            stopForegroundCompat()
            stopSelf()
        }
    }

    private data class DetachedSession(
        val renewJob: Job?,
        val credentialWatchdogJob: Job?,
        val quotaWatchdogJob: Job?,
        val idleWatchdogJob: Job?,
        val trafficMonitor: NativeTrafficMonitor?,
        val boxRuntime: CoreABoxRuntime?,
    )

    private fun detachSessionLocked(): DetachedSession {
        val detached =
            DetachedSession(
                renewJob = renewJob,
                credentialWatchdogJob = credentialWatchdogJob,
                quotaWatchdogJob = quotaWatchdogJob,
                idleWatchdogJob = idleWatchdogJob,
                trafficMonitor = trafficMonitor,
                boxRuntime = boxRuntime,
            )
        renewJob = null
        credentialWatchdogJob = null
        quotaWatchdogJob = null
        idleWatchdogJob = null
        trafficMonitor = null
        boxRuntime = null
        currentPass = null
        credentialDeadline = null
        activeCandidate = null
        coreATunnelEstablished = false
        _connectedCountry.value = null
        _exitIp.value = null
        _vpnEdgeIp.value = null
        _vpnExitLocation.value = null
        _traffic.value = TrafficSnapshot()
        _vpnLiveStats.value = VpnLiveStats()
        _connectedEndpoint.value = null
        _connectedSinceElapsedMs.value = null
        _connectedAtEpochMs.value = null
        _pingLatencyMs.value = null
        _idleTimerSeconds.value = 0L
        return detached
    }

    private fun closeResources(detached: DetachedSession) {
        detached.renewJob?.cancel()
        detached.credentialWatchdogJob?.cancel()
        detached.quotaWatchdogJob?.cancel()
        detached.idleWatchdogJob?.cancel()
        runCatching { detached.trafficMonitor?.stop() }
        runCatching { detached.boxRuntime?.close() }
    }

    override fun onRevoke() {
        AppLogger.i(TAG, "VPN permission revoked by system")
        serviceScope.launch { disconnectAndStop() }
        super.onRevoke()
    }

    override fun onDestroy() {
        stoppingIntentionally = true
        connectJob?.cancel()
        val detached = detachSessionLocked()
        closeResources(detached)
        unregisterUnderlyingNetworkMonitor()
        ControlPlaneHttp.socketProtector = null
        ControlPlaneHttp.networkProvider = null
        releaseWakeLock()
        serviceScope.cancel()
        setState(ConnectionState.DISCONNECTED)
        super.onDestroy()
    }

    private fun setState(newState: ConnectionState) {
        _state.value = newState
        runCatching { QuickConnectTileService.requestTileUpdate(this) }
        runCatching { QuickConnectWidgetProvider.updateAll(this, newState) }
        syncDynamicIslandOverlay(newState)
    }

    private fun syncDynamicIslandOverlay(currentState: ConnectionState) {
        runCatching {
            val app = application as? FoxyVpnApp ?: return
            val overlayEnabled =
                app.settingsStore.dynamicIslandEnabled &&
                    app.settingsStore.dynamicIslandOverlayEnabled &&
                    Settings.canDrawOverlays(this)
            if (overlayEnabled && currentState != ConnectionState.DISCONNECTED) {
                DynamicIslandOverlayService.start(this)
            } else if (!overlayEnabled || currentState == ConnectionState.DISCONNECTED) {
                DynamicIslandOverlayService.stop(this)
            }
        }
    }

    private fun buildStatusText(country: String?, snapshot: TrafficSnapshot): String {
        val loc = country?.takeIf { it.isNotBlank() } ?: "Connected"
        val proto = _activeProtocol.value
        val down = formatBytesPerSecond(snapshot.downlinkBytesPerSecond)
        val up = formatBytesPerSecond(snapshot.uplinkBytesPerSecond)
        return "$loc ($proto) • ↓ $down ↑ $up"
    }

    private fun buildNotification(contentText: String): Notification {
        val app = application as? FoxyVpnApp
        val stealth = app?.settingsStore?.stealthNotificationEnabled == true
        val openIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val stopIntent =
            PendingIntent.getService(
                this,
                1,
                Intent(this, FoxyVpnService::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        return NotificationCompat.Builder(this, FoxyVpnApp.VPN_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_foxy_status_round)
            .setContentTitle(if (stealth) "System Network Service" else getString(R.string.app_name))
            .setContentText(if (stealth) "Active" else contentText)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, getString(R.string.disconnect), stopIntent)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java) ?: return
        wakeLock =
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FoxyVPN::TunnelWakeLock").apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L)
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    companion object {
        const val ACTION_START = "com.vauth.foxyvpn.START"
        const val ACTION_STOP = "com.vauth.foxyvpn.STOP"
        const val ACTION_RECONNECT = "com.vauth.foxyvpn.RECONNECT"
        const val ACTION_APPLY_NETWORK_CONFIG = "com.vauth.foxyvpn.APPLY_NETWORK_CONFIG"
        const val ACTION_SWITCH_PROTOCOL = "com.vauth.foxyvpn.SWITCH_PROTOCOL"
        const val ACTION_SWITCH_CORE = "com.vauth.foxyvpn.SWITCH_CORE"

        @Volatile
        var activeSocksPort: Int = SettingsStore.DEFAULT_SOCKS_PORT
            private set

        private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
        val state: StateFlow<ConnectionState> = _state

        private val _connectedCountry = MutableStateFlow<String?>(null)
        val connectedCountry: StateFlow<String?> = _connectedCountry

        private val _exitIp = MutableStateFlow<String?>(null)
        val exitIp: StateFlow<String?> = _exitIp

        private val _vpnEdgeIp = MutableStateFlow<IpLocationInfo?>(null)
        val vpnEdgeIp: StateFlow<IpLocationInfo?> = _vpnEdgeIp

        private val _vpnExitLocation = MutableStateFlow<CoreExitLocationInfo?>(null)
        val vpnExitLocation: StateFlow<CoreExitLocationInfo?> = _vpnExitLocation

        private val _lastError = MutableStateFlow<String?>(null)
        val lastError: StateFlow<String?> = _lastError

        private val _traffic = MutableStateFlow(TrafficSnapshot())
        val traffic: StateFlow<TrafficSnapshot> = _traffic

        private val _vpnLiveStats = MutableStateFlow(VpnLiveStats())
        val vpnLiveStats: StateFlow<VpnLiveStats> = _vpnLiveStats

        private val _connectedEndpoint = MutableStateFlow<Pair<String, Int>?>(null)
        val connectedEndpoint: StateFlow<Pair<String, Int>?> = _connectedEndpoint

        private val _connectedSinceElapsedMs = MutableStateFlow<Long?>(null)
        val connectedSinceElapsedMs: StateFlow<Long?> = _connectedSinceElapsedMs

        private val _connectedAtEpochMs = MutableStateFlow<Long?>(null)
        val connectedAtEpochMs: StateFlow<Long?> = _connectedAtEpochMs

        private val _pingLatencyMs = MutableStateFlow<Int?>(null)
        val pingLatencyMs: StateFlow<Int?> = _pingLatencyMs

        private val _idleTimerSeconds = MutableStateFlow(0L)
        val idleTimerSeconds: StateFlow<Long> = _idleTimerSeconds

        private val _keepAlivePulseCount = MutableStateFlow(0)
        val keepAlivePulseCount: StateFlow<Int> = _keepAlivePulseCount

        private val _lastKeepAliveAtMs = MutableStateFlow<Long?>(null)
        val lastKeepAliveAtMs: StateFlow<Long?> = _lastKeepAliveAtMs

        private val _activeConnectionsCount = MutableStateFlow(0)
        val activeConnectionsCount: StateFlow<Int> = _activeConnectionsCount

        private val _totalConnectionsCount = MutableStateFlow(0)
        val totalConnectionsCount: StateFlow<Int> = _totalConnectionsCount

        private val _activeProtocol = MutableStateFlow("AUTO (H3/H2)")
        val activeProtocol: StateFlow<String> = _activeProtocol

        private val _activeProtocolBadge = MutableStateFlow("AUTO (H3/H2)")
        val activeProtocolBadge: StateFlow<String> = _activeProtocolBadge

        fun clearLastError() {
            _lastError.value = null
        }

        suspend fun pingNow(): Int? {
            val endpoint = _connectedEndpoint.value
            val measured =
                PingUtil.measureRealPingWithFallback(
                    host = endpoint?.first,
                    port = endpoint?.second ?: 443,
                )
            if (measured != null && measured > 0) {
                _pingLatencyMs.value = measured
                _keepAlivePulseCount.value = _keepAlivePulseCount.value + 1
                _lastKeepAliveAtMs.value = System.currentTimeMillis()
            }
            return measured
        }

        fun start(context: Context) {
            val intent = Intent(context, FoxyVpnService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FoxyVpnService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }

        fun reconnect(context: Context) {
            if (_state.value != ConnectionState.CONNECTED &&
                _state.value != ConnectionState.CONNECTING
            ) {
                return
            }
            val intent = Intent(context, FoxyVpnService::class.java).setAction(ACTION_RECONNECT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun applyNetworkConfiguration(context: Context) {
            if (_state.value != ConnectionState.CONNECTED) return
            val intent =
                Intent(context, FoxyVpnService::class.java).setAction(ACTION_APPLY_NETWORK_CONFIG)
            context.startService(intent)
        }

        fun applyRuntimeSettings(context: Context) {
            applyNetworkConfiguration(context)
        }

        fun switchProtocol(context: Context, mode: ConnectionProtocolMode) {
            _activeProtocol.value = mode.shortBadge
            _activeProtocolBadge.value = mode.shortBadge
            if (_state.value != ConnectionState.CONNECTED &&
                _state.value != ConnectionState.CONNECTING
            ) {
                return
            }
            val intent =
                Intent(context, FoxyVpnService::class.java).setAction(ACTION_SWITCH_PROTOCOL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun switchCore(context: Context, @Suppress("UNUSED_PARAMETER") selection: CoreSelection) {
            if (_state.value != ConnectionState.CONNECTED &&
                _state.value != ConnectionState.CONNECTING
            ) {
                return
            }
            val intent = Intent(context, FoxyVpnService::class.java).setAction(ACTION_SWITCH_CORE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
