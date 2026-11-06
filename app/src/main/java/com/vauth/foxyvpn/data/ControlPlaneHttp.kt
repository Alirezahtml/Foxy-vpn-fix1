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

    @Volatile private var pinnedFirefoxIpv4: ByteArray? = null

    fun resetPinnedDns() {
        pinnedFirefoxIpv4 = null
    }

    private fun lookupWithNetwork(hostname: String): List<InetAddress> {
        val net = underlyingNetwork()
        if (net != null) {
            val resolved = runCatching { net.getAllByName(hostname).toList() }.getOrNull()
            if (!resolved.isNullOrEmpty()) return resolved
        }
        return Dns.SYSTEM.lookup(hostname)
    }

    private val FASTLY_ANYCAST_FALLBACK_IPS = listOf(
        "151.101.65.91",
        "151.101.129.91",
        "151.101.193.91",
        "199.232.41.91",
        "146.75.121.91",
        "151.101.2.91",
        "151.101.66.91",
        "151.101.130.91",
        "151.101.194.91",
        "151.101.1.91", // Filtered in Iran on many mobile operators; demoted to end
    )

    internal fun resolveHostAddresses(hostname: String): List<InetAddress> {
        if (hostname == "vpn.mozilla.org") {
            val systemResolved = runCatching { lookupWithNetwork(hostname) }.getOrNull().orEmpty()
            val seen = mutableSetOf<String>()
            val result = mutableListOf<InetAddress>()
            // 1. Put unblocked Fastly Anycast nodes FIRST
            for (fallback in FASTLY_ANYCAST_FALLBACK_IPS) {
                if (fallback != "151.101.1.91" && seen.add(fallback)) {
                    runCatching { InetAddress.getByName(fallback) }.getOrNull()?.let {
                        result.add(InetAddress.getByAddress(hostname, it.address))
                    }
                }
            }
            // 2. Add system-resolved IPs next (skipping 151.101.1.91 for now)
            for (addr in systemResolved) {
                val ip = addr.hostAddress ?: continue
                if (ip != "151.101.1.91" && seen.add(ip)) result.add(addr)
            }
            // 3. Add 151.101.1.91 at the absolute end as last-resort fallback
            if (seen.add("151.101.1.91")) {
                runCatching { InetAddress.getByName("151.101.1.91") }.getOrNull()?.let {
                    result.add(InetAddress.getByAddress(hostname, it.address))
                }
            }
            if (result.isNotEmpty()) return result
        }

        if (hostname == "firefox.com" || hostname.endsWith(".firefox.com")) {
            val cached = pinnedFirefoxIpv4
            if (cached != null) {
                runCatching { InetAddress.getByAddress(hostname, cached) }
                    .getOrNull()
                    ?.let { return listOf(it) }
            }
            val accountsList =
                runCatching { lookupWithNetwork("accounts.firefox.com") }.getOrNull().orEmpty()
            val v4 =
                accountsList.filterIsInstance<java.net.Inet4Address>().firstOrNull()
                    ?: runCatching { lookupWithNetwork(hostname) }
                        .getOrNull()
                        .orEmpty()
                        .filterIsInstance<java.net.Inet4Address>()
                        .firstOrNull()
            if (v4 != null) {
                val raw = v4.address
                pinnedFirefoxIpv4 = raw
                return listOf(InetAddress.getByAddress(hostname, raw))
            }
        }
        return lookupWithNetwork(hostname)
    }

    val client: OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .socketFactory(ProtectingSocketFactory())
            .dns(
                object : Dns {
                    override fun lookup(hostname: String): List<InetAddress> =
                        resolveHostAddresses(hostname)
                },
            )
            .cookieJar(cookieJar)
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(18, TimeUnit.SECONDS)
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
        val byName = linkedMapOf<String, Cookie>()
        while (iter.hasNext()) {
            val cookie = iter.next().value
            if (cookie.expiresAt <= now) {
                iter.remove()
            } else if (cookie.matches(url)) {
                val prev = byName[cookie.name]
                if (prev == null || cookie.expiresAt >= prev.expiresAt) {
                    byName[cookie.name] = cookie
                }
            }
        }
        return byName.values.toList()
    }

    @Synchronized
    fun snapshotAll(): List<Cookie> {
        val now = System.currentTimeMillis()
        val iter = store.iterator()
        val out = mutableListOf<Cookie>()
        while (iter.hasNext()) {
            val cookie = iter.next().value
            if (cookie.expiresAt <= now) {
                iter.remove()
            } else {
                out.add(cookie)
            }
        }
        return out
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
    fun hasValidClearanceCookie(host: String = "api.accounts.firefox.com"): Boolean {
        val now = System.currentTimeMillis()
        return store.values.any { cookie ->
            cookie.name.startsWith("_fs_ch_cp_") &&
                cookie.expiresAt > now + 5_000L &&
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
                    if (name.isEmpty() || name.startsWith("_fs_ch_st_")) continue
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
                    val key = "${cookie.domain}|${cookie.hostOnly}|${cookie.path}|${cookie.name}"
                    store[key] = cookie
                }
            }
        }
    }

    @Synchronized
    fun syncToWebView(cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        runCatching {
            val cm = CookieManager.getInstance()
            cm.setAcceptCookie(true)
            for (cookie in cookies) {
                if (cookie.name.startsWith("_fs_ch_st_")) continue
                val cookieStr =
                    buildString {
                        append(cookie.name).append('=').append(cookie.value)
                        if (!cookie.hostOnly) {
                            append("; Domain=").append(cookie.domain)
                        }
                        append("; Path=").append(cookie.path)
                        if (cookie.secure) append("; Secure")
                        if (cookie.httpOnly) append("; HttpOnly")
                    }
                val targetUrls =
                    if (cookie.domain.endsWith("firefox.com")) {
                        listOf(
                            "https://accounts.firefox.com",
                            "https://api.accounts.firefox.com",
                            "https://oauth.accounts.firefox.com",
                        )
                    } else {
                        listOf("https://${cookie.domain}")
                    }
                for (targetUrl in targetUrls) {
                    cm.setCookie(targetUrl, cookieStr)
                }
            }
            cm.flush()
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
