package com.vauth.foxyvpn.vpn

import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

internal object LocalProxyClient {
    private val baseClient: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    fun forSocksPort(
        port: Int,
        host: String = "127.0.0.1",
        timeoutSeconds: Long = 12L,
    ): OkHttpClient {
        val targetHost = if (host == "0.0.0.0" || host.isBlank()) "127.0.0.1" else host
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(targetHost, port))
        return baseClient
            .newBuilder()
            .proxy(proxy)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .callTimeout((timeoutSeconds + 3L).coerceAtLeast(timeoutSeconds), TimeUnit.SECONDS)
            .build()
    }
}
