package com.vauth.foxyvpn.data

import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal object FirefoxAccountHeaders {
    private const val FIREFOX_VERSIONS_URL =
        "https://product-details.mozilla.org/1.0/firefox_versions.json"
    private const val DEFAULT_FIREFOX_MAJOR = "135"
    private const val VERSION_TTL_MS = 6 * 60 * 60 * 1_000L

    private val refreshMutex = Mutex()

    @Volatile private var cachedMajorVersion: String = DEFAULT_FIREFOX_MAJOR
    @Volatile private var cachedAtElapsedMs: Long = 0L

    val userAgent: String
        get() = buildFirefoxDesktopUserAgent(cachedMajorVersion)

    fun acceptLanguage(): String {
        val tag = Locale.getDefault().toLanguageTag().ifBlank { "en-US" }
        return if (tag.equals("en-US", ignoreCase = true) || tag.equals("en", ignoreCase = true)) {
            "en-US,en;q=0.9"
        } else {
            "$tag,en-US;q=0.9,en;q=0.8"
        }
    }

    suspend fun ensureFresh(httpClient: OkHttpClient) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (cachedAtElapsedMs != 0L && now - cachedAtElapsedMs < VERSION_TTL_MS) return

        refreshMutex.withLock {
            val lockedNow = android.os.SystemClock.elapsedRealtime()
            if (cachedAtElapsedMs != 0L && lockedNow - cachedAtElapsedMs < VERSION_TTL_MS) return

            val latestMajor =
                withContext(Dispatchers.IO) {
                    runCatching {
                        val req =
                            Request.Builder()
                                .url(FIREFOX_VERSIONS_URL)
                                .header("Accept", "application/json")
                                .header("User-Agent", userAgent)
                                .get()
                                .build()
                        httpClient.newCall(req).await().use { resp ->
                            if (!resp.isSuccessful) return@use null
                            val raw = resp.body?.readBoundedUtf8().orEmpty()
                            val version =
                                JSONObject(raw).optString("LATEST_FIREFOX_VERSION").trim()
                            version.substringBefore('.').takeIf { it.all(Char::isDigit) }
                        }
                    }.getOrNull()
                }

            if (!latestMajor.isNullOrBlank()) {
                cachedMajorVersion = latestMajor
            }
            cachedAtElapsedMs = lockedNow
        }
    }

    fun applyApi(
        builder: Request.Builder,
        @Suppress("UNUSED_PARAMETER") origin: String = "https://accounts.firefox.com",
        userAgentOverride: String = userAgent,
    ): Request.Builder =
        builder
            .header("User-Agent", userAgentOverride)
            .header("Accept", "*/*")

    fun applyBrowserDocument(
        builder: Request.Builder,
        userAgentOverride: String = userAgent,
    ): Request.Builder =
        builder
            .header("User-Agent", userAgentOverride)
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            )
            .header("Accept-Language", acceptLanguage())
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "none")

    fun applyChallengeAsset(
        builder: Request.Builder,
        origin: String,
        dest: String = "empty",
        accept: String = "*/*",
        userAgentOverride: String = userAgent,
    ): Request.Builder =
        builder
            .header("User-Agent", userAgentOverride)
            .header("Accept", accept)
            .header("Accept-Language", acceptLanguage())
            .header("Origin", origin)
            .header("Referer", "$origin/")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Dest", dest)

    private fun buildFirefoxDesktopUserAgent(majorVersion: String): String {
        val major = majorVersion.ifBlank { DEFAULT_FIREFOX_MAJOR }
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:$major.0) Gecko/20100101 Firefox/$major.0"
    }
}
