package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.ControlPlaneHttp
import com.vauth.foxyvpn.data.await
import com.vauth.foxyvpn.data.readBoundedUtf8
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Request

object PingUtil {
    data class PingMeasurement(
        val latencyMs: Int,
        val method: String, // "ICMP", "TCP", or "TLS"
    )

    data class ServerStabilityBenchmark(
        val avgLatencyMs: Int,
        val minLatencyMs: Int,
        val maxLatencyMs: Int,
        val jitterMs: Int,
        val packetLossPercent: Int,
        val successRatePercent: Int,
        val stabilityScore: Int, // 0..100 composite score
        val method: String,
    )

    data class Socks5TunnelVerification(
        val success: Boolean,
        val rttMs: Int?,
        val exitIp: String?,
        val exitCountry: String?,
        val telegramReachable: Boolean,
        val detailMessage: String,
    )

    private const val TIMEOUT_MS = 3_000

    suspend fun measureServerLatency(host: String, port: Int): PingMeasurement? =
        withContext(Dispatchers.IO) {
            val vpnActive =
                FoxyVpnService.state.value != com.vauth.foxyvpn.data.model.ConnectionState.DISCONNECTED
            if (!vpnActive) {
                val icmpResult =
                    runCatching {
                        val addr = InetAddress.getByName(host)
                        val startNs = System.nanoTime()
                        val reachable = addr.isReachable(1_200)
                        val elapsedMs =
                            ((System.nanoTime() - startNs) / 1_000_000.0)
                                .roundToInt()
                                .coerceAtLeast(1)
                        if (reachable && elapsedMs in 2..1_150) {
                            PingMeasurement(latencyMs = elapsedMs, method = "ICMP")
                        } else {
                            null
                        }
                    }.getOrNull()
                if (icmpResult != null) return@withContext icmpResult
            }

            val tcpRtt = measureTcpLatencyMs(host, port)
            if (tcpRtt != null) {
                return@withContext PingMeasurement(latencyMs = tcpRtt, method = "TCP")
            }

            val tlsRtt = measureTlsLatencyMs(host, port)
            if (tlsRtt != null) {
                return@withContext PingMeasurement(latencyMs = tlsRtt, method = "TLS")
            }

            null
        }

    suspend fun benchmarkServerStability(
        host: String,
        port: Int,
        probes: Int = 3,
        delayBetweenMs: Long = 60L,
    ): ServerStabilityBenchmark? =
        withContext(Dispatchers.IO) {
            val totalProbes = probes.coerceIn(2, 12)
            val samples = mutableListOf<Int>()
            var detectedMethod = "TCP"
            for (i in 0 until totalProbes) {
                val m = measureServerLatency(host, port)
                if (m != null) {
                    samples.add(m.latencyMs)
                    detectedMethod = m.method
                }
                if (i < totalProbes - 1) delay(delayBetweenMs)
            }
            if (samples.isEmpty()) return@withContext null

            val avgRtt = samples.average().roundToInt().coerceAtLeast(1)
            val minRtt = samples.minOrNull() ?: avgRtt
            val maxRtt = samples.maxOrNull() ?: avgRtt
            val jitter =
                if (samples.size >= 2) {
                    samples.zipWithNext { a, b -> abs(a - b) }.average().roundToInt()
                } else {
                    0
                }
            val successRate =
                ((samples.size * 100.0) / totalProbes.toDouble()).roundToInt().coerceIn(0, 100)
            val packetLoss = (100 - successRate).coerceIn(0, 100)

            val latencyPenalty = (avgRtt / 8).coerceIn(0, 45)
            val jitterPenalty = (jitter / 3).coerceIn(0, 25)
            val lossPenalty = packetLoss * 0.7
            val stabilityScore =
                (100.0 - latencyPenalty - jitterPenalty - lossPenalty)
                    .roundToInt()
                    .coerceIn(5, 100)

            ServerStabilityBenchmark(
                avgLatencyMs = avgRtt,
                minLatencyMs = minRtt,
                maxLatencyMs = maxRtt,
                jitterMs = jitter,
                packetLossPercent = packetLoss,
                successRatePercent = successRate,
                stabilityScore = stabilityScore,
                method = detectedMethod,
            )
        }

