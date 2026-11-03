package com.vauth.foxyvpn.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionProtocolModeTest {
    @Test
    fun supportedModesMapToRealSingBoxCoreTransports() {
        assertEquals("auto", ConnectionProtocolMode.AUTO.coreTransport)
        assertEquals("h2", ConnectionProtocolMode.HTTP2_TCP.coreTransport)
        assertEquals("h3", ConnectionProtocolMode.HTTP3_QUIC.coreTransport)
        assertEquals("h1", ConnectionProtocolMode.HTTP11_TLS.coreTransport)
    }

    @Test
    fun allFourMasqueAndHttpProtocolsAreSupportedInSingBoxV2() {
        assertTrue(ConnectionProtocolMode.AUTO.isSupported)
        assertTrue(ConnectionProtocolMode.HTTP2_TCP.isSupported)
        assertTrue(ConnectionProtocolMode.HTTP3_QUIC.isSupported)
        assertTrue(ConnectionProtocolMode.HTTP11_TLS.isSupported)
    }
}
