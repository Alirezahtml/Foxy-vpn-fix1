package com.vauth.foxyvpn.quicksettings

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.MainActivity
import com.vauth.foxyvpn.R
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.TokenStore
import com.vauth.foxyvpn.data.model.ConnectionState
import com.vauth.foxyvpn.vpn.FoxyVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class QuickConnectTileService : TileService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listenJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        listenJob?.cancel()
        val app = applicationContext as? FoxyVpnApp
        listenJob = serviceScope.launch {
            combine(
                FoxyVpnService.state,
                FoxyVpnService.vpnEdgeIp,
                FoxyVpnService.pingLatencyMs,
            ) { state, edgeIp, ping ->
                Triple(state, edgeIp, ping)
            }.collect { (state, edgeIp, ping) ->
                val proxy = app?.proxyStateStore?.load()
                val verifiedExitCode = edgeIp?.takeIf {
                    state == ConnectionState.CONNECTED && it.ip.isNotBlank() &&
                        it.countryCode.matches(Regex("[A-Za-z]{2}"))
                }?.countryCode?.uppercase()
                val selectedCode = proxy?.countryCode
                    ?.takeIf { state == ConnectionState.DISCONNECTED && it.matches(Regex("[A-Za-z]{2}")) }
                    ?.uppercase()
                val countryLabel = when (state) {
                    ConnectionState.CONNECTED -> verifiedExitCode?.let { "Exit $it" }
                    ConnectionState.CONNECTING -> null
                    ConnectionState.DISCONNECTED -> selectedCode?.let { "Server $it" }
                }
                updateTileState(state, countryLabel, ping)
            }
        }
    }

    override fun onStopListening() {
        listenJob?.cancel()
        listenJob = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        when (FoxyVpnService.state.value) {
            ConnectionState.CONNECTED, ConnectionState.CONNECTING -> {
                FoxyVpnService.stop(this)
            }
            ConnectionState.DISCONNECTED -> {
                val app = applicationContext as? FoxyVpnApp
                val tokenStore = app?.tokenStore ?: TokenStore(this)
                if (!tokenStore.hasStoredSession()) {
                    launchMainActivityAndCollapse()
                    return
                }

                val settingsStore = app?.settingsStore ?: SettingsStore(this)
                if (settingsStore.proxyOnlyMode) {
                    FoxyVpnService.start(this)
                    return
                }

                val prepareIntent = VpnService.prepare(this)
                if (prepareIntent != null) {
                    launchMainActivityAndCollapse()
                } else {
                    FoxyVpnService.start(this)
                }
            }
        }
    }

    private fun updateTileState(
        state: ConnectionState,
        serverCountry: String? = null,
        pingMs: Int? = null,
    ) {
        val tile = qsTile ?: return
        tile.icon = Icon.createWithResource(this, R.drawable.ic_foxy_fox_tile)
        tile.label = getString(R.string.app_name)
        when (state) {
            ConnectionState.CONNECTED -> {
                tile.state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val serverPart = serverCountry?.takeIf { it.isNotBlank() }
                    val pingPart = pingMs?.takeIf { it > 0 }?.let { "${it}ms" }
                    tile.subtitle = listOfNotNull(
                        serverPart ?: getString(R.string.quick_connect_connected),
                        pingPart,
                    ).joinToString(" • ")
                }
            }
            ConnectionState.CONNECTING -> {
                tile.state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.quick_connect_connecting)
                }
            }
            ConnectionState.DISCONNECTED -> {
                tile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.quick_connect_disconnected)
                }
            }
        }
        tile.updateTile()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun launchMainActivityAndCollapse() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        listenJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun requestTileUpdate(context: Context) {
            runCatching {
                requestListeningState(
                    context,
                    ComponentName(context, QuickConnectTileService::class.java),
                )
            }
        }
    }
}
