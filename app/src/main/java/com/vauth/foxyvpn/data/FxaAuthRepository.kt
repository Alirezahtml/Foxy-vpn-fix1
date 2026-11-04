package com.vauth.foxyvpn.data

import android.net.Uri
import android.util.Base64
import com.vauth.foxyvpn.data.model.AuthTokens
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import java.time.Instant
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val FXA_AUTH_SERVER = "https://api.accounts.firefox.com/v1"
private const val FXA_OAUTH_SERVER = "https://oauth.accounts.firefox.com/v1"
private const val FXA_CONTENT_SERVER = "https://accounts.firefox.com"
private const val FXA_CLIENT_ID = "5882386c6d801776"
private const val FXA_LEGACY_CLIENT_ID = "e6eb0d1e856335fc"
private const val FXA_SCOPE = "profile https://identity.mozilla.com/apps/vpn"
private const val FXA_LEGACY_SCOPE = "profile:email profile:uid"
private const val SESSION_TOKEN_BYTES = 32
private const val TAG = "FxaAuth"

enum class SessionStatus {
    ACTIVE,
    UNREACHABLE,
    NEEDS_LOGIN,
}

class FxaAuthRepository(
    private val tokenStore: TokenStore,
    private val coreDataDir: String = "",
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val challengeSolver: FastlyChallengeSolver = FastlyChallengeSolver(httpClient),
    private val guardianClient: GuardianClient = GuardianClient(httpClient, challengeSolver),
    private val onAuthUpdated: suspend (String, AuthTokens) -> Unit = { _, _ -> },
) {
    class AuthInvalidException(message: String, cause: Throwable? = null) :
        IOException(message, cause)

    @Volatile
    var pendingSessionToken: String? = null
        private set

    @Volatile
    var pendingEmail: String? = null
        private set

    @Volatile
    private var pendingVerificationMethod: String? = null

    fun isSignedIn(): Boolean = tokenStore.loadAuth() != null

    fun currentEmail(): String? = tokenStore.loadEmail()

    suspend fun currentAccessToken(): String? =
        ensureFreshAccessToken(forceRefresh = false).getOrNull()
            ?: tokenStore.loadAuth()?.accessToken

    suspend fun restoreSession(): SessionStatus {
        val stored = tokenStore.loadAuth() ?: return SessionStatus.NEEDS_LOGIN
        if (stored.accessToken.isBlank()) return SessionStatus.NEEDS_LOGIN
        val refreshed = ensureFreshAccessToken(forceRefresh = false)
        return refreshed.fold(
            onSuccess = { SessionStatus.ACTIVE },
            onFailure = { err ->
                if (err is AuthInvalidException) {
                    SessionStatus.NEEDS_LOGIN
                } else {
                    SessionStatus.UNREACHABLE
                }
            },
        )
    }

    fun clearPendingSession() {
        pendingSessionToken = null
        pendingEmail = null
        pendingVerificationMethod = null
    }

    fun getBrowserLoginUrl(): String =
        Uri.parse("$FXA_CONTENT_SERVER/")
            .buildUpon()
            .appendQueryParameter("context", "fx_desktop_v3")
            .appendQueryParameter("service", "sync")
            .appendQueryParameter("action", "email")
            .build()
            .toString()

    suspend fun startLogin(rawEmail: String, rawPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                val email = normalizeEmail(rawEmail)
                val password = normalizePassword(rawPassword)
                require(email.isNotBlank() && email.contains("@")) { "Enter a valid email address" }
                require(password.isNotEmpty()) { "Enter your password" }

                val authPW = stretchPassword(email, password)
                val payload =
                    JSONObject()
                        .put("email", email)
                        .put("authPW", toHex(authPW))
                        .put("reason", "signin")
                        .put("service", "sync")
                        .put("verificationMethod", "email-otp")

                val resp =
                    fxaRequest(
                        url = "$FXA_AUTH_SERVER/account/login?keys=true",
                        method = "POST",
                        jsonBody = payload.toString(),
                        hawkTokenHex = null,
                    )

                val sessionToken = resp.optString("sessionToken")
                if (sessionToken.length != SESSION_TOKEN_BYTES * 2) {
                    error("Account server did not return a valid session token")
                }
                val verified = resp.optBoolean("verified", true)
                val verificationMethod = resp.optString("verificationMethod")
                val verificationReason = resp.optString("verificationReason")

                pendingSessionToken = sessionToken
                pendingEmail = email
                pendingVerificationMethod = verificationMethod.ifBlank { "email-otp" }

                if (!verified || verificationReason.isNotBlank()) {
                    if (verificationMethod == "email-2fa" ||
                        verificationMethod == "email-otp" ||
                        verificationReason == "login" ||
                        verificationReason == "signup"
                    ) {
                        runCatching { sendSessionVerifyCode(sessionToken) }
                    }
                    true
                } else {
                    finalizeSessionSignIn(sessionToken, email)
                    false
                }
            }
        }

    suspend fun startSignUp(rawEmail: String, rawPassword: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                val email = normalizeEmail(rawEmail)
                val password = normalizePassword(rawPassword)
                require(email.isNotBlank() && email.contains("@")) { "Enter a valid email address" }
                require(password.length >= 8) { "Password must be at least 8 characters" }

                val authPW = stretchPassword(email, password)
                val payload =
                    JSONObject()
                        .put("email", email)
                        .put("authPW", toHex(authPW))
                        .put("service", "sync")
                        .put("verificationMethod", "email-otp")

                val resp =
                    try {
                        fxaRequest(
                            url = "$FXA_AUTH_SERVER/account/create?keys=true",
                            method = "POST",
                            jsonBody = payload.toString(),
                            hawkTokenHex = null,
                        )
                    } catch (e: FxaHttpException) {
                        if (e.errno == 101) {
                            return@withContext startLogin(email, password)
                        }
                        throw e
                    }

                val sessionToken = resp.optString("sessionToken")
                if (sessionToken.length != SESSION_TOKEN_BYTES * 2) {
                    error("Account server did not return a valid session token")
                }

                pendingSessionToken = sessionToken
                pendingEmail = email
                pendingVerificationMethod = "email-otp"

                runCatching { sendSessionVerifyCode(sessionToken) }
                true
            }
        }

    suspend fun submitTwoFactorCode(rawCode: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val code = sanitizeCode(rawCode)
                require(code.length in 6..8) { "Enter the verification code sent to your email or authenticator" }
                val sessionToken =
                    pendingSessionToken
                        ?: error("Your sign-in session expired. Please sign in again.")
                val email = pendingEmail

                var verifiedOk = false
                var lastErr: Throwable? = null

                val endpoints =
                    if (pendingVerificationMethod == "totp-2fa") {
                        listOf(
                            "$FXA_AUTH_SERVER/session/verify/totp" to
                                JSONObject().put("code", code).put("service", "sync"),
                            "$FXA_AUTH_SERVER/session/verify_code" to
                                JSONObject().put("code", code),
                        )
                    } else {
                        listOf(
                            "$FXA_AUTH_SERVER/session/verify_code" to
                                JSONObject().put("code", code),
                            "$FXA_AUTH_SERVER/session/verify/totp" to
                                JSONObject().put("code", code).put("service", "sync"),
                        )
                    }

                for ((endpoint, body) in endpoints) {
                    val attemptResult =
                        runCatching {
                            fxaRequest(
                                url = endpoint,
                                method = "POST",
                                jsonBody = body.toString(),
                                hawkTokenHex = sessionToken,
                            )
                        }
                    if (attemptResult.isSuccess) {
                        verifiedOk = true
                        break
                    } else {
                        lastErr = attemptResult.exceptionOrNull()
                    }
                }

                if (!verifiedOk) {
                    throw lastErr ?: IOException("Invalid or expired verification code")
                }

                finalizeSessionSignIn(sessionToken, email)
                clearPendingSession()
            }
        }

    suspend fun resendVerificationCodeIfNeeded(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sessionToken = pendingSessionToken ?: return@runCatching
                sendSessionVerifyCode(sessionToken)
            }
        }

    private suspend fun sendSessionVerifyCode(sessionToken: String) {
        fxaRequest(
            url = "$FXA_AUTH_SERVER/session/resend_code",
            method = "POST",
            jsonBody = "{}",
            hawkTokenHex = sessionToken,
        )
    }

    suspend fun completeBrowserLogin(sessionTokenHex: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cleanToken = sessionTokenHex.trim().lowercase()
                require(cleanToken.length == SESSION_TOKEN_BYTES * 2) {
                    "Invalid session token length"
                }
                ControlPlaneHttp.cookieJar.syncFromWebView()
                finalizeSessionSignIn(cleanToken, pendingEmail)
            }
        }

    suspend fun completeBrowserLoginWithTokens(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long,
        sessionTokenHex: String? = null,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(accessToken.isNotBlank()) { "Empty OAuth access token" }
                ControlPlaneHttp.cookieJar.syncFromWebView()
                val now = Instant.now().epochSecond
                val auth =
                    AuthTokens(
                        sessionToken = sessionTokenHex?.trim()?.lowercase()?.takeIf { it.length == SESSION_TOKEN_BYTES * 2 },
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAtEpochSeconds = now + expiresIn.coerceAtLeast(300L),
                    )
                val email =
                    runCatching { fetchProfileEmail(accessToken) }.getOrNull()
                        ?: pendingEmail
                        ?: tokenStore.loadEmail()
                        ?: "Mozilla Account"
                tokenStore.saveAuth(email, auth)
                runCatching { guardianClient.activateGuardian(accessToken) }
                runCatching { onAuthUpdated(email, auth) }
                AppLogger.i(TAG, "Browser login completed via in-page OAuth exchange")
            }
        }

    private suspend fun finalizeSessionSignIn(sessionTokenHex: String, fallbackEmail: String?) {
        val tokens = exchangeSessionForOAuthTokens(sessionTokenHex)
        val email =
            runCatching { fetchProfileEmail(tokens.accessToken) }.getOrNull()
                ?: fallbackEmail
                ?: tokenStore.loadEmail()
                ?: "Mozilla Account"
        tokenStore.saveAuth(email, tokens)
        runCatching { guardianClient.activateGuardian(tokens.accessToken) }
        runCatching { onAuthUpdated(email, tokens) }
    }

    private suspend fun exchangeSessionForOAuthTokens(sessionTokenHex: String): AuthTokens {
        var lastError: Throwable? = null
        val scopes = listOf(FXA_SCOPE, FXA_LEGACY_SCOPE)
        for (scope in scopes) {
            val payload =
                JSONObject()
                    .put("client_id", FXA_CLIENT_ID)
                    .put("ttl", 3600)
                    .put("grant_type", "fxa-credentials")
                    .put("access_type", "offline")
                    .put("scope", scope)
                    .toString()

            val urls =
                listOf(
                    "$FXA_AUTH_SERVER/oauth/token",
                    "$FXA_OAUTH_SERVER/oauth/token",
                )
            for (url in urls) {
                val res =
                    runCatching {
                        fxaRequest(
                            url = url,
                            method = "POST",
                            jsonBody = payload,
                            hawkTokenHex = sessionTokenHex,
                        )
                    }
                if (res.isSuccess) {
                    val json = res.getOrThrow()
                    val access = json.optString("access_token")
                    if (access.isNotBlank()) {
                        val refresh = json.optString("refresh_token").ifBlank { null }
                        val expiresIn = json.optLong("expires_in", 3600L).coerceAtLeast(60L)
                        val now = Instant.now().epochSecond
                        return AuthTokens(
                            sessionToken = sessionTokenHex,
                            accessToken = access,
                            refreshToken = refresh,
                            expiresAtEpochSeconds = now + expiresIn,
                        )
                    }
                } else {
                    lastError = res.exceptionOrNull()
                }
            }
        }
        throw lastError ?: IOException("Could not obtain OAuth token from Mozilla account server")
    }

    suspend fun ensureFreshAccessToken(forceRefresh: Boolean = false): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val existing =
                    tokenStore.loadAuth()
                        ?: throw AuthInvalidException("Not signed in")
                val now = Instant.now().epochSecond
                if (!forceRefresh &&
                    existing.accessToken.isNotBlank() &&
                    existing.expiresAtEpochSeconds - now > 90
                ) {
                    return@runCatching existing.accessToken
                }

                val refresh = existing.refreshToken
                if (!refresh.isNullOrBlank()) {
                    val refreshed = tryRefreshOAuthToken(refresh, existing)
                    if (refreshed != null) {
                        val email = tokenStore.loadEmail() ?: "Mozilla Account"
                        tokenStore.saveAuth(email, refreshed)
                        runCatching { onAuthUpdated(email, refreshed) }
                        return@runCatching refreshed.accessToken
                    }
                }

                val session = existing.sessionToken
                if (!session.isNullOrBlank() && session.length == SESSION_TOKEN_BYTES * 2) {
                    val reExchanged = runCatching { exchangeSessionForOAuthTokens(session) }.getOrNull()
                    if (reExchanged != null) {
                        val email = tokenStore.loadEmail() ?: "Mozilla Account"
                        tokenStore.saveAuth(email, reExchanged)
                        runCatching { onAuthUpdated(email, reExchanged) }
                        return@runCatching reExchanged.accessToken
                    }
                }

                if (!forceRefresh && existing.accessToken.isNotBlank()) {
                    return@runCatching existing.accessToken
                }
                throw AuthInvalidException("Session expired; please sign in again")
            }
        }

    private suspend fun tryRefreshOAuthToken(
        refreshToken: String,
        existing: AuthTokens,
    ): AuthTokens? {
        val clientIds = listOf(FXA_CLIENT_ID, FXA_LEGACY_CLIENT_ID)
        val scopes = listOf(FXA_SCOPE, null)
        val urls =
            listOf(
                "$FXA_AUTH_SERVER/oauth/token",
                "$FXA_OAUTH_SERVER/token",
            )
        var sawTerminalAuthError = false

        for (clientId in clientIds) {
            for (scope in scopes) {
                val payloadObj =
                    JSONObject()
                        .put("client_id", clientId)
                        .put("ttl", 3600)
                        .put("grant_type", "refresh_token")
                        .put("refresh_token", refreshToken)
                if (scope != null) {
                    payloadObj.put("scope", scope)
                }
                val bodyString = payloadObj.toString()

                for (url in urls) {
                    val res =
                        runCatching {
                            fxaRequest(
                                url = url,
                                method = "POST",
                                jsonBody = bodyString,
                                hawkTokenHex = null,
                            )
                        }
                    if (res.isSuccess) {
                        val json = res.getOrThrow()
                        val access = json.optString("access_token")
                        if (access.isNotBlank()) {
                            val newRefresh =
                                json.optString("refresh_token").ifBlank { refreshToken }
                            val expiresIn = json.optLong("expires_in", 3600L).coerceAtLeast(60L)
                            val now = Instant.now().epochSecond
                            return existing.copy(
                                accessToken = access,
                                refreshToken = newRefresh,
                                expiresAtEpochSeconds = now + expiresIn,
                            )
                        }
                    } else {
                        val err = res.exceptionOrNull()
                        if (err is FxaHttpException && (err.code == 400 || err.code == 401)) {
                            if (err.errno in listOf(108, 110)) {
                                sawTerminalAuthError = true
                            }
                        }
                    }
                }
            }
        }

        if (sawTerminalAuthError && existing.sessionToken.isNullOrBlank()) {
            throw AuthInvalidException("Refresh token is no longer valid; please sign in again")
        }
        return null
    }

    private suspend fun fetchProfileEmail(accessToken: String): String? {
        val urls =
            listOf(
                "https://profile.accounts.firefox.com/v1/profile",
                "https://profile.stage.mozaws.net/v1/profile",
            )
        for (url in urls) {
            val req =
                FirefoxAccountHeaders.applyApi(Request.Builder().url(url).get())
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            val email =
                runCatching {
                        httpClient.newCall(req).await().use { resp ->
                            if (!resp.isSuccessful) return@use null
                            val text = resp.body?.readBoundedUtf8().orEmpty()
                            JSONObject(text).optString("email").takeIf { it.isNotBlank() }
                        }
                    }
                    .getOrNull()
            if (!email.isNullOrBlank()) return email
        }
        return null
    }

    internal fun buildOAuthHawkHeader(sessionTokenHex: String): Pair<String, String> {
        val tokenBytes = fromHex(sessionTokenHex)
        val keyMaterial = hkdfSha256(tokenBytes, ByteArray(32), kw("sessionToken"), 64)
        val tokenId = toHex(keyMaterial.copyOfRange(0, 32))
        val reqHMACKey = keyMaterial.copyOfRange(32, 64)

        val bodyJson =
            JSONObject()
                .put("client_id", FXA_CLIENT_ID)
                .put("ttl", 3600)
                .put("grant_type", "fxa-credentials")
                .put("access_type", "offline")
                .put("scope", FXA_SCOPE)
                .toString()

        val url = "$FXA_AUTH_SERVER/oauth/token"
        val header =
            computeHawkHeader(
                tokenId = tokenId,
                hmacKey = reqHMACKey,
                method = "POST",
                url = url,
                payload = bodyJson,
                contentType = "application/json",
            )
        return header to bodyJson
    }

    private class FxaHttpException(
        val code: Int,
        val errno: Int,
        message: String,
    ) : IOException(message)

    private suspend fun fxaRequest(
        url: String,
        method: String,
        jsonBody: String?,
        hawkTokenHex: String?,
    ): JSONObject {
        ControlPlaneHttp.cookieJar.syncFromWebView()
        FirefoxAccountHeaders.ensureFresh(httpClient)

        var attempt = 0
        val maxAttempts = 5
        val origin =
            if (url.startsWith(FXA_OAUTH_SERVER)) {
                "https://oauth.accounts.firefox.com"
            } else {
                "https://accounts.firefox.com"
            }

        while (true) {
            attempt++
            val builder =
                FirefoxAccountHeaders.applyApi(
                    Request.Builder().url(url),
                    origin = origin,
                )

            if (hawkTokenHex != null) {
                val tokenBytes = fromHex(hawkTokenHex)
                val keyMaterial = hkdfSha256(tokenBytes, ByteArray(32), kw("sessionToken"), 64)
                val tokenId = toHex(keyMaterial.copyOfRange(0, 32))
                val reqHMACKey = keyMaterial.copyOfRange(32, 64)
                val hawk =
                    computeHawkHeader(
                        tokenId = tokenId,
                        hmacKey = reqHMACKey,
                        method = method,
                        url = url,
                        payload = jsonBody,
                        contentType = if (jsonBody != null) "application/json" else null,
                    )
                builder.header("Authorization", hawk)
            }

            if (method.equals("POST", ignoreCase = true)) {
                val payload = (jsonBody ?: "{}").toRequestBody("application/json".toMediaType())
                builder.post(payload)
                builder.header("Content-Type", "application/json")
            } else {
                builder.get()
            }

            val response = httpClient.newCall(builder.build()).await()
            val code = response.code
            val retryAfterHeader = response.header("Retry-After")
            val bodyText =
                try {
                    response.body?.readBoundedUtf8().orEmpty()
                } finally {
                    response.close()
                }

            if (code == 406 && attempt < maxAttempts) {
                AppLogger.w(
                    TAG,
                    "FxA returned HTTP 406 on ${Uri.parse(url).path} (attempt $attempt/$maxAttempts); solving Fastly challenge...",
                )
                val challengeOrigin =
                    runCatching {
                            val parsed = Uri.parse(url)
                            "${parsed.scheme}://${parsed.host}"
                        }
                        .getOrDefault("https://api.accounts.firefox.com")
                val solved =
                    challengeSolver.solveAndInstall(
                        challengedBody = bodyText,
                        challengedOrigin = challengeOrigin,
                        challengedBaseUrl = challengeOrigin,
                        userAgent = FirefoxAccountHeaders.userAgent,
                    )
                if (solved) {
                    delay(350L * attempt)
                    continue
                }
                delay(400L * attempt)
                continue
            }

            if (code == 406) {
                throw FxaHttpException(
                    code = 406,
                    errno = 999,
                    message =
                        "The account service rejected the anti-bot challenge after retries (HTTP 406). Use 'Sign In via Official Mozilla Web Portal' or try again.",
                )
            }

            if (code == 429 && attempt < maxAttempts) {
                val waitMs = parseRetryAfterMs(retryAfterHeader)
                AppLogger.w(TAG, "FxA rate-limited (HTTP 429); waiting ${waitMs}ms")
                delay(waitMs)
                continue
            }

            if (code in 200..299) {
                return if (bodyText.isBlank()) JSONObject() else JSONObject(bodyText)
            }

            val errJson = runCatching { JSONObject(bodyText) }.getOrNull()
            val errno = errJson?.optInt("errno", -1) ?: -1
            val serverMessage =
                errJson?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: "Account request failed (HTTP $code)"
            throw FxaHttpException(code = code, errno = errno, message = serverMessage)
        }
    }

    private fun computeHawkHeader(
        tokenId: String,
        hmacKey: ByteArray,
        method: String,
        url: String,
        payload: String?,
        contentType: String?,
    ): String {
        val parsed = Uri.parse(url)
        val host = parsed.host ?: "api.accounts.firefox.com"
        val port =
            when {
                parsed.port > 0 -> parsed.port
                parsed.scheme.equals("https", ignoreCase = true) -> 443
                else -> 80
            }
        val pathAndQuery =
            buildString {
                append(parsed.encodedPath?.ifEmpty { "/" } ?: "/")
                val q = parsed.encodedQuery
                if (!q.isNullOrEmpty()) {
                    append('?').append(q)
                }
            }
        val ts = (System.currentTimeMillis() / 1000L).toString()
        val nonceBytes = ByteArray(6).also { SecureRandom().nextBytes(it) }
        val nonce =
            Base64.encodeToString(
                nonceBytes,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )

        val payloadHashB64 =
            if (payload != null && contentType != null) {
                val normalized =
                    "hawk.1.payload\n${contentType.lowercase()}\n$payload\n"
                val digest =
                    MessageDigest.getInstance("SHA-256")
                        .digest(normalized.toByteArray(StandardCharsets.UTF_8))
                Base64.encodeToString(digest, Base64.NO_WRAP)
            } else {
                ""
            }

        val normalizedHeader =
            buildString {
                append("hawk.1.header\n")
                append(ts).append('\n')
                append(nonce).append('\n')
                append(method.uppercase()).append('\n')
                append(pathAndQuery).append('\n')
                append(host.lowercase()).append('\n')
                append(port).append('\n')
                append(payloadHashB64).append('\n')
                append('\n')
            }

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(hmacKey, "HmacSHA256"))
        val macB64 =
            Base64.encodeToString(
                mac.doFinal(normalizedHeader.toByteArray(StandardCharsets.UTF_8)),
                Base64.NO_WRAP,
            )

        return buildString {
            append("Hawk id=\"").append(tokenId).append("\"")
            append(", ts=\"").append(ts).append("\"")
            append(", nonce=\"").append(nonce).append("\"")
            if (payloadHashB64.isNotEmpty()) {
                append(", hash=\"").append(payloadHashB64).append("\"")
            }
            append(", ext=\"\"")
            append(", mac=\"").append(macB64).append("\"")
        }
    }

    companion object {
        fun normalizeEmail(raw: String): String =
            Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replace(Regex("[\\p{Z}\\u200B-\\u200D\\uFEFF\\u00A0\\s]+"), "")
                .lowercase()

        fun normalizePassword(raw: String): String =
            raw.replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "")

        fun sanitizeCode(raw: String): String =
            Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replace(Regex("[^0-9]"), "")

        private fun kw(name: String): ByteArray =
            "identity.mozilla.com/picl/v1/$name".toByteArray(StandardCharsets.UTF_8)

        private fun kwExtend(name: String, extra: String): ByteArray =
            "identity.mozilla.com/picl/v1/$name:$extra".toByteArray(StandardCharsets.UTF_8)

        internal fun stretchPassword(email: String, password: String): ByteArray {
            val salt = kwExtend("quickStretch", email)
            val passChars = password.toCharArray()
            val quickStretched =
                try {
                    val spec = PBEKeySpec(passChars, salt, 1000, 32 * 8)
                    try {
                        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                            .generateSecret(spec)
                            .encoded
                    } catch (_: GeneralSecurityException) {
                        pbkdf2HmacSha256Fallback(
                            password.toByteArray(StandardCharsets.UTF_8),
                            salt,
                            1000,
                            32,
                        )
                    } finally {
                        spec.clearPassword()
                    }
                } finally {
                    passChars.fill('\u0000')
                }
            return hkdfSha256(quickStretched, ByteArray(32), kw("authPW"), 32)
        }

        private fun pbkdf2HmacSha256Fallback(
            password: ByteArray,
            salt: ByteArray,
            iterations: Int,
            dkLen: Int,
        ): ByteArray {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(password, "HmacSHA256"))
            val hLen = mac.macLength
            val blocks = (dkLen + hLen - 1) / hLen
            val output = ByteArray(blocks * hLen)
            val blockIndexBytes = ByteArray(4)
            for (block in 1..blocks) {
                ByteBuffer.wrap(blockIndexBytes).putInt(block)
                mac.update(salt)
                val u = mac.doFinal(blockIndexBytes)
                val t = u.clone()
                var prev = u
                for (i in 2..iterations) {
                    prev = mac.doFinal(prev)
                    for (j in t.indices) {
                        t[j] = (t[j].toInt() xor prev[j].toInt()).toByte()
                    }
                }
                System.arraycopy(t, 0, output, (block - 1) * hLen, hLen)
            }
            return output.copyOf(dkLen)
        }

        internal fun hkdfSha256(
            ikm: ByteArray,
            salt: ByteArray,
            info: ByteArray,
            length: Int,
        ): ByteArray {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(if (salt.isEmpty()) ByteArray(32) else salt, "HmacSHA256"))
            val prk = mac.doFinal(ikm)
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            val result = ByteArray(length)
            var t = ByteArray(0)
            var offset = 0
            var counter = 1
            while (offset < length) {
                mac.reset()
                mac.update(t)
                mac.update(info)
                mac.update(counter.toByte())
                t = mac.doFinal()
                val toCopy = minOf(t.size, length - offset)
                System.arraycopy(t, 0, result, offset, toCopy)
                offset += toCopy
                counter++
            }
            return result
        }

        internal fun toHex(bytes: ByteArray): String =
            bytes.joinToString("") { "%02x".format(it) }

        internal fun fromHex(hex: String): ByteArray {
            val clean = hex.trim()
            val out = ByteArray(clean.length / 2)
            for (i in out.indices) {
                out[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            return out
        }
    }
}
