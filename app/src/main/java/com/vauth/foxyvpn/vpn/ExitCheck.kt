package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.ControlPlaneHttp
import com.vauth.foxyvpn.data.await
import com.vauth.foxyvpn.data.readBoundedUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

private const val EXIT_CHECK_URL = "https://www.cloudflare.com/cdn-cgi/trace"
private const val EXIT_CHECK_TIMEOUT_SECONDS = 15L

data class IpLocationInfo(
    val ip: String,
    val countryCode: String,
)

class ExitCheck {

    suspend fun fetchDirectIpInfo(): Result<IpLocationInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request =
                    Request.Builder()
                        .url(EXIT_CHECK_URL)
                        .header("Cache-Control", "no-cache")
                        .build()
                ControlPlaneHttp.client.newCall(request).await().use { response ->
                    if (!response.isSuccessful) {
                        error("Direct IP check request failed: HTTP ${response.code}")
                    }
                    parseTrace(response.body?.readBoundedUtf8().orEmpty())
                }
            }
        }

    suspend fun fetchProxiedIpInfo(socksPort: Int): Result<IpLocationInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val client =
                    LocalProxyClient.forSocksPort(
                        port = socksPort,
                        timeoutSeconds = EXIT_CHECK_TIMEOUT_SECONDS,
                    )
                val request =
                    Request.Builder()
                        .url(EXIT_CHECK_URL)
                        .header("Cache-Control", "no-cache")
                        .build()
                client.newCall(request).await().use { response ->
                    if (!response.isSuccessful) {
                        error("Proxied exit check failed: HTTP ${response.code}")
                    }
                    parseTrace(response.body?.readBoundedUtf8().orEmpty())
                }
            }
        }

    suspend fun verifyExitCountry(socksPort: Int, expectedCountryCode: String?): Result<String> =
        fetchProxiedIpInfo(socksPort).mapCatching { info ->
            val actualCountry = info.countryCode
            if (expectedCountryCode != null &&
                !expectedCountryCode.equals("REC", ignoreCase = true) &&
                !actualCountry.equals(expectedCountryCode, ignoreCase = true)
            ) {
                error("Exit country mismatch: expected $expectedCountryCode, got $actualCountry")
            }
            actualCountry
        }

    internal fun parseTrace(body: String): IpLocationInfo {
        var ip = ""
        var loc = ""
        for (line in body.lineSequence()) {
            if (line.startsWith("ip=")) {
                ip = line.removePrefix("ip=").trim()
            } else if (line.startsWith("loc=")) {
                loc = line.removePrefix("loc=").trim()
            }
        }
        if (!isIpLiteral(ip)) error("Trace response did not contain a valid egress IP")
        val countryCode = loc.uppercase().takeIf { it.matches(Regex("[A-Z]{2}")) }.orEmpty()
        return IpLocationInfo(ip = ip, countryCode = countryCode)
    }

    private fun isIpLiteral(value: String): Boolean {
        if (value.isBlank()) return false
        if (':' in value) {
            return value.matches(Regex("[0-9a-fA-F:]+")) && value.count { it == ':' } in 2..7
        }
        val octets = value.split('.')
        return octets.size == 4 &&
            octets.all { part ->
                val number = part.toIntOrNull()
                part.isNotEmpty() &&
                    part.length <= 3 &&
                    part.all(Char::isDigit) &&
                    number != null &&
                    number in 0..255
            }
    }
}
