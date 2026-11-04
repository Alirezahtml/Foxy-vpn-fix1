package com.vauth.foxyvpn.data

import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

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
private val TEXT_MEDIA = "text/plain;charset=UTF-8".toMediaType()
private const val MAX_CHALLENGE_ROUNDS = 4
private const val TAG = "FastlyChallenge"

class FastlyChallengeSolver(
    private val httpClient: OkHttpClient = ControlPlaneHttp.client,
    private val cookieJar: SimpleCookieJar = ControlPlaneHttp.cookieJar,
) {
    private val mutex = Mutex()

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
                                add(effectiveBase)
                                add("https://accounts.firefox.com")
                                addAll(FASTLY_CHALLENGE_BASES)
                            }.map { it.trimEnd('/') }.distinct()

                        for (candidateBase in candidateBases) {
                            val candidateUrl =
                                if (candidateBase.contains("accounts.firefox.com") &&
                                    !candidateBase.contains("api.accounts.firefox.com")
                                ) {
                                    "$candidateBase/?context=fx_desktop_v3&service=sync&action=email"
                                } else {
                                    "$candidateBase/"
                                }
                            val candidateUA =
                                if (candidateBase.contains("firefox.com")) {
                                    browserUserAgent
                                } else {
                                    effectiveUA
                                }

                            // Crucial: Use the SAME solverJar and solver client for both fetching
                            // the challenge HTML page (which sets the mandatory _fs_ch_st_<id> cookie)
                            // and fetching /_fs-ch-<id>/script.js?reload=true + /fst-post-back.
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
        var challenges = JSONArray(initMatch.groupValues[1])
        val token = initMatch.groupValues[2]

        for (round in 0 until MAX_CHALLENGE_ROUNDS) {
            val answers = JSONArray()
            for (i in 0 until challenges.length()) {
                val ch = challenges.optJSONObject(i) ?: continue
                when (ch.optString("ty")) {
                    "pat" -> {
                        val patAns = fetchPat(solver, prefixUrl, origin, token, userAgent)
                        answers.put(JSONObject().put("ty", "pat").put("answer", patAns))
                    }
                    "pow" -> {
                        val data = ch.optJSONObject("data") ?: return false
                        val solved = solvePow(data) ?: return false
                        answers.put(solved)
                    }
                    "clientmetrics" -> answers.put(buildClientMetrics())
                }
            }

            val postBody =
                JSONObject()
                    .put("token", token)
                    .put("data", answers)
                    .toString()

            val postReq =
                FirefoxAccountHeaders.applyChallengeAsset(
                        Request.Builder()
                            .url("$prefixUrl/fst-post-back")
                            .header("Content-Type", "application/json")
                            .post(postBody.toRequestBody(JSON_MEDIA)),
                        origin = origin,
                        dest = "empty",
                        accept = "*/*",
                        userAgentOverride = userAgent,
                    )
                    .build()

            val (code, respBody) =
                solver.newCall(postReq).execute().use { resp ->
                    resp.code to (resp.body?.readBoundedUtf8() ?: "")
                }

            val respJson = runCatching { JSONObject(respBody) }.getOrNull()
            val status = respJson?.optString("status").orEmpty()
            if (code in 200..299 && status == "success") {
                installSolvedCookies(solverJar, origin)
                return hasClearanceCookies(solverJar)
            }

            val next = respJson?.optJSONArray("challenges")
            if (next != null && next.length() > 0) {
                challenges = next
                continue
            }
            break
        }

        if (hasClearanceCookies(solverJar)) {
            installSolvedCookies(solverJar, origin)
            return true
        }
        return false
    }

    private fun hasClearanceCookies(solverJar: SimpleCookieJar): Boolean =
        solverJar.snapshotAll().any { cookie ->
            cookie.name.startsWith("_fs_ch_cp_") || cookie.name.startsWith("_fs_ch_")
        }

    private fun installSolvedCookies(solverJar: SimpleCookieJar, origin: String) {
        val rawCookies = solverJar.snapshotAll()
        if (rawCookies.isEmpty()) return

        val expanded = mutableListOf<Cookie>()
        expanded.addAll(rawCookies)

        val originHost = origin.toHttpUrlOrNull()?.host
        for (cookie in rawCookies) {
            if (cookie.name.startsWith("_fs_ch_")) {
                val domains =
                    buildList {
                        add("firefox.com")
                        add("accounts.firefox.com")
                        add("api.accounts.firefox.com")
                        add("oauth.accounts.firefox.com")
                        if (!originHost.isNullOrBlank()) add(originHost)
                    }.distinct()
                for (domain in domains) {
                    expanded.add(
                        Cookie.Builder()
                            .name(cookie.name)
                            .value(cookie.value)
                            .domain(domain)
                            .path("/")
                            .secure()
                            .expiresAt(cookie.expiresAt)
                            .build(),
                    )
                }
            }
        }

        cookieJar.importCookies(expanded)
        cookieJar.syncToWebView(expanded)
        AppLogger.i(TAG, "Installed Fastly clearance cookies (${rawCookies.size} base / ${expanded.size} total)")
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
                            .header("Cache-Control", "no-store")
                            .post("".toRequestBody(TEXT_MEDIA)),
                        origin = origin,
                        dest = "empty",
                        accept = "*/*",
                        userAgentOverride = userAgent,
                    )
                    .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) resp.body?.readBoundedUtf8().orEmpty() else ""
            }
        }.getOrDefault("")

    internal fun solvePow(data: JSONObject): JSONObject? {
        val base = data.optString("base")
        val targetHash = data.optString("hash").lowercase()
        val hmac = data.optString("hmac")
        val expires = data.optString("expires")
        if (base.isEmpty() || targetHash.isEmpty()) return null

        val md = MessageDigest.getInstance("SHA-256")
        val answer = findTwoCharMatch(md, base, targetHash) ?: return null
        return JSONObject()
            .put("ty", "pow")
            .put("base", base)
            .put("answer", answer)
            .put("hmac", hmac)
            .put("expires", expires)
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

    private fun buildClientMetrics(): JSONObject =
        JSONObject()
            .put("ty", "clientmetrics")
            .put("webdriver", false)
            .put(
                "bot_detection_result",
                JSONObject().put("bot_detected", false).put("bot_kind", JSONObject.NULL),
            )
            .put(
                "browser_metrics",
                JSONObject().put("client_data", "{}").put("error_trace", JSONObject.NULL),
            )
            .put("detector_results", JSONObject())
            .put("v", 2)

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
