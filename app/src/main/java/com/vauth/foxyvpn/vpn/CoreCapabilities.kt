package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.CoreSelection
import com.vauth.foxyvpn.data.model.VpnServerNode

enum class CoreId { CORE_A, CORE_B_H2_HEV, SMART }
enum class VerificationStatus { VERIFIED, EXPERIMENTAL, UNSUPPORTED }

data class ProtocolCapability(
    val id: String,
    val displayName: String,
    val core: CoreId,
    val status: VerificationStatus,
    val transports: Set<String>,
    val supportsExitVerify: Boolean,
    val supportsSplitTunnel: Boolean,
    val supportsDnsOverride: Boolean,
    val supportsUpstreamProxy: Boolean,
    val requiresServerFields: Set<String> = emptySet(),
)

data class CoreCandidate(
    val core: CoreId,
    val protocolId: String,
    val score: Int,
    val reasons: List<String>,
)

object CoreCapabilityRegistry {
    val capabilities: List<ProtocolCapability> = listOf(
        ProtocolCapability(
            "core-a-auto",
            "Sing-Box MASQUE · Smart Auto (H3/H2)",
            CoreId.CORE_A,
            VerificationStatus.VERIFIED,
            setOf("h3", "h2", "quic", "tcp", "tls", "masque"),
            true,
            supportsSplitTunnel = true,
            supportsDnsOverride = true,
            supportsUpstreamProxy = true,
        ),
        ProtocolCapability(
            "core-a-h2",
            "Sing-Box MASQUE · HTTP/2 (TCP/TLS + MASQUE)",
            CoreId.CORE_A,
            VerificationStatus.VERIFIED,
            setOf("h2", "tcp", "tls", "masque"),
            true,
            supportsSplitTunnel = true,
            supportsDnsOverride = true,
            supportsUpstreamProxy = true,
        ),
        ProtocolCapability(
            "core-a-h3",
            "Sing-Box MASQUE · HTTP/3 (QUIC + MASQUE)",
            CoreId.CORE_A,
            VerificationStatus.VERIFIED,
            setOf("h3", "quic", "udp", "masque"),
            true,
            supportsSplitTunnel = true,
            supportsDnsOverride = true,
            supportsUpstreamProxy = false,
        ),
        ProtocolCapability(
            "core-a-h1",
            "Sing-Box MASQUE · HTTP/1.1 (TLS + Fragment)",
            CoreId.CORE_A,
            VerificationStatus.VERIFIED,
            setOf("h1", "tcp", "tls"),
            true,
            supportsSplitTunnel = true,
            supportsDnsOverride = true,
            supportsUpstreamProxy = true,
        ),
    )

    fun all(): List<ProtocolCapability> = capabilities

    fun forCore(core: CoreId): List<ProtocolCapability> =
        if (core == CoreId.SMART) emptyList() else capabilities

    fun eligible(core: CoreId, server: VpnServerNode?, protocolId: String): Result<ProtocolCapability> {
        if (core == CoreId.SMART) return Result.failure(IllegalArgumentException("SMART is a policy, not a networking core"))
        val cap = capabilities.firstOrNull { it.id == protocolId }
            ?: return Result.failure(IllegalStateException("Protocol $protocolId is not registered for unified Sing-Box core"))
        if (cap.status != VerificationStatus.VERIFIED) {
            return Result.failure(IllegalStateException("${cap.displayName} is ${cap.status}"))
        }
        if (server != null && (server.quarantined || server.hostname.isBlank() || (server.port != 0 && server.port !in 1..65535))) {
            return Result.failure(IllegalStateException("Server ${server.hostname} is quarantined or has an invalid host/port"))
        }
        return Result.success(cap)
    }
}

object SmartSelectionEngine {
    const val SWITCH_HYSTERESIS_POINTS = 15

    fun recommend(
        candidates: List<CoreCandidate>,
        current: CoreCandidate? = null,
    ): CoreCandidate? {
        val best = candidates.filter { it.score >= 0 }.maxByOrNull { it.score } ?: return null
        if (current != null && current.score >= 0 && best.score - current.score < SWITCH_HYSTERESIS_POINTS) {
            return current
        }
        return best
    }
}

object CoreProtocolPolicy {
    fun isEnabled(@Suppress("UNUSED_PARAMETER") selection: CoreSelection, mode: ConnectionProtocolMode): Boolean {
        return mode.isSupported
    }

    fun safeMode(selection: CoreSelection, requested: ConnectionProtocolMode): ConnectionProtocolMode {
        return if (isEnabled(selection, requested)) requested else ConnectionProtocolMode.AUTO
    }
}
