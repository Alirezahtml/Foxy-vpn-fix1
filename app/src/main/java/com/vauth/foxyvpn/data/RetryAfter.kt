package com.vauth.foxyvpn.data

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DEFAULT_RETRY_AFTER_MS = 2_000L
private const val MIN_RETRY_AFTER_MS = 500L
private const val MAX_RETRY_AFTER_MS = 30_000L

internal fun parseRetryAfterMs(
    headerValue: String?,
    nowEpochMs: Long = System.currentTimeMillis(),
): Long {
    val raw = headerValue?.trim().orEmpty()
    if (raw.isEmpty()) return DEFAULT_RETRY_AFTER_MS
    raw.toLongOrNull()?.let { seconds ->
        return (seconds * 1_000L).coerceIn(MIN_RETRY_AFTER_MS, MAX_RETRY_AFTER_MS)
    }
    val dateMillis =
        runCatching {
                ZonedDateTime.parse(raw, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()
            }
            .getOrNull()
            ?: runCatching {
                    val formatter =
                        DateTimeFormatter.ofPattern(
                            "EEE, dd MMM yyyy HH:mm:ss zzz",
                            Locale.US,
                        )
                    ZonedDateTime.parse(raw, formatter).toInstant().toEpochMilli()
                }
                .getOrNull()
    if (dateMillis != null) {
        val delta = dateMillis - nowEpochMs
        return delta.coerceIn(MIN_RETRY_AFTER_MS, MAX_RETRY_AFTER_MS)
    }
    return DEFAULT_RETRY_AFTER_MS
}
