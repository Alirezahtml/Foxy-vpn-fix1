package com.vauth.foxyvpn.data

import com.vauth.foxyvpn.data.model.AccountDeviceInfo
import com.vauth.foxyvpn.data.model.Entitlement
import com.vauth.foxyvpn.data.model.ProxyPass
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

const val GUARDIAN_ENDPOINT_DEFAULT: String = "https://vpn.mozilla.org"
private const val GUARDIAN_API_BASE = "https://vpn.mozilla.org/api/v1/vpn"
private const val GUARDIAN_ORIGIN = "https://vpn.mozilla.org"
private const val GUARDIAN_USER_AGENT = MOZILLA_VPN_USER_AGENT
private const val TAG = "GuardianClient"

class GuardianClient(
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val challengeSolver: FastlyChallengeSolver = FastlyChallengeSolver(httpClient),
) {
    private val json = Json { ignoreUnknownKeys = true }

    class TokenInvalidError(val code: Int, message: String) : IOException(message)

    suspend fun activateGuardian(accessToken: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$GUARDIAN_API_BASE/account"
                val (code, body) =
                    authorizedRequest(
                        url = url,
                        accessToken = accessToken,
                        method = "POST",
                        jsonBody = "{}",
                    )
                if (code == 401 || code == 403) {
                    throw TokenInvalidError(code, "Guardian rejected token during activation (HTTP $code)")
                }
                if (code !in 200..299 && code != 409) {
                    error("Guardian activation failed (HTTP $code): ${body.take(180)}")
                }
                AppLogger.i(TAG, "Guardian entitlement activated (HTTP $code)")
            }
        }

    suspend fun fetchProxyPass(accessToken: String): Result<ProxyPass> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$GUARDIAN_API_BASE/proxypass"
                var (code, body) = authorizedRequest(url = url, accessToken = accessToken)

                if (code == 401 || code == 403) {
                    AppLogger.i(TAG, "Guardian returned HTTP $code; attempting automatic Guardian activation...")
                    activateGuardian(accessToken).getOrNull()
                    val retry = authorizedRequest(url = url, accessToken = accessToken)
                    code = retry.first
                    body = retry.second
                }

                if (code == 401 || code == 403) {
                    throw TokenInvalidError(
                        code,
                        "Guardian rejected token (HTTP $code). If this account does not have an active subscription yet, verify your Mozilla account or try again.",
                    )
                }
                if (code != 200) {
                    error("Guardian proxypass failed (HTTP $code): ${body.take(180)}")
                }
                val parsed = json.decodeFromString(ProxyPass.serializer(), body)
                if (parsed.token.isBlank()) error("Guardian returned an empty token")
                val epoch =
                    runCatching { Instant.parse(parsed.expiresAt).epochSecond }
                        .getOrElse {
                            error("Guardian returned unparseable expires_at: ${parsed.expiresAt}")
                        }
                val nowEpoch = Instant.now().epochSecond
                if (epoch <= nowEpoch) {
                    error("Guardian returned an already-expired proxy credential")
                }
                parsed.copy(expiresEpochSeconds = epoch)
            }
        }

    suspend fun fetchUserInfo(
        endpoint: String = GUARDIAN_ENDPOINT_DEFAULT,
        accessToken: String,
    ): Entitlement =
        withContext(Dispatchers.IO) {
            val base = endpoint.trimEnd('/').ifBlank { GUARDIAN_ENDPOINT_DEFAULT }
            val url =
                if (base.endsWith("/api/v1/vpn")) "$base/account" else "$base/api/v1/vpn/account"
            var (code, body) = authorizedRequest(url = url, accessToken = accessToken)
            if (code == 401 || code == 403) {
                activateGuardian(accessToken).getOrNull()
                val retry = authorizedRequest(url = url, accessToken = accessToken)
                code = retry.first
                body = retry.second
            }
            if (code == 401 || code == 403) {
                throw TokenInvalidError(code, "Guardian rejected token (HTTP $code)")
            }
            if (code != 200) {
                throw IOException("Guardian account request failed (HTTP $code)")
            }
            val root = JSONObject(body)
            val subObj = root.optJSONObject("subscriptions")?.optJSONObject("vpn")
            val active = subObj?.optBoolean("active", true) ?: true
            val uid = root.optString("fxa_uid", root.optString("uid", ""))
            val maxBytes =
                root.optLong("max_bytes", -1L).takeIf { it > 0L }
                    ?: subObj?.optLong("max_bytes", -1L)?.takeIf { it > 0L }
            val quotaRem =
                root.optLong("quota_remaining", -1L).takeIf { it >= 0L }
                    ?: subObj?.optLong("quota_remaining", -1L)?.takeIf { it >= 0L }
            val limited =
                if (root.has("limited_bandwidth")) {
                    root.optBoolean("limited_bandwidth", false)
                } else {
                    maxBytes != null
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
                val (code, body) = authorizedRequest(url = url, accessToken = accessToken)
                if (code == 401 || code == 403) {
                    throw TokenInvalidError(code, "Guardian rejected token (HTTP $code)")
                }
                if (code != 200) {
                    error("Guardian account request failed (HTTP $code)")
                }
                val root = JSONObject(body)
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
                val (code, _) =
                    authorizedRequest(
                        url = url,
                        accessToken = accessToken,
                        method = "DELETE",
                    )
                if (code !in 200..299 && code != 404) {
                    error("Could not remove device (HTTP $code)")
                }
            }
        }

    private suspend fun authorizedRequest(
        url: String,
        accessToken: String,
        method: String = "GET",
        jsonBody: String? = null,
    ): Pair<Int, String> {
        var attempt = 0
        val maxAttempts = 3
        while (true) {
            attempt++
            val builder =
                Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $accessToken")
                    .header("Accept", "application/json")
                    .header("User-Agent", GUARDIAN_USER_AGENT)
            when (method.uppercase()) {
                "POST" -> builder.post((jsonBody ?: "{}").toRequestBody("application/json".toMediaType()))
                "DELETE" -> builder.delete()
                else -> builder.get()
            }

            val response = httpClient.newCall(builder.build()).await()
            val code = response.code
            val retryAfterHeader = response.header("Retry-After")
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
                        userAgent = GUARDIAN_USER_AGENT,
                    )
                if (solved) {
                    delay(350L)
                    continue
                }
                error(
                    "Guardian service rejected the anti-bot challenge (HTTP 406). Try again later or switch networks."
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

            return code to body
        }
    }
}
