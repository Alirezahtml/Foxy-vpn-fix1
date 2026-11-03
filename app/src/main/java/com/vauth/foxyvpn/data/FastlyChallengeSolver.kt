package com.vauth.foxyvpn.data

import android.util.Base64
import java.math.BigInteger
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "FastlyChallenge"
private const val CHALLENGE_ORIGIN = "https://api.accounts.firefox.com"
private const val CHALLENGE_SCRIPT_PATH = "/_fs-ch-1T16115794132240/script.js"

internal val FASTLY_CHALLENGE_BASES: List<String> =
    listOf(
        "https://api.accounts.firefox.com",
        "https://accounts.firefox.com",
        "https://vpn.mozilla.org",
    )

private val CHALLENGE_WASM_REGEX =
    Regex("/_fs-ch-[A-Za-z0-9]+/assets/[A-Za-z0-9_-]+\\.wasm")

private val INIT_CALL_REGEX = Regex("\\binit\\s*\\(\\s*\\[")
private val STRING_LITERAL_REGEX = Regex("\"((?:\\\\.|[^\"\\\\])*)\"")

class FastlyChallengeSolver(
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val cookieJar: SimpleCookieJar = ControlPlaneHttp.cookieJar,
) {
    private val mutex = Mutex()

    suspend fun ensureClearance(): Boolean = solveAndInstall()

    suspend fun solveAndInstall(
        challengedBody: String? = null,
        challengedOrigin: String = CHALLENGE_ORIGIN,
        userAgent: String = FirefoxAccountHeaders.userAgent,
    ): Boolean =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val origin = challengedOrigin.trimEnd('/').ifBlank { CHALLENGE_ORIGIN }
                val scriptPath = extractScriptPath(challengedBody) ?: CHALLENGE_SCRIPT_PATH
                val scriptUrl =
                    if (scriptPath.startsWith("http://") || scriptPath.startsWith("https://")) {
                        scriptPath
                    } else {
                        "$origin/${scriptPath.trimStart('/')}"
                    }

                runCatching {
                        AppLogger.i(TAG, "Fetching Fastly anti-bot challenge script from $origin")
                        val scriptReqBuilder =
                            FirefoxAccountHeaders.applyChallengeAsset(
                                Request.Builder().url(scriptUrl).get(),
                                origin = origin,
                                dest = "script",
                                accept = "*/*",
                                userAgentOverride = userAgent,
                            )
                        val scriptText =
                            httpClient.newCall(scriptReqBuilder.build()).await().use { resp ->
                                if (!resp.isSuccessful) {
                                    error("Challenge script request failed: HTTP ${resp.code}")
                                }
                                resp.body?.readBoundedUtf8() ?: error("Empty challenge script body")
                            }

                        val init = parseChallengeInit(scriptText)
                        maybeWarmWasmAsset(scriptText, origin, userAgent)

                        val solved = solveFromInit(init, origin, userAgent)
                        installCookies(solved, origin)
                        AppLogger.i(TAG, "Installed Fastly clearance cookies (${solved.cookies.size})")
                        true
                    }
                    .onFailure { err ->
                        AppLogger.w(
                            TAG,
                            "Could not solve Fastly challenge: ${err.message ?: err.javaClass.simpleName}",
                        )
                    }
                    .getOrDefault(false)
            }
        }

    private fun extractScriptPath(challengedBody: String?): String? {
        if (challengedBody.isNullOrBlank()) return null
        val match =
            Regex("""/_fs-ch-[A-Za-z0-9]+/script\.js(?:\?[^"'\s>]*)?""")
                .find(challengedBody)
                ?.value
        return match
    }

    private suspend fun maybeWarmWasmAsset(
        scriptText: String,
        origin: String,
        userAgent: String,
    ) {
        val wasmPath = CHALLENGE_WASM_REGEX.find(scriptText)?.value ?: return
        runCatching {
            val req =
                FirefoxAccountHeaders.applyChallengeAsset(
                        Request.Builder().url("$origin$wasmPath").get(),
                        origin = origin,
                        dest = "empty",
                        accept = "application/wasm,*/*;q=0.8",
                        userAgentOverride = userAgent,
                    )
                    .build()
            httpClient.newCall(req).await().use { resp ->
                resp.body?.byteStream()?.readBoundedBytes(DEFAULT_MAX_BODY_BYTES)
            }
        }
    }

    private data class ChallengeInit(
        val types: List<String>,
        val requestId: String,
        val basePath: String,
        val clientIp: String,
        val aux1: String,
        val aux2: String,
        val initialPayloadB64: String,
    )

    private data class SolvedChallenge(val cookies: List<String>)

    private fun parseChallengeInit(scriptText: String): ChallengeInit {
        val callMatch =
            INIT_CALL_REGEX.find(scriptText)
                ?: error("Could not locate init([...], ...) in challenge script")
        val openBracket = scriptText.indexOf('[', callMatch.range.first)
        if (openBracket < 0) error("Malformed challenge init call (missing '[')")
        val closeBracket = findMatchingBracket(scriptText, openBracket, '[', ']')
        val closeParen = findMatchingBracket(scriptText, callMatch.range.first + 4, '(', ')')

        val typesSegment = scriptText.substring(openBracket + 1, closeBracket)
        val argsSegment = scriptText.substring(closeBracket + 1, closeParen)

        val types =
            STRING_LITERAL_REGEX.findAll(typesSegment)
                .map { unescapeJsString(it.groupValues[1]) }
                .toList()
        val args =
            STRING_LITERAL_REGEX.findAll(argsSegment)
                .map { unescapeJsString(it.groupValues[1]) }
                .toList()
        if (args.size < 6) {
            error("Unexpected init() argument count: ${args.size}")
        }
        return ChallengeInit(
            types = types,
            requestId = args[0],
            basePath = args[1],
            clientIp = args[2],
            aux1 = args[3],
            aux2 = args[4],
            initialPayloadB64 = args[5],
        )
    }

    private fun findMatchingBracket(
        text: String,
        startFrom: Int,
        openChar: Char,
        closeChar: Char,
    ): Int {
        var i = startFrom
        while (i < text.length && text[i] != openChar) i++
        if (i >= text.length) error("Missing '$openChar' in challenge script")
        var depth = 0
        var inString = false
        var stringQuote = '"'
        var escaped = false
        while (i < text.length) {
            val c = text[i]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == stringQuote) {
                    inString = false
                }
            } else {
                when (c) {
                    '"', '\'' -> {
                        inString = true
                        stringQuote = c
                    }
                    openChar -> depth++
                    closeChar -> {
                        depth--
                        if (depth == 0) return i
                    }
                }
            }
            i++
        }
        error("Unbalanced '$openChar'..'$closeChar' in challenge script")
    }

    private fun unescapeJsString(raw: String): String {
        if ('\\' !in raw) return raw
        val sb = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                val next = raw[i + 1]
                when (next) {
                    '\\', '"', '\'' -> {
                        sb.append(next)
                        i += 2
                    }
                    'n' -> {
                        sb.append('\n')
                        i += 2
                    }
                    'r' -> {
                        sb.append('\r')
                        i += 2
                    }
                    't' -> {
                        sb.append('\t')
                        i += 2
                    }
                    else -> {
                        sb.append(next)
                        i += 2
                    }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private suspend fun solveFromInit(
        init: ChallengeInit,
        origin: String,
        userAgent: String,
    ): SolvedChallenge {
        val currentData = decodeChallengePayload(init.initialPayloadB64)
        var powSolution: JSONObject? = null
        var patError: String? = null

        for (type in init.types) {
            when (type) {
                "pow" -> {
                    val powObj =
                        currentData.optJSONObject("pow")
                            ?: error("Challenge requested 'pow' but payload had no pow parameters")
                    powSolution = solvePow(powObj)
                }
                "pat" -> {
                    patError =
                        "NotSupportedError: Private Access Token challenge is not supported on this device"
                }
                " headless", "headless", " browser", "browser", "spec" -> Unit
            }
        }

        val inner =
            JSONObject()
                .put("dc", "s")
                .put(
                    "_",
                    JSONObject()
                        .put("req_id", init.requestId)
                        .put("ip", init.clientIp)
                        .put("d_1", init.aux1)
                        .put("d_2", init.aux2),
                )
        val baseObj = currentData.opt("base")
        if (baseObj != null) {
            inner.put("base", baseObj)
        }
        if ("headless" in init.types || "browser" in init.types || "spec" in init.types) {
            inner.put("spec", buildBrowserFingerprint(userAgent))
        }
        if (powSolution != null) {
            inner.put("pow", powSolution)
        }
        if (patError != null) {
            inner.put("pat", JSONObject().put("error", patError))
        }

        val encodedPayload = encodeChallengePayload(inner)
        val outerBody = JSONObject().put("data", encodedPayload).toString()

        val verifyPath = "${init.basePath.trimEnd('/')}/verify"
        val verifyUrl =
            if (verifyPath.startsWith("http://") || verifyPath.startsWith("https://")) {
                verifyPath
            } else {
                "$origin/${verifyPath.trimStart('/')}"
            }

        val req =
            FirefoxAccountHeaders.applyChallengeAsset(
                    Request.Builder()
                        .url(verifyUrl)
                        .post(outerBody.toRequestBody("application/json".toMediaType())),
                    origin = origin,
                    dest = "empty",
                    accept = "*/*",
                    userAgentOverride = userAgent,
                )
                .header("Content-Type", "application/json")
                .build()

        val collectedCookies = mutableListOf<String>()
        httpClient.newCall(req).await().use { resp ->
            collectedCookies.addAll(resp.headers("Set-Cookie"))
            val respText = resp.body?.readBoundedUtf8().orEmpty()
            if (resp.code != 200 && resp.code != 204 && collectedCookies.isEmpty()) {
                val jsonCookies =
                    runCatching {
                            val obj = JSONObject(respText)
                            val arr = obj.optJSONArray("cookies") ?: JSONArray()
                            (0 until arr.length()).mapNotNull { idx ->
                                arr.optString(idx).takeIf { it.isNotBlank() }
                            }
                        }
                        .getOrDefault(emptyList())
                collectedCookies.addAll(jsonCookies)
                if (collectedCookies.isEmpty()) {
                    error("Challenge verify failed: HTTP ${resp.code} ($respText)")
                }
            }
        }
        return SolvedChallenge(cookies = collectedCookies)
    }

    internal fun solvePow(pow: JSONObject): JSONObject {
        val base = pow.getString("base")
        val target = BigInteger(pow.getString("hash"), 16)
        val hmac = pow.getString("hmac")
        val expires = pow.getString("expires")

        val digest = MessageDigest.getInstance("SHA-256")
        val maxIterations = 25_000_000L
        var nonce = 0L
        while (nonce < maxIterations) {
            val candidate = "$base$nonce"
            val hashBytes = digest.digest(candidate.toByteArray(Charsets.UTF_8))
            val value = BigInteger(1, hashBytes)
            if (value <= target) {
                return JSONObject()
                    .put("base", base)
                    .put("Expires", expires)
                    .put("hmac", hmac)
                    .put("solution", nonce.toString())
            }
            nonce++
        }
        error("PoW solver exceeded iteration budget")
    }

    private fun buildBrowserFingerprint(userAgent: String): JSONObject {
        val screen = listOf(1080, 2400, 1080, 2316, 24, 24, "portrait-primary")
        val window = listOf(412, 892, 412, 892, 0, 0, true)
        val langs = JSONArray().put("en-US").put("en")
        val nav =
            JSONObject()
                .put("userAgent", userAgent)
                .put("platform", "Linux armv81")
                .put("language", "en-US")
                .put("languages", langs)
                .put("hardwareConcurrency", 8)
                .put("deviceMemory", 8)
                .put("maxTouchPoints", 5)
                .put("webdriver", false)
                .put("cookieEnabled", true)
                .put("pdfViewerEnabled", false)
                .put("vendor", "Google Inc.")
                .put("productSub", "20030107")
        return JSONObject()
            .put("screen", JSONArray(screen))
            .put("window", JSONArray(window))
            .put("navigator", nav)
            .put("timezoneOffset", 0)
            .put("timeOrigin", System.currentTimeMillis().toDouble())
    }

    internal fun decodeChallengePayload(b64: String): JSONObject {
        val raw = Base64.decode(b64, Base64.DEFAULT)
        val reversed = String(raw, Charsets.UTF_8).reversed()
        return JSONObject(reversed)
    }

    internal fun encodeChallengePayload(obj: JSONObject): String {
        val reversed = obj.toString().reversed()
        return Base64.encodeToString(reversed.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun installCookies(solved: SolvedChallenge, origin: String) {
        val originHttpUrl = origin.toHttpUrl()
        val apiUrl = CHALLENGE_ORIGIN.toHttpUrl()
        val webUrl = "https://accounts.firefox.com/".toHttpUrl()
        val oauthUrl = "https://oauth.accounts.firefox.com/".toHttpUrl()
        val guardianUrl = "https://vpn.mozilla.org/".toHttpUrl()
        val parsedList = mutableListOf<Cookie>()
        for (raw in solved.cookies) {
            Cookie.parse(originHttpUrl, raw)?.let { parsedList.add(it) }
            Cookie.parse(apiUrl, raw)?.let { parsedList.add(it) }
            val semicolonIdx = raw.indexOf(';')
            val nameValue = if (semicolonIdx > 0) raw.substring(0, semicolonIdx) else raw
            val eqIdx = nameValue.indexOf('=')
            if (eqIdx > 0) {
                val name = nameValue.substring(0, eqIdx).trim()
                val value = nameValue.substring(eqIdx + 1).trim()
                if (name.isNotEmpty()) {
                    val domains =
                        listOf(
                            "firefox.com",
                            "api.accounts.firefox.com",
                            "oauth.accounts.firefox.com",
                            "accounts.firefox.com",
                            originHttpUrl.host,
                        ).distinct()
                    for (domain in domains) {
                        val broad =
                            Cookie.Builder()
                                .name(name)
                                .value(value)
                                .domain(domain)
                                .path("/")
                                .secure()
                                .expiresAt(System.currentTimeMillis() + 3_600_000L)
                                .build()
                        parsedList.add(broad)
                    }
                    cookieJar.syncToWebView(origin, raw)
                    cookieJar.syncToWebView("https://accounts.firefox.com", raw)
                    cookieJar.syncToWebView("https://api.accounts.firefox.com", raw)
                    cookieJar.syncToWebView("https://oauth.accounts.firefox.com", raw)
                }
            }
        }
        if (parsedList.isNotEmpty()) {
            cookieJar.saveFromResponse(originHttpUrl, parsedList)
            cookieJar.saveFromResponse(apiUrl, parsedList)
            cookieJar.saveFromResponse(webUrl, parsedList)
            cookieJar.saveFromResponse(oauthUrl, parsedList)
            cookieJar.saveFromResponse(guardianUrl, parsedList)
        }
    }
}
