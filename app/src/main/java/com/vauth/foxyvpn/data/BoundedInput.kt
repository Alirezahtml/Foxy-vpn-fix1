package com.vauth.foxyvpn.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import okhttp3.ResponseBody

internal const val DEFAULT_MAX_BODY_BYTES: Int = 2 * 1024 * 1024

internal fun ResponseBody.readBoundedUtf8(maxBytes: Int = DEFAULT_MAX_BODY_BYTES): String {
    val charset: Charset =
        runCatching { contentType()?.charset(StandardCharsets.UTF_8) }.getOrNull()
            ?: StandardCharsets.UTF_8
    val bytes = byteStream().readBoundedBytes(maxBytes)
    return String(bytes, charset)
}

internal fun InputStream.readBoundedBytes(maxBytes: Int): ByteArray {
    require(maxBytes > 0) { "maxBytes must be positive" }
    return use { stream ->
        val buffer = ByteArrayOutputStream(minOf(maxBytes, 16 * 1024))
        val chunk = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val read = stream.read(chunk)
            if (read < 0) break
            if (read == 0) continue
            total += read
            if (total > maxBytes) {
                throw IOException("Response body exceeds $maxBytes bytes")
            }
            buffer.write(chunk, 0, read)
        }
        buffer.toByteArray()
    }
}
