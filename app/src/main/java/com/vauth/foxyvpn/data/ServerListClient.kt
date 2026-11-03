package com.vauth.foxyvpn.data

import android.content.Context
import com.vauth.foxyvpn.data.model.ProxyCandidate
import com.vauth.foxyvpn.data.model.VpnCity
import com.vauth.foxyvpn.data.model.VpnCountry
import com.vauth.foxyvpn.data.model.VpnProtocol
import com.vauth.foxyvpn.data.model.VpnServerNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

private const val TAG = "ServerListClient"

private const val REMOTE_SETTINGS_URL =
    "https://firefox.settings.services.mozilla.com/v1/buckets/main/collections/vpn-serverlist/records"

const val RECOMMENDED_COUNTRY_CODE = "REC"

private val EXCLUDED_COUNTRY_NAMES = setOf("CatchAll Anycast")

class ServerListClient(
    private val context: Context? = null,
    private val client: OkHttpClient = ControlPlaneHttp.client,
) {

    private val cacheFile: File?
        get() = context?.let { File(it.filesDir, "vpn_serverlist_cache.json") }

    suspend fun fetchCountries(): List<VpnCountry> = withContext(Dispatchers.IO) {
        runCatching {
            refreshFromOfficial().getOrThrow()
            loadCachedCountries()
        }.getOrElse { err ->
            AppLogger.w(TAG, "remote server list unavailable (${err.message}); using cached/official built-in servers")
            loadCachedCountries().ifEmpty { builtInOfficialCountries() }
        }
    }

    suspend fun loadProxies(
        protocolMode: ConnectionProtocolMode = ConnectionProtocolMode.AUTO,
    ): Result<List<ProxyCandidate>> = withContext(Dispatchers.IO) {
        runCatching {
            val countries = loadCachedCountries().ifEmpty { fetchCountries() }
            val candidates = mutableListOf<ProxyCandidate>()
            for (country in countries) {
                for (city in country.cities) {
                    for (server in city.servers) {
                        if (server.quarantined) continue
                        val target = defaultConnectTarget(server, protocolMode) ?: continue
                        candidates.add(
                            ProxyCandidate(
                                host = target.first,
                                port = target.second,
                                countryCode = country.code,
                                countryName = country.name,
                                cityCode = city.code,
                            ),
                        )
                    }
                }
            }
            candidates
        }
    }

    /** Strict manual/worker refresh. An invalid response never replaces a valid cache. */
    suspend fun refreshFromOfficial(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(REMOTE_SETTINGS_URL)
                .applyMozillaVpnHeaders()
                .header("Cache-Control", "no-cache")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Mozilla Remote Settings HTTP ${response.code}")
                val raw = response.body?.string().orEmpty()
                val countries = parseRecords(raw)
                if (countries.isEmpty()) throw IOException("Mozilla returned an empty or invalid server list")
                saveCache(raw)
                AppLogger.i(TAG, "updated official Mozilla server list: ${countries.size} countries")
                countries.size
            }
        }
    }

    fun loadCachedCountries(): List<VpnCountry> {
        val file = cacheFile ?: return builtInOfficialCountries()
        if (!file.exists() || !file.isFile) return builtInOfficialCountries()
        return runCatching {
            val raw = file.readText()
            val parsed = parseRecords(raw)
            if (parsed.isNotEmpty()) {
                AppLogger.d(TAG, "loaded ${parsed.size} countries from local cache")
                parsed
            } else {
                builtInOfficialCountries()
            }
        }.getOrElse {
            AppLogger.w(TAG, "failed to read server list cache", it)
            builtInOfficialCountries()
        }
    }

    private fun saveCache(rawJson: String) {
        val file = cacheFile ?: return
        runCatching {
            val tempFile = File.createTempFile("serverlist", ".tmp", file.parentFile)
            tempFile.writeText(rawJson)
            if (!tempFile.renameTo(file)) {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
            }
            AppLogger.d(TAG, "updated local server list cache")
        }.onFailure {
            AppLogger.w(TAG, "failed to save server list cache", it)
        }
    }

    private fun parseRecords(rawJson: String): List<VpnCountry> {
        if (rawJson.isBlank()) return emptyList()
        val body = JSONObject(rawJson)
        val records = body.optJSONArray("data") ?: JSONArray()
        val countries = mutableListOf<VpnCountry>()
        for (i in 0 until records.length()) {
            val record = records.getJSONObject(i)
            val countryJson = record.optJSONObject("country") ?: record
            val country = parseCountry(countryJson)
            val isExcluded = EXCLUDED_COUNTRY_NAMES.any { it.equals(country.name, ignoreCase = true) }
            if (country.code.isNotBlank() && country.cities.isNotEmpty() && !isExcluded) {
                countries.add(country)
            }
        }
        return countries
    }

    private fun parseCountry(json: JSONObject): VpnCountry = VpnCountry(
        name = json.optString("name"),
        code = json.optString("code"),
        cities = buildList {
            val cities = json.optJSONArray("cities") ?: JSONArray()
            for (i in 0 until cities.length()) add(parseCity(cities.getJSONObject(i)))
        },
    )

    private fun parseCity(json: JSONObject): VpnCity = VpnCity(
        name = json.optString("name"),
        code = json.optString("code"),
        servers = buildList {
            val servers = json.optJSONArray("servers") ?: JSONArray()
            for (i in 0 until servers.length()) add(parseServer(servers.getJSONObject(i)))
        },
    )

    private fun parseServer(json: JSONObject): VpnServerNode = VpnServerNode(
        hostname = json.optString("hostname"),
        port = json.optInt("port", 0),
        quarantined = json.optBoolean("quarantined", false),
        protocols = buildList {
            val protocols = json.optJSONArray("protocols") ?: JSONArray()
            for (i in 0 until protocols.length()) add(parseProtocol(protocols.getJSONObject(i)))
        },
    )

    private fun parseProtocol(json: JSONObject): VpnProtocol = VpnProtocol(
        name = json.optString("name"),
        host = json.optString("host"),
        port = json.optInt("port", 0),
        scheme = json.optString("scheme"),
        templateString = json.optString("templateString"),
    )

    companion object {

        fun defaultConnectTarget(
            server: VpnServerNode,
            protocolMode: ConnectionProtocolMode = ConnectionProtocolMode.AUTO,
        ): Pair<String, Int>? {
            if (protocolMode == ConnectionProtocolMode.HTTP3_QUIC) {
                for (proto in server.protocols) {
                    if (proto.name.equals("connect-udp", ignoreCase = true) ||
                        proto.name.equals("masque", ignoreCase = true) ||
                        proto.name.equals("h3", ignoreCase = true)
                    ) {
                        val host = proto.host.ifBlank { server.hostname }
                        val port = if (proto.port != 0) proto.port else if (server.port != 0) server.port else 443
                        if (host.isNotBlank()) return host to port
                    }
                }
            }
            for (proto in server.protocols) {
                if (proto.name == "connect") {
                    val host = proto.host.ifBlank { server.hostname }
                    val port = if (proto.port != 0) proto.port else if (server.port != 0) server.port else 443
                    return host to port
                }
            }
            if (server.hostname.isNotBlank()) {
                return server.hostname to (if (server.port != 0) server.port else 443)
            }
            return null
        }

        fun candidatesForCountry(
            countries: List<VpnCountry>,
            countryCode: String,
            protocolMode: ConnectionProtocolMode = ConnectionProtocolMode.AUTO,
        ): List<ProxyCandidate> {
            val out = mutableListOf<ProxyCandidate>()
            for (country in countries) {
                if (!country.code.equals(countryCode, ignoreCase = true)) continue
                for (city in country.cities) {
                    for (server in city.servers) {
                        if (server.quarantined) continue
                        val target = defaultConnectTarget(server, protocolMode) ?: continue
                        out.add(
                            ProxyCandidate(
                                host = target.first,
                                port = target.second,
                                countryCode = country.code,
                                countryName = country.name,
                                cityCode = city.code,
                            ),
                        )
                    }
                }
            }
            return out
        }

        fun candidatesForCity(
            countries: List<VpnCountry>,
            countryCode: String,
            cityCode: String,
            protocolMode: ConnectionProtocolMode = ConnectionProtocolMode.AUTO,
        ): List<ProxyCandidate> =
            candidatesForCountry(countries, countryCode, protocolMode).filter {
                it.cityCode.equals(cityCode, ignoreCase = true)
            }

        private fun node(host: String, port: Int = 443): VpnServerNode = VpnServerNode(
            hostname = host,
            port = port,
            quarantined = false,
            protocols = listOf(
                VpnProtocol(name = "connect", host = host, port = port, scheme = "https", templateString = ""),
                VpnProtocol(
                    name = "connect-udp",
                    host = host,
                    port = port,
                    scheme = "https",
                    templateString = "https://$host:$port/.well-known/masque/udp/{target_host}/{target_port}/",
                ),
            ),
        )

        fun builtInOfficialCountries(): List<VpnCountry> = listOf(
            VpnCountry(
                name = "Recommended",
                code = RECOMMENDED_COUNTRY_CODE,
                cities = listOf(
                    VpnCity(
                        name = "Auto Best Edge",
                        code = "auto",
                        servers = listOf(
                            node("de-fra-wg-001.relays.mullvad.net", 443),
                            node("nl-ams-wg-001.relays.mullvad.net", 443),
                            node("us-nyc-wg-301.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Germany",
                code = "DE",
                cities = listOf(
                    VpnCity(
                        name = "Frankfurt",
                        code = "fra",
                        servers = listOf(
                            node("de-fra-wg-001.relays.mullvad.net", 443),
                            node("de-fra-wg-002.relays.mullvad.net", 443),
                            node("de-fra-wg-003.relays.mullvad.net", 443),
                        ),
                    ),
                    VpnCity(
                        name = "Berlin",
                        code = "ber",
                        servers = listOf(
                            node("de-ber-wg-001.relays.mullvad.net", 443),
                            node("de-ber-wg-002.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Netherlands",
                code = "NL",
                cities = listOf(
                    VpnCity(
                        name = "Amsterdam",
                        code = "ams",
                        servers = listOf(
                            node("nl-ams-wg-001.relays.mullvad.net", 443),
                            node("nl-ams-wg-002.relays.mullvad.net", 443),
                            node("nl-ams-wg-003.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "United States",
                code = "US",
                cities = listOf(
                    VpnCity(
                        name = "New York, NY",
                        code = "nyc",
                        servers = listOf(
                            node("us-nyc-wg-301.relays.mullvad.net", 443),
                            node("us-nyc-wg-302.relays.mullvad.net", 443),
                        ),
                    ),
                    VpnCity(
                        name = "Los Angeles, CA",
                        code = "lax",
                        servers = listOf(
                            node("us-lax-wg-401.relays.mullvad.net", 443),
                            node("us-lax-wg-402.relays.mullvad.net", 443),
                        ),
                    ),
                    VpnCity(
                        name = "Miami, FL",
                        code = "mia",
                        servers = listOf(
                            node("us-mia-wg-101.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "United Kingdom",
                code = "GB",
                cities = listOf(
                    VpnCity(
                        name = "London",
                        code = "lon",
                        servers = listOf(
                            node("gb-lon-wg-001.relays.mullvad.net", 443),
                            node("gb-lon-wg-002.relays.mullvad.net", 443),
                        ),
                    ),
                    VpnCity(
                        name = "Manchester",
                        code = "mnc",
                        servers = listOf(
                            node("gb-mnc-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "France",
                code = "FR",
                cities = listOf(
                    VpnCity(
                        name = "Paris",
                        code = "par",
                        servers = listOf(
                            node("fr-par-wg-001.relays.mullvad.net", 443),
                            node("fr-par-wg-002.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Switzerland",
                code = "CH",
                cities = listOf(
                    VpnCity(
                        name = "Zurich",
                        code = "zrh",
                        servers = listOf(
                            node("ch-zrh-wg-001.relays.mullvad.net", 443),
                            node("ch-zrh-wg-002.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Sweden",
                code = "SE",
                cities = listOf(
                    VpnCity(
                        name = "Stockholm",
                        code = "sto",
                        servers = listOf(
                            node("se-sto-wg-001.relays.mullvad.net", 443),
                            node("se-sto-wg-002.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Finland",
                code = "FI",
                cities = listOf(
                    VpnCity(
                        name = "Helsinki",
                        code = "hel",
                        servers = listOf(
                            node("fi-hel-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Austria",
                code = "AT",
                cities = listOf(
                    VpnCity(
                        name = "Vienna",
                        code = "vie",
                        servers = listOf(
                            node("at-vie-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Canada",
                code = "CA",
                cities = listOf(
                    VpnCity(
                        name = "Toronto",
                        code = "tor",
                        servers = listOf(
                            node("ca-tor-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Singapore",
                code = "SG",
                cities = listOf(
                    VpnCity(
                        name = "Singapore",
                        code = "sin",
                        servers = listOf(
                            node("sg-sin-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
            VpnCountry(
                name = "Japan",
                code = "JP",
                cities = listOf(
                    VpnCity(
                        name = "Tokyo",
                        code = "tyo",
                        servers = listOf(
                            node("jp-tyo-wg-001.relays.mullvad.net", 443),
                        ),
                    ),
                ),
            ),
        )
    }
}
