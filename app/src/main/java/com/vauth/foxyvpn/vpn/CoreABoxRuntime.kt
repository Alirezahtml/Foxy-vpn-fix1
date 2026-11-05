package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.model.ProxyCandidate
import io.nekohasekai.libbox.CommandServer
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.OverrideOptions
import io.nekohasekai.libbox.SetupOptions

/** Runtime adapter for the unified v2.0 sing-box/MASQUE core. */
internal class CoreABoxRuntime(
    private val service: FoxyVpnService,
) {
    private var commandServer: CommandServer? = null
    private var platform: FoxyPlatformInterface? = null
    private var active = false
    private var selectedProtocol = ConnectionProtocolMode.AUTO
    private var currentCandidate: ProxyCandidate? = null

    fun start(
        candidate: ProxyCandidate,
        bearerToken: String,
        settings: SettingsStore,
        requested: ConnectionProtocolMode,
    ): ConnectionProtocolMode {
        require(bearerToken.isNotBlank()) { "Sing-Box proxy credential is empty" }
        require(candidate.host.isNotBlank() && candidate.port in 1..65535) {
            "Selected server address is invalid"
        }
        ensureSetup()
        val token = bearerToken.trim()
        Libbox.setBearerToken(token)

        val effective =
            if (requested.isSupported) {
                requested
            } else {
                ConnectionProtocolMode.AUTO
            }
        val config =
            SingBoxConfigBuilder.build(
                candidate = candidate,
                bearerToken = token,
                settings = settings,
                appPackageName = service.packageName,
                protocolOverride = effective,
            )
        Libbox.checkConfig(config)

        val includeOnly =
            settings.splitTunnelMode == SettingsStore.SplitTunnelMode.INCLUDE &&
                settings.includedApps.isNotEmpty()
        val p =
            FoxyPlatformInterface(service, includeOnly = includeOnly) {
                service.markCoreATunnelActive()
            }
        val server = CommandServer(p, p)
        try {
            server.start()
            server.startOrReloadService(config, OverrideOptions())
            platform = p
            commandServer = server
            active = true
            selectedProtocol = effective
            currentCandidate = candidate
            AppLogger.i(TAG, "Sing-Box MASQUE core connected using ${effective.shortBadge} (${candidate.host}:${candidate.port})")
            return effective
        } catch (error: Exception) {
            runCatching { server.closeService() }
            runCatching { server.close() }
            p.closeTun()
            Libbox.setBearerToken("")
            throw error
        }
    }

    fun updateBearerToken(
        newBearerToken: String,
        settings: SettingsStore,
    ) {
        val token = newBearerToken.trim()
        if (token.isBlank()) return
        Libbox.setBearerToken(token)
        // HTTP/1.1 mode embeds the Bearer token in static outbound headers, so reload config seamlessly
        val candidate = currentCandidate
        val server = commandServer
        if (active && selectedProtocol == ConnectionProtocolMode.HTTP11_TLS && candidate != null && server != null) {
            runCatching {
                val updatedConfig =
                    SingBoxConfigBuilder.build(
                        candidate = candidate,
                        bearerToken = token,
                        settings = settings,
                        appPackageName = service.packageName,
                        protocolOverride = selectedProtocol,
                    )
                Libbox.checkConfig(updatedConfig)
                server.startOrReloadService(updatedConfig, OverrideOptions())
            }.onFailure { err ->
                AppLogger.w(TAG, "Failed to reload HTTP/1.1 outbound with rotated bearer token", err)
            }
        }
    }

    fun notifyDefaultInterface() {
        runCatching { platform?.notifyDefaultInterface() }
    }

    fun protocol(): ConnectionProtocolMode = selectedProtocol

    fun isActive(): Boolean = active

    fun close() {
        active = false
        currentCandidate = null
        runCatching { commandServer?.closeService() }
            .onFailure { AppLogger.w(TAG, "sing-box closeService failed", it) }
        runCatching { commandServer?.close() }
            .onFailure { AppLogger.w(TAG, "sing-box command server close failed", it) }
        runCatching { platform?.closeTun() }
        commandServer = null
        platform = null
        Libbox.setBearerToken("")
    }

    private fun ensureSetup() {
        if (setupReady) return
        val base = service.filesDir
        Libbox.setup(
            SetupOptions().apply {
                basePath = base.absolutePath
                workingPath = base.absolutePath
                tempPath = service.cacheDir.absolutePath
                logMaxLines = 5000
                debug = false
            },
        )
        setupReady = true
    }

    companion object {
        private const val TAG = "CoreABoxRuntime"
        @Volatile private var setupReady = false
    }
}
