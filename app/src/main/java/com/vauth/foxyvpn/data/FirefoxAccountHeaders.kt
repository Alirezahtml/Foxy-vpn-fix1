package com.vauth.foxyvpn.data

import java.util.Locale
import okhttp3.Request

internal object FirefoxAccountHeaders {
    const val userAgent: String =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

    private const val SEC_CH_UA =
        "\"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\", \"Google Chrome\";v=\"131\""

    fun acceptLanguage(): String {
        val tag = Locale.getDefault().toLanguageTag().ifBlank { "en-US" }
        return if (tag.equals("en-US", ignoreCase = true) || tag.equals("en", ignoreCase = true)) {
            "en-US,en;q=0.9"
        } else {
            "$tag,en-US;q=0.9,en;q=0.8"
        }
    }

    fun applyApi(
        builder: Request.Builder,
        origin: String = "https://accounts.firefox.com",
        userAgentOverride: String = userAgent,
    ): Request.Builder =
        builder
            .header("User-Agent", userAgentOverride)
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", acceptLanguage())
            .header("Origin", origin)
            .header("Referer", "$origin/")
            .header("Sec-CH-UA", SEC_CH_UA)
            .header("Sec-CH-UA-Mobile", "?1")
            .header("Sec-CH-UA-Platform", "\"Android\"")
            .header("Sec-Fetch-Site", "same-site")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Dest", "empty")

    fun applyChallengeAsset(
        builder: Request.Builder,
        origin: String,
        dest: String,
        accept: String,
        userAgentOverride: String = userAgent,
    ): Request.Builder =
        builder
            .header("User-Agent", userAgentOverride)
            .header("Accept", accept)
            .header("Accept-Language", acceptLanguage())
            .header("Origin", origin)
            .header("Referer", "$origin/")
            .header("Sec-CH-UA", SEC_CH_UA)
            .header("Sec-CH-UA-Mobile", "?1")
            .header("Sec-CH-UA-Platform", "\"Android\"")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Dest", dest)
}
