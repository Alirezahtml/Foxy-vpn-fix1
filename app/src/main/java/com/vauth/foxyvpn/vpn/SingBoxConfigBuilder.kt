package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.SettingsStore
import com.vauth.foxyvpn.data.model.ProxyCandidate
import org.json.JSONArray
import org.json.JSONObject

object SingBoxConfigBuilder {

    const val DEFAULT_CONTROLLER_LISTEN = "127.0.0.1:19090"

    fun build(
        candidate: ProxyCandidate,
        bearerToken: String,
        settings: SettingsStore,
        appPackageName: String,
        protocolOverride: ConnectionProtocolMode? = null,
        controllerListen: String? = null,
    ): String {
        val protocol = protocolOverride ?: settings.connectionProtocolMode
        val effectiveHost =
            settings.effectiveCustomEdgeAddress?.takeIf { it.isNotBlank() } ?: candidate.host
        val serverName = candidate.host

        val root = JSONObject()

        // 1. Log configuration
        root.put(
            "log",
            JSONObject()
                .put("level", "info")
                .put("timestamp", true),
        )

        // 2. Experimental Clash API controller (optional)
        if (!controllerListen.isNullOrBlank()) {
            root.put(
                "experimental",
                JSONObject().put(
                    "clash_api",
                    JSONObject().put("external_controller", controllerListen),
                ),
            )
        }

        // 3. Certificate store
        root.put("certificate", JSONObject().put("store", "mozilla"))

        // 4. DNS configuration
        root.put("dns", buildDnsConfig(settings))

        // 5. Inbounds (TUN + Mixed SOCKS5/HTTP + optional dedicated HTTP proxy port)
        val inbounds = JSONArray()
        if (!settings.proxyOnlyMode) {
            inbounds.put(buildTunInbound(settings, appPackageName))
        }
        val bindAddress =
            if (settings.telegramProxyModeEnabled) {
                SettingsStore.DEFAULT_SOCKS_BIND_ADDRESS
            } else {
                settings.socksBindAddress.ifBlank { SettingsStore.DEFAULT_SOCKS_BIND_ADDRESS }
            }
        inbounds.put(
            JSONObject()
                .put("type", "mixed")
                .put("tag", "mixed-in")
                .put("listen", bindAddress)
                .put("listen_port", settings.socksPort),
        )
        if (settings.httpProxyEnabled && settings.httpProxyPort != settings.socksPort) {
            inbounds.put(
                JSONObject()
                    .put("type", "http")
                    .put("tag", "http-in")
                    .put("listen", bindAddress)
                    .put("listen_port", settings.httpProxyPort),
            )
        }
        root.put("inbounds", inbounds)

        // 6. Outbounds (Direct + optional upstream proxy + Foxy HTTP/MASQUE outbound)
        val outbounds = JSONArray()
        outbounds.put(JSONObject().put("type", "direct").put("tag", "direct-out"))

        val hasUpstreamChaining =
            settings.upstreamProxyEnabled &&
                settings.upstreamProxyHost.isNotBlank() &&
                protocol != ConnectionProtocolMode.HTTP3_QUIC
        if (hasUpstreamChaining) {
            val chainOutbound =
                JSONObject()
                    .put(
                        "type",
                        when (settings.upstreamProxyType) {
                            SettingsStore.UpstreamProxyType.SOCKS5 -> "socks"
                            SettingsStore.UpstreamProxyType.HTTP -> "http"
                        },
                    )
                    .put("tag", "upstream-chain")
                    .put("server", settings.upstreamProxyHost.trim())
                    .put("server_port", settings.upstreamProxyPort)
            if (settings.upstreamProxyUsername.isNotBlank()) {
                chainOutbound.put("username", settings.upstreamProxyUsername)
                chainOutbound.put("password", settings.upstreamProxyPassword)
            }
            outbounds.put(chainOutbound)
        }

        outbounds.put(
            buildProxyOutbound(
                effectiveHost = effectiveHost,
                serverPort = candidate.port,
                serverName = serverName,
                bearerToken = bearerToken,
                settings = settings,
                protocol = protocol,
                detourTag = if (hasUpstreamChaining) "upstream-chain" else null,
            ),
        )
        root.put("outbounds", outbounds)

        // 7. Route configuration
        root.put("route", buildRouteConfig(settings))

        return root.toString()
    }

