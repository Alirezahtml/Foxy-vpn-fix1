package com.vauth.foxyvpn.vpn

internal class CredentialDeadline(initialExpiresEpochSeconds: Long) {
    @Volatile
    private var expiresAtEpochSeconds: Long = initialExpiresEpochSeconds

    fun update(newExpiresEpochSeconds: Long) {
        expiresAtEpochSeconds = newExpiresEpochSeconds
    }

    fun current(): Long = expiresAtEpochSeconds

    fun remainingSeconds(nowEpochSeconds: Long = System.currentTimeMillis() / 1000L): Long =
        (expiresAtEpochSeconds - nowEpochSeconds).coerceAtLeast(0L)

    fun isExpired(nowEpochSeconds: Long = System.currentTimeMillis() / 1000L): Boolean =
        nowEpochSeconds >= expiresAtEpochSeconds
}