    suspend fun verifySocks5TelegramTunnel(
        socksHost: String,
        socksPort: Int,
    ): Socks5TunnelVerification =
        withContext(Dispatchers.IO) {
            val client =
                LocalProxyClient.forSocksPort(
                    port = socksPort,
                    host = socksHost,
                    timeoutSeconds = 10L,
                )
            val startNs = System.nanoTime()
            val traceRes =
                runCatching {
                    val req =
                        Request.Builder()
                            .url("https://www.cloudflare.com/cdn-cgi/trace")
                            .header("Cache-Control", "no-cache")
                            .build()
                    client.newCall(req).await().use { resp ->
                        if (!resp.isSuccessful) error("HTTP ${resp.code}")
                        resp.body?.readBoundedUtf8().orEmpty()
                    }
                }
            val rttMs =
                ((System.nanoTime() - startNs) / 1_000_000.0).roundToInt().coerceAtLeast(1)
            val body =
                traceRes.getOrElse { err ->
                    return@withContext Socks5TunnelVerification(
                        success = false,
                        rttMs = null,
                        exitIp = null,
                        exitCountry = null,
                        telegramReachable = false,
                        detailMessage = err.message ?: "SOCKS5 upstream connection failed",
                    )
                }
            var ip = ""
            var loc = ""
            for (line in body.lineSequence()) {
                if (line.startsWith("ip=")) ip = line.removePrefix("ip=").trim()
                if (line.startsWith("loc=")) loc = line.removePrefix("loc=").trim()
            }

            val tgOk =
                runCatching {
                    val tgReq =
                        Request.Builder()
                            .url("https://core.telegram.org")
                            .head()
                            .build()
                    client.newCall(tgReq).await().use { resp ->
                        resp.code in 200..499
                    }
                }.getOrDefault(false)

            Socks5TunnelVerification(
                success = ip.isNotBlank(),
                rttMs = rttMs,
                exitIp = ip.ifBlank { null },
                exitCountry = loc.ifBlank { null },
                telegramReachable = tgOk,
                detailMessage =
                    if (tgOk) {
                        "Verified via SOCKS5 ($loc • ${rttMs}ms • Telegram OK)"
                    } else {
                        "Verified via SOCKS5 ($loc • ${rttMs}ms)"
                    },
            )
        }

    suspend fun measureTcpLatencyMs(host: String, port: Int): Int? =
        withContext(Dispatchers.IO) {
            val cleanHost = host.trim()
            if (cleanHost.isEmpty()) return@withContext null

            try {
                Socket().use { socket ->
                    runCatching { ControlPlaneHttp.underlyingNetwork()?.bindSocket(socket) }
                    ControlPlaneHttp.socketProtector?.invoke(socket)
                    val addr =
                        runCatching {
                            val net = ControlPlaneHttp.underlyingNetwork()
                            val ip = net?.getAllByName(cleanHost)?.firstOrNull()
                            if (ip != null) InetSocketAddress(ip, port) else InetSocketAddress(cleanHost, port)
                        }.getOrDefault(InetSocketAddress(cleanHost, port))
                    val start = System.nanoTime()
                    socket.connect(addr, 2_200)
                    ((System.nanoTime() - start) / 1_000_000.0).roundToInt().coerceAtLeast(1)
                }
            } catch (_: Throwable) {
                null
            }
        }

    suspend fun measureRealPingWithFallback(host: String?, port: Int = 443): Int? =
        withContext(Dispatchers.IO) {
            val cleanHost =
                host?.trim()?.takeIf {
                    it.isNotEmpty() && !it.equals("recommended", ignoreCase = true)
                }
            if (cleanHost != null) {
                val primary = measureTcpLatencyMs(cleanHost, port.coerceIn(1, 65535))
                if (primary != null) return@withContext primary
            }
            val activeEndpoint = FoxyVpnService.connectedEndpoint.value
            if (activeEndpoint != null && activeEndpoint.first.isNotBlank()) {
                val activeRtt = measureTcpLatencyMs(activeEndpoint.first, activeEndpoint.second)
                if (activeRtt != null) return@withContext activeRtt
            }
            measureTcpLatencyMs("1.1.1.1", 443)
                ?: measureTcpLatencyMs("8.8.8.8", 443)
                ?: measureTcpLatencyMs("9.9.9.9", 443)
        }

    private fun measureTlsLatencyMs(host: String, port: Int): Int? {
        return try {
            Socket().use { rawSocket ->
                runCatching { ControlPlaneHttp.underlyingNetwork()?.bindSocket(rawSocket) }
                ControlPlaneHttp.socketProtector?.invoke(rawSocket)
                rawSocket.connect(InetSocketAddress(host, port), TIMEOUT_MS)
                rawSocket.soTimeout = TIMEOUT_MS
                val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val start = System.nanoTime()
                (factory.createSocket(rawSocket, host, port, true) as SSLSocket).use { ssl ->
                    ssl.startHandshake()
                }
                ((System.nanoTime() - start) / 1_000_000.0).roundToInt().coerceAtLeast(1)
            }
        } catch (_: Throwable) {
            null
        }
    }
}