    private fun buildDnsConfig(settings: SettingsStore): JSONObject {
        val servers = JSONArray()
        servers.put(
            JSONObject()
                .put("type", "local")
                .put("tag", "local-dns")
                .put("detour", "direct-out"),
        )

        val dohUrl = settings.effectiveDohUrl
        var finalDnsTag = "remote-dns"

        if (!dohUrl.isNullOrBlank()) {
            val parsed = runCatching { java.net.URI.create(dohUrl) }.getOrNull()
            val host = parsed?.host?.takeIf { it.isNotBlank() }
            if (host != null) {
                val port = if (parsed.port > 0) parsed.port else 443
                val path = parsed.rawPath?.takeIf { it.isNotBlank() } ?: "/dns-query"
                val dohServer =
                    JSONObject()
                        .put("type", "https")
                        .put("tag", "doh-dns")
                        .put("server", host)
                        .put("server_port", port)
                        .put("path", path)
                        .put("detour", "proxy-out")
                        .put("domain_resolver", "local-dns")
                servers.put(dohServer)
                finalDnsTag = "doh-dns"
            }
        }

        // High-speed remote DNS over TCP via the VPN proxy tunnel (uncensored European resolvers)
        servers.put(
            JSONObject()
                .put("type", "tcp")
                .put("tag", "remote-dns")
                .put("server", "1.1.1.1")
                .put("server_port", 53)
                .put("detour", "proxy-out"),
        )
        servers.put(
            JSONObject()
                .put("type", "tcp")
                .put("tag", "remote-dns-backup")
                .put("server", "8.8.8.8")
                .put("server_port", 53)
                .put("detour", "proxy-out"),
        )

        val strategy = if (settings.ipv6Enabled) "prefer_ipv4" else "ipv4_only"
        return JSONObject()
            .put("servers", servers)
            .put("final", finalDnsTag)
            .put("strategy", strategy)
    }

    private fun buildTunInbound(
        settings: SettingsStore,
        appPackageName: String,
    ): JSONObject {
        val addressArr = JSONArray().put("172.19.0.1/30")
        val dnsAddressArr = JSONArray().put("172.19.0.2")
        if (settings.ipv6Enabled) {
            addressArr.put("fdfe:dcba:9876::1/126")
            dnsAddressArr.put("fdfe:dcba:9876::2")
        }

        val tun =
            JSONObject()
                .put("type", "tun")
                .put("tag", "tun-in")
                .put("stack", "system")
                .put("mtu", settings.effectiveMtu)
                .put("address", addressArr)
                .put("dns_address", dnsAddressArr)
                .put("auto_route", true)
                .put("strict_route", false)

        if (settings.splitTunnelMode == SettingsStore.SplitTunnelMode.INCLUDE &&
            settings.includedApps.isNotEmpty()
        ) {
            val included = JSONArray()
            settings.includedApps
                .filter { it.isNotBlank() && it != appPackageName }
                .sorted()
                .forEach(included::put)
            if (included.length() > 0) {
                tun.put("include_package", included)
                return tun
            }
        }

        val excluded = JSONArray()
        excluded.put(appPackageName)
        settings.excludedApps
            .filter { it.isNotBlank() && it != appPackageName }
            .sorted()
            .forEach(excluded::put)
        tun.put("exclude_package", excluded)
        return tun
    }

