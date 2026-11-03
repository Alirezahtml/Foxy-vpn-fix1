package com.vauth.foxyvpn.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class ExitCheckTest {
    private val exitCheck = ExitCheck()

    @Test
    fun parsesIpv4AndNormalizesCountryCode() {
        val info = exitCheck.parseTrace("fl=abc\nip=203.0.113.7\nloc=us\n")

        assertEquals("203.0.113.7", info.ip)
        assertEquals("US", info.countryCode)
    }

    @Test
    fun parsesIpv6EgressAddress() {
        val info = exitCheck.parseTrace("ip=2606:4700:4700::1111\nloc=CA\n")

        assertEquals("2606:4700:4700::1111", info.ip)
        assertEquals("CA", info.countryCode)
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsMissingEgressIp() {
        exitCheck.parseTrace("loc=US\n")
    }

    @Test
    fun ignoresMalformedCountryCodeRatherThanProducingAFlag() {
        val info = exitCheck.parseTrace("ip=203.0.113.7\nloc=USA\n")

        assertEquals("", info.countryCode)
    }
}
