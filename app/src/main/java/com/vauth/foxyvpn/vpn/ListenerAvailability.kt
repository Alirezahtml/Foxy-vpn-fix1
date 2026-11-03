package com.vauth.foxyvpn.vpn

import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal object ListenerAvailability {

    fun ensurePortBindable(bindAddress: String, port: Int) {
        require(port in 1..65535) { "Invalid local proxy port: $port" }
        runCatching {
            ServerSocket().use { server ->
                server.reuseAddress = true
                server.bind(InetSocketAddress(bindAddress, port))
            }
        }.onFailure { err ->
            throw IllegalStateException(
                "Local proxy port $bindAddress:$port is already in use: ${err.message}",
                err,
            )
        }
    }

    suspend fun awaitPortReady(
        host: String = "127.0.0.1",
        port: Int,
        timeoutMs: Long = 5_000L,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val targetHost = if (host == "0.0.0.0" || host.isBlank()) "127.0.0.1" else host
            val deadline = System.nanoTime() + timeoutMs * 1_000_000L
            while (System.nanoTime() < deadline) {
                val reachable =
                    runCatching {
                        Socket().use { s ->
                            s.connect(InetSocketAddress(targetHost, port), 350)
                            true
                        }
                    }.getOrDefault(false)
                if (reachable) return@withContext true
                delay(80L)
            }
            false
        }
}