    private fun buildProxyOutbound(
        effectiveHost: String,
        serverPort: Int,
        serverName: String,
        bearerToken: String,
        settings: SettingsStore,
        protocol: ConnectionProtocolMode,
        detourTag: String?,
    ): JSONObject {
        val tls =
            JSONObject()
                .put("enabled", true)
                .put("server_name", serverName)

        // uTLS ClientHello fingerprint applies to TCP-based TLS handshakes (HTTP/2, HTTP/1.1, and Auto fallback)
        if (protocol != ConnectionProtocolMode.HTTP3_QUIC) {
            tls.put(
                "utls",
                JSONObject()
                    .put("enabled", true)
                    .put("fingerprint", settings.utlsFingerprint.id),
            )
        }

        // TLS Record & Packet Fragmentation (supported on TCP TLS handshakes; also active for HTTP/1.1)
        val splitMode =
            if (protocol == ConnectionProtocolMode.HTTP11_TLS &&
                settings.tlsSplitMode == SettingsStore.TlsSplitMode.OFF
            ) {
                SettingsStore.TlsSplitMode.FIRST_BYTE
            } else {
                settings.tlsSplitMode
            }

        if (protocol != ConnectionProtocolMode.HTTP3_QUIC) {
            when (splitMode) {
                SettingsStore.TlsSplitMode.OFF -> Unit
                SettingsStore.TlsSplitMode.FIRST_BYTE -> {
                    tls.put("record_fragment", true)
                }
                SettingsStore.TlsSplitMode.SNI -> {
                    tls.put("fragment", true)
                    tls.put("fragment_fallback_delay", settings.fragmentFallbackDelay)
                }
                SettingsStore.TlsSplitMode.COMBINED -> {
                    tls.put("record_fragment", true)
                    tls.put("fragment", true)
                    tls.put("fragment_fallback_delay", settings.fragmentFallbackDelay)
                }
            }
        }

        val outbound =
            JSONObject()
                .put("type", "http")
                .put("tag", "proxy-out")
                .put("server", effectiveHost)
                .put("server_port", serverPort)
                .put("domain_resolver", "local-dns")
                .put("tls", tls)

        if (!detourTag.isNullOrBlank()) {
            outbound.put("detour", detourTag)
        }

        if (settings.tcpFastOpen && protocol != ConnectionProtocolMode.HTTP3_QUIC) {
            outbound.put("tcp_fast_open", true)
        }

        when (protocol) {
            ConnectionProtocolMode.AUTO -> {
                // Races HTTP/3 QUIC first and automatically falls back to HTTP/2 TCP/TLS + MASQUE
                outbound
                    .put("version", 3)
                    .put("disable_version_fallback", false)
                    .put("dynamic_bearer_token", true)
                    .put("max_concurrent_streams", settings.maxConcurrentStreams)
                    .put("idle_timeout", "30s")
                    .put("keep_alive_period", "15s")
            }
            ConnectionProtocolMode.HTTP3_QUIC -> {
                // HTTP/3 over QUIC + MASQUE
                outbound
                    .put("version", 3)
                    .put("disable_version_fallback", false)
                    .put("dynamic_bearer_token", true)
                    .put("idle_timeout", "30s")
                    .put("keep_alive_period", "15s")
            }
            ConnectionProtocolMode.HTTP2_TCP -> {
                // Strict HTTP/2 over TLS/TCP + RFC 9298 connect-udp MASQUE
                outbound
                    .put("version", 2)
                    .put("disable_version_fallback", true)
                    .put("dynamic_bearer_token", true)
                    .put("max_concurrent_streams", settings.maxConcurrentStreams)
                    .put("idle_timeout", "30s")
                    .put("keep_alive_period", "15s")
            }
            ConnectionProtocolMode.HTTP11_TLS -> {
                // HTTP/1.1 CONNECT over TLS/TCP (version=1 in Vauth/sing-box requires static headers and dynamic_bearer_token=false)
                outbound
                    .put("version", 1)
                    .put("disable_version_fallback", true)
                    .put("dynamic_bearer_token", false)
                    .put(
                        "headers",
                        JSONObject().put("Proxy-Authorization", "Bearer $bearerToken"),
                    )
                    .put("idle_timeout", "30s")
                    .put("keep_alive_period", "15s")
            }
        }

        return outbound
    }

    private fun buildRouteConfig(settings: SettingsStore): JSONObject {
        val rules = JSONArray()
        rules.put(JSONObject().put("action", "sniff"))
        rules.put(
            JSONObject()
                .put("protocol", "dns")
                .put("action", "hijack-dns"),
        )
        if (settings.bypassLocalNetwork) {
            rules.put(
                JSONObject()
                    .put("ip_is_private", true)
                    .put("outbound", "direct-out"),
            )
        }
        return JSONObject()
            .put("auto_detect_interface", true)
            .put("rules", rules)
            .put("final", "proxy-out")
    }
}
