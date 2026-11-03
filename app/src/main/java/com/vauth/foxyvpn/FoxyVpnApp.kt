package com.vauth.foxyvpn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.ControlPlaneHttp
import com.vauth.foxyvpn.data.CrashReporter
import com.vauth.foxyvpn.data.FxaAuthRepository
import com.vauth.foxyvpn.data.GuardianClient
import com.vauth.foxyvpn.data.OfficialServerListWorker
import com.vauth.foxyvpn.data.ProxyStateStore
import com.vauth.foxyvpn.data.ServerListClient
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.TokenStore
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.vpn.FoxyVpnService
import com.vauth.foxyvpn.widget.QuickConnectWidgetProvider
import java.io.File
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FoxyVpnApp : Application() {

    lateinit var tokenStore: TokenStore
    lateinit var proxyStateStore: ProxyStateStore
    lateinit var settingsStore: SettingsStore
    lateinit var guardianClient: GuardianClient
    lateinit var serverListClient: ServerListClient
    lateinit var authRepository: FxaAuthRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        runCatching { CrashReporter.install(this) }
        runCatching { CrashReporter.replayLastCrashIfAny(this) }

        tokenStore = TokenStore(this)
        proxyStateStore = ProxyStateStore(this)
        settingsStore = SettingsStore(this)
        ControlPlaneHttp.tlsSplitModeProvider = {
            runCatching { settingsStore.tlsSplitMode }.getOrDefault(SettingsStore.TlsSplitMode.OFF)
        }
        guardianClient = GuardianClient()
        serverListClient = ServerListClient(this)
        authRepository =
            FxaAuthRepository(
                tokenStore = tokenStore,
                coreDataDir = File(filesDir, "core-a").absolutePath,
                guardianClient = guardianClient,
            )
        runCatching { createNotificationChannel() }
        runCatching { OfficialServerListWorker.schedule(this) }
        startScheduleAutomationLoop()
    }

    private fun startScheduleAutomationLoop() {
        appScope.launch {
            var lastTriggeredStartKey = -1
            var lastTriggeredEndKey = -1
            while (isActive) {
                delay(15_000L)
                runCatching {
                    if (!::settingsStore.isInitialized || !settingsStore.scheduleEnabled) return@runCatching

                    val cal = Calendar.getInstance()
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                    val minuteKey = dayOfYear * 1440 + hour * 60 + minute

                    val startMatch = hour == settingsStore.scheduleStartHour && minute == settingsStore.scheduleStartMinute
                    val endMatch = hour == settingsStore.scheduleEndHour && minute == settingsStore.scheduleEndMinute

                    if (startMatch && lastTriggeredStartKey != minuteKey) {
                        lastTriggeredStartKey = minuteKey
                        if (FoxyVpnService.state.value == ConnectionState.DISCONNECTED && tokenStore.loadAuth() != null) {
                            val canStartWithoutPrompt = settingsStore.proxyOnlyMode || VpnService.prepare(this@FoxyVpnApp) == null
                            if (canStartWithoutPrompt) {
                                AppLogger.i("FoxySchedule", "Scheduled Auto-Connect triggered at $hour:$minute")
                                FoxyVpnService.start(this@FoxyVpnApp)
                            }
                        }
                    }

                    if (endMatch && lastTriggeredEndKey != minuteKey) {
                        lastTriggeredEndKey = minuteKey
                        if (FoxyVpnService.state.value != ConnectionState.DISCONNECTED) {
                            AppLogger.i("FoxySchedule", "Scheduled Auto-Disconnect triggered at $hour:$minute")
                            FoxyVpnService.stop(this@FoxyVpnApp)
                        }
                    }
                }.onFailure {
                    AppLogger.w("FoxySchedule", "Schedule check error: ${it.message}")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            VPN_NOTIFICATION_CHANNEL_ID,
            "VPN status",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching { QuickConnectWidgetProvider.updateAll(this, FoxyVpnService.state.value) }
    }

    companion object {
        const val VPN_NOTIFICATION_CHANNEL_ID = "foxyvpn_status"
    }
}
