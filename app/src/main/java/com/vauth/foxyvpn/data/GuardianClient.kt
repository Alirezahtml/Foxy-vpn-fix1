package com.vauth.foxyvpn.data

import android.util.Base64
import com.vauth.foxyvpn.data.model.AccountDeviceInfo
import com.vauth.foxyvpn.data.model.Entitlement
import com.vauth.foxyvpn.data.model.ProxyPass
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

const val GUARDIAN_ENDPOINT_DEFAULT: String = "https://vpn.mozilla.org"
private const val GUARDIAN_API_BASE = "https://vpn.mozilla.org/api/v1/vpn"
private const val GUARDIAN_ORIGIN = "https://vpn.mozilla.org"
internal const val GUARDIAN_USER_AGENT: String = "MozillaVPN/2.35.0 (sys:android 14; iap:true)"
private const val TAG = "GuardianClient"

class GuardianClient(
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val challengeSolver: FastlyChallengeSolver = FastlyChallengeSolver(httpClient),
) {
    class TokenInvalidError(val code: Int, message: String) : IOException(message)

    private data class GuardianHttpResponse(
        val code: Int,
        val body: String,
        val quotaLimit: Long?,
        val quotaRemaining: Long?,
        val quotaReset: Long?,
    )

    suspend fun activateGuardian(
        accessToken: String,
        endpoint: String = GUARDIAN_ENDPOINT_DEFAULT,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val base = endpoint.trimEnd('/').ifBlank { GUARDIAN_ENDPOINT_DEFAULT }
                val primaryUrl = "$base/api/v1/fpn/activate"
                var resp =
                    authorizedRequest(
                        url = primaryUrl,
                        accessToken = accessToken,
                        method = "POST",
                        jsonBody = "",
                    )
                if (resp.code == 404) {
                    resp =
                        authorizedRequest(
                            url = "$base/api/v1/vpn/account",
                            accessToken = accessToken,
                            method = "POST",
                            jsonBody = "{}",
                        )
                }
                if (resp.code == 401 || resp.code == 403) {
                    throw TokenInvalidError(
                        resp.code,
                        "Guardian rejected token during activation (HTTP ${resp.code})",
                    )
                }
                if (resp.code !in 200..299 && resp.code != 409) {
                    error("Guardian activation failed (HTTP ${resp.code}): ${resp.body.take(180)}")
                }
                AppLogger.i(TAG, "Guardian entitlement activated (HTTP ${resp.code})")
            }
        }

    suspend fun fetchProxyPass(
        accessToken: String,
        endpoint: String = GUARDIAN_ENDPOINT_DEFAULT,
    ): Result<ProxyPass> =
        withContext(Dispatchers.IO) {
            runCatching {
                val base = endpoint.trimEnd('/').ifBlank { GUARDIAN_ENDPOINT_DEFAULT }
                val fpnTokenUrl = "$base/api/v1/fpn/token"
                val legacyProxyPassUrl = "$base/api/v1/vpn/proxypass"

                var targetUrl = fpnTokenUrl
                var resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
                if (resp.code == 404) {
                    targetUrl = legacyProxyPassUrl
                    resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
                }

                if (resp.code == 401 || resp.code == 403) {
                    AppLogger.i(
                        TAG,
                        "Guardian returned HTTP ${resp.code}; attempting automatic Guardian activation...",
                    )
                    activateGuardian(accessToken, base).getOrNull()
                    resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
                }

                if (resp.code == 401 || resp.code == 403) {
                    throw TokenInvalidError(
                        resp.code,
                        "Guardian rejected token (HTTP ${resp.code}). Verify your Mozilla account or sign in again.",
                    )
                }
                if (resp.code !in 200..299) {
                    error("Guardian token request failed (HTTP ${resp.code}): ${resp.body.take(180)}")
                }

                val json = JSONObject(resp.body)
                val token = json.optString("token").trim()
                if (token.isEmpty()) error("Guardian returned an empty token")

                val expiresEpoch = parseExpiresAtEpochSeconds(json, token)
                val nowEpoch = Instant.now().epochSecond
                val effectiveExpires =
                    if (expiresEpoch > nowEpoch) expiresEpoch else (nowEpoch + 3600L)

                ProxyPass(
                    token = token,
                    expiresAt = Instant.ofEpochSecond(effectiveExpires).toString(),
                    expiresEpochSeconds = effectiveExpires,
                    quotaLimitBytes = resp.quotaLimit,
                    quotaRemainingBytes = resp.quotaRemaining,
                    quotaResetEpochSeconds = resp.quotaReset,
                )
            }
        }

    private fun parseExpiresAtEpochSeconds(json: JSONObject, token: String): Long {
        val numeric = json.optLong("expires_at", 0L)
        if (numeric > 1_000_000_000L) {
            return if (numeric > 100_000_000_000L) numeric / 1000L else numeric
        }
        val rawStr = json.optString("expires_at").trim()
        if (rawStr.isNotEmpty()) {
            val asLong = rawStr.toLongOrNull()
            if (asLong != null && asLong > 1_000_000_000L) {
                return if (asLong > 100_000_000_000L) asLong / 1000L else asLong
            }
            val parsedInstant = runCatching { Instant.parse(rawStr).epochSecond }.getOrNull()
            if (parsedInstant != null && parsedInstant > 0L) {
                return parsedInstant
            }
        }
        return jwtExpiryEpochSeconds(token) ?: (Instant.now().epochSecond + 3600L)
    }

    private fun jwtExpiryEpochSeconds(token: String): Long? {
        val parts = token.split('.')
        if (parts.size < 2) return null
        return runCatching {
            val decoded =
                Base64.decode(
                    parts[1],
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
                )
            val payload = JSONObject(String(decoded, StandardCharsets.UTF_8))
            val exp = payload.optLong("exp", 0L)
            if (exp > 0L) exp else null
        }.getOrNull()
    }

    suspend fun fetchUserInfo(
        endpoint: String = GUARDIAN_ENDPOINT_DEFAULT,
        accessToken: String,
    ): Entitlement =
        withContext(Dispatchers.IO) {
            val base = endpoint.trimEnd('/').ifBlank { GUARDIAN_ENDPOINT_DEFAULT }
            val fpnStatusUrl = "$base/api/v1/fpn/status"
            val legacyAccountUrl =
                if (base.endsWith("/api/v1/vpn")) "$base/account" else "$base/api/v1/vpn/account"

            var targetUrl = fpnStatusUrl
            var resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
            if (resp.code == 404) {
                targetUrl = legacyAccountUrl
                resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
            }
            if (resp.code == 401 || resp.code == 403) {
                activateGuardian(accessToken, base).getOrNull()
                resp = authorizedRequest(url = targetUrl, accessToken = accessToken)
            }
            if (resp.code == 401 || resp.code == 403) {
                throw TokenInvalidError(resp.code, "Guardian rejected token (HTTP ${resp.code})")
            }
            if (resp.code !in 200..299) {
                throw IOException("Guardian status request failed (HTTP ${resp.code})")
            }

            val root = JSONObject(resp.body)
            val userObj = root.optJSONObject("user") ?: root
            val subObj =
                userObj.optJSONObject("subscriptions")?.optJSONObject("vpn")
                    ?: root.optJSONObject("subscriptions")?.optJSONObject("vpn")
            val active =
                when {
                    subObj != null -> subObj.optBoolean("active", true)
                    userObj.has("subscribed") -> userObj.optBoolean("subscribed", true)
                    else -> true
                }
            val uid =
                userObj.optString("fxa_uid", userObj.optString("uid", root.optString("fxa_uid", "")))
            val maxBytes =
                userObj.optLong("max_bytes", -1L).takeIf { it > 0L }
                    ?: subObj?.optLong("max_bytes", -1L)?.takeIf { it > 0L }
                    ?: resp.quotaLimit?.takeIf { it > 0L }
            val quotaRem =
                userObj.optLong("quota_remaining", -1L).takeIf { it >= 0L }
                    ?: subObj?.optLong("quota_remaining", -1L)?.takeIf { it >= 0L }
                    ?: resp.quotaRemaining?.takeIf { it >= 0L }
            val limited =
                when {
                    userObj.has("limited_bandwidth") -> userObj.optBoolean("limited_bandwidth", false)
                    root.has("limited_bandwidth") -> root.optBoolean("limited_bandwidth", false)
                    else -> maxBytes != null
                }
            Entitlement(
                subscribed = active,
                uid = uid,
                maxBytes = maxBytes,
                limitedBandwidth = limited,
                quotaRemaining = quotaRem,
            )
        }

    suspend fun fetchAccountDevices(accessToken: String): Result<List<AccountDeviceInfo>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$GUARDIAN_API_BASE/account"
                val resp = authorizedRequest(url = url, accessToken = accessToken)
                if (resp.code == 401 || resp.code == 403) {
                    throw TokenInvalidError(resp.code, "Guardian rejected token (HTTP ${resp.code})")
                }
                if (resp.code != 200) {
                    error("Guardian account request failed (HTTP ${resp.code})")
                }
                val root = JSONObject(resp.body)
                val devicesArr = root.optJSONArray("devices") ?: JSONArray()
                buildList {
                    for (i in 0 until devicesArr.length()) {
                        val obj = devicesArr.optJSONObject(i) ?: continue
                        val name = obj.optString("name").ifBlank { "Device ${i + 1}" }
                        val pubKey = obj.optString("pubkey")
                        val ipv4 = obj.optString("ipv4_address")
                        val createdAt = obj.optString("created_at")
                        if (pubKey.isNotBlank()) {
                            add(
                                AccountDeviceInfo(
                                    name = name,
                                    publicKey = pubKey,
                                    ipv4Address = ipv4,
                                    createdAt = createdAt,
                                    isCurrentDevice = i == 0,
                                ),
                            )
                        }
                    }
                }
            }
        }

    suspend fun removeDevice(accessToken: String, publicKey: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encodedKey = java.net.URLEncoder.encode(publicKey, "UTF-8")
                val url = "$GUARDIAN_API_BASE/device/$encodedKey"
                val resp =
                    authorizedRequest(
                        url = url,
                        accessToken = accessToken,
                        method = "DELETE",
                    )
                if (resp.code !in 200..299 && resp.code != 404) {
                    error("Could not remove device (HTTP ${resp.code})")
                }
            }
        }

    private suspend fun authorizedRequest(
        url: String,
        accessToken: String,
        method: String = "GET",
        jsonBody: String? = null,
    ): GuardianHttpResponse {
        ControlPlaneHttp.cookieJar.syncFromWebView()
        var attempt = 0
        val maxAttempts = 5
        while (true) {
            attempt++
            val builder =
                Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $accessToken")
                    .header("Accept", "*/*")
                    .header("User-Agent", GUARDIAN_USER_AGENT)
            when (method.uppercase()) {
                "POST" -> {
                    val payload =
                        if (jsonBody == null || jsonBody.isEmpty()) {
                            ByteArray(0).toRequestBody(null)
                        } else {
                            jsonBody.toRequestBody("application/json".toMediaType())
                        }
                    builder.post(payload)
                }
                "DELETE" -> builder.delete()
                else -> builder.get()
            }

            val response = httpClient.newCall(builder.build()).await()
            val code = response.code
            val retryAfterHeader = response.header("Retry-After")
            val quotaLimit = response.header("X-Quota-Limit")?.toLongOrNull()
            val quotaRemaining = response.header("X-Quota-Remaining")?.toLongOrNull()
            val quotaReset = response.header("X-Quota-Reset")?.toLongOrNull()
            val body =
                try {
                    response.body?.readBoundedUtf8().orEmpty()
                } finally {
                    response.close()
                }

            if (code == 406 && attempt < maxAttempts) {
                AppLogger.w(
                    TAG,
                    "Guardian returned HTTP 406 (attempt $attempt/$maxAttempts); solving challenge...",
                )
                val solved =
                    challengeSolver.solveAndInstall(
                        challengedBody = body,
                        challengedOrigin = GUARDIAN_ORIGIN,
                        challengedBaseUrl = GUARDIAN_ORIGIN,
                        userAgent = GUARDIAN_USER_AGENT,
                    )
                if (solved) {
                    delay(350L * attempt)
                    continue
                }
                delay(400L * attempt)
                continue
            }

            if (code == 406) {
                error(
                    "Guardian service rejected the anti-bot challenge (HTTP 406). Try again later or switch networks.",
                )
            }

            if (code == 429 && attempt < maxAttempts) {
                val waitMs = parseRetryAfterMs(retryAfterHeader)
                AppLogger.w(
                    TAG,
                    "Guardian rate-limited (HTTP 429); waiting ${waitMs}ms before retry",
                )
                delay(waitMs)
                continue
            }

            return GuardianHttpResponse(
                code = code,
                body = body,
                quotaLimit = quotaLimit,
                quotaRemaining = quotaRemaining,
                quotaReset = quotaReset,
            )
        }
    }
}
