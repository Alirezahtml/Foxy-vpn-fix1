package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.AppLogger
import com.vauth.foxyvpn.data.model.TrafficSnapshot
import io.nekohasekai.libbox.CommandClient
import io.nekohasekai.libbox.CommandClientHandler
import io.nekohasekai.libbox.CommandClientOptions
import io.nekohasekai.libbox.ConnectionEvents
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LogIterator
import io.nekohasekai.libbox.OutboundGroupItemIterator
import io.nekohasekai.libbox.OutboundGroupIterator
import io.nekohasekai.libbox.StatusMessage
import io.nekohasekai.libbox.StringIterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class NativeTrafficMonitor(
    private val scope: CoroutineScope,
    private val onConnectionCounts: (activeIn: Int, totalOut: Int) -> Unit = { _, _ -> },
    private val onTrafficUpdate: (TrafficSnapshot) -> Unit,
) : CommandClientHandler {

    companion object {
        private const val TAG = "NativeTrafficMonitor"
    }

    private val rateSampler = TrafficRateSampler()
    private var commandClient: CommandClient? = null
    private var connectJob: Job? = null

    fun start() {
        stop()
        rateSampler.reset()
        connectJob =
            scope.launch {
                val options =
                    CommandClientOptions().apply {
                        addCommand(Libbox.CommandStatus)
                        statusInterval = 1_000_000_000L // 1 second in nanoseconds
                    }
                val client = CommandClient(this@NativeTrafficMonitor, options)
                commandClient = client
                var attempt = 0
                while (isActive && attempt < 6) {
                    attempt++
                    val connected =
                        runCatching {
                            client.connect()
                            true
                        }.getOrElse { err ->
                            AppLogger.d(TAG, "CommandClient connect attempt $attempt failed: ${err.message}")
                            false
                        }
                    if (connected) break
                    delay(300L * attempt)
                }
            }
    }

    fun stop() {
        connectJob?.cancel()
        connectJob = null
        runCatching { commandClient?.disconnect() }
        commandClient = null
        rateSampler.reset()
    }

    override fun connected() {
        AppLogger.d(TAG, "Connected to sing-box status stream")
    }

    override fun disconnected(message: String?) {
        if (!message.isNullOrBlank()) {
            AppLogger.d(TAG, "Disconnected from sing-box status stream: $message")
        }
    }

    override fun writeStatus(message: StatusMessage?) {
        if (message == null) return
        val uplinkTotal = message.uplinkTotal.coerceAtLeast(0L)
        val downlinkTotal = message.downlinkTotal.coerceAtLeast(0L)
        val sampled = rateSampler.sample(uplinkTotal, downlinkTotal)
        val upRate = message.uplink.takeIf { it >= 0L } ?: sampled.uplinkBytesPerSecond
        val downRate = message.downlink.takeIf { it >= 0L } ?: sampled.downlinkBytesPerSecond
        runCatching {
            onConnectionCounts(
                message.connectionsIn.coerceAtLeast(0),
                message.connectionsOut.coerceAtLeast(0),
            )
        }
        onTrafficUpdate(
            TrafficSnapshot(
                uplinkBytesPerSecond = upRate,
                downlinkBytesPerSecond = downRate,
                totalUplinkBytes = uplinkTotal,
                totalDownlinkBytes = downlinkTotal,
            ),
        )
    }

    override fun clearLogs() {}

    override fun writeLogs(messageList: LogIterator?) {}

    override fun setDefaultLogLevel(level: Int) {}

    override fun initializeClashMode(modeList: StringIterator?, currentMode: String?) {}

    override fun updateClashMode(newMode: String?) {}

    override fun writeConnectionEvents(events: ConnectionEvents?) {}

    override fun writeGroups(message: OutboundGroupIterator?) {}

    override fun writeOutbounds(message: OutboundGroupItemIterator?) {}
}
