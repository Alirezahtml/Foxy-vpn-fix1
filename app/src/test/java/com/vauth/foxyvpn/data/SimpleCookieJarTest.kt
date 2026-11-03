package com.vauth.foxyvpn.data

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleCookieJarTest {
    private val accountsUrl = "https://accounts.firefox.com/".toHttpUrl()
    private val apiUrl = "https://api.accounts.firefox.com/v1/account/login".toHttpUrl()

    @Test
    fun hostOnlyChallengeCookieIsNotSentToSiblingHostname() {
        val jar = SimpleCookieJar()
        val cookie = Cookie.Builder()
            .name("_fs_ch_st_")
            .value("challenge-token")
            .hostOnlyDomain("accounts.firefox.com")
            .path("/")
            .build()

        jar.importCookies(listOf(cookie))

        assertEquals(listOf(cookie), jar.loadForRequest(accountsUrl))
        assertTrue(jar.loadForRequest(apiUrl).isEmpty())
    }

    @Test
    fun domainCookieIsSentOnlyWithinItsDeclaredDomain() {
        val jar = SimpleCookieJar()
        val cookie = Cookie.Builder()
            .name("_fs_ch_cp_")
            .value("challenge-proof")
            .domain("firefox.com")
            .path("/")
            .build()

        jar.importCookies(listOf(cookie))

        assertEquals(listOf(cookie), jar.loadForRequest(apiUrl))
        assertTrue(jar.loadForRequest("https://example.com/".toHttpUrl()).isEmpty())
    }

    @Test
    fun challengeTargetsTheRejectedApiHostnameFirst() {
        assertEquals("https://api.accounts.firefox.com", FASTLY_CHALLENGE_BASES.first())
    }
}
