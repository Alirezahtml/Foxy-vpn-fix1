package com.vauth.foxyvpn.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

data class VpnProtocol(
    val name: String,
    val host: String = "",
    val port: Int = 0,
    val scheme: String = "",
    val templateString: String = "",
)

data class VpnServerNode(
    val hostname: String,
    val port: Int = 0,
    val quarantined: Boolean = false,
    val protocols: List<VpnProtocol> = emptyList(),
)

data class VpnCity(
    val name: String,
    val code: String,
    val servers: List<VpnServerNode> = emptyList(),
)

data class VpnCountry(
    val name: String,
    val code: String,
    val cities: List<VpnCity> = emptyList(),
)

data class ProxyCandidate(
    val host: String,
    val port: Int,
    val countryCode: String,
    val countryName: String,
    val cityCode: String = "",
    val weight: Int = 100,
) {
    val authority: String get() = "$host:$port"
}

data class Entitlement(
    val subscribed: Boolean,
    val uid: String,
    val maxBytes: Long?,
    val limitedBandwidth: Boolean,
    val quotaRemaining: Long? = null,
)

data class RuntimeAuth(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAtEpochSeconds: Long,
    val sessionToken: String? = null,
)

typealias AuthTokens = RuntimeAuth

@Serializable
data class ProxyPass(
    @SerialName("token") val token: String = "",
    @SerialName("expires_at") val expiresAt: String = "",
    @Transient val expiresEpochSeconds: Long = 0L,
    @Transient val quotaLimitBytes: Long? = null,
    @Transient val quotaRemainingBytes: Long? = null,
    @Transient val quotaResetEpochSeconds: Long? = null,
)

data class AccountDeviceInfo(
    val name: String,
    val publicKey: String,
    val ipv4Address: String,
    val createdAt: String,
    val isCurrentDevice: Boolean = false,
)

data class TrafficSnapshot(
    val uplinkBytesPerSecond: Long = 0L,
    val downlinkBytesPerSecond: Long = 0L,
    val totalUplinkBytes: Long = 0L,
    val totalDownlinkBytes: Long = 0L,
)

fun countryCodeToEmojiFlag(countryCode: String): String {
    if (countryCode.equals("REC", ignoreCase = true)) return ""
    if (countryCode.length != 2) return ""
    val upper = countryCode.uppercase()
    if (upper == "IR") return "🦁☀️"
    val first = upper[0]
    val second = upper[1]
    if (first !in 'A'..'Z' || second !in 'A'..'Z') return "🌐"
    val firstCp = 0x1F1E6 + (first - 'A')
    val secondCp = 0x1F1E6 + (second - 'A')
    return String(Character.toChars(firstCp)) + String(Character.toChars(secondCp))
}

val VpnCountry.emojiFlag: String get() = countryCodeToEmojiFlag(code)
val ProxyCandidate.emojiFlag: String get() = countryCodeToEmojiFlag(countryCode)
