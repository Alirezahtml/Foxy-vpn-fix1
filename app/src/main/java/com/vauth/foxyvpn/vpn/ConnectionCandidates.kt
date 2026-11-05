package com.vauth.foxyvpn.vpn

import com.vauth.foxyvpn.data.model.ProxyCandidate

internal const val MAX_RECOMMENDED_CONNECTION_CANDIDATES = 4

internal fun selectConnectionCandidates(
    allCandidates: List<ProxyCandidate>,
    selectedCountryCode: String?,
    selectedProxyHost: String?,
    maxRecommendedCandidates: Int = MAX_RECOMMENDED_CONNECTION_CANDIDATES,
): List<ProxyCandidate> {
    if (allCandidates.isEmpty()) return emptyList()
    val valid = allCandidates.filter { it.host.isNotBlank() && it.port in 1..65535 }
    if (valid.isEmpty()) return emptyList()

    val explicitHost = selectedProxyHost?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
    if (explicitHost != null) {
        val exact = valid.filter { it.host.equals(explicitHost, ignoreCase = true) }
        val sameCountry =
            if (!selectedCountryCode.isNullOrBlank() &&
                !selectedCountryCode.equals("REC", ignoreCase = true)
            ) {
                valid.filter {
                    it.countryCode.equals(selectedCountryCode, ignoreCase = true) &&
                        !it.host.equals(explicitHost, ignoreCase = true)
                }
            } else {
                emptyList()
            }
        val combined = (exact + sameCountry).distinctBy { "${it.host.lowercase()}:${it.port}" }
        if (combined.isNotEmpty()) {
            return combined.take(maxRecommendedCandidates.coerceAtLeast(1))
        }
    }

    val country = selectedCountryCode?.trim()?.uppercase()
    if (!country.isNullOrBlank() && country != "REC") {
        val matching = valid.filter { it.countryCode.equals(country, ignoreCase = true) }
        if (matching.isNotEmpty()) {
            return matching
                .sortedBy { it.weight.takeIf { w -> w > 0 } ?: 100 }
                .distinctBy { "${it.host.lowercase()}:${it.port}" }
                .take(maxRecommendedCandidates.coerceAtLeast(1))
        }
    }

    // Priority ranking for Recommended ("REC") or default server:
    // Prioritize high-bandwidth, low-latency European & Middle Eastern nodes (Frankfurt, Amsterdam, Paris, Vienna, etc.)
    // over remote destinations (e.g. Argentina or Australia) which suffer from 400ms+ latency in Iran.
    val priorityCountries = listOf(
        "DE", "NL", "FR", "AT", "CH", "SE", "GB", "AE", "IT", "ES", "FI", "PL", "NO", "DK", "BE", "US", "CA"
    )
    val priorityRank = priorityCountries.withIndex().associate { it.value to it.index }

    return valid
        .sortedWith(
            compareBy(
                { priorityRank[it.countryCode.trim().uppercase()] ?: 999 },
                { it.weight.takeIf { w -> w > 0 } ?: 100 },
            ),
        )
        .distinctBy { "${it.host.lowercase()}:${it.port}" }
        .take(maxRecommendedCandidates.coerceAtLeast(1))
}
