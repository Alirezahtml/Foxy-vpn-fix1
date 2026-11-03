package com.vauth.foxyvpn.data

import android.net.Network
import android.webkit.CookieManager
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol

import okhttp3.Request

const val MOZILLA_VPN_USER_AGENT: String = "MozillaVPN/2.35.0 (sys:linux; iap:true)"

fun Request.Builder.applyMozillaVpnHeaders(): Request.Builder =
    header("User-Agent", MOZILLA_VPN_USER_AGENT)
        .header("Accept", "application/json")

object ControlPlaneHttp {
    val cookieJar: SimpleCookieJar = SimpleCookieJar()

    @Volatile var socketProtector: ((Socket) -> Unit)? = null

    @Volatile var networkProvider: (() -> Network?)? = null

    @Volatile var tlsSplitModeProvider: (() -> SettingsStore.TlsSplitMode)? = null

    fun underlyingNetwork(): Network? = networkProvider?.invoke()

    val client: OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .socketFactory(ProtectingSocketFactory())
            .dns(
                object : Dns {
                    override fun lookup(hostname: String): List<InetAddress> {
                        val net = underlyingNetwork()
                        if (net != null) {
                            val resolved =
                                runCatching { net.getAllByName(hostname).toList() }.getOrNull()
                            if (!resolved.isNullOrEmpty()) return resolved
                        }
                        return Dns.SYSTEM.lookup(hostname)
                    }
                },
            )
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(35, TimeUnit.SECONDS)
            .build()
}

private class ProtectingSocketFactory : SocketFactory() {
    private val delegate: SocketFactory = getDefault()

    private fun protect(socket: Socket): Socket {
        runCatching { ControlPlaneHttp.underlyingNetwork()?.bindSocket(socket) }
        val protector = ControlPlaneHttp.socketProtector
        if (protector != null) {
            try {
                protector.invoke(socket)
            } catch (e: IOException) {
                runCatching { socket.close() }
                throw e
            } catch (e: Exception) {
                runCatching { socket.close() }
                throw IOException("Failed to protect control-plane socket", e)
            }
        }
        return socket
    }

    override fun createSocket(): Socket = protect(delegate.createSocket())

    override fun createSocket(host: String?, port: Int): Socket =
        protect(Socket()).apply { connect(java.net.InetSocketAddress(host, port)) }

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int,
    ): Socket =
        protect(Socket()).apply {
            bind(java.net.InetSocketAddress(localHost, localPort))
            connect(java.net.InetSocketAddress(host, port))
        }

    override fun createSocket(host: InetAddress?, port: Int): Socket =
        protect(Socket()).apply { connect(java.net.InetSocketAddress(host, port)) }

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int,
    ): Socket =
        protect(Socket()).apply {
            bind(java.net.InetSocketAddress(localAddress, localPort))
            connect(java.net.InetSocketAddress(address, port))
        }
}

class SimpleCookieJar : CookieJar {
    private val store = mutableMapOf<String, Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        importCookies(cookies)
    }

    @Synchronized
    fun importCookies(cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        for (cookie in cookies) {
            val key = "${cookie.domain}|${cookie.hostOnly}|${cookie.path}|${cookie.name}"
            if (cookie.expiresAt <= now) {
                store.remove(key)
            } else {
                store[key] = cookie
            }
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val iter = store.iterator()
        val matched = mutableListOf<Cookie>()
        while (iter.hasNext()) {
            val cookie = iter.next().value
            if (cookie.expiresAt <= now) {
                iter.remove()
            } else if (cookie.matches(url)) {
                matched.add(cookie)
            }
        }
        return matched
    }

    @Synchronized
    fun hasValidCookie(name: String, host: String = "api.accounts.firefox.com"): Boolean {
        val now = System.currentTimeMillis()
        return store.values.any { cookie ->
            cookie.name == name &&
                cookie.expiresAt > now &&
                (cookie.domain == host || host.endsWith(".${cookie.domain}"))
        }
    }

    @Synchronized
    fun syncFromWebView() {
        runCatching {
            val cm = CookieManager.getInstance()
            val urls =
                listOf(
                    "https://accounts.firefox.com",
                    "https://api.accounts.firefox.com",
                    "https://oauth.accounts.firefox.com",
                    "https://vpn.mozilla.org",
                )
            for (urlStr in urls) {
                val cookieHeader = cm.getCookie(urlStr) ?: continue
                val httpUrl = urlStr.toHttpUrl()
                for (pair in cookieHeader.split(";")) {
                    val trimmed = pair.trim()
                    if (trimmed.isEmpty()) continue
                    val eqIdx = trimmed.indexOf('=')
                    if (eqIdx <= 0) continue
                    val name = trimmed.substring(0, eqIdx).trim()
                    val value = trimmed.substring(eqIdx + 1).trim()
                    if (name.isEmpty()) continue
                    val domain =
                        if (httpUrl.host.endsWith("firefox.com")) {
                            "firefox.com"
                        } else {
                            httpUrl.host
                        }
                    val cookie =
                        Cookie.Builder()
                            .name(name)
                            .value(value)
                            .domain(domain)
                            .path("/")
                            .secure()
                            .expiresAt(System.currentTimeMillis() + 3_600_000L)
                            .build()
                    val key = "${cookie.domain}|${cookie.path}|${cookie.name}"
                    store[key] = cookie
                }
            }
        }
    }

    @Synchronized
    fun syncToWebView(url: String, cookieString: String) {
        runCatching {
            val cm = CookieManager.getInstance()
            cm.setAcceptCookie(true)
            cm.setCookie(url, cookieString)
            cm.flush()
        }
    }

    @Synchronized
    fun clear() {
        store.clear()
    }
}
