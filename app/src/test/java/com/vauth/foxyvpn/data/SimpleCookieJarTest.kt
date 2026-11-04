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

    @Test
    fun fastlyPowSolverFindsExactTwoCharAlphanumericMatch() {
        val solver = FastlyChallengeSolver()
        val answer =
            solver.solvePowAnswer(
                base = "GLL85QSxfGOLnIH2",
                targetHash = "a2aa5da3a8696871f8cbbbecfce50045b278abe26c6eb02c6f88a2e280731c63",
            )
        requireNotNull(answer)
        assertEquals(2, answer.length)
    }

    @Test
    fun liveFastlyChallengeAndFxaLoginBypass406() {
        kotlinx.coroutines.runBlocking {
            ControlPlaneHttp.cookieJar.clear()
            ControlPlaneHttp.resetPinnedDns()

            val solved =
                FastlyChallengeSolver().solveAndInstall(
                    challengedOrigin = "https://accounts.firefox.com",
                    challengedBaseUrl = "https://accounts.firefox.com",
                )
            assertTrue("Expected FastlyChallengeSolver to solve live challenge", solved)
            assertTrue(
                "Expected valid _fs_ch_cp_ cookie for api.accounts.firefox.com",
                ControlPlaneHttp.cookieJar.hasValidClearanceCookie("api.accounts.firefox.com"),
            )

            val loginUrl = "https://api.accounts.firefox.com/v1/account/login?keys=true".toHttpUrl()
            val mediaType = with(okhttp3.MediaType) { "application/json".toMediaType() }
            val payload =
                """{"email":"nonexistent_coldstart_${System.nanoTime()}@example.com","authPW":"0000000000000000000000000000000000000000000000000000000000000000","reason":"signin","service":"sync","verificationMethod":"email-otp"}"""
            val req =
                FirefoxAccountHeaders.applyApi(okhttp3.Request.Builder().url(loginUrl))
                    .post(okhttp3.RequestBody.create(mediaType, payload.toByteArray(Charsets.UTF_8)))
                    .header("Content-Type", "application/json")
                    .build()

            ControlPlaneHttp.client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                println("Production Cold-Start Login Result: status=${resp.code} body=$body")
                assertEquals(
                    "Expected HTTP 400 (Unknown account) instead of HTTP 406 from live FxA API",
                    400,
                    resp.code,
                )
                assertTrue("Expected errno 102 in FxA response body", body.contains("\"errno\":102"))
            }
        }
    }
}
