package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.ConnectionProtocolMode
import com.vauth.foxyvpn.data.CoreSelection
import com.vauth.foxyvpn.data.model.VpnServerNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreCapabilitiesTest {
    @Test
    fun smartIsPolicyNotNetworkingCore() {
        assertTrue(CoreCapabilityRegistry.eligible(CoreId.SMART, null, "core-a-h2").isFailure)
    }

    @Test
    fun quarantinedServerIsNeverEligible() {
        val result = CoreCapabilityRegistry.eligible(
            CoreId.CORE_A,
            VpnServerNode("example.invalid", quarantined = true),
            "core-a-h2",
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun rankingIsDeterministicAndKeepsCurrentOnTie() {
        val current = CoreCandidate(CoreId.CORE_B_H2_HEV, "hev-h2", 10, emptyList())
        val recommendation = SmartSelectionEngine.recommend(
            listOf(
                CoreCandidate(CoreId.CORE_A, "core-a-h2", 10, emptyList()),
                current,
            ),
            current,
        )
        assertEquals(current, recommendation)
    }

    @Test
    fun unifiedSingBoxCoreEnablesAllFourProtocols() {
        for (selection in CoreSelection.entries) {
            for (mode in ConnectionProtocolMode.entries) {
                assertTrue(CoreProtocolPolicy.isEnabled(selection, mode))
                assertEquals(mode, CoreProtocolPolicy.safeMode(selection, mode))
            }
        }
    }

    @Test
    fun coreAEnablesHttp3AndHttp11AndRegistryVerifiesAllTransports() {
        assertTrue(CoreProtocolPolicy.isEnabled(CoreSelection.CORE_A, ConnectionProtocolMode.HTTP3_QUIC))
        assertTrue(CoreProtocolPolicy.isEnabled(CoreSelection.CORE_A, ConnectionProtocolMode.HTTP11_TLS))
        for (id in listOf("core-a-auto", "core-a-h2", "core-a-h3", "core-a-h1")) {
            val capability = CoreCapabilityRegistry.capabilities.first { it.id == id }
            assertNotEquals(VerificationStatus.UNSUPPORTED, capability.status)
            assertEquals(VerificationStatus.VERIFIED, capability.status)
        }
    }
}
