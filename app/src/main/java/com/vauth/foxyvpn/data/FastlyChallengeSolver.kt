package com.vauth.foxyvpn.data

import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal val FASTLY_CHALLENGE_BASES: List<String> =
    listOf(
        "https://api.accounts.firefox.com",
        "https://accounts.firefox.com",
        "https://vpn.mozilla.org",
    )

private val CHALLENGE_PREFIX_REGEX = Regex("""/_fs-ch-[A-Za-z0-9]+""")
private val INIT_REGEX =
    Regex(
        """init\(\s*(\[\s*\{.*?\}\s*\])\s*,\s*"([^"]+)"\s*,\s*"(/_fs-ch-[A-Za-z0-9]+)"\s*,""",
        setOf(RegexOption.DOT_MATCHES_ALL),
    )
private const val POW_ALPHABET =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
private val JSON_MEDIA = "application/json".toMediaType()
private const val MAX_CHALLENGE_ROUNDS = 4
private const val TAG = "FastlyChallenge"

class FastlyChallengeSolver(
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val cookieJar: SimpleCookieJar = ControlPlaneHttp.cookieJar,
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ensureClearance(): Boolean = solveAndInstall()

    suspend fun solveAndInstall(
        challengedBody: String? = null,
        challengedOrigin: String = FASTLY_CHALLENGE_BASES.first(),
        challengedBaseUrl: String = challengedOrigin,
        userAgent: String? = null,
    ): Boolean =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                runCatching {
                        FirefoxAccountHeaders.ensureFresh(httpClient)
                        val browserUserAgent = FirefoxAccountHeaders.userAgent
                        val effectiveUA = userAgent ?: browserUserAgent
                        val effectiveBase =
                            challengedBaseUrl.trimEnd('/').ifBlank {
                                challengedOrigin.trimEnd('/').ifBlank { FASTLY_CHALLENGE_BASES.first() }
                            }

                        if (effectiveBase.contains("firefox.com")) {
                            warmUpTlsSession(
                                client = httpClient,
                                url = "https://api.accounts.firefox.com/",
                                userAgent = browserUserAgent,
                            )
                        }

                        if (challengedBody != null && isChallengeHtml(challengedBody)) {
                            val directJar = SimpleCookieJar()
                            directJar.importCookies(cookieJar.snapshotAll())
                            val directSolver = newSolverClient(directJar)
                            if (solveChallengePage(
                                    page = challengedBody,
                                    baseUrl = effectiveBase,
                                    userAgent = effectiveUA,
                                    solverJar = directJar,
                                    solver = directSolver,
                                )
                            ) {
                                return@runCatching true
                            }
                        }

                        val candidateBases =
                            buildList {
                                if (effectiveBase.contains("firefox.com")) {
                                    add("https://accounts.firefox.com")
                                }
                                add(effectiveBase)
                                addAll(FASTLY_CHALLENGE_BASES)
                            }.map { it.trimEnd('/') }.distinct()

                        for (candidateBase in candidateBases) {
                            val candidateUrl = "$candidateBase/"
                            val candidateUA =
                                if (candidateBase.contains("firefox.com")) {
                                    browserUserAgent
                                } else {
                                    effectiveUA
                                }

                            val solverJar = SimpleCookieJar()
                            val solver = newSolverClient(solverJar)
                            val page =
                                runCatching {
                                    fetchDocument(solver, candidateUrl, candidateUA)
                                }.getOrNull() ?: continue

                            if (!isChallengeHtml(page)) continue

                            if (solveChallengePage(
                                    page = page,
                                    baseUrl = candidateBase,
                                    userAgent = candidateUA,
                                    solverJar = solverJar,
                                    solver = solver,
                                )
                            ) {
                                return@runCatching true
                            }
                        }
                        false
                    }
                    .onFailure { err ->
                        AppLogger.w(
                            TAG,
                            "Fastly challenge solver failed: ${err.message ?: err.javaClass.simpleName}",
                        )
                    }
                    .getOrDefault(false)
            }
        }

    private fun isChallengeHtml(body: String): Boolean =
        body.contains("<title>Client Challenge</title>", ignoreCase = true) ||
            CHALLENGE_PREFIX_REGEX.containsMatchIn(body)

    private fun solveChallengePage(
        page: String,
        baseUrl: String,
        userAgent: String,
        solverJar: SimpleCookieJar,
        solver: OkHttpClient,
    ): Boolean {
        val origin = baseUrl.trimEnd('/')
        val prefix = CHALLENGE_PREFIX_REGEX.find(page)?.value ?: return false
        val prefixUrl = origin + prefix

        val scriptReq =
            FirefoxAccountHeaders.applyChallengeAsset(
                    Request.Builder().url("$prefixUrl/script.js?reload=true").get(),
                    origin = origin,
                    dest = "script",
                    accept = "*/*",
                    userAgentOverride = userAgent,
                )
                .build()

        val script =
            solver.newCall(scriptReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    AppLogger.w(TAG, "Fastly challenge script returned HTTP ${resp.code} on $origin")
                    return false
                }
                resp.body?.readBoundedUtf8() ?: return false
            }

        val initMatch = INIT_REGEX.find(script) ?: return false
        var challenges: JsonArray =
            runCatching { json.parseToJsonElement(initMatch.groupValues[1]).jsonArray }
                .getOrNull() ?: return false
        var token = initMatch.groupValues[2]

        for (round in 0 until MAX_CHALLENGE_ROUNDS) {
            val answers = buildJsonArray {
                for (element in challenges) {
                    val ch = runCatching { element.jsonObject }.getOrNull() ?: continue
                    val ty = ch["ty"]?.jsonPrimitive?.content.orEmpty()
                    when (ty) {
                        "pat" -> {
                            val patAuth = fetchPat(solver, prefixUrl, origin, token, userAgent)
                            add(
                                buildJsonObject {
                                    put("ty", JsonPrimitive("pat"))
                                    put("auth", JsonPrimitive(patAuth))
                                },
                            )
                        }
                        "pow" -> {
                            val data = runCatching { ch["data"]?.jsonObject }.getOrNull() ?: return false
                            val solved = solvePow(data) ?: return false
                            add(solved)
                        }
                        "clientmetrics" -> add(buildClientMetrics())
                    }
                }
            }

            val postBody =
                buildJsonObject {
                    put("token", JsonPrimitive(token))
                    put("data", answers)
                }.toString()

            val postReq =
                FirefoxAccountHeaders.applyChallengeAsset(
                        Request.Builder()
                            .url("$prefixUrl/fst-post-back")
                            .header("Content-Type", "application/json")
                            .post(postBody.toByteArray(Charsets.UTF_8).toRequestBody(JSON_MEDIA)),
                        origin = origin,
                        dest = "empty",
                        accept = "application/json",
                        userAgentOverride = userAgent,
                    )
                    .build()

            val (code, respBody) =
                solver.newCall(postReq).execute().use { resp ->
                    resp.code to (resp.body?.readBoundedUtf8() ?: "")
                }

            val respJson =
                runCatching { json.parseToJsonElement(respBody).jsonObject }.getOrNull()
            val status = respJson?.get("status")?.jsonPrimitive?.content.orEmpty()
            if (code in 200..299 && status == "success") {
                installSolvedCookies(solverJar)
                return hasClearanceCookies(solverJar)
            }

            val next =
                runCatching { respJson?.get("ch")?.jsonArray }.getOrNull()
                    ?: runCatching { respJson?.get("challenges")?.jsonArray }.getOrNull()
            val nextTok =
                runCatching { respJson?.get("tok")?.jsonPrimitive?.content }.getOrNull()?.takeIf { it.isNotBlank() }
                    ?: runCatching { respJson?.get("token")?.jsonPrimitive?.content }.getOrNull()?.takeIf { it.isNotBlank() }
            if (next != null && next.isNotEmpty()) {
                challenges = next
                if (nextTok != null) {
                    token = nextTok
                }
                continue
            }
            break
        }

        if (hasClearanceCookies(solverJar)) {
            installSolvedCookies(solverJar)
            return true
        }
        return false
    }

    private fun hasClearanceCookies(solverJar: SimpleCookieJar): Boolean =
        solverJar.snapshotAll().any { cookie ->
            cookie.name.startsWith("_fs_ch_cp_")
        }

    private fun installSolvedCookies(solverJar: SimpleCookieJar) {
        val clearanceCookies =
            solverJar.snapshotAll().filter { cookie ->
                !cookie.name.startsWith("_fs_ch_st_")
            }
        if (clearanceCookies.isEmpty()) return

        cookieJar.importCookies(clearanceCookies)
        cookieJar.syncToWebView(clearanceCookies)
        runCatching { httpClient.connectionPool.evictAll() }
        AppLogger.i(
            TAG,
            "Installed Fastly clearance cookies (${clearanceCookies.map { "${it.name}@${it.domain}" }})",
        )
    }

    private fun warmUpTlsSession(
        client: OkHttpClient,
        url: String,
        userAgent: String,
    ) {
        runCatching {
            val req =
                FirefoxAccountHeaders.applyBrowserDocument(
                        Request.Builder()
                            .url(url)
                            .header("Connection", "close")
                            .get(),
                        userAgentOverride = userAgent,
                    )
                    .build()
            client.newCall(req).execute().use { resp ->
                resp.body?.readBoundedUtf8()
            }
            client.connectionPool.evictAll()
        }
    }

    private fun newSolverClient(jar: SimpleCookieJar): OkHttpClient =
        httpClient
            .newBuilder()
            .cookieJar(jar)
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

    private fun fetchDocument(
        client: OkHttpClient,
        url: String,
        userAgent: String,
    ): String {
        val req =
            FirefoxAccountHeaders.applyBrowserDocument(
                    Request.Builder().url(url).get(),
                    userAgentOverride = userAgent,
                )
                .build()
        return client.newCall(req).execute().use { resp ->
            resp.body?.readBoundedUtf8().orEmpty()
        }
    }

    private fun fetchPat(
        client: OkHttpClient,
        prefixUrl: String,
        origin: String,
        token: String,
        userAgent: String,
    ): String =
        runCatching {
            val encoded = URLEncoder.encode(token, "UTF-8")
            val req =
                FirefoxAccountHeaders.applyChallengeAsset(
                        Request.Builder()
                            .url("$prefixUrl/pat?token=$encoded")
                            .header("Content-Type", "application/json")
                            .post(ByteArray(0).toRequestBody(null)),
                        origin = origin,
                        dest = "empty",
                        accept = "text/plain",
                        userAgentOverride = userAgent,
                    )
                    .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use ""
                val raw = resp.body?.readBoundedUtf8().orEmpty()
                runCatching {
                    json.parseToJsonElement(raw).jsonObject["auth"]?.jsonPrimitive?.content.orEmpty()
                }.getOrDefault("")
            }
        }.getOrDefault("")

    internal fun solvePow(data: JsonObject): JsonObject? {
        val base = data["base"]?.jsonPrimitive?.content.orEmpty()
        val targetHash = data["hash"]?.jsonPrimitive?.content.orEmpty().lowercase()
        val hmac = data["hmac"]?.jsonPrimitive?.content.orEmpty()
        val expires = data["expires"]?.jsonPrimitive?.content.orEmpty()
        if (base.isEmpty() || targetHash.isEmpty()) return null

        val answer = solvePowAnswer(base, targetHash) ?: return null
        return buildJsonObject {
            put("ty", JsonPrimitive("pow"))
            put("base", JsonPrimitive(base))
            put("answer", JsonPrimitive(answer))
            put("hmac", JsonPrimitive(hmac))
            put("expires", JsonPrimitive(expires))
        }
    }

    internal fun solvePowAnswer(base: String, targetHash: String): String? {
        val md = MessageDigest.getInstance("SHA-256")
        return findTwoCharMatch(md, base, targetHash.lowercase())
    }

    private fun findTwoCharMatch(
        md: MessageDigest,
        base: String,
        targetHex: String,
    ): String? {
        for (i in 0 until POW_ALPHABET.length) {
            val c1 = POW_ALPHABET[i]
            for (j in 0 until POW_ALPHABET.length) {
                val c2 = POW_ALPHABET[j]
                val candidate = "$base$c1$c2"
                val digest = md.digest(candidate.toByteArray(Charsets.UTF_8))
                if (bytesToHex(digest) == targetHex) {
                    return "$c1$c2"
                }
            }
        }
        return null
    }

    private fun buildClientMetrics(): JsonObject =
        buildJsonObject {
            put("ty", JsonPrimitive("clientmetrics"))
            put("webdriver", JsonPrimitive(false))
            put(
                "bot_detection_result",
                buildJsonObject {
                    put("bot_detected", JsonPrimitive(false))
                    put("bot_kind", JsonNull)
                },
            )
            put(
                "browser_metrics",
                buildJsonObject {
                    put("client_data", JsonPrimitive("{}"))
                    put("error_trace", JsonNull)
                },
            )
            put("detector_results", buildJsonObject {})
            put("v", JsonPrimitive(2))
        }

    private fun bytesToHex(bytes: ByteArray): String {
        val chars = CharArray(bytes.size * 2)
        val hex = "0123456789abcdef"
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            chars[i * 2] = hex[v ushr 4]
            chars[i * 2 + 1] = hex[v and 0x0F]
        }
        return String(chars)
    }
}
